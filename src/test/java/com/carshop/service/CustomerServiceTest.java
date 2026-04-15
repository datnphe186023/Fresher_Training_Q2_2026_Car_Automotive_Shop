package com.carshop.service;

import com.carshop.entity.Customer;
import com.carshop.repository.CustomerRepository;
import com.carshop.util.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService.
 * Tests customer find-or-create logic and data update operations.
 * 
 * Validates: Requirements 9.4, 12.1, 12.2, 12.3, 12.4
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    private Customer existingCustomer;
    private String testPhone;
    private String normalizedPhone;
    
    @BeforeEach
    void setUp() {
        testPhone = "+1 (555) 123-4567";
        normalizedPhone = "15551234567";
        
        existingCustomer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email("existing@example.com")
                .name("John Doe")
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // ========== findOrCreateCustomer Tests ==========
    
    @Test
    void findOrCreateCustomer_WithExistingCustomer_ReturnsExistingCustomer() {
        // Given
        try (MockedStatic<PhoneNumberValidator> mockedValidator = mockStatic(PhoneNumberValidator.class)) {
            mockedValidator.when(() -> PhoneNumberValidator.normalize(testPhone))
                    .thenReturn(normalizedPhone);
            
            when(customerRepository.findByPhoneNumber(normalizedPhone))
                    .thenReturn(Optional.of(existingCustomer));
            
            // When
            Customer result = customerService.findOrCreateCustomer(testPhone, null, null);
            
            // Then
            assertNotNull(result);
            assertEquals(existingCustomer.getId(), result.getId());
            assertEquals(normalizedPhone, result.getPhoneNumber());
            assertEquals("existing@example.com", result.getEmail());
            assertEquals("John Doe", result.getName());
            
            verify(customerRepository).findByPhoneNumber(normalizedPhone);
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }
    
    @Test
    void findOrCreateCustomer_WithNewCustomer_CreatesAndReturnsNewCustomer() {
        // Given
        String newEmail = "new@example.com";
        String newName = "Jane Smith";
        
        try (MockedStatic<PhoneNumberValidator> mockedValidator = mockStatic(PhoneNumberValidator.class)) {
            mockedValidator.when(() -> PhoneNumberValidator.normalize(testPhone))
                    .thenReturn(normalizedPhone);
            
            when(customerRepository.findByPhoneNumber(normalizedPhone))
                    .thenReturn(Optional.empty());
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer customer = invocation.getArgument(0);
                customer.setId(2L);
                customer.setCreatedAt(LocalDateTime.now());
                return customer;
            });
            
            // When
            Customer result = customerService.findOrCreateCustomer(testPhone, newEmail, newName);
            
            // Then
            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals(normalizedPhone, result.getPhoneNumber());
            assertEquals(newEmail, result.getEmail());
            assertEquals(newName, result.getName());
            assertNotNull(result.getCreatedAt());
            
            verify(customerRepository).findByPhoneNumber(normalizedPhone);
            verify(customerRepository).save(any(Customer.class));
        }
    }
    
    @Test
    void findOrCreateCustomer_WithNewCustomerAndNullData_CreatesCustomerWithNullEmailAndName() {
        // Given
        try (MockedStatic<PhoneNumberValidator> mockedValidator = mockStatic(PhoneNumberValidator.class)) {
            mockedValidator.when(() -> PhoneNumberValidator.normalize(testPhone))
                    .thenReturn(normalizedPhone);
            
            when(customerRepository.findByPhoneNumber(normalizedPhone))
                    .thenReturn(Optional.empty());
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
                Customer customer = invocation.getArgument(0);
                customer.setId(3L);
                customer.setCreatedAt(LocalDateTime.now());
                return customer;
            });
            
            // When
            Customer result = customerService.findOrCreateCustomer(testPhone, null, null);
            
            // Then
            assertNotNull(result);
            assertEquals(normalizedPhone, result.getPhoneNumber());
            assertNull(result.getEmail());
            assertNull(result.getName());
            
            verify(customerRepository).save(any(Customer.class));
        }
    }
    
    @Test
    void findOrCreateCustomer_WithInvalidPhone_ThrowsIllegalArgumentException() {
        // Given
        String invalidPhone = "invalid";
        
        try (MockedStatic<PhoneNumberValidator> mockedValidator = mockStatic(PhoneNumberValidator.class)) {
            mockedValidator.when(() -> PhoneNumberValidator.normalize(invalidPhone))
                    .thenThrow(new IllegalArgumentException("Invalid phone number format: " + invalidPhone));
            
            // When & Then
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> customerService.findOrCreateCustomer(invalidPhone, null, null)
            );
            
            assertTrue(exception.getMessage().contains("Invalid phone number format"));
            verify(customerRepository, never()).findByPhoneNumber(anyString());
            verify(customerRepository, never()).save(any(Customer.class));
        }
    }
    
    @Test
    void findOrCreateCustomer_WithExistingCustomerAndNewEmail_UpdatesEmail() {
        // Given
        Customer customerWithoutEmail = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name("John Doe")
                .createdAt(LocalDateTime.now())
                .build();
        
        String newEmail = "newemail@example.com";
        
        try (MockedStatic<PhoneNumberValidator> mockedValidator = mockStatic(PhoneNumberValidator.class)) {
            mockedValidator.when(() -> PhoneNumberValidator.normalize(testPhone))
                    .thenReturn(normalizedPhone);
            
            when(customerRepository.findByPhoneNumber(normalizedPhone))
                    .thenReturn(Optional.of(customerWithoutEmail));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // When
            Customer result = customerService.findOrCreateCustomer(testPhone, newEmail, null);
            
            // Then
            assertNotNull(result);
            assertEquals(newEmail, result.getEmail());
            assertEquals("John Doe", result.getName());
            
            verify(customerRepository).save(customerWithoutEmail);
        }
    }
    
    @Test
    void findOrCreateCustomer_WithExistingCustomerAndNewName_UpdatesName() {
        // Given
        Customer customerWithoutName = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email("existing@example.com")
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        String newName = "Jane Smith";
        
        try (MockedStatic<PhoneNumberValidator> mockedValidator = mockStatic(PhoneNumberValidator.class)) {
            mockedValidator.when(() -> PhoneNumberValidator.normalize(testPhone))
                    .thenReturn(normalizedPhone);
            
            when(customerRepository.findByPhoneNumber(normalizedPhone))
                    .thenReturn(Optional.of(customerWithoutName));
            when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
            
            // When
            Customer result = customerService.findOrCreateCustomer(testPhone, null, newName);
            
            // Then
            assertNotNull(result);
            assertEquals("existing@example.com", result.getEmail());
            assertEquals(newName, result.getName());
            
            verify(customerRepository).save(customerWithoutName);
        }
    }
    
    // ========== updateCustomerInfo Tests ==========
    
    @Test
    void updateCustomerInfo_WithNullEmailAndName_UpdatesBothFields() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        String newEmail = "updated@example.com";
        String newName = "Updated Name";
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, newEmail, newName);
        
        // Then
        assertNotNull(result);
        assertEquals(newEmail, result.getEmail());
        assertEquals(newName, result.getName());
        
        verify(customerRepository).save(customer);
    }
    
    @Test
    void updateCustomerInfo_WithExistingEmail_DoesNotUpdateEmail() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email("existing@example.com")
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        String newEmail = "newemail@example.com";
        String newName = "New Name";
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, newEmail, newName);
        
        // Then
        assertNotNull(result);
        assertEquals("existing@example.com", result.getEmail()); // Email not updated
        assertEquals(newName, result.getName()); // Name updated
        
        verify(customerRepository).save(customer);
    }
    
    @Test
    void updateCustomerInfo_WithExistingName_DoesNotUpdateName() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name("Existing Name")
                .createdAt(LocalDateTime.now())
                .build();
        
        String newEmail = "newemail@example.com";
        String newName = "New Name";
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, newEmail, newName);
        
        // Then
        assertNotNull(result);
        assertEquals(newEmail, result.getEmail()); // Email updated
        assertEquals("Existing Name", result.getName()); // Name not updated
        
        verify(customerRepository).save(customer);
    }
    
    @Test
    void updateCustomerInfo_WithExistingEmailAndName_DoesNotUpdate() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email("existing@example.com")
                .name("Existing Name")
                .createdAt(LocalDateTime.now())
                .build();
        
        String newEmail = "newemail@example.com";
        String newName = "New Name";
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, newEmail, newName);
        
        // Then
        assertNotNull(result);
        assertEquals("existing@example.com", result.getEmail()); // Email not updated
        assertEquals("Existing Name", result.getName()); // Name not updated
        
        verify(customerRepository, never()).save(any(Customer.class)); // No save called
    }
    
    @Test
    void updateCustomerInfo_WithNullNewEmailAndName_DoesNotUpdate() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, null, null);
        
        // Then
        assertNotNull(result);
        assertNull(result.getEmail());
        assertNull(result.getName());
        
        verify(customerRepository, never()).save(any(Customer.class)); // No save called
    }
    
    @Test
    void updateCustomerInfo_WithEmptyStringEmail_DoesNotUpdate() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, "   ", "Valid Name");
        
        // Then
        assertNotNull(result);
        assertNull(result.getEmail()); // Empty string not saved
        assertEquals("Valid Name", result.getName());
        
        verify(customerRepository).save(customer); // Save called for name update only
    }
    
    @Test
    void updateCustomerInfo_WithEmptyStringName_DoesNotUpdate() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, "valid@example.com", "   ");
        
        // Then
        assertNotNull(result);
        assertEquals("valid@example.com", result.getEmail());
        assertNull(result.getName()); // Empty string not saved
        
        verify(customerRepository).save(customer); // Save called for email update only
    }
    
    @Test
    void updateCustomerInfo_WithOnlyEmailUpdate_SavesOnce() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email(null)
                .name("Existing Name")
                .createdAt(LocalDateTime.now())
                .build();
        
        String newEmail = "newemail@example.com";
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, newEmail, "New Name");
        
        // Then
        assertNotNull(result);
        assertEquals(newEmail, result.getEmail());
        assertEquals("Existing Name", result.getName()); // Name not updated
        
        verify(customerRepository, times(1)).save(customer); // Save called once
    }
    
    @Test
    void updateCustomerInfo_WithOnlyNameUpdate_SavesOnce() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber(normalizedPhone)
                .email("existing@example.com")
                .name(null)
                .createdAt(LocalDateTime.now())
                .build();
        
        String newName = "New Name";
        
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Customer result = customerService.updateCustomerInfo(customer, "newemail@example.com", newName);
        
        // Then
        assertNotNull(result);
        assertEquals("existing@example.com", result.getEmail()); // Email not updated
        assertEquals(newName, result.getName());
        
        verify(customerRepository, times(1)).save(customer); // Save called once
    }
}
