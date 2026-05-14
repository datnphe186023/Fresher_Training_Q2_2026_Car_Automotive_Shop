package com.carshop.exception;

/**
 * Exception thrown when inventory stock operation is invalid.
 */
public class InventoryException extends RuntimeException {
    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
