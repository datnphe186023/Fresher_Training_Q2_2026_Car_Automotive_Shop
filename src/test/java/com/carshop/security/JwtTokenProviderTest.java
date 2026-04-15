package com.carshop.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider component.
 * Tests token generation, validation, and claim extraction.
 */
class JwtTokenProviderTest {
    
    private JwtTokenProvider jwtTokenProvider;
    
    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-generation-minimum-256-bits-required";
    private static final long ACCESS_TOKEN_EXPIRY = 900000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY = 604800000; // 7 days
    
    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiry", ACCESS_TOKEN_EXPIRY);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiry", REFRESH_TOKEN_EXPIRY);
    }
    
    @Test
    void generateAccessToken_WithValidUserDetails_ReturnsToken() {
        // Given
        UserDetails userDetails = createUserDetails("testuser", "ADMIN");
        
        // When
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }
    
    @Test
    void generateAccessToken_IncludesRoleInClaims() {
        // Given
        UserDetails userDetails = createUserDetails("testuser", "ADMIN");
        
        // When
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        
        // Then
        assertEquals("ADMIN", claims.get("role"));
    }
    
    @Test
    void generateRefreshToken_WithValidUserDetails_ReturnsToken() {
        // Given
        UserDetails userDetails = createUserDetails("testuser", "STAFF");
        
        // When
        String token = jwtTokenProvider.generateRefreshToken(userDetails);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }
    
    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        // Given
        UserDetails userDetails = createUserDetails("testuser", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.string";
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void validateToken_WithMalformedToken_ReturnsFalse() {
        // Given
        String malformedToken = "not-a-jwt-token";
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void getUsernameFromToken_WithValidToken_ReturnsUsername() {
        // Given
        UserDetails userDetails = createUserDetails("testuser", "ADMIN");
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);
        
        // Then
        assertEquals("testuser", username);
    }
    
    @Test
    void getClaimsFromToken_WithValidToken_ReturnsClaims() {
        // Given
        UserDetails userDetails = createUserDetails("testuser", "STAFF");
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        
        // When
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        
        // Then
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
        assertEquals("STAFF", claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
    
    @Test
    void generateAccessToken_WithStaffRole_IncludesCorrectRole() {
        // Given
        UserDetails userDetails = createUserDetails("staffuser", "STAFF");
        
        // When
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        
        // Then
        assertEquals("STAFF", claims.get("role"));
    }
    
    @Test
    void generateAccessToken_DifferentUsers_GenerateDifferentTokens() {
        // Given
        UserDetails user1 = createUserDetails("user1", "ADMIN");
        UserDetails user2 = createUserDetails("user2", "STAFF");
        
        // When
        String token1 = jwtTokenProvider.generateAccessToken(user1);
        String token2 = jwtTokenProvider.generateAccessToken(user2);
        
        // Then
        assertNotEquals(token1, token2);
    }
    
    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        // Given - create provider with very short expiry
        JwtTokenProvider shortExpiryProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpiryProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpiryProvider, "accessTokenExpiry", 1L); // 1ms
        ReflectionTestUtils.setField(shortExpiryProvider, "refreshTokenExpiry", REFRESH_TOKEN_EXPIRY);
        
        UserDetails userDetails = createUserDetails("testuser", "ADMIN");
        String token = shortExpiryProvider.generateAccessToken(userDetails);
        
        // When - wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValid = shortExpiryProvider.validateToken(token);
        
        // Then
        assertFalse(isValid);
    }
    
    /**
     * Helper method to create UserDetails for testing
     */
    private UserDetails createUserDetails(String username, String role) {
        return User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
    }
}
