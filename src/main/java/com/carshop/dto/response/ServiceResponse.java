package com.carshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for service information.
 * Contains service details including category, pricing, and images.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    
    /**
     * Service ID
     */
    private Long id;
    
    /**
     * Service name
     */
    private String name;
    
    /**
     * Service category information
     */
    private CategoryResponse category;
    
    /**
     * Service description
     */
    private String description;
    
    /**
     * Base price for the service
     */
    private BigDecimal basePrice;
    
    /**
     * Duration of the service in minutes
     */
    private Integer durationMinutes;
    
    /**
     * List of image URLs for the service
     */
    private List<String> imageUrls;
}
