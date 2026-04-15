package com.carshop.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PhoneNumberValidator utility class.
 * Tests validation and normalization of phone numbers.
 */
class PhoneNumberValidatorTest {
    
    // ========== Validation Tests - Valid Phone Numbers ==========
    
    @ParameterizedTest
    @ValueSource(strings = {
        "1234567890",           // 10 digits (minimum)
        "123456789012345",      // 15 digits (maximum)
        "+1234567890",          // With country code
        "+12345678901",         // 11 digits with +
        "555-123-4567",         // With hyphens
        "(555) 123-4567",       // With parentheses
        "+1 (555) 123-4567",    // Full international format
        "555 123 4567",         // With spaces
        "+1-555-123-4567",      // Mixed formatting
        "(555)123-4567",        // No space after parentheses
        "+44 20 7946 0958",     // UK format
        "+81 3-1234-5678"       // Japan format
    })
    void isValid_WithValidFormats_ReturnsTrue(String phoneNumber) {
        assertTrue(PhoneNumberValidator.isValid(phoneNumber));
    }
    
    // ========== Validation Tests - Invalid Phone Numbers ==========
    
    @Test
    void isValid_WithNull_ReturnsFalse() {
        assertFalse(PhoneNumberValidator.isValid(null));
    }
    
    @Test
    void isValid_WithEmptyString_ReturnsFalse() {
        assertFalse(PhoneNumberValidator.isValid(""));
    }
    
    @Test
    void isValid_WithWhitespaceOnly_ReturnsFalse() {
        assertFalse(PhoneNumberValidator.isValid("   "));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "123456789",            // 9 digits (too short)
        "1234567890123456",     // 16 digits (too long)
        "abc1234567890",        // Contains letters
        "123-456-7890x123",     // Contains extension marker
        "123.456.7890",         // Contains dots
        "123/456/7890",         // Contains slashes
        "+1 (555) 123-4567#",   // Contains hash
        "555-123-4567 ext 123", // Contains text
        "123@4567890",          // Contains @
        "123*456*7890"          // Contains asterisk
    })
    void isValid_WithInvalidFormats_ReturnsFalse(String phoneNumber) {
        assertFalse(PhoneNumberValidator.isValid(phoneNumber));
    }
    
    // ========== Normalization Tests - Valid Phone Numbers ==========
    
    @Test
    void normalize_WithPlainDigits_ReturnsUnchanged() {
        String phoneNumber = "1234567890";
        assertEquals("1234567890", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    @Test
    void normalize_WithCountryCode_RemovesPlus() {
        String phoneNumber = "+1234567890";
        assertEquals("1234567890", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    @Test
    void normalize_WithHyphens_RemovesHyphens() {
        String phoneNumber = "555-123-4567";
        assertEquals("5551234567", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    @Test
    void normalize_WithParentheses_RemovesParentheses() {
        String phoneNumber = "(555) 123-4567";
        assertEquals("5551234567", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    @Test
    void normalize_WithSpaces_RemovesSpaces() {
        String phoneNumber = "555 123 4567";
        assertEquals("5551234567", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    @Test
    void normalize_WithMixedFormatting_RemovesAllFormatting() {
        String phoneNumber = "+1 (555) 123-4567";
        assertEquals("15551234567", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    @Test
    void normalize_WithInternationalFormat_RemovesAllFormatting() {
        String phoneNumber = "+44 20 7946 0958";
        assertEquals("442079460958", PhoneNumberValidator.normalize(phoneNumber));
    }
    
    // ========== Normalization Tests - Invalid Phone Numbers ==========
    
    @Test
    void normalize_WithNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.normalize(null)
        );
        assertTrue(exception.getMessage().contains("Invalid phone number format"));
    }
    
    @Test
    void normalize_WithEmptyString_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.normalize("")
        );
        assertTrue(exception.getMessage().contains("Invalid phone number format"));
    }
    
    @Test
    void normalize_WithTooShort_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.normalize("123456789")
        );
        assertTrue(exception.getMessage().contains("Invalid phone number format"));
    }
    
    @Test
    void normalize_WithTooLong_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.normalize("1234567890123456")
        );
        assertTrue(exception.getMessage().contains("Invalid phone number format"));
    }
    
    @Test
    void normalize_WithInvalidCharacters_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> PhoneNumberValidator.normalize("123-456-7890x123")
        );
        assertTrue(exception.getMessage().contains("Invalid phone number format"));
    }
    
    // ========== Edge Cases ==========
    
    @Test
    void isValid_WithMinimumLength_ReturnsTrue() {
        String phoneNumber = "1234567890"; // Exactly 10 digits
        assertTrue(PhoneNumberValidator.isValid(phoneNumber));
    }
    
    @Test
    void isValid_WithMaximumLength_ReturnsTrue() {
        String phoneNumber = "123456789012345"; // Exactly 15 digits
        assertTrue(PhoneNumberValidator.isValid(phoneNumber));
    }
    
    @Test
    void isValid_WithOneLessThanMinimum_ReturnsFalse() {
        String phoneNumber = "123456789"; // 9 digits
        assertFalse(PhoneNumberValidator.isValid(phoneNumber));
    }
    
    @Test
    void isValid_WithOneMoreThanMaximum_ReturnsFalse() {
        String phoneNumber = "1234567890123456"; // 16 digits
        assertFalse(PhoneNumberValidator.isValid(phoneNumber));
    }
    
    // ========== Constructor Test ==========
    
    @Test
    void constructor_ThrowsException() {
        java.lang.reflect.InvocationTargetException exception = assertThrows(
            java.lang.reflect.InvocationTargetException.class, 
            () -> {
                // Use reflection to access private constructor
                java.lang.reflect.Constructor<PhoneNumberValidator> constructor = 
                    PhoneNumberValidator.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }
        );
        
        // Verify the cause is UnsupportedOperationException
        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertTrue(exception.getCause().getMessage().contains("Utility class cannot be instantiated"));
    }
}
