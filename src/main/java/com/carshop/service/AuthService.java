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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling authentication operations including registration, login,
 * token refresh, and logout for ADMIN and STAFF users.
 * 
 * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9,
 *            6.1, 6.2, 6.5, 6.6, 6.7, 6.9, 7.1, 7.4, 7.5, 7.6
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    
    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;
    
    /**
     * Registers a new ADMIN or STAFF user.
     * 
     * Business rules:
     * - Validates unique username and email
     * - Only allows ADMIN and STAFF roles
     * - Hashes password using BCrypt
     * - Generates access and refresh tokens
     * 
     * @param request the registration request containing username, email, password, and role
     * @return AuthResponse containing tokens and user information
     * @throws DuplicateResourceException if username or email already exists
     * @throws InvalidRoleException if role is not ADMIN or STAFF
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Processing registration request for username: {}", request.getUsername());
        
        // Validate role - only ADMIN and STAFF allowed
        if (request.getRole() != Role.ADMIN && request.getRole() != Role.STAFF) {
            log.warn("Registration attempt with invalid role: {}", request.getRole());
            throw new InvalidRoleException("Only ADMIN and STAFF roles are allowed");
        }
        
        // Check for duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: Username already exists: {}", request.getUsername());
            throw new DuplicateResourceException("Username already exists");
        }
        
        // Check for duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        
        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(hashedPassword)
                .role(request.getRole())
                .build();
        
        // Save user
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());
        
        // Generate tokens
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
        
        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateAndStoreRefreshToken(user);
        
        // Map to response
        UserResponse userResponse = userMapper.toResponse(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiry / 1000) // Convert to seconds
                .user(userResponse)
                .build();
    }
    
    /**
     * Authenticates a user and generates JWT tokens.
     * 
     * Business rules:
     * - Verifies credentials against stored hash
     * - Only allows ADMIN and STAFF authentication
     * - Generates new access and refresh tokens
     * - Stores refresh token in database
     * 
     * @param request the login request containing username and password
     * @return AuthResponse containing tokens and user information
     * @throws InvalidCredentialsException if credentials are invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for username: {}", request.getUsername());
        
        // Find user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found: {}", request.getUsername());
                    return new InvalidCredentialsException("Invalid username or password");
                });
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for user: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        // Verify user has ADMIN or STAFF role
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.STAFF) {
            log.warn("Login failed: User does not have ADMIN or STAFF role: {}", request.getUsername());
            throw new InvalidCredentialsException("Invalid username or password");
        }
        
        log.info("User authenticated successfully: {}", user.getUsername());
        
        // Generate tokens
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
        
        String accessToken = tokenService.generateAccessToken(userDetails);
        String refreshToken = tokenService.generateAndStoreRefreshToken(user);
        
        // Map to response
        UserResponse userResponse = userMapper.toResponse(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiry / 1000) // Convert to seconds
                .user(userResponse)
                .build();
    }
    
    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * Business rules:
     * - Validates refresh token exists and is not expired
     * - Generates new access token
     * - Does not generate new refresh token (reuse existing)
     * 
     * @param refreshToken the refresh token string
     * @return AuthResponse containing new access token and user information
     * @throws InvalidTokenException if refresh token is invalid or expired
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Processing token refresh request");
        
        // Validate refresh token
        RefreshToken token = tokenService.validateRefreshToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Token refresh failed: Invalid or expired refresh token");
                    return new InvalidTokenException("Invalid refresh token");
                });
        
        User user = token.getUser();
        log.info("Refresh token validated for user: {}", user.getUsername());
        
        // Generate new access token
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
        
        String accessToken = tokenService.generateAccessToken(userDetails);
        
        // Map to response
        UserResponse userResponse = userMapper.toResponse(user);
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Return the same refresh token
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiry / 1000) // Convert to seconds
                .user(userResponse)
                .build();
    }
    
    /**
     * Logs out a user by deleting all their refresh tokens.
     * 
     * Business rules:
     * - Deletes all refresh tokens for the user
     * - Invalidates all user sessions
     * 
     * @param username the username of the user to logout
     * @throws UsernameNotFoundException if user is not found
     */
    @Transactional
    public void logout(String username) {
        log.info("Processing logout request for username: {}", username);
        
        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Logout failed: User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        // Delete all refresh tokens
        tokenService.deleteAllUserRefreshTokens(user);
        
        log.info("User logged out successfully: {}", username);
    }
}
