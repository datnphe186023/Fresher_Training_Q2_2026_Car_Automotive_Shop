package com.carshop.controller;

import com.carshop.dto.request.CreateServiceRequest;
import com.carshop.dto.request.UpdateServiceRequest;
import com.carshop.dto.response.ServiceResponse;
import com.carshop.service.ServiceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for service management endpoints.
 * Provides public GET endpoints for browsing services and protected POST/PUT/DELETE endpoints for ADMIN users.
 * 
 * Validates: Requirements 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7, 13.8, 13.9, 13.10, 
 *            14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {
    
    private final ServiceManagementService serviceManagementService;
    
    /**
     * GET /api/services - Retrieve all services with pagination and optional category filtering.
     * Public endpoint - no authentication required.
     * 
     * @param page page number (default: 0)
     * @param size page size (default: 10)
     * @param sort sort specification (e.g., "price,asc" or "name,desc")
     * @param category optional category ID to filter by
     * @return paginated list of services
     */
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long category) {
        
        log.info("GET /api/services - page: {}, size: {}, sort: {}, category: {}", page, size, sort, category);
        
        // Build pageable with sorting if provided
        Pageable pageable = buildPageable(page, size, sort);
        
        // Retrieve services with pagination and optional category filter
        Page<ServiceResponse> services = serviceManagementService.getAllServices(pageable, category);
        
        log.info("Retrieved {} services (page {} of {})", 
                services.getNumberOfElements(), 
                services.getNumber() + 1, 
                services.getTotalPages());
        
        return ResponseEntity.ok(services);
    }
    
    /**
     * GET /api/services/{id} - Retrieve a single service by ID.
     * Public endpoint - no authentication required.
     * 
     * @param id service ID
     * @return service details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        log.info("GET /api/services/{} - Retrieving service by ID", id);
        
        ServiceResponse service = serviceManagementService.getServiceById(id);
        
        log.info("Service found: {}", service.getName());
        return ResponseEntity.ok(service);
    }
    
    /**
     * POST /api/services - Create a new service.
     * Requires ADMIN role.
     * 
     * @param request service creation request with validation
     * @return created service details with HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody CreateServiceRequest request) {
        log.info("POST /api/services - Creating service: {}", request.getName());
        
        ServiceResponse createdService = serviceManagementService.createService(request);
        
        log.info("Service created successfully with ID: {}", createdService.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdService);
    }
    
    /**
     * PUT /api/services/{id} - Update an existing service.
     * Requires ADMIN role.
     * 
     * @param id service ID to update
     * @param request service update request with validation
     * @return updated service details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request) {
        
        log.info("PUT /api/services/{} - Updating service", id);
        
        ServiceResponse updatedService = serviceManagementService.updateService(id, request);
        
        log.info("Service updated successfully with ID: {}", updatedService.getId());
        return ResponseEntity.ok(updatedService);
    }
    
    /**
     * DELETE /api/services/{id} - Delete a service.
     * Requires ADMIN role.
     * 
     * @param id service ID to delete
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        log.info("DELETE /api/services/{} - Deleting service", id);
        
        serviceManagementService.deleteService(id);
        
        log.info("Service deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Helper method to build Pageable with sorting support.
     * Parses sort parameter in format "field,direction" (e.g., "price,asc" or "name,desc").
     * 
     * @param page page number
     * @param size page size
     * @param sort sort specification (optional)
     * @return Pageable with pagination and sorting configuration
     */
    private Pageable buildPageable(int page, int size, String sort) {
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                String field = sortParams[0];
                String direction = sortParams[1];
                
                Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                        ? Sort.Direction.DESC 
                        : Sort.Direction.ASC;
                
                return PageRequest.of(page, size, Sort.by(sortDirection, field));
            }
        }
        
        return PageRequest.of(page, size);
    }
}
