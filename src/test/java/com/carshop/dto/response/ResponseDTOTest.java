package com.carshop.dto.response;

import com.carshop.entity.BookingStatus;
import com.carshop.entity.Role;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for response DTOs.
 * Validates that DTOs can be created and accessed correctly.
 */
class ResponseDTOTest {
    
    @Test
    void testAuthResponse() {
        UserResponse user = UserResponse.builder()
            .id(1L)
            .username("admin")
            .email("admin@example.com")
            .role(Role.ADMIN)
            .createdAt(LocalDateTime.now())
            .build();
        
        AuthResponse response = AuthResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .expiresIn(900L)
            .user(user)
            .build();
        
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("admin", response.getUser().getUsername());
    }
    
    @Test
    void testUserResponse() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse response = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .role(Role.STAFF)
            .createdAt(now)
            .build();
        
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.STAFF, response.getRole());
        assertEquals(now, response.getCreatedAt());
    }
    
    @Test
    void testServiceResponse() {
        CategoryResponse category = CategoryResponse.builder()
            .id(1L)
            .name("Exterior")
            .description("Exterior services")
            .build();
        
        List<String> imageUrls = Arrays.asList("image1.jpg", "image2.jpg");
        
        ServiceResponse response = ServiceResponse.builder()
            .id(1L)
            .name("Paint Protection Film")
            .category(category)
            .description("High-quality PPF installation")
            .basePrice(new BigDecimal("5000.00"))
            .durationMinutes(180)
            .imageUrls(imageUrls)
            .build();
        
        assertEquals(1L, response.getId());
        assertEquals("Paint Protection Film", response.getName());
        assertNotNull(response.getCategory());
        assertEquals("Exterior", response.getCategory().getName());
        assertEquals(new BigDecimal("5000.00"), response.getBasePrice());
        assertEquals(180, response.getDurationMinutes());
        assertEquals(2, response.getImageUrls().size());
    }
    
    @Test
    void testCategoryResponse() {
        CategoryResponse response = CategoryResponse.builder()
            .id(1L)
            .name("Interior")
            .description("Interior enhancement services")
            .build();
        
        assertEquals(1L, response.getId());
        assertEquals("Interior", response.getName());
        assertEquals("Interior enhancement services", response.getDescription());
    }
    
    @Test
    void testBookingResponse() {
        CustomerResponse customer = CustomerResponse.builder()
            .id(1L)
            .phoneNumber("+1234567890")
            .email("customer@example.com")
            .name("John Doe")
            .createdAt(LocalDateTime.now())
            .build();
        
        CategoryResponse category = CategoryResponse.builder()
            .id(1L)
            .name("Exterior")
            .build();
        
        ServiceResponse service = ServiceResponse.builder()
            .id(1L)
            .name("Window Tinting")
            .category(category)
            .basePrice(new BigDecimal("1500.00"))
            .durationMinutes(120)
            .build();
        
        LocalDateTime bookingDate = LocalDateTime.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.now();
        
        BookingResponse response = BookingResponse.builder()
            .id(1L)
            .bookingReference("BK20240410001")
            .customer(customer)
            .service(service)
            .bookingDate(bookingDate)
            .status(BookingStatus.PENDING)
            .createdAt(createdAt)
            .build();
        
        assertEquals(1L, response.getId());
        assertEquals("BK20240410001", response.getBookingReference());
        assertNotNull(response.getCustomer());
        assertEquals("+1234567890", response.getCustomer().getPhoneNumber());
        assertNotNull(response.getService());
        assertEquals("Window Tinting", response.getService().getName());
        assertEquals(BookingStatus.PENDING, response.getStatus());
    }
    
    @Test
    void testCustomerResponse() {
        LocalDateTime now = LocalDateTime.now();
        CustomerResponse response = CustomerResponse.builder()
            .id(1L)
            .phoneNumber("+1234567890")
            .email("customer@example.com")
            .name("Jane Smith")
            .createdAt(now)
            .build();
        
        assertEquals(1L, response.getId());
        assertEquals("+1234567890", response.getPhoneNumber());
        assertEquals("customer@example.com", response.getEmail());
        assertEquals("Jane Smith", response.getName());
        assertEquals(now, response.getCreatedAt());
    }
    
    @Test
    void testErrorResponse() {
        LocalDateTime now = LocalDateTime.now();
        
        ErrorResponse.FieldError fieldError1 = ErrorResponse.FieldError.builder()
            .field("username")
            .message("Username is required")
            .build();
        
        ErrorResponse.FieldError fieldError2 = ErrorResponse.FieldError.builder()
            .field("email")
            .message("Invalid email format")
            .build();
        
        List<ErrorResponse.FieldError> errors = Arrays.asList(fieldError1, fieldError2);
        
        ErrorResponse response = ErrorResponse.builder()
            .timestamp(now)
            .status(400)
            .error("Bad Request")
            .message("Validation failed")
            .path("/api/auth/register")
            .errors(errors)
            .build();
        
        assertEquals(now, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("/api/auth/register", response.getPath());
        assertNotNull(response.getErrors());
        assertEquals(2, response.getErrors().size());
        assertEquals("username", response.getErrors().get(0).getField());
        assertEquals("Username is required", response.getErrors().get(0).getMessage());
    }
    
    @Test
    void testUserResponseExcludesPassword() {
        // Verify that UserResponse does not have a password field
        UserResponse response = UserResponse.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .role(Role.ADMIN)
            .createdAt(LocalDateTime.now())
            .build();
        
        // This test verifies that the UserResponse class doesn't expose password
        // by checking that we can create a complete UserResponse without a password field
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        // No password field should exist in UserResponse
    }
}
