package com.carshop.service;

import com.carshop.entity.RefreshToken;
import com.carshop.entity.User;
import com.carshop.repository.RefreshTokenRepository;
import com.carshop.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing JWT tokens and refresh tokens.
 * Handles token generation, validation, storage, and cleanup.
 * 
 * Validates: Requirements 6.3, 6.4, 6.5, 7.2, 7.3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    
    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;
    
    /**
     * Generates a JWT access token for the given user.
     * Access tokens have a 15-minute expiry.
     * 
     * @param userDetails the user details
     * @return JWT access token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        log.debug("Generating access token for user: {}", userDetails.getUsername());
        return jwtTokenProvider.generateAccessToken(userDetails);
    }
    
    /**
     * Generates and stores a refresh token for the given user.
     * Refresh tokens have a 7-day expiry and are persisted in the database.
     * 
     * @param user the user entity
     * @return the generated refresh token string
     */
    @Transactional
    public String generateAndStoreRefreshToken(User user) {
        log.debug("Generating refresh token for user: {}", user.getUsername());
        
        // Generate a unique token string
        String tokenString = UUID.randomUUID().toString();
        
        // Calculate expiry date
        LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000);
        
        // Create and save refresh token entity
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenString)
                .expiryDate(expiryDate)
                .build();
        
        refreshTokenRepository.save(refreshToken);
        
        log.info("Refresh token created for user: {}", user.getUsername());
        return tokenString;
    }
    
    /**
     * Validates a JWT access token.
     * 
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
    
    /**
     * Validates a refresh token by checking if it exists in the database
     * and has not expired.
     * 
     * @param token the refresh token string
     * @return Optional containing the RefreshToken entity if valid, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> validateRefreshToken(String token) {
        log.debug("Validating refresh token");
        
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        
        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found in database");
            return Optional.empty();
        }
        
        RefreshToken refreshToken = refreshTokenOpt.get();
        
        if (refreshToken.isExpired()) {
            log.warn("Refresh token has expired for user: {}", refreshToken.getUser().getUsername());
            // Clean up expired token
            deleteRefreshToken(token);
            return Optional.empty();
        }
        
        log.debug("Refresh token is valid for user: {}", refreshToken.getUser().getUsername());
        return Optional.of(refreshToken);
    }
    
    /**
     * Retrieves a refresh token from the database.
     * 
     * @param token the refresh token string
     * @return Optional containing the RefreshToken entity if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> getRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    /**
     * Deletes a specific refresh token from the database.
     * Used for token cleanup when tokens expire or during logout.
     * 
     * @param token the refresh token string to delete
     */
    @Transactional
    public void deleteRefreshToken(String token) {
        log.debug("Deleting refresh token");
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshTokenRepository.delete(refreshToken);
                    log.info("Refresh token deleted for user: {}", refreshToken.getUser().getUsername());
                });
    }
    
    /**
     * Deletes all refresh tokens for a specific user.
     * Used during logout to invalidate all user sessions.
     * 
     * @param user the user whose tokens should be deleted
     */
    @Transactional
    public void deleteAllUserRefreshTokens(User user) {
        log.debug("Deleting all refresh tokens for user: {}", user.getUsername());
        refreshTokenRepository.deleteByUser(user);
        log.info("All refresh tokens deleted for user: {}", user.getUsername());
    }
    
    /**
     * Extracts the username from a JWT access token.
     * 
     * @param token the JWT token
     * @return the username from the token
     */
    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }
    
    /**
     * Cleans up all expired refresh tokens from the database.
     * This method should be called periodically (e.g., via scheduled task)
     * to prevent accumulation of expired tokens.
     * 
     * @return the number of expired tokens deleted
     */
    @Transactional
    public int cleanupExpiredTokens() {
        log.debug("Starting cleanup of expired refresh tokens");
        
        // Find all tokens and filter expired ones
        var allTokens = refreshTokenRepository.findAll();
        var expiredTokens = allTokens.stream()
                .filter(RefreshToken::isExpired)
                .toList();
        
        if (!expiredTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(expiredTokens);
            log.info("Cleaned up {} expired refresh tokens", expiredTokens.size());
            return expiredTokens.size();
        }
        
        log.debug("No expired refresh tokens found");
        return 0;
    }
}
