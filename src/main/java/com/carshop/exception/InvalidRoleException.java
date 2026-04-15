package com.carshop.exception;

/**
 * Exception thrown when an invalid role is specified during registration.
 * Results in HTTP 400 Bad Request response.
 */
public class InvalidRoleException extends RuntimeException {
    
    public InvalidRoleException(String message) {
        super(message);
    }
    
    public InvalidRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}
