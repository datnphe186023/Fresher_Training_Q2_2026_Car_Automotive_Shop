package com.carshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Component responsible for JWT token generation, validation, and parsing.
 * Handles both access tokens (15-minute expiry) and refresh tokens (7-day expiry).
 * 
 * Validates: Requirements 6.3, 6.4, 6.8, 19.2
 */
@Component
@Slf4j
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;
    
    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;
    
    /**
     * Generates a JWT access token with 15-minute expiry.
     * Includes username and role in the token claims.
     * 
     * @param userDetails the user details containing username and authorities
     * @return JWT access token string
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Extract role from authorities (assuming single role)
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");
        
        claims.put("role", role);
        
        return createToken(claims, userDetails.getUsername(), accessTokenExpiry);
    }
    
    /**
     * Generates a JWT refresh token with 7-day expiry.
     * 
     * @param userDetails the user details containing username
     * @return JWT refresh token string
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiry);
    }
    
    /**
     * Creates a JWT token with specified claims, subject, and expiry time.
     * 
     * @param claims additional claims to include in the token
     * @param subject the subject (username) of the token
     * @param expiryTime expiry time in milliseconds
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject, long expiryTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryTime);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Validates a JWT token by checking signature and expiration.
     * 
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts the username from a JWT token.
     * 
     * @param token the JWT token
     * @return username (subject) from the token
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * Extracts all claims from a JWT token.
     * 
     * @param token the JWT token
     * @return Claims object containing all token claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Extracts a specific claim from a JWT token using a claims resolver function.
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the desired claim
     * @param <T> the type of the claim
     * @return the extracted claim value
     */
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Gets the signing key for JWT token generation and validation.
     * 
     * @return SecretKey for HMAC-SHA signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
