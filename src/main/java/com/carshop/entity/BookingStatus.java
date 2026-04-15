package com.carshop.entity;

/**
 * Enum representing the status of a booking.
 */
public enum BookingStatus {
    /**
     * Booking has been created but not yet confirmed
     */
    PENDING,
    
    /**
     * Booking has been confirmed by staff
     */
    CONFIRMED,
    
    /**
     * Service is currently being performed
     */
    IN_PROGRESS,
    
    /**
     * Service has been completed
     */
    COMPLETED,
    
    /**
     * Booking has been cancelled
     */
    CANCELLED
}
