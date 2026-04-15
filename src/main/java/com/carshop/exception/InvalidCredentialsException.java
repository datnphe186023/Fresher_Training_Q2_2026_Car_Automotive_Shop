package com.carshop.exception;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 * Results in HTTP 401 Unauthorized response.
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
