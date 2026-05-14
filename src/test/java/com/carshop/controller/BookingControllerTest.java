package com.carshop.controller;

import com.carshop.dto.request.CreateBookingRequest;
import com.carshop.dto.response.BookingResponse;
import com.carshop.dto.response.CustomerResponse;
import com.carshop.dto.response.ServiceResponse;
import com.carshop.entity.BookingStatus;
import com.carshop.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for BookingController.
 * Tests REST endpoints for booking creation and tracking.
 * 
 * Validates: Requirements 9.1, 9.7, 9.8, 9.9, 9.10, 9.11, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8
 */
@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private BookingService bookingService;
    
    private CreateBookingRequest validRequest;
    private BookingResponse bookingResponse;
    private String bookingReference;
    
    @BeforeEach
    void setUp() {
        bookingReference = "7K9M2A5P3T";
        
        validRequest = CreateBookingRequest.builder()
                .phoneNumber("+1 (555) 123-4567")
                .email("test@example.com")
                .name("John Doe")
                .serviceId(1L)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .build();
        
        bookingResponse = BookingResponse.builder()
                .id(1L)
                .bookingReference(bookingReference)
                .customer(CustomerResponse.builder()
                        .id(1L)
                        .phoneNumber("15551234567")
                        .email("test@example.com")
                        .name("John Doe")
                        .build())
                .service(ServiceResponse.builder()
                        .id(1L)
                        .name("Test Service")
                        .build())
                .bookingDate(validRequest.getBookingDate())
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // ========== POST /api/bookings Tests ==========
    
    @Test
    void createBooking_WithValidRequest_ReturnsCreated() throws Exception {
        // Given
        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenReturn(bookingResponse);
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingReference").value(bookingReference))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customer.phoneNumber").value("15551234567"))
                .andExpect(jsonPath("$.service.name").value("Test Service"));
        
        verify(bookingService).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithMissingPhoneNumber_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setPhoneNumber(null);
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field", hasItem("phoneNumber")));
        
        verify(bookingService, never()).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithInvalidPhoneNumberFormat_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setPhoneNumber("invalid");
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").isArray());
        
        verify(bookingService, never()).createBooking(any(CreateBookingRequest.class));
    }

    @Test
    void createBooking_WithInvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setEmail("invalid-email");
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
        
        verify(bookingService, never()).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithMissingServiceId_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setServiceId(null);
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItem("serviceId")));
        
        verify(bookingService, never()).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithMissingBookingDate_ReturnsBadRequest() throws Exception {
        // Given
        validRequest.setBookingDate(null);
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[*].field", hasItem("bookingDate")));
        
        verify(bookingService, never()).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithServiceNotFound_ReturnsBadRequest() throws Exception {
        // Given
        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenThrow(new IllegalArgumentException("Service not found with ID: 999"));
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("Service not found")));
        
        verify(bookingService).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithInvalidPhoneNumberFromService_ReturnsBadRequest() throws Exception {
        // Given
        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid phone number format"));
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));
        
        verify(bookingService).createBooking(any(CreateBookingRequest.class));
    }
    
    @Test
    void createBooking_WithUnexpectedException_ReturnsInternalServerError() throws Exception {
        // Given
        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        
        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        
        verify(bookingService).createBooking(any(CreateBookingRequest.class));
    }
    
    // ========== GET /api/bookings/track Tests ==========
    
    @Test
    void trackBooking_WithValidBookingReference_ReturnsBooking() throws Exception {
        // Given
        when(bookingService.getBookingByReference(bookingReference))
                .thenReturn(bookingResponse);
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("bookingReference", bookingReference))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookingReference").value(bookingReference))
                .andExpect(jsonPath("$.status").value("PENDING"));
        
        verify(bookingService).getBookingByReference(bookingReference);
        verify(bookingService, never()).getBookingsByPhone(anyString());
    }
    
    @Test
    void trackBooking_WithValidPhoneNumber_ReturnsBookingList() throws Exception {
        // Given
        String phoneNumber = "+1 (555) 123-4567";
        List<BookingResponse> bookings = Arrays.asList(bookingResponse);
        
        when(bookingService.getBookingsByPhone(phoneNumber))
                .thenReturn(bookings);
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bookingReference").value(bookingReference));
        
        verify(bookingService).getBookingsByPhone(phoneNumber);
        verify(bookingService, never()).getBookingByReference(anyString());
    }
    
    @Test
    void trackBooking_WithPhoneNumberNoBookings_ReturnsEmptyList() throws Exception {
        // Given
        String phoneNumber = "+1 (555) 123-4567";
        
        when(bookingService.getBookingsByPhone(phoneNumber))
                .thenReturn(Collections.emptyList());
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
        
        verify(bookingService).getBookingsByPhone(phoneNumber);
    }
    
    @Test
    void trackBooking_WithNonExistentBookingReference_ReturnsNotFound() throws Exception {
        // Given
        String nonExistentRef = "NOTFOUND";
        when(bookingService.getBookingByReference(nonExistentRef))
                .thenThrow(new IllegalArgumentException("Booking not found with reference: " + nonExistentRef));
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("bookingReference", nonExistentRef))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("Booking not found")));
        
        verify(bookingService).getBookingByReference(nonExistentRef);
    }
    
    @Test
    void trackBooking_WithNoParameters_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/bookings/track"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Either bookingReference or phoneNumber must be provided"));
        
        verify(bookingService, never()).getBookingByReference(anyString());
        verify(bookingService, never()).getBookingsByPhone(anyString());
    }
    
    @Test
    void trackBooking_WithEmptyParameters_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("bookingReference", "")
                        .param("phoneNumber", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Either bookingReference or phoneNumber must be provided"));
        
        verify(bookingService, never()).getBookingByReference(anyString());
        verify(bookingService, never()).getBookingsByPhone(anyString());
    }
    
    @Test
    void trackBooking_WithInvalidPhoneNumber_ReturnsBadRequest() throws Exception {
        // Given
        String invalidPhone = "invalid";
        when(bookingService.getBookingsByPhone(invalidPhone))
                .thenThrow(new IllegalArgumentException("Invalid phone number format"));
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("phoneNumber", invalidPhone))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid phone number format"));
        
        verify(bookingService).getBookingsByPhone(invalidPhone);
    }
    
    @Test
    void trackBooking_BookingReferenceTakesPrecedenceOverPhoneNumber() throws Exception {
        // Given
        String phoneNumber = "+1 (555) 123-4567";
        when(bookingService.getBookingByReference(bookingReference))
                .thenReturn(bookingResponse);
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("bookingReference", bookingReference)
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value(bookingReference));
        
        verify(bookingService).getBookingByReference(bookingReference);
        verify(bookingService, never()).getBookingsByPhone(anyString());
    }
    
    @Test
    void trackBooking_WithUnexpectedException_ReturnsInternalServerError() throws Exception {
        // Given
        when(bookingService.getBookingByReference(bookingReference))
                .thenThrow(new RuntimeException("Unexpected error"));
        
        // When & Then
        mockMvc.perform(get("/api/bookings/track")
                        .param("bookingReference", bookingReference))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        
        verify(bookingService).getBookingByReference(bookingReference);
    }
}
