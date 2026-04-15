package com.carshop.dto.response;

import lombok.*;

/**
 * Response DTO for service category information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    
    /**
     * Category ID
     */
    private Long id;
    
    /**
     * Category name
     */
    private String name;
    
    /**
     * Category description
     */
    private String description;
}
