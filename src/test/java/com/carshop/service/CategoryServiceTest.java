package com.carshop.service;

import com.carshop.dto.request.CreateCategoryRequest;
import com.carshop.dto.response.CategoryResponse;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceCategory;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.CategoryMapper;
import com.carshop.repository.ServiceCategoryRepository;
import com.carshop.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryService.
 * Tests CRUD operations, validation, and business rules for category management.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    
    @Mock
    private ServiceCategoryRepository categoryRepository;
    
    @Mock
    private ServiceRepository serviceRepository;
    
    @Mock
    private CategoryMapper categoryMapper;
    
    @InjectMocks
    private CategoryService categoryService;
    
    private ServiceCategory testCategory;
    private CreateCategoryRequest createRequest;
    private CategoryResponse categoryResponse;
    
    @BeforeEach
    void setUp() {
        // Set up test category
        testCategory = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .description("Paint protection services")
                .build();
        
        // Set up create request
        createRequest = CreateCategoryRequest.builder()
                .name("Paint Protection")
                .description("Paint protection services")
                .build();
        
        // Set up category response
        categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Paint Protection")
                .description("Paint protection services")
                .build();
    }
    
    @Test
    void createCategory_WithValidData_CreatesCategory() {
        // Given
        when(categoryMapper.toEntity(createRequest)).thenReturn(testCategory);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);
        
        // When
        CategoryResponse result = categoryService.createCategory(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(categoryResponse.getId(), result.getId());
        assertEquals(categoryResponse.getName(), result.getName());
        verify(categoryMapper).toEntity(createRequest);
        verify(categoryRepository).save(testCategory);
        verify(categoryMapper).toResponse(testCategory);
    }
    
    @Test
    void createCategory_WithDuplicateName_ThrowsException() {
        // Given
        when(categoryMapper.toEntity(createRequest)).thenReturn(testCategory);
        when(categoryRepository.save(testCategory)).thenThrow(new DataIntegrityViolationException("Duplicate"));
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.createCategory(createRequest)
        );
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(categoryRepository).save(testCategory);
    }
    
    @Test
    void updateCategory_WithValidData_UpdatesCategory() {
        // Given
        CreateCategoryRequest updateRequest = CreateCategoryRequest.builder()
                .name("Updated Paint Protection")
                .description("Updated description")
                .build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);
        
        // When
        CategoryResponse result = categoryService.updateCategory(1L, updateRequest);
        
        // Then
        assertNotNull(result);
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(testCategory);
        verify(categoryMapper).toResponse(testCategory);
    }
    
    @Test
    void updateCategory_WithInvalidId_ThrowsException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.updateCategory(999L, createRequest)
        );
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any());
    }
    
    @Test
    void updateCategory_WithDuplicateName_ThrowsException() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenThrow(new DataIntegrityViolationException("Duplicate"));
        
        // When & Then
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> categoryService.updateCategory(1L, createRequest)
        );
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(testCategory);
    }
    
    @Test
    void deleteCategory_WithValidId_DeletesCategory() {
        // Given
        Page<Service> emptyPage = new PageImpl<>(Arrays.asList());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(serviceRepository.findByCategory(eq(testCategory), any(Pageable.class))).thenReturn(emptyPage);
        
        // When
        categoryService.deleteCategory(1L);
        
        // Then
        verify(categoryRepository).findById(1L);
        verify(serviceRepository).findByCategory(eq(testCategory), any(Pageable.class));
        verify(categoryRepository).delete(testCategory);
    }
    
    @Test
    void deleteCategory_WithInvalidId_ThrowsException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(999L)
        );
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).delete(any());
    }
    
    @Test
    void deleteCategory_WithAssociatedServices_ThrowsException() {
        // Given
        Service service = Service.builder().id(1L).name("Test Service").build();
        Page<Service> servicesPage = new PageImpl<>(Arrays.asList(service));
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(serviceRepository.findByCategory(eq(testCategory), any(Pageable.class))).thenReturn(servicesPage);
        
        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoryService.deleteCategory(1L)
        );
        
        assertTrue(exception.getMessage().contains("Cannot delete category with existing services"));
        verify(categoryRepository).findById(1L);
        verify(serviceRepository).findByCategory(eq(testCategory), any(Pageable.class));
        verify(categoryRepository, never()).delete(any());
    }
    
    @Test
    void getAllCategories_ReturnsAllCategories() {
        // Given
        ServiceCategory category2 = ServiceCategory.builder()
                .id(2L)
                .name("Interior Modification")
                .description("Interior services")
                .build();
        
        List<ServiceCategory> categories = Arrays.asList(testCategory, category2);
        CategoryResponse response2 = CategoryResponse.builder()
                .id(2L)
                .name("Interior Modification")
                .description("Interior services")
                .build();
        
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);
        when(categoryMapper.toResponse(category2)).thenReturn(response2);
        
        // When
        List<CategoryResponse> result = categoryService.getAllCategories();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository).findAll();
        verify(categoryMapper, times(2)).toResponse(any(ServiceCategory.class));
    }
    
    @Test
    void getCategoryById_WithValidId_ReturnsCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);
        
        // When
        CategoryResponse result = categoryService.getCategoryById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(categoryResponse.getId(), result.getId());
        assertEquals(categoryResponse.getName(), result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryMapper).toResponse(testCategory);
    }
    
    @Test
    void getCategoryById_WithInvalidId_ThrowsException() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(999L)
        );
        
        assertTrue(exception.getMessage().contains("not found"));
        verify(categoryRepository).findById(999L);
    }
}
