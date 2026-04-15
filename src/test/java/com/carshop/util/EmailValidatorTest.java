package com.carshop.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmailValidator utility class.
 * Tests specific examples and edge cases for email validation.
 */
class EmailValidatorTest {
    
    // ========== Valid Email Tests ==========
    
    @ParameterizedTest
    @ValueSource(strings = {
        "user@example.com",
        "john.doe@company.co.uk",
        "test_user@domain.org",
        "user+tag@example.com",
        "first.last@subdomain.example.com",
        "user123@test123.com",
        "a@b.co",
        "test-user@example-domain.com"
    })
    void isValid_WithValidEmails_ReturnsTrue(String email) {
        assertTrue(EmailValidator.isValid(email),
            "Email should be valid: " + email);
    }
    
    @Test
    void isValid_WithValidEmailAndWhitespace_ReturnsTrue() {
        assertTrue(EmailValidator.isValid("  user@example.com  "));
        assertTrue(EmailValidator.isValid("\tuser@example.com\t"));
        assertTrue(EmailValidator.isValid(" user@example.com"));
        assertTrue(EmailValidator.isValid("user@example.com "));
    }
    
    // ========== Invalid Email Tests ==========
    
    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "invalid@",
        "@invalid.com",
        "invalid@@example.com",
        "invalid @example.com",
        "invalid@example",
        "invalid@.com",
        "invalid@example.",
        "invalid@example.c",
        "user name@example.com",
        "user@exam ple.com",
        "user!name@example.com",
        "user#name@example.com",
        "user$name@example.com",
        "user%name@example.com",
        "user^name@example.com",
        "user(name)@example.com",
        "user[name]@example.com"
    })
    void isValid_WithInvalidEmails_ReturnsFalse(String email) {
        assertFalse(EmailValidator.isValid(email),
            "Email should be invalid: " + email);
    }
    
    @Test
    void isValid_WithNullOrEmpty_ReturnsFalse() {
        assertFalse(EmailValidator.isValid(null));
        assertFalse(EmailValidator.isValid(""));
        assertFalse(EmailValidator.isValid("   "));
        assertFalse(EmailValidator.isValid("\t"));
        assertFalse(EmailValidator.isValid("\n"));
    }
    
    // ========== Edge Cases ==========
    
    @Test
    void isValid_WithMultipleAtSymbols_ReturnsFalse() {
        assertFalse(EmailValidator.isValid("user@@example.com"));
        assertFalse(EmailValidator.isValid("user@domain@example.com"));
        assertFalse(EmailValidator.isValid("@user@example.com"));
    }
    
    @Test
    void isValid_WithMissingParts_ReturnsFalse() {
        assertFalse(EmailValidator.isValid("@example.com"));
        assertFalse(EmailValidator.isValid("user@"));
        assertFalse(EmailValidator.isValid("@"));
    }
    
    @Test
    void isValid_WithCaseSensitivity_HandlesCorrectly() {
        // Email validation should handle case variations
        assertTrue(EmailValidator.isValid("User@Example.COM"));
        assertTrue(EmailValidator.isValid("USER@EXAMPLE.COM"));
        assertTrue(EmailValidator.isValid("user@example.com"));
    }
    
    @Test
    void isValid_WithSpecialCharactersInLocalPart_ValidatesCorrectly() {
        // Valid special characters
        assertTrue(EmailValidator.isValid("user.name@example.com"));
        assertTrue(EmailValidator.isValid("user_name@example.com"));
        assertTrue(EmailValidator.isValid("user-name@example.com"));
        assertTrue(EmailValidator.isValid("user+tag@example.com"));
        
        // Invalid special characters
        assertFalse(EmailValidator.isValid("user name@example.com"));
        assertFalse(EmailValidator.isValid("user!name@example.com"));
        assertFalse(EmailValidator.isValid("user#name@example.com"));
    }
    
    @Test
    void isValid_WithSubdomains_ReturnsTrue() {
        assertTrue(EmailValidator.isValid("user@mail.example.com"));
        assertTrue(EmailValidator.isValid("user@subdomain.mail.example.com"));
        assertTrue(EmailValidator.isValid("user@a.b.c.example.com"));
    }
    
    @Test
    void isValid_WithInternationalTLDs_ReturnsTrue() {
        assertTrue(EmailValidator.isValid("user@example.co.uk"));
        assertTrue(EmailValidator.isValid("user@example.com.au"));
        assertTrue(EmailValidator.isValid("user@example.co.jp"));
    }
    
    // ========== Constructor Test ==========
    
    @Test
    void constructor_ThrowsException() throws Exception {
        // Use reflection to access private constructor
        var constructor = EmailValidator.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class, 
            constructor::newInstance,
            "Utility class constructor should throw exception");
        
        // Verify the cause is UnsupportedOperationException
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }
}
