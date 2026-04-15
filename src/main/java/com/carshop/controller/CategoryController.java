package com.carshop.controller;

import com.carshop.dto.request.CreateCategoryRequest;
import com.carshop.dto.response.CategoryResponse;
import com.carshop.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for service category management.
 * Provides endpoints for CRUD operations on service categories.
 * Public endpoints: GET operations (no authentication required)
 * Protected endpoints: POST, PUT, DELETE (ADMIN role required)
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Get all service categories.
     * Public endpoint - no authentication required.
     *
     * @return List of all categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get a single category by ID.
     * Public endpoint - no authentication required.
     *
     * @param id the category ID
     * @return CategoryResponse with category details
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Create a new service category.
     * Protected endpoint - requires ADMIN role.
     *
     * @param request the category creation request
     * @return CategoryResponse with created category details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    /**
     * Update an existing service category.
     * Protected endpoint - requires ADMIN role.
     *
     * @param id the category ID to update
     * @param request the category update request
     * @return CategoryResponse with updated category details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Delete a service category.
     * Protected endpoint - requires ADMIN role.
     * Prevents deletion if category has associated services.
     *
     * @param id the category ID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
