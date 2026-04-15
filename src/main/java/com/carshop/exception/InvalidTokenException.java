package com.carshop.exception;

/**
 * Exception thrown when a token is invalid or expired.
 * Results in HTTP 401 Unauthorized response.
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
