package com.carshop.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Customer entity
 */
class CustomerEntityTest {
    
    @Test
    void testCustomerCreation() {
        // Given
        Customer customer = Customer.builder()
                .phoneNumber("+1234567890")
                .email("customer@example.com")
                .name("John Doe")
                .build();
        
        // Then
        assertNotNull(customer);
        assertEquals("+1234567890", customer.getPhoneNumber());
        assertEquals("customer@example.com", customer.getEmail());
        assertEquals("John Doe", customer.getName());
        assertNotNull(customer.getBookings());
        assertTrue(customer.getBookings().isEmpty());
    }
    
    @Test
    void testCustomerWithMinimalData() {
        // Given - customer with only phone number (required field)
        Customer customer = Customer.builder()
                .phoneNumber("+1234567890")
                .build();
        
        // Then
        assertNotNull(customer);
        assertEquals("+1234567890", customer.getPhoneNumber());
        assertNull(customer.getEmail());
        assertNull(customer.getName());
        assertNotNull(customer.getBookings());
        assertTrue(customer.getBookings().isEmpty());
    }
    
    @Test
    void testCustomerBookingRelationship() {
        // Given
        Customer customer = Customer.builder()
                .phoneNumber("+1234567890")
                .email("customer@example.com")
                .name("John Doe")
                .build();
        
        Booking booking = Booking.builder()
                .build();
        
        // When
        customer.addBooking(booking);
        
        // Then
        assertEquals(1, customer.getBookings().size());
        assertEquals(customer, booking.getCustomer());
        assertTrue(customer.getBookings().contains(booking));
    }
    
    @Test
    void testRemoveBooking() {
        // Given
        Customer customer = Customer.builder()
                .phoneNumber("+1234567890")
                .build();
        
        Booking booking = Booking.builder()
                .build();
        
        customer.addBooking(booking);
        assertEquals(1, customer.getBookings().size());
        
        // When
        customer.removeBooking(booking);
        
        // Then
        assertEquals(0, customer.getBookings().size());
        assertNull(booking.getCustomer());
    }
    
    @Test
    void testCustomerEquality() {
        // Given
        Customer customer1 = Customer.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .build();
        
        Customer customer2 = Customer.builder()
                .id(1L)
                .phoneNumber("+9876543210")
                .build();
        
        Customer customer3 = Customer.builder()
                .id(2L)
                .phoneNumber("+1234567890")
                .build();
        
        // Then - equality based on id only
        assertEquals(customer1, customer2);
        assertNotEquals(customer1, customer3);
    }
}
