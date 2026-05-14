package com.carshop.dto.response;

import com.carshop.entity.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for booking information.
 * Contains booking details including customer, service, and status.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    
    /**
     * Booking ID
     */
    private Long id;
    
    /**
     * Unique booking reference for tracking
     */
    private String bookingReference;
    
    /**
     * Customer information
     */
    private CustomerResponse customer;
    
    /**
     * Service information
     */
    private ServiceResponse service;
    
    /**
     * Scheduled booking date and time
     */
    private LocalDateTime bookingDate;
    
    /**
     * Current booking status
     */
    private BookingStatus status;

    private TimeSlotResponse timeSlot;

    private VehicleResponse vehicle;

    private BigDecimal discountPercent;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;
}
