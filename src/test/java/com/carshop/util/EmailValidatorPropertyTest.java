package com.carshop.util;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for EmailValidator utility class.
 * **Validates: Requirements 9.3**
 * 
 * Property 5: Email Format Validation
 * For any string input representing an email address:
 * - IF the input matches standard email format (local@domain), THEN validation SHALL succeed
 * - IF the input does not match email format, THEN validation SHALL fail with appropriate error message
 */
class EmailValidatorPropertyTest {
    
    // ========== Property 5.1: Valid Email Addresses ==========
    
    @Property
    @Label("Valid email addresses with correct format should pass validation")
    void validEmails_WithCorrectFormat_PassValidation(
            @ForAll("validEmails") String email
    ) {
        assertTrue(EmailValidator.isValid(email),
            "Email should be valid: " + email);
    }
    
    @Property
    @Label("Valid emails with various local part patterns should pass validation")
    void validEmails_WithVariousLocalParts_PassValidation(
            @ForAll("localParts") String localPart,
            @ForAll("domains") String domain
    ) {
        String email = localPart + "@" + domain;
        assertTrue(EmailValidator.isValid(email),
            "Email should be valid: " + email);
    }
    
    @Property
    @Label("Valid emails with various domain patterns should pass validation")
    void validEmails_WithVariousDomains_PassValidation(
            @ForAll("simpleLocalParts") String localPart,
            @ForAll("complexDomains") String domain
    ) {
        String email = localPart + "@" + domain;
        assertTrue(EmailValidator.isValid(email),
            "Email should be valid: " + email);
    }
    
    @Property
    @Label("Valid emails with leading/trailing whitespace should pass after trimming")
    void validEmails_WithWhitespace_PassValidationAfterTrimming(
            @ForAll("validEmails") String email,
            @ForAll("whitespace") String whitespace
    ) {
        String emailWithWhitespace = whitespace + email + whitespace;
        assertTrue(EmailValidator.isValid(emailWithWhitespace),
            "Email with whitespace should be valid after trimming: " + emailWithWhitespace);
    }
    
    // ========== Property 5.2: Invalid Email Addresses ==========
    
    @Property
    @Label("Email addresses without @ symbol should fail validation")
    void emails_WithoutAtSymbol_FailValidation(
            @ForAll("stringsWithoutAt") String email
    ) {
        assertFalse(EmailValidator.isValid(email),
            "Email without @ should be invalid: " + email);
    }
    
    @Property
    @Label("Email addresses with multiple @ symbols should fail validation")
    void emails_WithMultipleAtSymbols_FailValidation(
            @ForAll("emailsWithMultipleAt") String email
    ) {
        assertFalse(EmailValidator.isValid(email),
            "Email with multiple @ should be invalid: " + email);
    }
    
    @Property
    @Label("Email addresses with invalid characters should fail validation")
    void emails_WithInvalidCharacters_FailValidation(
            @ForAll("emailsWithInvalidChars") String email
    ) {
        assertFalse(EmailValidator.isValid(email),
            "Email with invalid characters should be invalid: " + email);
    }
    
    @Property
    @Label("Email addresses with missing domain should fail validation")
    void emails_WithMissingDomain_FailValidation(
            @ForAll("localParts") String localPart
    ) {
        String email = localPart + "@";
        assertFalse(EmailValidator.isValid(email),
            "Email with missing domain should be invalid: " + email);
    }
    
    @Property
    @Label("Email addresses with missing local part should fail validation")
    void emails_WithMissingLocalPart_FailValidation(
            @ForAll("domains") String domain
    ) {
        String email = "@" + domain;
        assertFalse(EmailValidator.isValid(email),
            "Email with missing local part should be invalid: " + email);
    }
    
    @Property
    @Label("Email addresses with invalid domain format should fail validation")
    void emails_WithInvalidDomain_FailValidation(
            @ForAll("simpleLocalParts") String localPart,
            @ForAll("invalidDomains") String domain
    ) {
        String email = localPart + "@" + domain;
        assertFalse(EmailValidator.isValid(email),
            "Email with invalid domain should be invalid: " + email);
    }
    
    @Property
    @Label("Empty or null-like strings should fail validation")
    void emptyOrNull_FailValidation(
            @ForAll("emptyOrNullLike") String email
    ) {
        assertFalse(EmailValidator.isValid(email),
            "Empty or null-like email should be invalid: " + email);
    }
    
    // ========== Property 5.3: Edge Cases ==========
    
    @Property
    @Label("Email validation is case-insensitive for domain")
    void emailValidation_IsCaseInsensitive(
            @ForAll("validEmails") String email
    ) {
        String upperCase = email.toUpperCase();
        String lowerCase = email.toLowerCase();
        
        boolean originalValid = EmailValidator.isValid(email);
        boolean upperValid = EmailValidator.isValid(upperCase);
        boolean lowerValid = EmailValidator.isValid(lowerCase);
        
        assertEquals(originalValid, upperValid,
            "Email validation should handle uppercase: " + email);
        assertEquals(originalValid, lowerValid,
            "Email validation should handle lowercase: " + email);
    }
    
    // ========== Arbitraries (Generators) ==========
    
    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.oneOf(
            simpleEmails(),
            emailsWithDots(),
            emailsWithHyphens(),
            emailsWithUnderscores(),
            emailsWithPlus(),
            emailsWithSubdomains()
        );
    }
    
    @Provide
    Arbitrary<String> simpleEmails() {
        return Combinators.combine(
            simpleLocalParts(),
            simpleDomains()
        ).as((local, domain) -> local + "@" + domain);
    }
    
    @Provide
    Arbitrary<String> emailsWithDots() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10),
            simpleDomains()
        ).as((part1, part2, domain) -> part1 + "." + part2 + "@" + domain);
    }
    
    @Provide
    Arbitrary<String> emailsWithHyphens() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10),
            simpleDomains()
        ).as((local, domain) -> local + "-test@" + domain);
    }
    
    @Provide
    Arbitrary<String> emailsWithUnderscores() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10),
            simpleDomains()
        ).as((local, domain) -> local + "_user@" + domain);
    }
    
    @Provide
    Arbitrary<String> emailsWithPlus() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(5),
            simpleDomains()
        ).as((local, tag, domain) -> local + "+" + tag + "@" + domain);
    }
    
    @Provide
    Arbitrary<String> emailsWithSubdomains() {
        return Combinators.combine(
            simpleLocalParts(),
            complexDomains()
        ).as((local, domain) -> local + "@" + domain);
    }
    
    @Provide
    Arbitrary<String> localParts() {
        return Arbitraries.oneOf(
            simpleLocalParts(),
            Arbitraries.strings().alpha().numeric().ofMinLength(3).ofMaxLength(20)
                .map(s -> s + "." + s.substring(0, Math.min(5, s.length()))),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15)
                .map(s -> s + "_user"),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15)
                .map(s -> s + "+tag")
        );
    }
    
    @Provide
    Arbitrary<String> simpleLocalParts() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .ofMinLength(3)
            .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> domains() {
        return Arbitraries.oneOf(
            simpleDomains(),
            complexDomains()
        );
    }
    
    @Provide
    Arbitrary<String> simpleDomains() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
            Arbitraries.of("com", "org", "net", "edu", "gov", "io", "co")
        ).as((name, tld) -> name.toLowerCase() + "." + tld);
    }
    
    @Provide
    Arbitrary<String> complexDomains() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.of("com", "org", "net", "co.uk", "com.au")
        ).as((subdomain, domain, tld) -> 
            subdomain.toLowerCase() + "." + domain.toLowerCase() + "." + tld);
    }
    
    @Provide
    Arbitrary<String> invalidDomains() {
        return Arbitraries.oneOf(
            // Domain without TLD
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(15),
            // Domain with single character TLD
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10)
                .map(s -> s + ".x"),
            // Domain with special characters
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10)
                .map(s -> s + "!.com")
        );
    }
    
    @Provide
    Arbitrary<String> stringsWithoutAt() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .withChars('.', '-', '_')
            .ofMinLength(5)
            .ofMaxLength(30)
            .filter(s -> !s.contains("@"));
    }
    
    @Provide
    Arbitrary<String> emailsWithMultipleAt() {
        return Combinators.combine(
            simpleLocalParts(),
            simpleDomains()
        ).as((local, domain) -> local + "@@" + domain);
    }
    
    @Provide
    Arbitrary<String> emailsWithInvalidChars() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(10),
            Arbitraries.of(' ', '!', '#', '$', '%', '^', '(', ')', '[', ']'),
            simpleDomains()
        ).as((local, invalidChar, domain) -> local + invalidChar + "@" + domain);
    }
    
    @Provide
    Arbitrary<String> emptyOrNullLike() {
        return Arbitraries.of("", "   ", "\t", "\n");
    }
    
    @Provide
    Arbitrary<String> whitespace() {
        return Arbitraries.of(" ", "  ", "\t", " \t ");
    }
}
