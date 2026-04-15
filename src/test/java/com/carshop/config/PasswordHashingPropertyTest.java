package com.carshop.config;

import net.jqwik.api.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for password hashing consistency.
 * Validates universal properties across all password inputs.
 * 
 * Feature: week-1-foundation-setup, Property 2: Password Hashing Consistency
 */
class PasswordHashingPropertyTest {
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    
    /**
     * Property 2: Password Hashing Consistency
     * 
     * For any valid password string provided during user registration, the stored 
     * password in the database SHALL be a valid BCrypt hash and SHALL successfully 
     * verify against the original password.
     * 
     * **Validates: Requirements 5.2**
     */
    @Property(tries = 100)
    // Feature: week-1-foundation-setup, Property 2: Password Hashing Consistency
    void passwordHashingIsConsistentAndReversible(
            @ForAll("passwords") String password) {
        
        // When - hash the password
        String hashedPassword = passwordEncoder.encode(password);
        
        // Then - hash should not be null or empty
        assertNotNull(hashedPassword, 
                "Hashed password should not be null");
        assertFalse(hashedPassword.isEmpty(), 
                "Hashed password should not be empty");
        
        // And - hash should be a valid BCrypt hash (starts with $2a$10$ for strength 10)
        assertTrue(hashedPassword.startsWith("$2a$10$"), 
                "Hashed password should be a valid BCrypt hash with strength 10");
        
        // And - hash should not equal the original password
        assertNotEquals(password, hashedPassword, 
                "Hashed password should not equal original password");
        
        // And - verification should succeed with original password
        assertTrue(passwordEncoder.matches(password, hashedPassword), 
                "Password verification should succeed with original password");
        
        // And - verification should fail with a completely different password
        // Note: We use a fixed different password to avoid BCrypt's 72-byte limit issue
        String differentPassword = "CompletelyDifferentPassword123!";
        if (!password.equals(differentPassword)) {
            assertFalse(passwordEncoder.matches(differentPassword, hashedPassword), 
                    "Password verification should fail with different password");
        }
    }
    
    /**
     * Property: Different passwords produce different hashes
     * 
     * For any two different passwords, the BCrypt hashes should be different.
     */
    @Property(tries = 100)
    void differentPasswordsProduceDifferentHashes(
            @ForAll("passwords") String password1,
            @ForAll("passwords") String password2) {
        
        // Given - two different passwords
        Assume.that(!password1.equals(password2));
        
        // When - hash both passwords
        String hash1 = passwordEncoder.encode(password1);
        String hash2 = passwordEncoder.encode(password2);
        
        // Then - hashes should be different
        assertNotEquals(hash1, hash2, 
                "Different passwords should produce different hashes");
        
        // And - each hash should only match its original password
        assertTrue(passwordEncoder.matches(password1, hash1), 
                "First hash should match first password");
        assertTrue(passwordEncoder.matches(password2, hash2), 
                "Second hash should match second password");
        assertFalse(passwordEncoder.matches(password1, hash2), 
                "First password should not match second hash");
        assertFalse(passwordEncoder.matches(password2, hash1), 
                "Second password should not match first hash");
    }
    
    /**
     * Property: Same password produces different hashes due to salt
     * 
     * For any password, hashing it multiple times should produce different hashes
     * due to BCrypt's random salt, but all hashes should verify against the original.
     */
    @Property(tries = 100)
    void samePasswordProducesDifferentHashesDueToSalt(
            @ForAll("passwords") String password) {
        
        // When - hash the same password twice
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);
        
        // Then - hashes should be different (due to random salt)
        assertNotEquals(hash1, hash2, 
                "Same password should produce different hashes due to salt");
        
        // And - both hashes should verify against the original password
        assertTrue(passwordEncoder.matches(password, hash1), 
                "First hash should match original password");
        assertTrue(passwordEncoder.matches(password, hash2), 
                "Second hash should match original password");
    }
    
    /**
     * Property: Hash length consistency
     * 
     * For any password, the BCrypt hash should have a consistent length (60 characters).
     */
    @Property(tries = 100)
    void bcryptHashHasConsistentLength(
            @ForAll("passwords") String password) {
        
        // When - hash the password
        String hashedPassword = passwordEncoder.encode(password);
        
        // Then - hash should be exactly 60 characters (BCrypt standard)
        assertEquals(60, hashedPassword.length(), 
                "BCrypt hash should be exactly 60 characters");
    }
    
    /**
     * Property: Password hashing with special characters
     * 
     * For any password containing special characters, hashing and verification
     * should work correctly.
     */
    @Property(tries = 100)
    void passwordHashingWorksWithSpecialCharacters(
            @ForAll("passwordsWithSpecialChars") String password) {
        
        // When - hash the password with special characters
        String hashedPassword = passwordEncoder.encode(password);
        
        // Then - verification should succeed
        assertTrue(passwordEncoder.matches(password, hashedPassword), 
                "Password with special characters should verify correctly");
    }
    
    /**
     * Property: Password hashing with unicode characters
     * 
     * For any password containing unicode characters, hashing and verification
     * should work correctly.
     */
    @Property(tries = 100)
    void passwordHashingWorksWithUnicodeCharacters(
            @ForAll("passwordsWithUnicode") String password) {
        
        // When - hash the password with unicode characters
        String hashedPassword = passwordEncoder.encode(password);
        
        // Then - verification should succeed
        assertTrue(passwordEncoder.matches(password, hashedPassword), 
                "Password with unicode characters should verify correctly");
    }
    
    // ========== Arbitraries ==========
    
    /**
     * Generate random passwords with varying lengths and character compositions.
     * Minimum 8 characters as per requirement 5.8.
     */
    @Provide
    Arbitrary<String> passwords() {
        return Arbitraries.strings()
                .withCharRange('!', '~')  // All printable ASCII characters
                .ofMinLength(8)
                .ofMaxLength(100);
    }
    
    /**
     * Generate passwords with special characters.
     */
    @Provide
    Arbitrary<String> passwordsWithSpecialChars() {
        return Arbitraries.strings()
                .withChars("!@#$%^&*()_+-=[]{}|;:',.<>?/~`")
                .alpha()
                .numeric()
                .ofMinLength(8)
                .ofMaxLength(50);
    }
    
    /**
     * Generate passwords with unicode characters.
     */
    @Provide
    Arbitrary<String> passwordsWithUnicode() {
        return Arbitraries.strings()
                .withChars("αβγδεζηθικλμνξοπρστυφχψω")  // Greek letters
                .withChars("你好世界")  // Chinese characters
                .withChars("こんにちは")  // Japanese characters
                .alpha()
                .numeric()
                .ofMinLength(8)
                .ofMaxLength(50);
    }
}
