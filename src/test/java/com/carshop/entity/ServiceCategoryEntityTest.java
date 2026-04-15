package com.carshop.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServiceCategory entity
 */
class ServiceCategoryEntityTest {
    
    @Test
    void testServiceCategoryCreation() {
        ServiceCategory category = ServiceCategory.builder()
                .name("Paint Protection")
                .description("Services related to paint protection and coating")
                .build();
        
        assertNotNull(category);
        assertEquals("Paint Protection", category.getName());
        assertEquals("Services related to paint protection and coating", category.getDescription());
        assertNotNull(category.getServices());
        assertTrue(category.getServices().isEmpty());
    }
    
    @Test
    void testAddServiceToCategory() {
        ServiceCategory category = ServiceCategory.builder()
                .name("Paint Protection")
                .build();
        
        Service service = Service.builder()
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        category.addService(service);
        
        assertEquals(1, category.getServices().size());
        assertTrue(category.getServices().contains(service));
        assertEquals(category, service.getCategory());
    }
    
    @Test
    void testRemoveServiceFromCategory() {
        ServiceCategory category = ServiceCategory.builder()
                .name("Paint Protection")
                .build();
        
        Service service = Service.builder()
                .name("Ceramic Coating")
                .basePrice(new BigDecimal("500.00"))
                .durationMinutes(180)
                .build();
        
        category.addService(service);
        category.removeService(service);
        
        assertTrue(category.getServices().isEmpty());
        assertNull(service.getCategory());
    }
    
    @Test
    void testEqualsAndHashCode() {
        ServiceCategory category1 = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .build();
        
        ServiceCategory category2 = ServiceCategory.builder()
                .id(1L)
                .name("Different Name")
                .build();
        
        ServiceCategory category3 = ServiceCategory.builder()
                .id(2L)
                .name("Paint Protection")
                .build();
        
        assertEquals(category1, category2); // Same ID
        assertNotEquals(category1, category3); // Different ID
        assertEquals(category1.hashCode(), category2.hashCode());
    }
    
    @Test
    void testToString() {
        ServiceCategory category = ServiceCategory.builder()
                .id(1L)
                .name("Paint Protection")
                .description("Test description")
                .build();
        
        String toString = category.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Paint Protection"));
        assertFalse(toString.contains("services")); // Excluded from toString
    }
}
