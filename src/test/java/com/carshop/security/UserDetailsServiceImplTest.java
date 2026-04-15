package com.carshop.security;

import com.carshop.entity.Role;
import com.carshop.entity.User;
import com.carshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserDetailsServiceImpl.
 * Tests user loading, mapping to UserDetails, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword123")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    @Test
    void loadUserByUsername_WithValidUsername_ReturnsUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("hashedPassword123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        
        verify(userRepository, times(1)).findByUsername("testuser");
    }
    
    @Test
    void loadUserByUsername_WithAdminRole_ReturnsCorrectAuthority() {
        // Given
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("adminuser");
        
        // Then
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_ADMIN", authority.getAuthority());
    }
    
    @Test
    void loadUserByUsername_WithStaffRole_ReturnsCorrectAuthority() {
        // Given
        testUser.setRole(Role.STAFF);
        when(userRepository.findByUsername("staffuser")).thenReturn(Optional.of(testUser));
        
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("staffuser");
        
        // Then
        assertNotNull(userDetails.getAuthorities());
        assertEquals(1, userDetails.getAuthorities().size());
        
        GrantedAuthority authority = userDetails.getAuthorities().iterator().next();
        assertEquals("ROLE_STAFF", authority.getAuthority());
    }
    
    @Test
    void loadUserByUsername_WithNonExistentUsername_ThrowsUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent")
        );
        
        assertEquals("User not found with username: nonexistent", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
    
    @Test
    void loadUserByUsername_WithNullUsername_ThrowsUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null)
        );
    }
    
    @Test
    void loadUserByUsername_WithEmptyUsername_ThrowsUsernameNotFoundException() {
        // Given
        when(userRepository.findByUsername("")).thenReturn(Optional.empty());
        
        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("")
        );
        
        assertTrue(exception.getMessage().contains("User not found"));
    }
    
    @Test
    void loadUserByUsername_CallsRepositoryOnce() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // When
        userDetailsService.loadUserByUsername("testuser");
        
        // Then
        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }
}
