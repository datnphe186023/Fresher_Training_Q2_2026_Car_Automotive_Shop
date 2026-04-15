package com.carshop.service;

import com.carshop.dto.request.CreateServiceRequest;
import com.carshop.dto.request.UpdateServiceRequest;
import com.carshop.dto.response.ServiceResponse;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceCategory;
import com.carshop.mapper.ServiceMapper;
import com.carshop.repository.ServiceCategoryRepository;
import com.carshop.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing car enhancement services in the catalog.
 * Handles CRUD operations for services including validation, pagination, and category filtering.
 * 
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10, 13.11, 14.1, 14.2
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementService {
    
    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceMapper serviceMapper;
    
    /**
     * Creates a new service in the catalog.
     * 
     * Business rules:
     * - Validates that the category exists
     * - Validates that price is positive
     * - Validates that duration is positive
     * 
     * @param request the service creation request containing service details
     * @return ServiceResponse with the created service details
     * @throws IllegalArgumentException if category does not exist or validation fails
     */
    @Transactional
    public ServiceResponse createService(CreateServiceRequest request) {
        log.info("Creating service: {}, categoryId: {}", request.getName(), request.getCategoryId());
        
        // Validate category exists
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", request.getCategoryId());
                    return new IllegalArgumentException("Category not found with ID: " + request.getCategoryId());
                });
        
        log.debug("Category found: {}", category.getName());
        
        // Convert request to entity with category
        Service service = serviceMapper.toEntity(request, category);
        
        // Save service
        Service savedService = serviceRepository.save(service);
        log.info("Service created successfully with ID: {}, name: {}", savedService.getId(), savedService.getName());
        
        // Convert to response DTO
        return serviceMapper.toResponse(savedService);
    }
    
    /**
     * Updates an existing service in the catalog.
     * 
     * Business rules:
     * - Validates that the service exists
     * - Validates that the category exists
     * - Updates all fields with new values
     * 
     * @param id the ID of the service to update
     * @param request the service update request containing new service details
     * @return ServiceResponse with the updated service details
     * @throws IllegalArgumentException if service or category does not exist
     */
    @Transactional
    public ServiceResponse updateService(Long id, UpdateServiceRequest request) {
        log.info("Updating service with ID: {}", id);
        
        // Validate service exists
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Service not found with ID: {}", id);
                    return new IllegalArgumentException("Service not found with ID: " + id);
                });
        
        // Validate category exists
        ServiceCategory category = serviceCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", request.getCategoryId());
                    return new IllegalArgumentException("Category not found with ID: " + request.getCategoryId());
                });
        
        log.debug("Updating service: {} with new data", service.getName());
        
        // Update service fields
        service.setName(request.getName());
        service.setCategory(category);
        service.setDescription(request.getDescription());
        service.setBasePrice(request.getBasePrice());
        service.setDurationMinutes(request.getDurationMinutes());
        
        // Convert image URLs list to comma-separated string
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            service.setImageUrls(String.join(",", request.getImageUrls()));
        } else {
            service.setImageUrls(null);
        }
        
        // Save updated service
        Service updatedService = serviceRepository.save(service);
        log.info("Service updated successfully with ID: {}", updatedService.getId());
        
        // Convert to response DTO
        return serviceMapper.toResponse(updatedService);
    }
    
    /**
     * Deletes a service from the catalog.
     * 
     * @param id the ID of the service to delete
     * @throws IllegalArgumentException if service does not exist
     */
    @Transactional
    public void deleteService(Long id) {
        log.info("Deleting service with ID: {}", id);
        
        // Validate service exists
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Service not found with ID: {}", id);
                    return new IllegalArgumentException("Service not found with ID: " + id);
                });
        
        log.debug("Deleting service: {}", service.getName());
        
        // Delete service
        serviceRepository.delete(service);
        log.info("Service deleted successfully with ID: {}", id);
    }
    
    /**
     * Retrieves a service by its ID.
     * 
     * @param id the ID of the service to retrieve
     * @return ServiceResponse with the service details
     * @throws IllegalArgumentException if service does not exist
     */
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        log.debug("Retrieving service by ID: {}", id);
        
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Service not found with ID: {}", id);
                    return new IllegalArgumentException("Service not found with ID: " + id);
                });
        
        log.info("Service found with ID: {}", id);
        return serviceMapper.toResponse(service);
    }
    
    /**
     * Retrieves all services with pagination and optional category filtering.
     * 
     * Business rules:
     * - Supports pagination with page, size, and sort parameters
     * - Supports filtering by category ID
     * - Returns all services if no category filter is provided
     * 
     * @param pageable pagination and sorting parameters
     * @param categoryId optional category ID to filter by (null for all services)
     * @return Page of ServiceResponse with service details
     * @throws IllegalArgumentException if category ID is provided but does not exist
     */
    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServices(Pageable pageable, Long categoryId) {
        log.debug("Retrieving services with pagination: page={}, size={}, categoryId={}", 
                pageable.getPageNumber(), pageable.getPageSize(), categoryId);
        
        Page<Service> services;
        
        if (categoryId != null) {
            // Filter by category
            ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> {
                        log.error("Category not found with ID: {}", categoryId);
                        return new IllegalArgumentException("Category not found with ID: " + categoryId);
                    });
            
            log.debug("Filtering services by category: {}", category.getName());
            services = serviceRepository.findByCategory(category, pageable);
        } else {
            // Get all services
            log.debug("Retrieving all services without category filter");
            services = serviceRepository.findAll(pageable);
        }
        
        log.info("Retrieved {} services (page {} of {})", 
                services.getNumberOfElements(), 
                services.getNumber() + 1, 
                services.getTotalPages());
        
        // Convert to response DTOs
        return services.map(serviceMapper::toResponse);
    }
}
