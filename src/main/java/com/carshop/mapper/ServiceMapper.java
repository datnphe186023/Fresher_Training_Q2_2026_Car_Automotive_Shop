package com.carshop.mapper;

import com.carshop.dto.request.CreateServiceRequest;
import com.carshop.dto.response.ServiceResponse;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceCategory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Service entity and DTOs.
 */
@Component
public class ServiceMapper {
    
    private final CategoryMapper categoryMapper;
    
    public ServiceMapper(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    
    /**
     * Converts a CreateServiceRequest DTO to a Service entity.
     * Note: The category must be set separately after retrieving from the database.
     * 
     * @param request the CreateServiceRequest DTO
     * @return Service entity (without category set)
     */
    public Service toEntity(CreateServiceRequest request) {
        if (request == null) {
            return null;
        }
        
        // Convert image URLs list to comma-separated string for storage
        String imageUrlsString = null;
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            imageUrlsString = String.join(",", request.getImageUrls());
        }
        
        return Service.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .imageUrls(imageUrlsString)
                .build();
    }
    
    /**
     * Converts a CreateServiceRequest DTO to a Service entity with category.
     * 
     * @param request the CreateServiceRequest DTO
     * @param category the ServiceCategory entity
     * @return Service entity with category set
     */
    public Service toEntity(CreateServiceRequest request, ServiceCategory category) {
        Service service = toEntity(request);
        if (service != null) {
            service.setCategory(category);
        }
        return service;
    }
    
    /**
     * Converts a Service entity to a ServiceResponse DTO.
     * 
     * @param service the Service entity to convert
     * @return ServiceResponse DTO with service information
     */
    public ServiceResponse toResponse(Service service) {
        if (service == null) {
            return null;
        }
        
        // Convert comma-separated image URLs string to list
        List<String> imageUrlsList = Collections.emptyList();
        if (service.getImageUrls() != null && !service.getImageUrls().isEmpty()) {
            imageUrlsList = Arrays.stream(service.getImageUrls().split(","))
                    .map(String::trim)
                    .filter(url -> !url.isEmpty())
                    .collect(Collectors.toList());
        }
        
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .category(categoryMapper.toResponse(service.getCategory()))
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .durationMinutes(service.getDurationMinutes())
                .imageUrls(imageUrlsList)
                .build();
    }
}
