package com.carshop.util;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for PhoneNumberValidator utility class.
 * **Validates: Requirements 10.1, 10.2, 10.3, 10.4**
 * 
 * Property 4: Phone Number Validation and Normalization
 * For any string input representing a phone number:
 * - IF the input contains only valid characters (digits, spaces, hyphens, parentheses, plus sign)
 *   AND has 10-15 digits after removing formatting characters, THEN validation SHALL succeed
 * - IF validation succeeds, THEN normalization SHALL remove all formatting characters
 *   and produce a digit-only string
 * - IF the input contains invalid characters OR has fewer than 10 or more than 15 digits,
 *   THEN validation SHALL fail
 */
class PhoneNumberValidatorPropertyTest {
    
    // ========== Property 4.1: Valid Phone Numbers ==========
    
    @Property
    @Label("Valid phone numbers with correct digit count should pass validation")
    void validPhoneNumbers_WithCorrectDigitCount_PassValidation(
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        assertTrue(PhoneNumberValidator.isValid(phoneNumber),
            "Phone number should be valid: " + phoneNumber);
    }
    
    @Property
    @Label("Normalization of valid phone numbers produces digit-only string")
    void normalization_OfValidPhoneNumbers_ProducesDigitsOnly(
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        String normalized = PhoneNumberValidator.normalize(phoneNumber);
        
        // Verify normalized string contains only digits
        assertTrue(normalized.matches("^\\d+$"),
            "Normalized phone number should contain only digits: " + normalized);
        
        // Verify digit count is within valid range
        int digitCount = normalized.length();
        assertTrue(digitCount >= 10 && digitCount <= 15,
            "Normalized phone number should have 10-15 digits, got: " + digitCount);
    }
    
    @Property
    @Label("Normalization removes all formatting characters")
    void normalization_RemovesAllFormattingCharacters(
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        String normalized = PhoneNumberValidator.normalize(phoneNumber);
        
        // Extract digits from original
        String expectedDigits = phoneNumber.replaceAll("[^\\d]", "");
        
        // Verify normalization produces same result
        assertEquals(expectedDigits, normalized,
            "Normalization should remove all non-digit characters");
    }
    
    @Property
    @Label("Normalization is idempotent - normalizing twice produces same result")
    void normalization_IsIdempotent(
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        String normalized1 = PhoneNumberValidator.normalize(phoneNumber);
        String normalized2 = PhoneNumberValidator.normalize(normalized1);
        
        assertEquals(normalized1, normalized2,
            "Normalizing an already normalized phone number should produce the same result");
    }
    
    // ========== Property 4.2: Invalid Phone Numbers ==========
    
    @Property
    @Label("Phone numbers with invalid characters should fail validation")
    void phoneNumbers_WithInvalidCharacters_FailValidation(
            @ForAll("phoneNumbersWithInvalidChars") String phoneNumber
    ) {
        assertFalse(PhoneNumberValidator.isValid(phoneNumber),
            "Phone number with invalid characters should be invalid: " + phoneNumber);
    }
    
    @Property
    @Label("Phone numbers with too few digits should fail validation")
    void phoneNumbers_WithTooFewDigits_FailValidation(
            @ForAll @IntRange(min = 1, max = 9) int digitCount
    ) {
        String phoneNumber = "1".repeat(digitCount);
        assertFalse(PhoneNumberValidator.isValid(phoneNumber),
            "Phone number with " + digitCount + " digits should be invalid");
    }
    
    @Property
    @Label("Phone numbers with too many digits should fail validation")
    void phoneNumbers_WithTooManyDigits_FailValidation(
            @ForAll @IntRange(min = 16, max = 25) int digitCount
    ) {
        String phoneNumber = "1".repeat(digitCount);
        assertFalse(PhoneNumberValidator.isValid(phoneNumber),
            "Phone number with " + digitCount + " digits should be invalid");
    }
    
    @Property
    @Label("Normalizing invalid phone numbers throws exception")
    void normalization_OfInvalidPhoneNumbers_ThrowsException(
            @ForAll("invalidPhoneNumbers") String phoneNumber
    ) {
        assertThrows(IllegalArgumentException.class,
            () -> PhoneNumberValidator.normalize(phoneNumber),
            "Normalizing invalid phone number should throw exception: " + phoneNumber);
    }
    
    // ========== Property 4.3: Formatting Variations ==========
    
    @Property
    @Label("Different formatting of same digits produces same normalized result")
    void differentFormatting_OfSameDigits_ProducesSameNormalizedResult(
            @ForAll("digitsOnly") String digits,
            @ForAll("formattingStyle1") String formatted1,
            @ForAll("formattingStyle2") String formatted2
    ) {
        String phoneNumber1 = applyFormatting(digits, formatted1);
        String phoneNumber2 = applyFormatting(digits, formatted2);
        
        if (PhoneNumberValidator.isValid(phoneNumber1) && PhoneNumberValidator.isValid(phoneNumber2)) {
            String normalized1 = PhoneNumberValidator.normalize(phoneNumber1);
            String normalized2 = PhoneNumberValidator.normalize(phoneNumber2);
            
            assertEquals(normalized1, normalized2,
                "Different formatting of same digits should normalize to same result");
        }
    }
    
    // ========== Arbitraries (Generators) ==========
    
    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Arbitraries.oneOf(
            plainDigits(),
            digitsWithHyphens(),
            digitsWithSpaces(),
            digitsWithParentheses(),
            digitsWithCountryCode(),
            digitsWithMixedFormatting()
        );
    }
    
    @Provide
    Arbitrary<String> plainDigits() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> 
                Arbitraries.strings()
                    .numeric()
                    .ofLength(length)
            );
    }
    
    @Provide
    Arbitrary<String> digitsWithHyphens() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> {
                Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(length);
                return digits.map(d -> {
                    // Insert hyphens at random positions
                    if (d.length() >= 10) {
                        return d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6);
                    }
                    return d;
                });
            });
    }
    
    @Provide
    Arbitrary<String> digitsWithSpaces() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> {
                Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(length);
                return digits.map(d -> {
                    // Insert spaces at random positions
                    if (d.length() >= 10) {
                        return d.substring(0, 3) + " " + d.substring(3, 6) + " " + d.substring(6);
                    }
                    return d;
                });
            });
    }
    
    @Provide
    Arbitrary<String> digitsWithParentheses() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> {
                Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(length);
                return digits.map(d -> {
                    // Add parentheses around area code
                    if (d.length() >= 10) {
                        return "(" + d.substring(0, 3) + ") " + d.substring(3, 6) + "-" + d.substring(6);
                    }
                    return d;
                });
            });
    }
    
    @Provide
    Arbitrary<String> digitsWithCountryCode() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> {
                Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(length);
                return digits.map(d -> "+" + d);
            });
    }
    
    @Provide
    Arbitrary<String> digitsWithMixedFormatting() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> {
                Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(length);
                return digits.map(d -> {
                    // Mix of all formatting styles
                    if (d.length() >= 11) {
                        return "+" + d.substring(0, 1) + " (" + d.substring(1, 4) + ") " + 
                               d.substring(4, 7) + "-" + d.substring(7);
                    }
                    return "+" + d;
                });
            });
    }
    
    @Provide
    Arbitrary<String> phoneNumbersWithInvalidChars() {
        return Arbitraries.integers()
            .between(10, 15)
            .flatMap(length -> {
                Arbitrary<String> digits = Arbitraries.strings().numeric().ofLength(length);
                Arbitrary<Character> invalidChar = Arbitraries.of('a', 'x', '.', '/', '@', '*', '#', 'e');
                
                return Combinators.combine(digits, invalidChar)
                    .as((d, c) -> {
                        // Insert invalid character at random position
                        int pos = Math.min(5, d.length() - 1);
                        return d.substring(0, pos) + c + d.substring(pos);
                    });
            });
    }
    
    @Provide
    Arbitrary<String> invalidPhoneNumbers() {
        return Arbitraries.oneOf(
            // Too short
            Arbitraries.strings().numeric().ofMinLength(1).ofMaxLength(9),
            // Too long
            Arbitraries.strings().numeric().ofMinLength(16).ofMaxLength(25),
            // With invalid characters
            phoneNumbersWithInvalidChars(),
            // Empty or null-like
            Arbitraries.of("", "   ")
        );
    }
    
    @Provide
    Arbitrary<String> digitsOnly() {
        return Arbitraries.strings()
            .numeric()
            .ofMinLength(10)
            .ofMaxLength(15);
    }
    
    @Provide
    Arbitrary<String> formattingStyle1() {
        return Arbitraries.of("plain", "hyphens", "spaces");
    }
    
    @Provide
    Arbitrary<String> formattingStyle2() {
        return Arbitraries.of("plain", "parentheses", "countryCode");
    }
    
    // ========== Helper Methods ==========
    
    private String applyFormatting(String digits, String style) {
        if (digits.length() < 10) {
            return digits;
        }
        
        switch (style) {
            case "plain":
                return digits;
            case "hyphens":
                return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
            case "spaces":
                return digits.substring(0, 3) + " " + digits.substring(3, 6) + " " + digits.substring(6);
            case "parentheses":
                return "(" + digits.substring(0, 3) + ") " + digits.substring(3, 6) + "-" + digits.substring(6);
            case "countryCode":
                return "+" + digits;
            default:
                return digits;
        }
    }
}
