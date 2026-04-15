package com.carshop.service;

import com.carshop.dto.request.LoginRequest;
import com.carshop.dto.request.RegisterRequest;
import com.carshop.dto.response.AuthResponse;
import com.carshop.dto.response.UserResponse;
import com.carshop.entity.RefreshToken;
import com.carshop.entity.Role;
import com.carshop.entity.User;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.InvalidCredentialsException;
import com.carshop.exception.InvalidRoleException;
import com.carshop.exception.InvalidTokenException;
import com.carshop.mapper.UserMapper;
import com.carshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests registration, login, token refresh, and logout operations.
 * 
 * Validates: Requirements 5.5, 5.6, 5.7, 6.7
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private TokenService tokenService;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserResponse userResponse;
    
    @BeforeEach
    void setUp() {
        // Set access token expiry (15 minutes in milliseconds)
        ReflectionTestUtils.setField(authService, "accessTokenExpiry", 900000L);
        
        // Create test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Create register request
        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();
        
        // Create login request
        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();
        
        // Create user response
        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // ========== Registration Tests ==========
    
    @Test
    void register_WithValidData_CreatesUserAndReturnsTokens() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });
        when(tokenService.generateAccessToken(any(UserDetails.class))).thenReturn("access.token.jwt");
        when(tokenService.generateAndStoreRefreshToken(any(User.class))).thenReturn("refresh-token-uuid");
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
        
        // When
        AuthResponse response = authService.register(registerRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("access.token.jwt", response.getAccessToken());
        assertEquals("refresh-token-uuid", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn()); // 900 seconds = 15 minutes
        assertNotNull(response.getUser());
        
        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(tokenService).generateAccessToken(any(UserDetails.class));
        verify(tokenService).generateAndStoreRefreshToken(any(User.class));
    }
    
    @Test
    void register_WithDuplicateUsername_ThrowsDuplicateResourceException() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest)
        );
        
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_WithDuplicateEmail_ThrowsDuplicateResourceException() {
        // Given
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(registerRequest)
        );
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_WithInvalidRole_ThrowsInvalidRoleException() {
        // Given - Create a request with null role to simulate invalid role scenario
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .role(null)
                .build();
        
        // When & Then
        InvalidRoleException exception = assertThrows(
                InvalidRoleException.class,
                () -> authService.register(invalidRequest)
        );
        
        assertEquals("Only ADMIN and STAFF roles are allowed", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void register_WithStaffRole_CreatesUserSuccessfully() {
        // Given
        RegisterRequest staffRequest = RegisterRequest.builder()
                .username("staffuser")
                .email("staff@example.com")
                .password("password123")
                .role(Role.STAFF)
                .build();
        
        when(userRepository.findByUsername(staffRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(staffRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(staffRequest.getPassword())).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            user.setCreatedAt(LocalDateTime.now());
            return user;
        });
        when(tokenService.generateAccessToken(any(UserDetails.class))).thenReturn("access.token.jwt");
        when(tokenService.generateAndStoreRefreshToken(any(User.class))).thenReturn("refresh-token-uuid");
        when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);
        
        // When
        AuthResponse response = authService.register(staffRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("access.token.jwt", response.getAccessToken());
        verify(userRepository).save(any(User.class));
    }
    
    // ========== Login Tests ==========
    
    @Test
    void login_WithValidCredentials_ReturnsTokens() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(tokenService.generateAccessToken(any(UserDetails.class))).thenReturn("access.token.jwt");
        when(tokenService.generateAndStoreRefreshToken(testUser)).thenReturn("refresh-token-uuid");
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        
        // When
        AuthResponse response = authService.login(loginRequest);
        
        // Then
        assertNotNull(response);
        assertEquals("access.token.jwt", response.getAccessToken());
        assertEquals("refresh-token-uuid", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
        assertNotNull(response.getUser());
        
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(tokenService).generateAccessToken(any(UserDetails.class));
        verify(tokenService).generateAndStoreRefreshToken(testUser);
    }
    
    @Test
    void login_WithInvalidUsername_ThrowsInvalidCredentialsException() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.empty());
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).generateAccessToken(any());
    }
    
    @Test
    void login_WithInvalidPassword_ThrowsInvalidCredentialsException() {
        // Given
        when(userRepository.findByUsername(loginRequest.getUsername()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername(loginRequest.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(tokenService, never()).generateAccessToken(any());
    }
    
    @Test
    void login_WithStaffRole_ReturnsTokensSuccessfully() {
        // Given
        User staffUser = User.builder()
                .id(2L)
                .username("staffuser")
                .email("staff@example.com")
                .password("hashedPassword123")
                .role(Role.STAFF)
                .createdAt(LocalDateTime.now())
                .build();
        
        LoginRequest staffLogin = LoginRequest.builder()
                .username("staffuser")
                .password("password123")
                .build();
        
        when(userRepository.findByUsername(staffLogin.getUsername()))
                .thenReturn(Optional.of(staffUser));
        when(passwordEncoder.matches(staffLogin.getPassword(), staffUser.getPassword()))
                .thenReturn(true);
        when(tokenService.generateAccessToken(any(UserDetails.class))).thenReturn("access.token.jwt");
        when(tokenService.generateAndStoreRefreshToken(staffUser)).thenReturn("refresh-token-uuid");
        when(userMapper.toResponse(staffUser)).thenReturn(userResponse);
        
        // When
        AuthResponse response = authService.login(staffLogin);
        
        // Then
        assertNotNull(response);
        assertEquals("access.token.jwt", response.getAccessToken());
        verify(tokenService).generateAccessToken(any(UserDetails.class));
    }
    
    // ========== Refresh Token Tests ==========
    
    @Test
    void refreshToken_WithValidToken_ReturnsNewAccessToken() {
        // Given
        String refreshTokenString = "valid-refresh-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token(refreshTokenString)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(tokenService.validateRefreshToken(refreshTokenString))
                .thenReturn(Optional.of(refreshToken));
        when(tokenService.generateAccessToken(any(UserDetails.class)))
                .thenReturn("new.access.token.jwt");
        when(userMapper.toResponse(testUser)).thenReturn(userResponse);
        
        // When
        AuthResponse response = authService.refreshToken(refreshTokenString);
        
        // Then
        assertNotNull(response);
        assertEquals("new.access.token.jwt", response.getAccessToken());
        assertEquals(refreshTokenString, response.getRefreshToken()); // Same refresh token returned
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
        assertNotNull(response.getUser());
        
        verify(tokenService).validateRefreshToken(refreshTokenString);
        verify(tokenService).generateAccessToken(any(UserDetails.class));
        verify(tokenService, never()).generateAndStoreRefreshToken(any()); // Should not generate new refresh token
    }
    
    @Test
    void refreshToken_WithInvalidToken_ThrowsInvalidTokenException() {
        // Given
        String invalidToken = "invalid-refresh-token";
        when(tokenService.validateRefreshToken(invalidToken))
                .thenReturn(Optional.empty());
        
        // When & Then
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(invalidToken)
        );
        
        assertEquals("Invalid refresh token", exception.getMessage());
        verify(tokenService).validateRefreshToken(invalidToken);
        verify(tokenService, never()).generateAccessToken(any());
    }
    
    @Test
    void refreshToken_WithExpiredToken_ThrowsInvalidTokenException() {
        // Given
        String expiredToken = "expired-refresh-token";
        when(tokenService.validateRefreshToken(expiredToken))
                .thenReturn(Optional.empty()); // TokenService returns empty for expired tokens
        
        // When & Then
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshToken(expiredToken)
        );
        
        assertEquals("Invalid refresh token", exception.getMessage());
        verify(tokenService).validateRefreshToken(expiredToken);
    }
    
    // ========== Logout Tests ==========
    
    @Test
    void logout_WithValidUsername_DeletesAllRefreshTokens() {
        // Given
        when(userRepository.findByUsername(testUser.getUsername()))
                .thenReturn(Optional.of(testUser));
        
        // When
        authService.logout(testUser.getUsername());
        
        // Then
        verify(userRepository).findByUsername(testUser.getUsername());
        verify(tokenService).deleteAllUserRefreshTokens(testUser);
    }
    
    @Test
    void logout_WithInvalidUsername_ThrowsUsernameNotFoundException() {
        // Given
        String invalidUsername = "nonexistent";
        when(userRepository.findByUsername(invalidUsername))
                .thenReturn(Optional.empty());
        
        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.logout(invalidUsername)
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findByUsername(invalidUsername);
        verify(tokenService, never()).deleteAllUserRefreshTokens(any());
    }
    
    // ========== Edge Case Tests ==========
    
    @Test
    void register_WithEmptyUsername_ThrowsIllegalArgumentException() {
        // Note: Bean validation (@NotBlank) is handled at controller level
        // However, Spring Security's UserDetails.User constructor validates non-empty username
        RegisterRequest emptyUsernameRequest = RegisterRequest.builder()
                .username("")
                .email("test@example.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();
        
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // When & Then - Spring Security's User constructor throws IllegalArgumentException for empty username
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(emptyUsernameRequest)
        );
    }
    
    @Test
    void login_WithNullPassword_ThrowsInvalidCredentialsException() {
        // Given
        LoginRequest nullPasswordRequest = LoginRequest.builder()
                .username("testuser")
                .password(null)
                .build();
        
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(null, testUser.getPassword()))
                .thenReturn(false);
        
        // When & Then
        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(nullPasswordRequest)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
    }
}
