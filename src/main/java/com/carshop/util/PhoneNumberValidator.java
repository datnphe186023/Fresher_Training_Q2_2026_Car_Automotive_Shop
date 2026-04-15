package com.carshop.util;

import java.util.regex.Pattern;

/**
 * Utility class for phone number validation and normalization.
 * Supports international phone number formats with various formatting characters.
 * 
 * <p>Valid phone numbers must:
 * <ul>
 *   <li>Contain only digits, spaces, hyphens, parentheses, and plus sign</li>
 *   <li>Have 10-15 digits after removing formatting characters</li>
 * </ul>
 * 
 * <p>Examples of valid formats:
 * <ul>
 *   <li>+1234567890</li>
 *   <li>+1 (555) 123-4567</li>
 *   <li>555-123-4567</li>
 *   <li>(555) 123 4567</li>
 * </ul>
 */
public class PhoneNumberValidator {
    
    /**
     * Pattern to validate phone number format.
     * Allows: digits, spaces, hyphens, parentheses, and plus sign.
     */
    private static final Pattern VALID_CHARACTERS_PATTERN = Pattern.compile("^[+\\d\\s\\-()]+$");
    
    /**
     * Minimum number of digits required in a valid phone number.
     */
    private static final int MIN_DIGITS = 10;
    
    /**
     * Maximum number of digits allowed in a valid phone number.
     */
    private static final int MAX_DIGITS = 15;
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private PhoneNumberValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates a phone number according to the following rules:
     * <ul>
     *   <li>Must contain only valid characters (digits, spaces, hyphens, parentheses, plus sign)</li>
     *   <li>Must have 10-15 digits after removing formatting characters</li>
     * </ul>
     * 
     * @param phoneNumber the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Check if contains only valid characters
        if (!VALID_CHARACTERS_PATTERN.matcher(phoneNumber).matches()) {
            return false;
        }
        
        // Extract digits and check length
        String digitsOnly = extractDigits(phoneNumber);
        int digitCount = digitsOnly.length();
        
        return digitCount >= MIN_DIGITS && digitCount <= MAX_DIGITS;
    }
    
    /**
     * Normalizes a phone number by removing all formatting characters.
     * Returns a string containing only digits.
     * 
     * <p>Examples:
     * <ul>
     *   <li>"+1 (555) 123-4567" → "15551234567"</li>
     *   <li>"555-123-4567" → "5551234567"</li>
     *   <li>(555) 123 4567" → "5551234567"</li>
     * </ul>
     * 
     * @param phoneNumber the phone number to normalize
     * @return the normalized phone number containing only digits
     * @throws IllegalArgumentException if the phone number is invalid
     */
    public static String normalize(String phoneNumber) {
        if (!isValid(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
        }
        
        return extractDigits(phoneNumber);
    }
    
    /**
     * Extracts only the digits from a phone number string.
     * 
     * @param phoneNumber the phone number string
     * @return a string containing only the digits
     */
    private static String extractDigits(String phoneNumber) {
        return phoneNumber.replaceAll("[^\\d]", "");
    }
}
