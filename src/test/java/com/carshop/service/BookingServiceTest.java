package com.carshop.service;

import com.carshop.dto.request.CreateBookingRequest;
import com.carshop.dto.response.BookingResponse;
import com.carshop.dto.response.CustomerResponse;
import com.carshop.dto.response.ServiceResponse;
import com.carshop.entity.Booking;
import com.carshop.entity.BookingStatus;
import com.carshop.entity.Customer;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceCategory;
import com.carshop.mapper.BookingMapper;
import com.carshop.repository.BookingRepository;
import com.carshop.repository.ServiceRepository;
import com.carshop.util.BookingReferenceGenerator;
import com.carshop.util.EmailValidator;
import com.carshop.util.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingService.
 * Tests booking creation, retrieval by reference, and tracking by phone number.
 * 
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.10, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.8
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private ServiceRepository serviceRepository;
    
    @Mock
    private CustomerService customerService;
    
    @Mock
    private BookingMapper bookingMapper;
    
    @InjectMocks
    private BookingService bookingService;
    
    private CreateBookingRequest validRequest;
    private Customer testCustomer;
    private Service testService;
    private Booking testBooking;
    private BookingResponse testBookingResponse;
    private String testPhone;
    private String normalizedPhone;
    private String bookingReference;
    
    @BeforeEach
    void setUp() {
        testPhone = "+1 (555) 123-4567";
        normalizedPhone = "15551234567";
        bookingReference = "7K9M2A5P3T";
        
        // Setup test customer
        testCustomer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email("test@example.com")
                .name("John Doe")
                .createdAt(LocalDateTime.now())
                .bookings(new ArrayList<>())
                .build();
        
        // Setup test service
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Test Category")
                .build();
        
        testService = Service.builder()
                .id(1L)
                .name("Test Service")
                .category(category)
                .description("Test Description")
                .basePrice(new BigDecimal("100.00"))
                .durationMinutes(60)
                .build();
        
        // Setup test booking
        testBooking = Booking.builder()
                .id(1L)
                .customer(testCustomer)
                .service(testService)
                .bookingReference(bookingReference)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Setup test booking response
        testBookingResponse = BookingResponse.builder()
                .id(1L)
                .bookingReference(bookingReference)
                .customer(CustomerResponse.builder()
                        .id(1L)
                        .phoneNumber(normalizedPhone)
                        .email("test@example.com")
                        .name("John Doe")
                        .build())
                .service(ServiceResponse.builder()
                        .id(1L)
                        .name("Test Service")
                        .build())
                .bookingDate(testBooking.getBookingDate())
                .status(BookingStatus.PENDING)
                .createdAt(testBooking.getCreatedAt())
                .build();
        
        // Setup valid request
        validRequest = CreateBookingRequest.builder()
                .phoneNumber(testPhone)
                .email("test@example.com")
                .name("John Doe")
                .serviceId(1L)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .build();
    }
    
    // ========== createBooking Tests ==========
    
    @Test
    void createBooking_WithValidData_CreatesBookingSuccessfully() {
        // Given
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class);
             MockedStatic<BookingReferenceGenerator> refGenerator = mockStatic(BookingReferenceGenerator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            emailValidator.when(() -> EmailValidator.isValid("test@example.com")).thenReturn(true);
            refGenerator.when(BookingReferenceGenerator::generateReference).thenReturn(bookingReference);
            
            when(customerService.findOrCreateCustomer(testPhone, "test@example.com", "John Doe"))
                    .thenReturn(testCustomer);
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toResponse(testBooking)).thenReturn(testBookingResponse);
            
            // When
            BookingResponse result = bookingService.createBooking(validRequest);
            
            // Then
            assertNotNull(result);
            assertEquals(bookingReference, result.getBookingReference());
            assertEquals(BookingStatus.PENDING, result.getStatus());
            
            verify(customerService).findOrCreateCustomer(testPhone, "test@example.com", "John Doe");
            verify(serviceRepository).findById(1L);
            verify(bookingRepository).save(any(Booking.class));
            verify(bookingMapper).toResponse(testBooking);
        }
    }
    
    @Test
    void createBooking_WithInvalidPhoneNumber_ThrowsIllegalArgumentException() {
        // Given
        String invalidPhone = "invalid";
        validRequest.setPhoneNumber(invalidPhone);
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class)) {
            phoneValidator.when(() -> PhoneNumberValidator.isValid(invalidPhone)).thenReturn(false);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> bookingService.createBooking(validRequest)
            );
            
            assertEquals("Invalid phone number format", exception.getMessage());
            verify(customerService, never()).findOrCreateCustomer(anyString(), anyString(), anyString());
            verify(serviceRepository, never()).findById(any());
            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }
    
    @Test
    void createBooking_WithInvalidEmail_ThrowsIllegalArgumentException() {
        // Given
        String invalidEmail = "invalid-email";
        validRequest.setEmail(invalidEmail);
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            emailValidator.when(() -> EmailValidator.isValid(invalidEmail)).thenReturn(false);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> bookingService.createBooking(validRequest)
            );
            
            assertEquals("Invalid email format", exception.getMessage());
            verify(customerService, never()).findOrCreateCustomer(anyString(), anyString(), anyString());
            verify(serviceRepository, never()).findById(any());
            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }
    
    @Test
    void createBooking_WithNullEmail_SkipsEmailValidation() {
        // Given
        validRequest.setEmail(null);
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class);
             MockedStatic<BookingReferenceGenerator> refGenerator = mockStatic(BookingReferenceGenerator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            refGenerator.when(BookingReferenceGenerator::generateReference).thenReturn(bookingReference);
            
            when(customerService.findOrCreateCustomer(testPhone, null, "John Doe"))
                    .thenReturn(testCustomer);
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toResponse(testBooking)).thenReturn(testBookingResponse);
            
            // When
            BookingResponse result = bookingService.createBooking(validRequest);
            
            // Then
            assertNotNull(result);
            emailValidator.verifyNoInteractions();
            verify(customerService).findOrCreateCustomer(testPhone, null, "John Doe");
        }
    }
    
    @Test
    void createBooking_WithEmptyEmail_SkipsEmailValidation() {
        // Given
        validRequest.setEmail("   ");
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class);
             MockedStatic<BookingReferenceGenerator> refGenerator = mockStatic(BookingReferenceGenerator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            refGenerator.when(BookingReferenceGenerator::generateReference).thenReturn(bookingReference);
            
            when(customerService.findOrCreateCustomer(testPhone, "   ", "John Doe"))
                    .thenReturn(testCustomer);
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
            when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
            when(bookingMapper.toResponse(testBooking)).thenReturn(testBookingResponse);
            
            // When
            BookingResponse result = bookingService.createBooking(validRequest);
            
            // Then
            assertNotNull(result);
            emailValidator.verifyNoInteractions();
        }
    }
    
    @Test
    void createBooking_WithNonExistentService_ThrowsIllegalArgumentException() {
        // Given
        Long nonExistentServiceId = 999L;
        validRequest.setServiceId(nonExistentServiceId);
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            emailValidator.when(() -> EmailValidator.isValid("test@example.com")).thenReturn(true);
            
            when(customerService.findOrCreateCustomer(testPhone, "test@example.com", "John Doe"))
                    .thenReturn(testCustomer);
            when(serviceRepository.findById(nonExistentServiceId)).thenReturn(Optional.empty());
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> bookingService.createBooking(validRequest)
            );
            
            assertTrue(exception.getMessage().contains("Service not found"));
            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }
    
    @Test
    void createBooking_GeneratesUniqueBookingReference() {
        // Given
        String uniqueReference = "UNIQUE123";
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class);
             MockedStatic<BookingReferenceGenerator> refGenerator = mockStatic(BookingReferenceGenerator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            emailValidator.when(() -> EmailValidator.isValid("test@example.com")).thenReturn(true);
            refGenerator.when(BookingReferenceGenerator::generateReference).thenReturn(uniqueReference);
            
            when(customerService.findOrCreateCustomer(testPhone, "test@example.com", "John Doe"))
                    .thenReturn(testCustomer);
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
                Booking booking = invocation.getArgument(0);
                assertEquals(uniqueReference, booking.getBookingReference());
                return booking;
            });
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testBookingResponse);
            
            // When
            bookingService.createBooking(validRequest);
            
            // Then
            refGenerator.verify(BookingReferenceGenerator::generateReference, times(1));
        }
    }
    
    @Test
    void createBooking_SetsPendingStatus() {
        // Given
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class);
             MockedStatic<EmailValidator> emailValidator = mockStatic(EmailValidator.class);
             MockedStatic<BookingReferenceGenerator> refGenerator = mockStatic(BookingReferenceGenerator.class)) {
            
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            emailValidator.when(() -> EmailValidator.isValid("test@example.com")).thenReturn(true);
            refGenerator.when(BookingReferenceGenerator::generateReference).thenReturn(bookingReference);
            
            when(customerService.findOrCreateCustomer(testPhone, "test@example.com", "John Doe"))
                    .thenReturn(testCustomer);
            when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
                Booking booking = invocation.getArgument(0);
                assertEquals(BookingStatus.PENDING, booking.getStatus());
                return booking;
            });
            when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testBookingResponse);
            
            // When
            bookingService.createBooking(validRequest);
            
            // Then
            verify(bookingRepository).save(any(Booking.class));
        }
    }
    
    // ========== getBookingByReference Tests ==========
    
    @Test
    void getBookingByReference_WithValidReference_ReturnsBooking() {
        // Given
        when(bookingRepository.findByBookingReference(bookingReference))
                .thenReturn(Optional.of(testBooking));
        when(bookingMapper.toResponse(testBooking)).thenReturn(testBookingResponse);
        
        // When
        BookingResponse result = bookingService.getBookingByReference(bookingReference);
        
        // Then
        assertNotNull(result);
        assertEquals(bookingReference, result.getBookingReference());
        assertEquals(BookingStatus.PENDING, result.getStatus());
        
        verify(bookingRepository).findByBookingReference(bookingReference);
        verify(bookingMapper).toResponse(testBooking);
    }
    
    @Test
    void getBookingByReference_WithNonExistentReference_ThrowsIllegalArgumentException() {
        // Given
        String nonExistentReference = "NOTFOUND";
        when(bookingRepository.findByBookingReference(nonExistentReference))
                .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.getBookingByReference(nonExistentReference)
        );
        
        assertTrue(exception.getMessage().contains("Booking not found"));
        verify(bookingMapper, never()).toResponse(any(Booking.class));
    }
    
    // ========== getBookingsByPhone Tests ==========
    
    @Test
    void getBookingsByPhone_WithValidPhone_ReturnsBookingList() {
        // Given
        Booking booking1 = Booking.builder()
                .id(1L)
                .bookingReference("REF001")
                .customer(testCustomer)
                .service(testService)
                .bookingDate(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.PENDING)
                .build();
        
        Booking booking2 = Booking.builder()
                .id(2L)
                .bookingReference("REF002")
                .customer(testCustomer)
                .service(testService)
                .bookingDate(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.CONFIRMED)
                .build();
        
        testCustomer.getBookings().add(booking1);
        testCustomer.getBookings().add(booking2);
        
        BookingResponse response1 = BookingResponse.builder()
                .id(1L)
                .bookingReference("REF001")
                .status(BookingStatus.PENDING)
                .build();
        
        BookingResponse response2 = BookingResponse.builder()
                .id(2L)
                .bookingReference("REF002")
                .status(BookingStatus.CONFIRMED)
                .build();
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class)) {
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            phoneValidator.when(() -> PhoneNumberValidator.normalize(testPhone)).thenReturn(normalizedPhone);
            
            when(customerService.findOrCreateCustomer(normalizedPhone, null, null))
                    .thenReturn(testCustomer);
            when(bookingMapper.toResponse(booking1)).thenReturn(response1);
            when(bookingMapper.toResponse(booking2)).thenReturn(response2);
            
            // When
            List<BookingResponse> result = bookingService.getBookingsByPhone(testPhone);
            
            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("REF001", result.get(0).getBookingReference());
            assertEquals("REF002", result.get(1).getBookingReference());
            
            verify(customerService).findOrCreateCustomer(normalizedPhone, null, null);
            verify(bookingMapper, times(2)).toResponse(any(Booking.class));
        }
    }
    
    @Test
    void getBookingsByPhone_WithNoBookings_ReturnsEmptyList() {
        // Given
        testCustomer.getBookings().clear();
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class)) {
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            phoneValidator.when(() -> PhoneNumberValidator.normalize(testPhone)).thenReturn(normalizedPhone);
            
            when(customerService.findOrCreateCustomer(normalizedPhone, null, null))
                    .thenReturn(testCustomer);
            
            // When
            List<BookingResponse> result = bookingService.getBookingsByPhone(testPhone);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            
            verify(bookingMapper, never()).toResponse(any(Booking.class));
        }
    }
    
    @Test
    void getBookingsByPhone_WithInvalidPhone_ThrowsIllegalArgumentException() {
        // Given
        String invalidPhone = "invalid";
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class)) {
            phoneValidator.when(() -> PhoneNumberValidator.isValid(invalidPhone)).thenReturn(false);
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> bookingService.getBookingsByPhone(invalidPhone)
            );
            
            assertEquals("Invalid phone number format", exception.getMessage());
            verify(customerService, never()).findOrCreateCustomer(anyString(), any(), any());
        }
    }
    
    @Test
    void getBookingsByPhone_NormalizesPhoneNumber() {
        // Given
        testCustomer.getBookings().clear();
        
        try (MockedStatic<PhoneNumberValidator> phoneValidator = mockStatic(PhoneNumberValidator.class)) {
            phoneValidator.when(() -> PhoneNumberValidator.isValid(testPhone)).thenReturn(true);
            phoneValidator.when(() -> PhoneNumberValidator.normalize(testPhone)).thenReturn(normalizedPhone);
            
            when(customerService.findOrCreateCustomer(normalizedPhone, null, null))
                    .thenReturn(testCustomer);
            
            // When
            bookingService.getBookingsByPhone(testPhone);
            
            // Then
            phoneValidator.verify(() -> PhoneNumberValidator.normalize(testPhone), times(1));
            verify(customerService).findOrCreateCustomer(normalizedPhone, null, null);
        }
    }
}
