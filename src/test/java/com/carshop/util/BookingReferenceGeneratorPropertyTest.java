package com.carshop.util;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for BookingReferenceGenerator utility class.
 * **Validates: Requirements 9.5**
 * 
 * Property 7: Booking Reference Uniqueness
 * For any set of booking requests, the system SHALL generate a unique booking reference
 * for each booking such that no two bookings have the same reference code.
 */
class BookingReferenceGeneratorPropertyTest {
    
    /**
     * Pattern to validate booking reference format.
     * Must be uppercase alphanumeric characters only.
     */
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Z0-9]+$");
    
    // ========== Property 7.1: Uniqueness ==========
    
    /**
     * Property 7: Booking Reference Uniqueness
     * 
     * For any set of booking requests, the system SHALL generate a unique booking reference
     * for each booking such that no two bookings have the same reference code.
     * 
     * This test generates 100+ booking references and verifies all are unique.
     * 
     * **Validates: Requirements 9.5**
     */
    @Property
    @Label("Property 7: Booking reference uniqueness - Generate 100+ references and verify uniqueness")
    // Feature: week-1-foundation-setup, Property 7: Booking Reference Uniqueness
    void generatedReferences_AreAlwaysUnique(
            @ForAll @IntRange(min = 100, max = 500) int referenceCount
    ) {
        Set<String> references = new HashSet<>();
        
        // Generate multiple references
        for (int i = 0; i < referenceCount; i++) {
            String reference = BookingReferenceGenerator.generateReference();
            references.add(reference);
        }
        
        // Verify all references are unique
        assertEquals(referenceCount, references.size(),
            "All " + referenceCount + " generated references should be unique");
    }
    
    @Property(tries = 50)
    @Label("Rapid generation maintains uniqueness")
    void rapidGeneration_MaintainsUniqueness() {
        Set<String> references = new HashSet<>();
        int count = 200;
        
        // Generate references as quickly as possible
        for (int i = 0; i < count; i++) {
            String reference = BookingReferenceGenerator.generateReference();
            boolean added = references.add(reference);
            
            assertTrue(added,
                "Reference should be unique, but duplicate found: " + reference);
        }
        
        assertEquals(count, references.size(),
            "All rapidly generated references should be unique");
    }
    
    @Property
    @Label("References with different lengths are unique")
    void references_WithDifferentLengths_AreUnique(
            @ForAll @IntRange(min = 8, max = 12) int length1,
            @ForAll @IntRange(min = 8, max = 12) int length2
    ) {
        Set<String> references = new HashSet<>();
        
        // Generate references with first length
        for (int i = 0; i < 50; i++) {
            references.add(BookingReferenceGenerator.generateReference(length1));
        }
        
        // Generate references with second length
        for (int i = 0; i < 50; i++) {
            references.add(BookingReferenceGenerator.generateReference(length2));
        }
        
        // Even if lengths are the same, all references should be unique
        assertEquals(100, references.size(),
            "All references should be unique regardless of length parameter");
    }
    
    // ========== Property 7.2: Format Consistency ==========
    
    @Property
    @Label("All generated references match the expected format")
    void allReferences_MatchExpectedFormat(
            @ForAll @IntRange(min = 8, max = 12) int length
    ) {
        String reference = BookingReferenceGenerator.generateReference(length);
        
        // Verify length
        assertEquals(length, reference.length(),
            "Reference should have exactly " + length + " characters");
        
        // Verify format (uppercase alphanumeric)
        assertTrue(REFERENCE_PATTERN.matcher(reference).matches(),
            "Reference should contain only uppercase alphanumeric characters: " + reference);
    }
    
    @Property
    @Label("References contain only uppercase characters")
    void references_ContainOnlyUppercase() {
        String reference = BookingReferenceGenerator.generateReference();
        
        assertEquals(reference, reference.toUpperCase(),
            "Reference should be entirely uppercase: " + reference);
    }
    
    @Property
    @Label("References contain no whitespace or special characters")
    void references_ContainNoWhitespaceOrSpecialChars() {
        String reference = BookingReferenceGenerator.generateReference();
        
        assertFalse(reference.contains(" "),
            "Reference should not contain spaces");
        assertFalse(reference.contains("-"),
            "Reference should not contain hyphens");
        assertFalse(reference.contains("_"),
            "Reference should not contain underscores");
        assertFalse(reference.contains("."),
            "Reference should not contain dots");
        
        // Verify only alphanumeric
        for (char c : reference.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c),
                "Reference should contain only letters and digits, found: " + c);
        }
    }
    
    // ========== Property 7.3: Length Validation ==========
    
    @Property
    @Label("Valid length parameters produce references of correct length")
    void validLengthParameters_ProduceCorrectLength(
            @ForAll @IntRange(min = 8, max = 12) int length
    ) {
        String reference = BookingReferenceGenerator.generateReference(length);
        
        assertEquals(length, reference.length(),
            "Reference length should match the requested length");
    }
    
    @Property
    @Label("Invalid length parameters throw IllegalArgumentException")
    void invalidLengthParameters_ThrowException(
            @ForAll("invalidLengths") int invalidLength
    ) {
        assertThrows(IllegalArgumentException.class,
            () -> BookingReferenceGenerator.generateReference(invalidLength),
            "Should throw exception for invalid length: " + invalidLength);
    }
    
    // ========== Property 7.4: Collision Resistance ==========
    
    @Property(tries = 20)
    @Label("Large batches of references have no collisions")
    void largeBatches_HaveNoCollisions() {
        Set<String> references = new HashSet<>();
        int batchSize = 1000;
        
        // Generate a large batch
        for (int i = 0; i < batchSize; i++) {
            String reference = BookingReferenceGenerator.generateReference();
            references.add(reference);
        }
        
        assertEquals(batchSize, references.size(),
            "Large batch of " + batchSize + " references should have no collisions");
    }
    
    @Property
    @Label("Multiple small batches combined have no collisions")
    void multipleSmallBatches_CombinedHaveNoCollisions(
            @ForAll @IntRange(min = 5, max = 20) int batchCount
    ) {
        Set<String> allReferences = new HashSet<>();
        int batchSize = 50;
        
        // Generate multiple batches
        for (int batch = 0; batch < batchCount; batch++) {
            for (int i = 0; i < batchSize; i++) {
                String reference = BookingReferenceGenerator.generateReference();
                allReferences.add(reference);
            }
        }
        
        int expectedTotal = batchCount * batchSize;
        assertEquals(expectedTotal, allReferences.size(),
            "All references across " + batchCount + " batches should be unique");
    }
    
    // ========== Property 7.5: Temporal Uniqueness ==========
    
    @Property(tries = 30)
    @Label("References generated at different times are unique")
    void references_GeneratedAtDifferentTimes_AreUnique() throws InterruptedException {
        Set<String> references = new HashSet<>();
        
        // Generate first batch
        for (int i = 0; i < 50; i++) {
            references.add(BookingReferenceGenerator.generateReference());
        }
        
        // Small delay to ensure different timestamp
        Thread.sleep(1);
        
        // Generate second batch
        for (int i = 0; i < 50; i++) {
            references.add(BookingReferenceGenerator.generateReference());
        }
        
        assertEquals(100, references.size(),
            "References generated at different times should all be unique");
    }
    
    // ========== Property 7.6: Distribution ==========
    
    @Property
    @Label("Generated references use diverse character set")
    void generatedReferences_UseDiverseCharacterSet() {
        Set<Character> observedChars = new HashSet<>();
        
        // Generate many references to observe character distribution
        for (int i = 0; i < 200; i++) {
            String reference = BookingReferenceGenerator.generateReference();
            for (char c : reference.toCharArray()) {
                observedChars.add(c);
            }
        }
        
        // Should observe a reasonable variety of characters
        // (at least 10 different characters across 200 references)
        assertTrue(observedChars.size() >= 10,
            "Should observe diverse character set, found: " + observedChars.size() + " unique characters");
    }
    
    // ========== Property 7.7: Consistency Across Lengths ==========
    
    @Property
    @Label("References of all valid lengths maintain uniqueness")
    void references_OfAllValidLengths_MaintainUniqueness() {
        Set<String> allReferences = new HashSet<>();
        
        // Generate references for each valid length
        for (int length = 8; length <= 12; length++) {
            for (int i = 0; i < 100; i++) {
                String reference = BookingReferenceGenerator.generateReference(length);
                allReferences.add(reference);
            }
        }
        
        // Total: 5 lengths × 100 references = 500 unique references
        assertEquals(500, allReferences.size(),
            "All references across all valid lengths should be unique");
    }
    
    // ========== Arbitraries (Generators) ==========
    
    @Provide
    Arbitrary<Integer> invalidLengths() {
        return Arbitraries.oneOf(
            // Too small
            Arbitraries.integers().between(-100, 7),
            // Too large
            Arbitraries.integers().between(13, 100)
        );
    }
}
