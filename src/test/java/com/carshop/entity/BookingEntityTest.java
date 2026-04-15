package com.carshop.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Booking entity
 */
class BookingEntityTest {
    
    @Test
    void testBookingCreation() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .build();
        
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior")
                .build();
        
        Service service = Service.builder()
                .id(1L)
                .name("Leather Seat Installation")
                .category(category)
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .build();
        
        LocalDateTime bookingDate = LocalDateTime.now().plusDays(1);
        
        Booking booking = Booking.builder()
                .customer(customer)
                .bookingReference("BK20240101001")
                .service(service)
                .bookingDate(bookingDate)
                .status(BookingStatus.PENDING)
                .build();
        
        // Then
        assertNotNull(booking);
        assertEquals(customer, booking.getCustomer());
        assertEquals("BK20240101001", booking.getBookingReference());
        assertEquals(service, booking.getService());
        assertEquals(bookingDate, booking.getBookingDate());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
    }
    
    @Test
    void testBookingStatusTransitions() {
        // Given
        Booking booking = Booking.builder()
                .bookingReference("BK20240101001")
                .status(BookingStatus.PENDING)
                .build();
        
        // When - transition through statuses
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        
        booking.setStatus(BookingStatus.CONFIRMED);
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        
        booking.setStatus(BookingStatus.IN_PROGRESS);
        assertEquals(BookingStatus.IN_PROGRESS, booking.getStatus());
        
        booking.setStatus(BookingStatus.COMPLETED);
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }
    
    @Test
    void testBookingCancellation() {
        // Given
        Booking booking = Booking.builder()
                .bookingReference("BK20240101001")
                .status(BookingStatus.CONFIRMED)
                .build();
        
        // When
        booking.setStatus(BookingStatus.CANCELLED);
        
        // Then
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }
    
    @Test
    void testBookingCustomerRelationship() {
        // Given
        Customer customer = Customer.builder()
                .id(1L)
                .phoneNumber("+1234567890")
                .build();
        
        Booking booking = Booking.builder()
                .bookingReference("BK20240101001")
                .status(BookingStatus.PENDING)
                .build();
        
        // When
        booking.setCustomer(customer);
        
        // Then
        assertEquals(customer, booking.getCustomer());
    }
    
    @Test
    void testBookingServiceRelationship() {
        // Given
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Interior")
                .build();
        
        Service service = Service.builder()
                .id(1L)
                .name("Leather Seat Installation")
                .category(category)
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(120)
                .build();
        
        Booking booking = Booking.builder()
                .bookingReference("BK20240101001")
                .status(BookingStatus.PENDING)
                .build();
        
        // When
        booking.setService(service);
        
        // Then
        assertEquals(service, booking.getService());
    }
    
    @Test
    void testBookingEquality() {
        // Given
        Booking booking1 = Booking.builder()
                .id(1L)
                .bookingReference("BK20240101001")
                .build();
        
        Booking booking2 = Booking.builder()
                .id(1L)
                .bookingReference("BK20240101002")
                .build();
        
        Booking booking3 = Booking.builder()
                .id(2L)
                .bookingReference("BK20240101001")
                .build();
        
        // Then - equality based on id only
        assertEquals(booking1, booking2);
        assertNotEquals(booking1, booking3);
    }
    
    @Test
    void testBookingReferenceUniqueness() {
        // Given - two bookings with same reference (simulating uniqueness constraint)
        String reference = "BK20240101001";
        
        Booking booking1 = Booking.builder()
                .id(1L)
                .bookingReference(reference)
                .build();
        
        Booking booking2 = Booking.builder()
                .id(2L)
                .bookingReference(reference)
                .build();
        
        // Then - both can be created in memory, but database constraint would prevent duplicate
        assertEquals(reference, booking1.getBookingReference());
        assertEquals(reference, booking2.getBookingReference());
        assertNotEquals(booking1.getId(), booking2.getId());
    }
    
    @Test
    void testAllBookingStatuses() {
        // Verify all enum values exist
        BookingStatus[] statuses = BookingStatus.values();
        
        assertEquals(5, statuses.length);
        assertEquals(BookingStatus.PENDING, BookingStatus.valueOf("PENDING"));
        assertEquals(BookingStatus.CONFIRMED, BookingStatus.valueOf("CONFIRMED"));
        assertEquals(BookingStatus.IN_PROGRESS, BookingStatus.valueOf("IN_PROGRESS"));
        assertEquals(BookingStatus.COMPLETED, BookingStatus.valueOf("COMPLETED"));
        assertEquals(BookingStatus.CANCELLED, BookingStatus.valueOf("CANCELLED"));
    }
}
