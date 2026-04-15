package com.carshop.security;

import io.jsonwebtoken.Claims;
import net.jqwik.api.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for JwtTokenProvider component.
 * Validates universal properties across all inputs.
 * 
 * Feature: week-1-foundation-setup, Property 3: JWT Role Inclusion
 */
class JwtTokenProviderPropertyTest {
    
    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-generation-minimum-256-bits-required";
    private static final long ACCESS_TOKEN_EXPIRY = 900000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRY = 604800000; // 7 days
    
    /**
     * Property 3: JWT Role Inclusion
     * 
     * For any authenticated user (ADMIN or STAFF), the generated JWT access token 
     * SHALL contain the user's role in the token claims and SHALL be extractable 
     * without loss of information.
     * 
     * **Validates: Requirements 6.8**
     */
    @Property(tries = 100)
    // Feature: week-1-foundation-setup, Property 3: JWT Role Inclusion
    void jwtTokenContainsUserRole(
            @ForAll("usernames") String username,
            @ForAll("roles") String role) {
        
        // Given
        JwtTokenProvider provider = createJwtTokenProvider();
        UserDetails userDetails = createUserDetails(username, role);
        
        // When
        String token = provider.generateAccessToken(userDetails);
        
        // Then - token should be valid
        assertTrue(provider.validateToken(token), 
                "Generated token should be valid");
        
        // And - username should be extractable
        String extractedUsername = provider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername, 
                "Extracted username should match original");
        
        // And - role should be in claims
        Claims claims = provider.getClaimsFromToken(token);
        String extractedRole = (String) claims.get("role");
        assertEquals(role, extractedRole, 
                "Extracted role should match original");
    }
    
    /**
     * Property: Access token validation consistency
     * 
     * For any valid user, the generated access token should always be valid
     * immediately after generation.
     */
    @Property(tries = 100)
    void generatedAccessTokenIsImmediatelyValid(
            @ForAll("usernames") String username,
            @ForAll("roles") String role) {
        
        // Given
        JwtTokenProvider provider = createJwtTokenProvider();
        UserDetails userDetails = createUserDetails(username, role);
        
        // When
        String token = provider.generateAccessToken(userDetails);
        
        // Then
        assertTrue(provider.validateToken(token),
                "Newly generated access token should be valid");
    }
    
    /**
     * Property: Refresh token validation consistency
     * 
     * For any valid user, the generated refresh token should always be valid
     * immediately after generation.
     */
    @Property(tries = 100)
    void generatedRefreshTokenIsImmediatelyValid(
            @ForAll("usernames") String username,
            @ForAll("roles") String role) {
        
        // Given
        JwtTokenProvider provider = createJwtTokenProvider();
        UserDetails userDetails = createUserDetails(username, role);
        
        // When
        String token = provider.generateRefreshToken(userDetails);
        
        // Then
        assertTrue(provider.validateToken(token),
                "Newly generated refresh token should be valid");
    }
    
    /**
     * Property: Username extraction consistency
     * 
     * For any valid user, the username extracted from the token should always
     * match the original username.
     */
    @Property(tries = 100)
    void usernameExtractionIsConsistent(
            @ForAll("usernames") String username,
            @ForAll("roles") String role) {
        
        // Given
        JwtTokenProvider provider = createJwtTokenProvider();
        UserDetails userDetails = createUserDetails(username, role);
        
        // When
        String accessToken = provider.generateAccessToken(userDetails);
        String refreshToken = provider.generateRefreshToken(userDetails);
        
        // Then
        assertEquals(username, provider.getUsernameFromToken(accessToken),
                "Username from access token should match original");
        assertEquals(username, provider.getUsernameFromToken(refreshToken),
                "Username from refresh token should match original");
    }
    
    
    /**
     * Property: Invalid tokens are rejected
     * 
     * For any random string that is not a valid JWT, validation should fail.
     */
    @Property(tries = 100)
    void invalidTokensAreRejected(@ForAll("invalidTokens") String invalidToken) {
        // Given
        JwtTokenProvider provider = createJwtTokenProvider();
        
        // When
        boolean isValid = provider.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid, "Invalid token should not be validated");
    }
    
    // ========== Arbitraries ==========
    
    @Provide
    Arbitrary<String> usernames() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .ofMinLength(3)
                .ofMaxLength(50);
    }
    
    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.of("ADMIN", "STAFF");
    }
    
    @Provide
    Arbitrary<String> invalidTokens() {
        return Arbitraries.oneOf(
                // Completely random strings
                Arbitraries.strings().ofMinLength(1).ofMaxLength(100),
                // Strings with wrong number of dots
                Arbitraries.strings().withChars('.', 'a', 'b', 'c').ofLength(20),
                // Empty string
                Arbitraries.just(""),
                // Single dot
                Arbitraries.just("."),
                // Two dots but invalid content
                Arbitraries.just("invalid.token.string")
        );
    }
    
    // ========== Helper Methods ==========
    
    private JwtTokenProvider createJwtTokenProvider() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(provider, "accessTokenExpiry", ACCESS_TOKEN_EXPIRY);
        ReflectionTestUtils.setField(provider, "refreshTokenExpiry", REFRESH_TOKEN_EXPIRY);
        return provider;
    }
    
    private UserDetails createUserDetails(String username, String role) {
        return User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
    }
}
