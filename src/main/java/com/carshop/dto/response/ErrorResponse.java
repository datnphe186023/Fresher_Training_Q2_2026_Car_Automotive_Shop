package com.carshop.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for error information.
 * Provides structured error details for API consumers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code
     */
    private Integer status;
    
    /**
     * HTTP status reason phrase
     */
    private String error;
    
    /**
     * Error message
     */
    private String message;
    
    /**
     * Request path that caused the error
     */
    private String path;
    
    /**
     * List of validation errors (for field-level errors)
     */
    private List<FieldError> errors;
    
    /**
     * Nested class for field-level validation errors
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldError {
        /**
         * Field name that failed validation
         */
        private String field;
        
        /**
         * Validation error message
         */
        private String message;
    }
}
