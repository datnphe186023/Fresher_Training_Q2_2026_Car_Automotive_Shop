package com.carshop.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity
 */
class UserEntityTest {
    
    @Test
    void testUserCreation() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .build();
        
        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals(Role.ADMIN, user.getRole());
        assertNotNull(user.getRefreshTokens());
        assertTrue(user.getRefreshTokens().isEmpty());
    }
    
    @Test
    void testRoleEnum() {
        // Test that Role enum has correct values
        assertEquals(2, Role.values().length);
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.STAFF, Role.valueOf("STAFF"));
    }
    
    @Test
    void testUserRefreshTokenRelationship() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.STAFF)
                .build();
        
        RefreshToken token = RefreshToken.builder()
                .token("test-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        // When
        user.addRefreshToken(token);
        
        // Then
        assertEquals(1, user.getRefreshTokens().size());
        assertEquals(user, token.getUser());
        assertTrue(user.getRefreshTokens().contains(token));
    }
    
    @Test
    void testRefreshTokenExpiry() {
        // Given - expired token
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        
        // Given - valid token
        RefreshToken validToken = RefreshToken.builder()
                .token("valid-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        // Then
        assertTrue(expiredToken.isExpired());
        assertFalse(validToken.isExpired());
    }
}
