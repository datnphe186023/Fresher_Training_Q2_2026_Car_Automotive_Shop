package com.carshop.service;

import com.carshop.dto.request.CreateCategoryRequest;
import com.carshop.dto.response.CategoryResponse;
import com.carshop.entity.ServiceCategory;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.CategoryMapper;
import com.carshop.repository.ServiceCategoryRepository;
import com.carshop.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing service categories.
 * Handles CRUD operations for categories and validates business rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;
    private final CategoryMapper categoryMapper;
    
    /**
     * Creates a new service category.
     * 
     * @param request the category creation request containing name and description
     * @return CategoryResponse with the created category details
     * @throws DuplicateResourceException if a category with the same name already exists
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating new category with name: {}", request.getName());
        
        try {
            ServiceCategory category = categoryMapper.toEntity(request);
            ServiceCategory savedCategory = categoryRepository.save(category);
            
            log.info("Successfully created category with ID: {}", savedCategory.getId());
            return categoryMapper.toResponse(savedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create category - duplicate name: {}", request.getName());
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
    }
    
    /**
     * Updates an existing service category.
     * 
     * @param id the ID of the category to update
     * @param request the category update request containing new name and description
     * @return CategoryResponse with the updated category details
     * @throws ResourceNotFoundException if the category does not exist
     * @throws DuplicateResourceException if the new name conflicts with an existing category
     */
    @Transactional
    public CategoryResponse updateCategory(Long id, CreateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory", "id", id));
        
        try {
            category.setName(request.getName());
            category.setDescription(request.getDescription());
            
            ServiceCategory updatedCategory = categoryRepository.save(category);
            
            log.info("Successfully updated category with ID: {}", id);
            return categoryMapper.toResponse(updatedCategory);
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to update category - duplicate name: {}", request.getName());
            throw new DuplicateResourceException("Category with name '" + request.getName() + "' already exists");
        }
    }
    
    /**
     * Deletes a service category.
     * Prevents deletion if the category has associated services.
     * 
     * @param id the ID of the category to delete
     * @throws ResourceNotFoundException if the category does not exist
     * @throws IllegalStateException if the category has associated services
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Attempting to delete category with ID: {}", id);
        
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory", "id", id));
        
        // Check if category has associated services
        long serviceCount = serviceRepository.findByCategory(category, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();
        
        if (serviceCount > 0) {
            log.error("Cannot delete category with ID {} - has {} associated services", id, serviceCount);
            throw new IllegalStateException("Cannot delete category with existing services");
        }
        
        categoryRepository.delete(category);
        log.info("Successfully deleted category with ID: {}", id);
    }
    
    /**
     * Retrieves all service categories.
     * 
     * @return List of all categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("Retrieving all categories");
        
        List<ServiceCategory> categories = categoryRepository.findAll();
        
        log.info("Found {} categories", categories.size());
        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a single category by ID.
     * 
     * @param id the ID of the category to retrieve
     * @return CategoryResponse with the category details
     * @throws ResourceNotFoundException if the category does not exist
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.info("Retrieving category with ID: {}", id);
        
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceCategory", "id", id));
        
        return categoryMapper.toResponse(category);
    }
}
