package com.carshop.mapper;

import com.carshop.dto.request.CreateCategoryRequest;
import com.carshop.dto.response.CategoryResponse;
import com.carshop.entity.ServiceCategory;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between ServiceCategory entity and DTOs.
 */
@Component
public class CategoryMapper {
    
    /**
     * Converts a CreateCategoryRequest DTO to a ServiceCategory entity.
     * 
     * @param request the CreateCategoryRequest DTO
     * @return ServiceCategory entity
     */
    public ServiceCategory toEntity(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        return ServiceCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
    }
    
    /**
     * Converts a ServiceCategory entity to a CategoryResponse DTO.
     * 
     * @param category the ServiceCategory entity to convert
     * @return CategoryResponse DTO with category information
     */
    public CategoryResponse toResponse(ServiceCategory category) {
        if (category == null) {
            return null;
        }
        
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
