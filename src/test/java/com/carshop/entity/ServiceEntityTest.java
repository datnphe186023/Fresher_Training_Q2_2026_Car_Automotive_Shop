package com.carshop.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Service entity
 */
class ServiceEntityTest {
    
    @Test
    void testServiceCreation() {
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .build();
        
        Service service = Service.builder()
                .name("Ceramic Coating")
                .category(category)
                .description("Premium ceramic coating for long-lasting protection")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .imageUrls("https://example.com/image1.jpg,https://example.com/image2.jpg")
                .build();
        
        assertNotNull(service);
        assertEquals("Ceramic Coating", service.getName());
        assertEquals(category, service.getCategory());
        assertEquals("Premium ceramic coating for long-lasting protection", service.getDescription());
        assertEquals(new BigDecimal("500.00"), service.getBasePrice());
        assertEquals(180, service.getDurationMinutes());
        assertEquals("https://example.com/image1.jpg,https://example.com/image2.jpg", service.getImageUrls());
        assertNotNull(service.getBookings());
        assertTrue(service.getBookings().isEmpty());
    }
    
    @Test
    void testServiceWithMinimalFields() {
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .build();
        
        Service service = Service.builder()
                .name("Basic Wax")
                .category(category)
                .basePrice(new BigDecimal("50.00"))
                .durationMinutes(30)
                .build();
        
        assertNotNull(service);
        assertEquals("Basic Wax", service.getName());
        assertNull(service.getDescription());
        assertNull(service.getImageUrls());
    }
    
    @Test
    void testAddBookingToService() {
        Service service = Service.builder()
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        Booking booking = Booking.builder()
                .id(1L)
                .build();
        
        service.addBooking(booking);
        
        assertEquals(1, service.getBookings().size());
        assertTrue(service.getBookings().contains(booking));
        assertEquals(service, booking.getService());
    }
    
    @Test
    void testRemoveBookingFromService() {
        Service service = Service.builder()
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        Booking booking = Booking.builder()
                .id(1L)
                .build();
        
        service.addBooking(booking);
        service.removeBooking(booking);
        
        assertTrue(service.getBookings().isEmpty());
        assertNull(booking.getService());
    }
    
    @Test
    void testEqualsAndHashCode() {
        Service service1 = Service.builder()
                .id(1L)
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        Service service2 = Service.builder()
                .id(1L)
                .name("Different Name")
                .basePrice(new BigDecimal("600.00"))
                .durationMinutes(120)
                .build();
        
        Service service3 = Service.builder()
                .id(2L)
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        assertEquals(service1, service2); // Same ID
        assertNotEquals(service1, service3); // Different ID
        assertEquals(service1.hashCode(), service2.hashCode());
    }
    
    @Test
    void testToString() {
        Service service = Service.builder()
                .id(1L)
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        String toString = service.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Ceramic Coating"));
        assertFalse(toString.contains("category")); // Excluded from toString
        assertFalse(toString.contains("bookings")); // Excluded from toString
    }
    
    @Test
    void testPriceValidation() {
        // Test that BigDecimal handles precision correctly
        Service service = Service.builder()
                .name("Test Service")
                .basePrice(new BigDecimal("123.45"))
                .durationMinutes(60)
                .build();
        
        assertEquals(0, service.getBasePrice().compareTo(new BigDecimal("123.45")));
    }
    
    @Test
    void testManyToOneRelationship() {
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .build();
        
        Service service1 = Service.builder()
                .name("Ceramic Coating")
                .category(category)
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        Service service2 = Service.builder()
                .name("Paint Sealant")
                .category(category)
                .basePrice(new BigDecimal("300.00"))
                .durationMinutes(120)
                .build();
        
        assertEquals(category, service1.getCategory());
        assertEquals(category, service2.getCategory());
    }
}
