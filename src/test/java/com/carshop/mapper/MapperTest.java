package com.carshop.mapper;

import com.carshop.dto.request.CreateCategoryRequest;
import com.carshop.dto.request.CreateServiceRequest;
import com.carshop.dto.response.*;
import com.carshop.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for mapper classes.
 * Validates: Requirements 18.3, 18.5
 */
class MapperTest {
    
    private UserMapper userMapper;
    private CategoryMapper categoryMapper;
    private ServiceMapper serviceMapper;
    private CustomerMapper customerMapper;
    private TimeSlotMapper timeSlotMapper;
    private VehicleMapper vehicleMapper;
    private BookingMapper bookingMapper;
    
    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        categoryMapper = new CategoryMapper();
        serviceMapper = new ServiceMapper(categoryMapper);
        customerMapper = new CustomerMapper();
        timeSlotMapper = new TimeSlotMapper();
        vehicleMapper = new VehicleMapper();
        bookingMapper = new BookingMapper(customerMapper, serviceMapper, timeSlotMapper, vehicleMapper);
    }
    
    @Test
    void userMapper_toResponse_convertsUserToUserResponse() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .role(Role.ADMIN)
                .createdAt(LocalDateTime.now())
                .build();
        
        // When
        UserResponse response = userMapper.toResponse(user);
        
        // Then
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }
    
    @Test
    void userMapper_toResponse_withNullUser_returnsNull() {
        // When
        UserResponse response = userMapper.toResponse(null);
        
        // Then
        assertNull(response);
    }
    
    @Test
    void categoryMapper_toEntity_convertsRequestToEntity() {
        // Given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Interior Modification")
                .description("Interior enhancement services")
                .build();
        
        // When
        ServiceCategory category = categoryMapper.toEntity(request);
        
        // Then
        assertNotNull(category);
        assertEquals(request.getName(), category.getName());
        assertEquals(request.getDescription(), category.getDescription());
    }
    
    @Test
    void categoryMapper_toResponse_convertsCategoryToResponse() {
        // Given
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior Modification")
                .description("Interior enhancement services")
                .build();
        
        // When
        CategoryResponse response = categoryMapper.toResponse(category);
        
        // Then
        assertNotNull(response);
        assertEquals(category.getId(), response.getId());
        assertEquals(category.getName(), response.getName());
        assertEquals(category.getDescription(), response.getDescription());
    }
    
    @Test
    void categoryMapper_withNullInput_returnsNull() {
        // When & Then
        assertNull(categoryMapper.toEntity(null));
        assertNull(categoryMapper.toResponse(null));
    }
    
    @Test
    void serviceMapper_toEntity_convertsRequestToEntity() {
        // Given
        CreateServiceRequest request = CreateServiceRequest.builder()
                .name("Leather Seat Installation")
                .categoryId(1L)
                .description("Premium leather seat installation")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .imageUrls(Arrays.asList("url1.jpg", "url2.jpg"))
                .build();
        
        // When
        Service service = serviceMapper.toEntity(request);
        
        // Then
        assertNotNull(service);
        assertEquals(request.getName(), service.getName());
        assertEquals(request.getDescription(), service.getDescription());
        assertEquals(request.getBasePrice(), service.getBasePrice());
        assertEquals(request.getDurationMinutes(), service.getDurationMinutes());
        assertEquals("url1.jpg,url2.jpg", service.getImageUrls());
    }
    
    @Test
    void serviceMapper_toEntity_withCategory_setsCategory() {
        // Given
        CreateServiceRequest request = CreateServiceRequest.builder()
                .name("Leather Seat Installation")
                .categoryId(1L)
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .build();
        
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior Modification")
                .build();
        
        // When
        Service service = serviceMapper.toEntity(request, category);
        
        // Then
        assertNotNull(service);
        assertEquals(category, service.getCategory());
    }
    
    @Test
    void serviceMapper_toResponse_convertsServiceToResponse() {
        // Given
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior Modification")
                .description("Interior enhancement services")
                .build();
        
        Service service = Service.builder()
                .id(1L)
                .name("Leather Seat Installation")
                .category(category)
                .description("Premium leather seat installation")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .imageUrls("url1.jpg,url2.jpg")
                .build();
        
        // When
        ServiceResponse response = serviceMapper.toResponse(service);
        
        // Then
        assertNotNull(response);
        assertEquals(service.getId(), response.getId());
        assertEquals(service.getName(), response.getName());
        assertEquals(service.getDescription(), response.getDescription());
        assertEquals(service.getBasePrice(), response.getBasePrice());
        assertEquals(service.getDurationMinutes(), response.getDurationMinutes());
        assertNotNull(response.getCategory());
        assertEquals(category.getId(), response.getCategory().getId());
        assertEquals(2, response.getImageUrls().size());
        assertTrue(response.getImageUrls().contains("url1.jpg"));
        assertTrue(response.getImageUrls().contains("url2.jpg"));
    }
    
    @Test
    void serviceMapper_toResponse_withEmptyImageUrls_returnsEmptyList() {
        // Given
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior Modification")
                .build();
        
        Service service = Service.builder()
                .id(1L)
                .name("Leather Seat Installation")
                .category(category)
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .imageUrls("")
                .build();
        
        // When
        ServiceResponse response = serviceMapper.toResponse(service);
        
        // Then
        assertNotNull(response);
        assertTrue(response.getImageUrls().isEmpty());
    }
    
    @Test
    void serviceMapper_withNullInput_returnsNull() {
        // When & Then
        assertNull(serviceMapper.toEntity(null));
        assertNull(serviceMapper.toResponse(null));
    }
    
    @Test
    void customerMapper_toResponse_convertsCustomerToResponse() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .email("customer@example.com")
                .name("John Doe")
                .createdAt(LocalDateTime.now())
                .build();
        
        // When
        CustomerResponse response = customerMapper.toResponse(customer);
        
        // Then
        assertNotNull(response);
        assertEquals(customer.getId(), response.getId());
        assertEquals(customer.getPhoneNumber(), response.getPhoneNumber());
        assertEquals(customer.getEmail(), response.getEmail());
        assertEquals(customer.getName(), response.getName());
        assertEquals(customer.getCreatedAt(), response.getCreatedAt());
    }
    
    @Test
    void customerMapper_toResponse_withNullCustomer_returnsNull() {
        // When
        CustomerResponse response = customerMapper.toResponse(null);
        
        // Then
        assertNull(response);
    }
    
    @Test
    void bookingMapper_toResponse_convertsBookingToResponse() {
        // Given
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior Modification")
                .build();
        
        Service service = Service.builder()
                .id(1L)
                .name("Leather Seat Installation")
                .category(category)
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .build();
        
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .email("customer@example.com")
                .name("John Doe")
                .createdAt(LocalDateTime.now())
                .build();
        
        Booking booking = Booking.builder()
                .id(1L)
                .bookingReference("BK123456")
                .customer(customer)
                .service(service)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        // When
        BookingResponse response = bookingMapper.toResponse(booking);
        
        // Then
        assertNotNull(response);
        assertEquals(booking.getId(), response.getId());
        assertEquals(booking.getBookingReference(), response.getBookingReference());
        assertEquals(booking.getBookingDate(), response.getBookingDate());
        assertEquals(booking.getStatus(), response.getStatus());
        assertEquals(booking.getCreatedAt(), response.getCreatedAt());
        
        assertNotNull(response.getCustomer());
        assertEquals(customer.getId(), response.getCustomer().getId());
        assertEquals(customer.getPhoneNumber(), response.getCustomer().getPhoneNumber());
        
        assertNotNull(response.getService());
        assertEquals(service.getId(), response.getService().getId());
        assertEquals(service.getName(), response.getService().getName());
    }
    
    @Test
    void bookingMapper_toResponse_withNullBooking_returnsNull() {
        // When
        BookingResponse response = bookingMapper.toResponse(null);
        
        // Then
        assertNull(response);
    }
}
