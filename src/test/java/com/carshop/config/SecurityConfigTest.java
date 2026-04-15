package com.carshop.config;

import com.carshop.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SecurityConfig.
 * Tests bean creation and configuration.
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {
    
    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private AuthenticationConfiguration authenticationConfiguration;
    
    @InjectMocks
    private SecurityConfig securityConfig;
    
    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        
        // Then
        assertNotNull(encoder);
        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }
    
    @Test
    void passwordEncoder_ShouldUseStrength10() {
        // When
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String password = "testPassword123";
        String encoded = encoder.encode(password);
        
        // Then
        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$2a$10$"), "BCrypt hash should use strength 10");
        assertTrue(encoder.matches(password, encoded), "Encoded password should match original");
    }
    
    @Test
    void authenticationProvider_ShouldReturnDaoAuthenticationProvider() {
        // When
        AuthenticationProvider provider = securityConfig.authenticationProvider();
        
        // Then
        assertNotNull(provider);
        assertInstanceOf(DaoAuthenticationProvider.class, provider);
    }
    
    @Test
    void corsConfigurationSource_ShouldReturnConfiguredSource() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        
        // Then
        assertNotNull(source);
    }
    
    @Test
    void authenticationManager_ShouldReturnManager() throws Exception {
        // Given
        AuthenticationManager mockManager = org.mockito.Mockito.mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockManager);
        
        // When
        AuthenticationManager manager = securityConfig.authenticationManager(authenticationConfiguration);
        
        // Then
        assertNotNull(manager);
        assertEquals(mockManager, manager);
    }
    
    @Test
    void passwordEncoder_DifferentPasswords_ShouldProduceDifferentHashes() {
        // Given
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String password1 = "password123";
        String password2 = "password456";
        
        // When
        String hash1 = encoder.encode(password1);
        String hash2 = encoder.encode(password2);
        
        // Then
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }
    
    @Test
    void passwordEncoder_SamePassword_ShouldProduceDifferentHashesDueToSalt() {
        // Given
        PasswordEncoder encoder = securityConfig.passwordEncoder();
        String password = "password123";
        
        // When
        String hash1 = encoder.encode(password);
        String hash2 = encoder.encode(password);
        
        // Then
        assertNotEquals(hash1, hash2, "Same password should produce different hashes due to salt");
        assertTrue(encoder.matches(password, hash1), "First hash should match password");
        assertTrue(encoder.matches(password, hash2), "Second hash should match password");
    }
}
