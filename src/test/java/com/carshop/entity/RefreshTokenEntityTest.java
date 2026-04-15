package com.carshop.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RefreshToken entity.
 * Validates entity structure, relationships, and business logic.
 */
class RefreshTokenEntityTest {
    
    @Test
    void testRefreshTokenCreation() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
        
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);
        
        // When
        RefreshToken token = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("test-refresh-token-12345")
                .expiryDate(expiryDate)
                .build();
        
        // Then
        assertNotNull(token);
        assertEquals(1L, token.getId());
        assertEquals(user, token.getUser());
        assertEquals("test-refresh-token-12345", token.getToken());
        assertEquals(expiryDate, token.getExpiryDate());
    }
    
    @Test
    void testRefreshTokenIsExpired_WhenExpired() {
        // Given
        RefreshToken token = RefreshToken.builder()
                .token("expired-token")
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();
        
        // When
        boolean expired = token.isExpired();
        
        // Then
        assertTrue(expired, "Token should be expired");
    }
    
    @Test
    void testRefreshTokenIsExpired_WhenNotExpired() {
        // Given
        RefreshToken token = RefreshToken.builder()
                .token("valid-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        // When
        boolean expired = token.isExpired();
        
        // Then
        assertFalse(expired, "Token should not be expired");
    }
    
    @Test
    void testRefreshTokenUserRelationship() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .build();
        
        RefreshToken token = RefreshToken.builder()
                .token("test-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        // When
        token.setUser(user);
        
        // Then
        assertNotNull(token.getUser());
        assertEquals(user, token.getUser());
        assertEquals("testuser", token.getUser().getUsername());
    }
    
    @Test
    void testRefreshTokenBidirectionalRelationship() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .build();
        
        RefreshToken token = RefreshToken.builder()
                .token("test-token")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        // When
        user.addRefreshToken(token);
        
        // Then
        assertEquals(user, token.getUser());
        assertTrue(user.getRefreshTokens().contains(token));
    }
    
    @Test
    void testRefreshTokenEquality() {
        // Given
        RefreshToken token1 = RefreshToken.builder()
                .id(1L)
                .token("token1")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken token2 = RefreshToken.builder()
                .id(1L)
                .token("token2")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        RefreshToken token3 = RefreshToken.builder()
                .id(2L)
                .token("token3")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        
        // Then
        assertEquals(token1, token2, "Tokens with same ID should be equal");
        assertNotEquals(token1, token3, "Tokens with different IDs should not be equal");
    }
}
