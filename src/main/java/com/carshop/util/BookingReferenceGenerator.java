package com.carshop.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating unique booking reference codes.
 * 
 * <p>Booking references are used to track guest orders without requiring authentication.
 * Each reference is a unique alphanumeric code that combines a timestamp component,
 * a sequence counter, and a random component to ensure uniqueness even under high load.
 * 
 * <p>Reference format:
 * <ul>
 *   <li>8-12 characters in length</li>
 *   <li>Uppercase alphanumeric characters (A-Z, 0-9)</li>
 *   <li>Timestamp-based component for temporal ordering</li>
 *   <li>Sequence counter for same-millisecond uniqueness</li>
 *   <li>Random component for additional collision resistance</li>
 * </ul>
 * 
 * <p>Example references:
 * <ul>
 *   <li>7K9M2A5P</li>
 *   <li>8N4Q1B6R3T</li>
 *   <li>9P5S2C7U4V1W</li>
 * </ul>
 */
public class BookingReferenceGenerator {
    
    /**
     * Characters used for generating booking references.
     * Excludes ambiguous characters (0/O, 1/I/L) for better readability.
     */
    private static final String ALPHANUMERIC_CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    
    /**
     * Minimum length of a booking reference.
     */
    private static final int MIN_LENGTH = 8;
    
    /**
     * Maximum length of a booking reference.
     */
    private static final int MAX_LENGTH = 12;
    
    /**
     * Default length of a booking reference.
     */
    private static final int DEFAULT_LENGTH = 10;
    
    /**
     * Number of characters to use from timestamp encoding.
     */
    private static final int TIMESTAMP_CHARS = 5;
    
    /**
     * Number of characters to use from sequence encoding.
     */
    private static final int SEQUENCE_CHARS = 2;
    
    /**
     * Secure random number generator for cryptographically strong random values.
     */
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Atomic counter for generating unique sequences within the same millisecond.
     * Resets periodically to prevent overflow.
     */
    private static final AtomicLong SEQUENCE_COUNTER = new AtomicLong(0);
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private BookingReferenceGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Generates a unique booking reference code with default length (10 characters).
     * 
     * <p>The reference combines:
     * <ul>
     *   <li>5 characters from timestamp encoding (for temporal uniqueness)</li>
     *   <li>2 characters from sequence counter (for same-millisecond uniqueness)</li>
     *   <li>3 random characters (for additional collision prevention)</li>
     * </ul>
     * 
     * @return a unique booking reference code
     */
    public static String generateReference() {
        return generateReference(DEFAULT_LENGTH);
    }
    
    /**
     * Generates a unique booking reference code with specified length.
     * 
     * <p>The reference combines:
     * <ul>
     *   <li>5 characters from timestamp encoding (for temporal uniqueness)</li>
     *   <li>2 characters from sequence counter (for same-millisecond uniqueness)</li>
     *   <li>Remaining characters are random (for additional collision prevention)</li>
     * </ul>
     * 
     * @param length the desired length of the reference (must be between 8 and 12)
     * @return a unique booking reference code
     * @throws IllegalArgumentException if length is not between MIN_LENGTH and MAX_LENGTH
     */
    public static String generateReference(int length) {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Reference length must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH)
            );
        }
        
        StringBuilder reference = new StringBuilder(length);
        
        // Add timestamp component (first 5 characters)
        String timestampPart = encodeTimestamp();
        reference.append(timestampPart);
        
        // Add sequence component (next 2 characters)
        String sequencePart = encodeSequence();
        reference.append(sequencePart);
        
        // Add random component (remaining characters)
        int randomChars = length - TIMESTAMP_CHARS - SEQUENCE_CHARS;
        for (int i = 0; i < randomChars; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            reference.append(ALPHANUMERIC_CHARS.charAt(index));
        }
        
        return reference.toString();
    }
    
    /**
     * Encodes the current timestamp into a 5-character alphanumeric string.
     * Uses base-32 encoding with custom character set for better readability.
     * 
     * <p>The timestamp is encoded from the current epoch milliseconds,
     * providing temporal ordering and reducing collision probability.
     * 
     * @return a 5-character encoded timestamp
     */
    private static String encodeTimestamp() {
        long timestamp = Instant.now().toEpochMilli();
        StringBuilder encoded = new StringBuilder(TIMESTAMP_CHARS);
        
        // Encode timestamp using base-32 with custom character set
        long value = timestamp;
        for (int i = 0; i < TIMESTAMP_CHARS; i++) {
            int index = (int) (value % ALPHANUMERIC_CHARS.length());
            encoded.append(ALPHANUMERIC_CHARS.charAt(index));
            value /= ALPHANUMERIC_CHARS.length();
        }
        
        return encoded.toString();
    }
    
    /**
     * Encodes the current sequence counter into a 2-character alphanumeric string.
     * The sequence counter increments for each reference generated and provides
     * uniqueness for references generated within the same millisecond.
     * 
     * <p>The counter wraps around after reaching a large value to prevent overflow.
     * 
     * @return a 2-character encoded sequence
     */
    private static String encodeSequence() {
        // Get and increment the sequence counter
        long sequence = SEQUENCE_COUNTER.getAndIncrement();
        
        // Wrap around if counter gets too large (prevent overflow)
        if (sequence > 1000000) {
            SEQUENCE_COUNTER.set(0);
            sequence = SEQUENCE_COUNTER.getAndIncrement();
        }
        
        StringBuilder encoded = new StringBuilder(SEQUENCE_CHARS);
        
        // Encode sequence using base-32 with custom character set
        long value = sequence;
        for (int i = 0; i < SEQUENCE_CHARS; i++) {
            int index = (int) (value % ALPHANUMERIC_CHARS.length());
            encoded.append(ALPHANUMERIC_CHARS.charAt(index));
            value /= ALPHANUMERIC_CHARS.length();
        }
        
        return encoded.toString();
    }
}
