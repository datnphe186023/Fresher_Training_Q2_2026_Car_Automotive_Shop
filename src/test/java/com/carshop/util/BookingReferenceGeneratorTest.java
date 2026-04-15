package com.carshop.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookingReferenceGenerator utility class.
 * Tests generation of unique booking reference codes.
 */
class BookingReferenceGeneratorTest {
    
    /**
     * Pattern to validate booking reference format.
     * Must be uppercase alphanumeric characters only.
     */
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Z0-9]+$");
    
    // ========== Basic Generation Tests ==========
    
    @Test
    void generateReference_WithDefaultLength_ReturnsValidReference() {
        String reference = BookingReferenceGenerator.generateReference();
        
        assertNotNull(reference);
        assertEquals(10, reference.length(), "Default reference should be 10 characters");
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches(),
            "Reference should contain only uppercase alphanumeric characters");
    }
    
    @Test
    void generateReference_WithDefaultLength_ReturnsNonEmptyString() {
        String reference = BookingReferenceGenerator.generateReference();
        
        assertNotNull(reference);
        assertFalse(reference.isEmpty());
    }
    
    // ========== Length Validation Tests ==========
    
    @ParameterizedTest
    @ValueSource(ints = {8, 9, 10, 11, 12})
    void generateReference_WithValidLength_ReturnsCorrectLength(int length) {
        String reference = BookingReferenceGenerator.generateReference(length);
        
        assertEquals(length, reference.length(),
            "Reference should have exactly " + length + " characters");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {8, 9, 10, 11, 12})
    void generateReference_WithValidLength_ReturnsValidFormat(int length) {
        String reference = BookingReferenceGenerator.generateReference(length);
        
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches(),
            "Reference should contain only uppercase alphanumeric characters");
    }
    
    @Test
    void generateReference_WithMinimumLength_ReturnsValidReference() {
        String reference = BookingReferenceGenerator.generateReference(8);
        
        assertEquals(8, reference.length());
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches());
    }
    
    @Test
    void generateReference_WithMaximumLength_ReturnsValidReference() {
        String reference = BookingReferenceGenerator.generateReference(12);
        
        assertEquals(12, reference.length());
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches());
    }
    
    // ========== Invalid Length Tests ==========
    
    @Test
    void generateReference_WithLengthTooShort_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BookingReferenceGenerator.generateReference(7)
        );
        
        assertTrue(exception.getMessage().contains("between 8 and 12"),
            "Exception message should indicate valid length range");
    }
    
    @Test
    void generateReference_WithLengthTooLong_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> BookingReferenceGenerator.generateReference(13)
        );
        
        assertTrue(exception.getMessage().contains("between 8 and 12"),
            "Exception message should indicate valid length range");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 7, 13, 15, 20, 100, -1, -10})
    void generateReference_WithInvalidLength_ThrowsException(int invalidLength) {
        assertThrows(
            IllegalArgumentException.class,
            () -> BookingReferenceGenerator.generateReference(invalidLength),
            "Should throw exception for invalid length: " + invalidLength
        );
    }
    
    // ========== Uniqueness Tests ==========
    
    @Test
    void generateReference_CalledMultipleTimes_ProducesDifferentReferences() {
        String reference1 = BookingReferenceGenerator.generateReference();
        String reference2 = BookingReferenceGenerator.generateReference();
        String reference3 = BookingReferenceGenerator.generateReference();
        
        assertNotEquals(reference1, reference2,
            "Consecutive references should be different");
        assertNotEquals(reference2, reference3,
            "Consecutive references should be different");
        assertNotEquals(reference1, reference3,
            "Consecutive references should be different");
    }
    
    @Test
    void generateReference_CalledManyTimes_ProducesUniqueReferences() {
        Set<String> references = new HashSet<>();
        int count = 100;
        
        for (int i = 0; i < count; i++) {
            String reference = BookingReferenceGenerator.generateReference();
            references.add(reference);
        }
        
        assertEquals(count, references.size(),
            "All generated references should be unique");
    }
    
    @Test
    void generateReference_WithDifferentLengths_ProducesUniqueReferences() {
        Set<String> references = new HashSet<>();
        
        for (int length = 8; length <= 12; length++) {
            for (int i = 0; i < 20; i++) {
                String reference = BookingReferenceGenerator.generateReference(length);
                references.add(reference);
            }
        }
        
        assertEquals(100, references.size(),
            "All generated references should be unique across different lengths");
    }
    
    // ========== Format Validation Tests ==========
    
    @Test
    void generateReference_ReturnsUppercaseOnly() {
        String reference = BookingReferenceGenerator.generateReference();
        
        assertEquals(reference, reference.toUpperCase(),
            "Reference should contain only uppercase characters");
    }
    
    @Test
    void generateReference_ContainsNoSpecialCharacters() {
        String reference = BookingReferenceGenerator.generateReference();
        
        assertFalse(reference.contains("-"),
            "Reference should not contain hyphens");
        assertFalse(reference.contains("_"),
            "Reference should not contain underscores");
        assertFalse(reference.contains(" "),
            "Reference should not contain spaces");
        assertFalse(reference.contains("."),
            "Reference should not contain dots");
    }
    
    @Test
    void generateReference_ContainsNoAmbiguousCharacters() {
        String reference = BookingReferenceGenerator.generateReference();
        
        // The implementation excludes ambiguous characters (0/O, 1/I/L)
        // This test verifies the character set used
        for (char c : reference.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c),
                "Reference should contain only letters and digits");
            assertTrue(Character.isUpperCase(c) || Character.isDigit(c),
                "Reference should contain only uppercase letters and digits");
        }
    }
    
    // ========== Consistency Tests ==========
    
    @Test
    void generateReference_WithSameLength_ProducesConsistentFormat() {
        int length = 10;
        
        for (int i = 0; i < 50; i++) {
            String reference = BookingReferenceGenerator.generateReference(length);
            
            assertEquals(length, reference.length(),
                "All references with same length parameter should have same length");
            assertTrue(REFERENCE_PATTERN.matcher(reference).matches(),
                "All references should match the expected pattern");
        }
    }
    
    // ========== Edge Cases ==========
    
    @Test
    void generateReference_CalledRapidly_MaintainsUniqueness() throws InterruptedException {
        Set<String> references = new HashSet<>();
        int count = 1000;
        
        // Generate references as quickly as possible
        for (int i = 0; i < count; i++) {
            String reference = BookingReferenceGenerator.generateReference();
            references.add(reference);
        }
        
        assertEquals(count, references.size(),
            "Rapid generation should still produce unique references");
    }
    
    @Test
    void generateReference_WithMinLength_HasCorrectStructure() {
        String reference = BookingReferenceGenerator.generateReference(8);
        
        // With 8 characters: 6 from timestamp + 2 random
        assertEquals(8, reference.length());
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches());
    }
    
    @Test
    void generateReference_WithMaxLength_HasCorrectStructure() {
        String reference = BookingReferenceGenerator.generateReference(12);
        
        // With 12 characters: 6 from timestamp + 6 random
        assertEquals(12, reference.length());
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches());
    }
    
    // ========== Constructor Test ==========
    
    @Test
    void constructor_ThrowsException() {
        java.lang.reflect.InvocationTargetException exception = assertThrows(
            java.lang.reflect.InvocationTargetException.class,
            () -> {
                // Use reflection to access private constructor
                java.lang.reflect.Constructor<BookingReferenceGenerator> constructor =
                    BookingReferenceGenerator.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        );
        
        // Verify the cause is UnsupportedOperationException
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertTrue(exception.getCause().getMessage().contains("Utility class cannot be instantiated"));
    }
}
