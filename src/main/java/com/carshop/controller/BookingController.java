package com.carshop.controller;

import com.carshop.dto.request.CreateBookingRequest;
import com.carshop.dto.request.UpdateBookingStatusRequest;
import com.carshop.dto.response.BookingResponse;
import com.carshop.dto.response.ErrorResponse;
import com.carshop.entity.BookingStatus;
import com.carshop.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for booking endpoints.
 * Handles guest booking creation and tracking without authentication.
 * 
 * Validates: Requirements 9.1, 9.7, 9.8, 9.9, 9.10, 9.11, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    
    /**
     * Creates a new booking for a guest customer.
     * No authentication required.
     * 
     * @param request the booking creation request with validation
     * @return ResponseEntity with BookingResponse and HTTP 201 on success
     */
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        try {
            log.info("Received booking request for phone: {}", request.getPhoneNumber());
            BookingResponse response = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Booking creation failed: {}", e.getMessage());
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .path("/api/bookings")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Unexpected error during booking creation", e);
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message("An unexpected error occurred")
                    .path("/api/bookings")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Tracks bookings by booking reference or phone number.
     * No authentication required.
     * 
     * @param bookingReference optional booking reference to search for
     * @param phoneNumber optional phone number to search for
     * @return ResponseEntity with BookingResponse or List<BookingResponse>
     */
    @GetMapping("/track")
    public ResponseEntity<?> trackBooking(@RequestParam(required = false) String bookingReference,
                                          @RequestParam(required = false) String phoneNumber) {
        try {
            // Validate that at least one parameter is provided
            if ((bookingReference == null || bookingReference.trim().isEmpty()) && 
                (phoneNumber == null || phoneNumber.trim().isEmpty())) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .message("Either bookingReference or phoneNumber must be provided")
                        .path("/api/bookings/track")
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Track by booking reference
            if (bookingReference != null && !bookingReference.trim().isEmpty()) {
                log.info("Tracking booking by reference: {}", bookingReference);
                BookingResponse response = bookingService.getBookingByReference(bookingReference);
                return ResponseEntity.ok(response);
            }
            
            // Track by phone number
            log.info("Tracking bookings by phone: {}", phoneNumber);
            List<BookingResponse> responses = bookingService.getBookingsByPhone(phoneNumber);
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            log.error("Booking tracking failed: {}", e.getMessage());
            
            // Check if it's a "not found" error
            if (e.getMessage().contains("not found")) {
                ErrorResponse error = ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .message(e.getMessage())
                        .path("/api/bookings/track")
                        .build();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Other validation errors
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Bad Request")
                    .message(e.getMessage())
                    .path("/api/bookings/track")
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Unexpected error during booking tracking", e);
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .error("Internal Server Error")
                    .message("An unexpected error occurred")
                    .path("/api/bookings/track")
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Lists bookings with optional filters. Requires ADMIN or STAFF role.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<BookingResponse>> getBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(bookingService.getBookingsWithFilters(status, startDate, endDate, phoneNumber, page, size));
    }

    /**
     * Updates the status of a booking. Requires ADMIN or STAFF role.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, request.getStatus()));
    }

    /**
     * Handles validation errors for request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return ErrorResponse.FieldError.builder()
                            .field(fieldName)
                            .message(errorMessage)
                            .build();
                })
                .toList();
        
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Invalid request data")
                .path("/api/bookings")
                .errors(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
