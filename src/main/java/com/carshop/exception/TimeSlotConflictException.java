package com.carshop.exception;

/**
 * Exception thrown when time slot conflict is detected during appointment booking.
 */
public class TimeSlotConflictException extends RuntimeException {
    public TimeSlotConflictException(String message) {
        super(message);
    }

    public TimeSlotConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
