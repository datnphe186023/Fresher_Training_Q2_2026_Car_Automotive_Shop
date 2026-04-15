package com.carshop.service;

import com.carshop.dto.request.CreateServiceRequest;
import com.carshop.dto.request.UpdateServiceRequest;
import com.carshop.dto.response.ServiceResponse;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceCategory;
import com.carshop.mapper.ServiceMapper;
import com.carshop.repository.ServiceCategoryRepository;
import com.carshop.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ServiceManagementService.
 * Tests CRUD operations, validation, pagination, and category filtering.
 */
@ExtendWith(MockitoExtension.class)
class ServiceManagementServiceTest {
    
    @Mock
    private ServiceRepository serviceRepository;
    
    @Mock
    private ServiceCategoryRepository serviceCategoryRepository;
    
    @Mock
    private ServiceMapper serviceMapper;
    
    @InjectMocks
    private ServiceManagementService serviceManagementService;
    
    private ServiceCategory testCategory;
    private Service testService;
    private CreateServiceRequest createRequest;
    private UpdateServiceRequest updateRequest;
    private ServiceResponse serviceResponse;
    
    @BeforeEach
    void setUp() {
        // Set up test category
        testCategory = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .description("Paint protection services")
                .build();
        
        // Set up test service
        testService = Service.builder()
                .id(1L)
                .name("Ceramic Coating")
                .category(testCategory)
                .description("Premium ceramic coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .imageUrls("image1.jpg,image2.jpg")
                .build();
        
        // Set up create request
        createRequest = CreateServiceRequest.builder()
                .name("Ceramic Coating")
                .categoryId(1L)
                .description("Premium ceramic coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .imageUrls(Arrays.asList("image1.jpg", "image2.jpg"))
                .build();
        
        // Set up update request
        updateRequest = UpdateServiceRequest.builder()
                .name("Updated Ceramic Coating")
                .categoryId(1L)
                .description("Updated description")
                .basePrice(new BigDecimal("600.00"))
                .durationMinutes(200)
                .imageUrls(Arrays.asList("new_image.jpg"))
                .build();
        
        // Set up service response
        serviceResponse = ServiceResponse.builder()
                .id(1L)
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
    }
    
    @Test
    void createService_WithValidData_CreatesService() {
        // Given
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(serviceMapper.toEntity(createRequest, testCategory)).thenReturn(testService);
        when(serviceRepository.save(testService)).thenReturn(testService);
        when(serviceMapper.toResponse(testService)).thenReturn(serviceResponse);
        
        // When
        ServiceResponse result = serviceManagementService.createService(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(serviceResponse.getId(), result.getId());
        verify(serviceCategoryRepository).findById(1L);
        verify(serviceRepository).save(testService);
        verify(serviceMapper).toResponse(testService);
    }
    
    @Test
    void createService_WithInvalidCategory_ThrowsException() {
        // Given
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceManagementService.createService(createRequest)
        );
        
        assertTrue(exception.getMessage().contains("Category not found"));
        verify(serviceCategoryRepository).findById(1L);
        verify(serviceRepository, never()).save(any());
    }
    
    @Test
    void updateService_WithValidData_UpdatesService() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(serviceRepository.save(testService)).thenReturn(testService);
        when(serviceMapper.toResponse(testService)).thenReturn(serviceResponse);
        
        // When
        ServiceResponse result = serviceManagementService.updateService(1L, updateRequest);
        
        // Then
        assertNotNull(result);
        verify(serviceRepository).findById(1L);
        verify(serviceCategoryRepository).findById(1L);
        verify(serviceRepository).save(testService);
    }
    
    @Test
    void updateService_WithInvalidServiceId_ThrowsException() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceManagementService.updateService(999L, updateRequest)
        );
        
        assertTrue(exception.getMessage().contains("Service not found"));
        verify(serviceRepository).findById(999L);
        verify(serviceRepository, never()).save(any());
    }
    
    @Test
    void updateService_WithInvalidCategory_ThrowsException() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceManagementService.updateService(1L, updateRequest)
        );
        
        assertTrue(exception.getMessage().contains("Category not found"));
        verify(serviceRepository).findById(1L);
        verify(serviceCategoryRepository).findById(1L);
        verify(serviceRepository, never()).save(any());
    }
    
    @Test
    void deleteService_WithValidId_DeletesService() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        
        // When
        serviceManagementService.deleteService(1L);
        
        // Then
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).delete(testService);
    }
    
    @Test
    void deleteService_WithInvalidId_ThrowsException() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceManagementService.deleteService(999L)
        );
        
        assertTrue(exception.getMessage().contains("Service not found"));
        verify(serviceRepository).findById(999L);
        verify(serviceRepository, never()).delete(any());
    }
    
    @Test
    void getServiceById_WithValidId_ReturnsService() {
        // Given
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceMapper.toResponse(testService)).thenReturn(serviceResponse);
        
        // When
        ServiceResponse result = serviceManagementService.getServiceById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(serviceResponse.getId(), result.getId());
        verify(serviceRepository).findById(1L);
        verify(serviceMapper).toResponse(testService);
    }
    
    @Test
    void getServiceById_WithInvalidId_ThrowsException() {
        // Given
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceManagementService.getServiceById(999L)
        );
        
        assertTrue(exception.getMessage().contains("Service not found"));
        verify(serviceRepository).findById(999L);
    }
    
    @Test
    void getAllServices_WithoutCategoryFilter_ReturnsAllServices() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Service> services = Arrays.asList(testService);
        Page<Service> servicePage = new PageImpl<>(services, pageable, services.size());
        
        when(serviceRepository.findAll(pageable)).thenReturn(servicePage);
        when(serviceMapper.toResponse(testService)).thenReturn(serviceResponse);
        
        // When
        Page<ServiceResponse> result = serviceManagementService.getAllServices(pageable, null);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(serviceRepository).findAll(pageable);
        verify(serviceMapper).toResponse(testService);
    }
    
    @Test
    void getAllServices_WithCategoryFilter_ReturnsFilteredServices() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Service> services = Arrays.asList(testService);
        Page<Service> servicePage = new PageImpl<>(services, pageable, services.size());
        
        when(serviceCategoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(serviceRepository.findByCategory(testCategory, pageable)).thenReturn(servicePage);
        when(serviceMapper.toResponse(testService)).thenReturn(serviceResponse);
        
        // When
        Page<ServiceResponse> result = serviceManagementService.getAllServices(pageable, 1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(serviceCategoryRepository).findById(1L);
        verify(serviceRepository).findByCategory(testCategory, pageable);
        verify(serviceMapper).toResponse(testService);
    }
    
    @Test
    void getAllServices_WithInvalidCategoryFilter_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(serviceCategoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serviceManagementService.getAllServices(pageable, 999L)
        );
        
        assertTrue(exception.getMessage().contains("Category not found"));
        verify(serviceCategoryRepository).findById(999L);
        verify(serviceRepository, never()).findAll(any(Pageable.class));
        verify(serviceRepository, never()).findByCategory(any(), any());
    }
}
