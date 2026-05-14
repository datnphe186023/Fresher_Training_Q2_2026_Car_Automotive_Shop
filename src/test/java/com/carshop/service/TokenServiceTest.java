package com.carshop.service;

import com.carshop.entity.RefreshToken;
import com.carshop.entity.Role;
import com.carshop.entity.User;
import com.carshop.repository.RefreshTokenRepository;
import com.carshop.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenService.
 * Tests token generation, validation, storage, and cleanup operations.
 */
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @InjectMocks
    private TokenService tokenService;
    
    private User testUser;
    private UserDetails testUserDetails;
    
    @BeforeEach
    void setUp() {
        // Set refresh token expiry (7 days in milliseconds)
        ReflectionTestUtils.setField(tokenService, "refreshTokenExpiry", 604800000L);
        
        // Create test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Create test user details
        testUserDetails = new org.springframework.security.core.userdetails.User(
                testUser.getUsername(),
                testUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name()))
        );
    }
    
    @Test
    void generateAccessToken_WithValidUserDetails_ReturnsToken() {
        // Given
        String expectedToken = "jwt.access.token";
        when(jwtTokenProvider.generateAccessToken(testUserDetails)).thenReturn(expectedToken);
        
        // When
        String actualToken = tokenService.generateAccessToken(testUserDetails);
        
        // Then
        assertEquals(expectedToken, actualToken);
        verify(jwtTokenProvider).generateAccessToken(testUserDetails);
    }
    
    @Test
    void generateAndStoreRefreshToken_WithValidUser_CreatesAndStoresToken() {
        // Given
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            token.setId(1L);
            return token;
        });
        
        // When
        String token = tokenService.generateAndStoreRefreshToken(testUser);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
    
    @Test
    void validateAccessToken_WithValidToken_ReturnsTrue() {
        // Given
        String validToken = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        
        // When
        boolean isValid = tokenService.validateAccessToken(validToken);
        
        // Then
        assertTrue(isValid);
        verify(jwtTokenProvider).validateToken(validToken);
    }
    
    @Test
    void validateAccessToken_WithInvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);
        
        // When
        boolean isValid = tokenService.validateAccessToken(invalidToken);
        
        // Then
        assertFalse(isValid);
        verify(jwtTokenProvider).validateToken(invalidToken);
    }
    
    @Test
    void validateRefreshToken_WithValidToken_ReturnsRefreshToken() {
        // Given
        String tokenString = "valid-refresh-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token(tokenString)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(refreshToken));
        
        // When
        Optional<RefreshToken> result = tokenService.validateRefreshToken(tokenString);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(tokenString, result.get().getToken());
        verify(refreshTokenRepository).findByToken(tokenString);
    }
    
    @Test
    void validateRefreshToken_WithExpiredToken_ReturnsEmptyAndDeletesToken() {
        // Given
        String tokenString = "expired-refresh-token";
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token(tokenString)
                .expiryDate(LocalDateTime.now().minusDays(1)) // Expired
                .build();
        
        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(expiredToken));
        
        // When
        Optional<RefreshToken> result = tokenService.validateRefreshToken(tokenString);
        
        // Then
        assertTrue(result.isEmpty());
        verify(refreshTokenRepository, times(2)).findByToken(tokenString); // Called twice: once in validate, once in delete
        verify(refreshTokenRepository).delete(expiredToken);
    }
    
    @Test
    void validateRefreshToken_WithNonExistentToken_ReturnsEmpty() {
        // Given
        String tokenString = "non-existent-token";
        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());
        
        // When
        Optional<RefreshToken> result = tokenService.validateRefreshToken(tokenString);
        
        // Then
        assertTrue(result.isEmpty());
        verify(refreshTokenRepository).findByToken(tokenString);
        verify(refreshTokenRepository, never()).delete(any());
    }
    
    @Test
    void getRefreshToken_WithExistingToken_ReturnsToken() {
        // Given
        String tokenString = "existing-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token(tokenString)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(refreshToken));
        
        // When
        Optional<RefreshToken> result = tokenService.getRefreshToken(tokenString);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(tokenString, result.get().getToken());
    }
    
    @Test
    void deleteRefreshToken_WithExistingToken_DeletesToken() {
        // Given
        String tokenString = "token-to-delete";
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token(tokenString)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(refreshToken));
        
        // When
        tokenService.deleteRefreshToken(tokenString);
        
        // Then
        verify(refreshTokenRepository).findByToken(tokenString);
        verify(refreshTokenRepository).delete(refreshToken);
    }
    
    @Test
    void deleteRefreshToken_WithNonExistentToken_DoesNothing() {
        // Given
        String tokenString = "non-existent-token";
        when(refreshTokenRepository.findByToken(tokenString)).thenReturn(Optional.empty());
        
        // When
        tokenService.deleteRefreshToken(tokenString);
        
        // Then
        verify(refreshTokenRepository).findByToken(tokenString);
        verify(refreshTokenRepository, never()).delete(any());
    }
    
    @Test
    void deleteAllUserRefreshTokens_WithValidUser_DeletesAllTokens() {
        // When
        tokenService.deleteAllUserRefreshTokens(testUser);
        
        // Then
        verify(refreshTokenRepository).deleteByUser(testUser);
    }
    
    @Test
    void getUsernameFromToken_WithValidToken_ReturnsUsername() {
        // Given
        String token = "valid.jwt.token";
        String expectedUsername = "testuser";
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(expectedUsername);
        
        // When
        String actualUsername = tokenService.getUsernameFromToken(token);
        
        // Then
        assertEquals(expectedUsername, actualUsername);
        verify(jwtTokenProvider).getUsernameFromToken(token);
    }
    
    @Test
    void cleanupExpiredTokens_WithExpiredTokens_DeletesThemAndReturnsCount() {
        // Given
        RefreshToken expiredToken1 = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("expired-token-1")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        
        RefreshToken expiredToken2 = RefreshToken.builder()
                .id(2L)
                .user(testUser)
                .token("expired-token-2")
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();
        
        RefreshToken validToken = RefreshToken.builder()
                .id(3L)
                .user(testUser)
                .token("valid-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(refreshTokenRepository.findAll()).thenReturn(List.of(expiredToken1, expiredToken2, validToken));
        
        // When
        int deletedCount = tokenService.cleanupExpiredTokens();
        
        // Then
        assertEquals(2, deletedCount);
        verify(refreshTokenRepository).findAll();
        verify(refreshTokenRepository).deleteAll(List.of(expiredToken1, expiredToken2));
    }
    
    @Test
    void cleanupExpiredTokens_WithNoExpiredTokens_ReturnsZero() {
        // Given
        RefreshToken validToken = RefreshToken.builder()
                .id(1L)
                .user(testUser)
                .token("valid-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        when(refreshTokenRepository.findAll()).thenReturn(List.of(validToken));
        
        // When
        int deletedCount = tokenService.cleanupExpiredTokens();
        
        // Then
        assertEquals(0, deletedCount);
        verify(refreshTokenRepository).findAll();
        verify(refreshTokenRepository, never()).deleteAll(anyList());
    }
}
