package com.carshop.exception;

/**
 * Exception thrown when an invalid status transition is attempted.
 * Results in HTTP 422 Unprocessable Entity response.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(Object currentStatus, Object requestedStatus) {
        super(String.format("Invalid status transition from %s to %s", currentStatus, requestedStatus));
    }
}
