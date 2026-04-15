package com.carshop.util;

import java.util.regex.Pattern;

/**
 * Utility class for email address validation.
 * Validates email addresses according to standard email format rules.
 * 
 * <p>Valid email addresses must:
 * <ul>
 *   <li>Have a local part (before @) containing alphanumeric characters, dots, hyphens, underscores</li>
 *   <li>Have exactly one @ symbol</li>
 *   <li>Have a domain part (after @) with at least one dot</li>
 *   <li>Have a valid top-level domain (at least 2 characters)</li>
 * </ul>
 * 
 * <p>Examples of valid formats:
 * <ul>
 *   <li>user@example.com</li>
 *   <li>john.doe@company.co.uk</li>
 *   <li>test_user+tag@domain.org</li>
 * </ul>
 */
public class EmailValidator {
    
    /**
     * Pattern to validate email format.
     * Follows RFC 5322 simplified pattern for practical email validation.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    );
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private EmailValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates an email address according to standard email format rules.
     * 
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Trim whitespace
        email = email.trim();
        
        // Check basic structure: must have exactly one @
        long atCount = email.chars().filter(ch -> ch == '@').count();
        if (atCount != 1) {
            return false;
        }
        
        // Check against pattern
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
