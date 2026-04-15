package com.carshop.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.jqwik.api.*;
import net.jqwik.api.arbitraries.StringArbitrary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for entity serialization round-trip.
 * **Validates: Requirements 3.10**
 */
class EntitySerializationPropertyTest {
    
    private final ObjectMapper objectMapper;
    
    public EntitySerializationPropertyTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Property 1: Entity serialization round-trip
     * For any entity instance, serializing to JSON and deserializing back
     * should produce an equivalent object.
     */
    
    @Property
    @Label("User entity serialization round-trip")
    void userSerializationRoundTrip(
            @ForAll("users") User user
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(user);
        
        // Deserialize back to entity
        User deserialized = objectMapper.readValue(json, User.class);
        
        // Verify field-by-field equivalence
        assertEquals(user.getId(), deserialized.getId());
        assertEquals(user.getUsername(), deserialized.getUsername());
        assertEquals(user.getEmail(), deserialized.getEmail());
        assertEquals(user.getPassword(), deserialized.getPassword());
        assertEquals(user.getRole(), deserialized.getRole());
        assertEquals(user.getCreatedAt(), deserialized.getCreatedAt());
    }
    
    @Property
    @Label("Customer entity serialization round-trip")
    void customerSerializationRoundTrip(
            @ForAll("customers") Customer customer
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(customer);
        
        // Deserialize back to entity
        Customer deserialized = objectMapper.readValue(json, Customer.class);
        
        // Verify field-by-field equivalence
        assertEquals(customer.getId(), deserialized.getId());
        assertEquals(customer.getPhoneNumber(), deserialized.getPhoneNumber());
        assertEquals(customer.getEmail(), deserialized.getEmail());
        assertEquals(customer.getName(), deserialized.getName());
        assertEquals(customer.getCreatedAt(), deserialized.getCreatedAt());
    }
    
    @Property
    @Label("ServiceCategory entity serialization round-trip")
    void serviceCategorySerializationRoundTrip(
            @ForAll("serviceCategories") ServiceCategory category
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(category);
        
        // Deserialize back to entity
        ServiceCategory deserialized = objectMapper.readValue(json, ServiceCategory.class);
        
        // Verify field-by-field equivalence
        assertEquals(category.getId(), deserialized.getId());
        assertEquals(category.getName(), deserialized.getName());
        assertEquals(category.getDescription(), deserialized.getDescription());
    }
    
    @Property
    @Label("Service entity serialization round-trip")
    void serviceSerializationRoundTrip(
            @ForAll("services") Service service
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(service);
        
        // Deserialize back to entity
        Service deserialized = objectMapper.readValue(json, Service.class);
        
        // Verify field-by-field equivalence
        assertEquals(service.getId(), deserialized.getId());
        assertEquals(service.getName(), deserialized.getName());
        assertEquals(service.getDescription(), deserialized.getDescription());
        assertEquals(service.getBasePrice(), deserialized.getBasePrice());
        assertEquals(service.getDurationMinutes(), deserialized.getDurationMinutes());
        assertEquals(service.getImageUrls(), deserialized.getImageUrls());
    }
    
    @Property
    @Label("Booking entity serialization round-trip")
    void bookingSerializationRoundTrip(
            @ForAll("bookings") Booking booking
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(booking);
        
        // Deserialize back to entity
        Booking deserialized = objectMapper.readValue(json, Booking.class);
        
        // Verify field-by-field equivalence
        assertEquals(booking.getId(), deserialized.getId());
        assertEquals(booking.getBookingReference(), deserialized.getBookingReference());
        assertEquals(booking.getBookingDate(), deserialized.getBookingDate());
        assertEquals(booking.getStatus(), deserialized.getStatus());
        assertEquals(booking.getCreatedAt(), deserialized.getCreatedAt());
    }
    
    @Property
    @Label("RefreshToken entity serialization round-trip")
    void refreshTokenSerializationRoundTrip(
            @ForAll("refreshTokens") RefreshToken token
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(token);
        
        // Deserialize back to entity
        RefreshToken deserialized = objectMapper.readValue(json, RefreshToken.class);
        
        // Verify field-by-field equivalence
        assertEquals(token.getId(), deserialized.getId());
        assertEquals(token.getToken(), deserialized.getToken());
        assertEquals(token.getExpiryDate(), deserialized.getExpiryDate());
    }
    
    // ========== Arbitraries (Generators) ==========
    
    @Provide
    Arbitrary<User> users() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> usernames = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50);
        Arbitrary<String> emails = emailArbitrary();
        Arbitrary<String> passwords = Arbitraries.strings().ofMinLength(8).ofMaxLength(100);
        Arbitrary<Role> roles = Arbitraries.of(Role.ADMIN, Role.STAFF);
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary().injectNull(0.1);
        
        return Combinators.combine(ids, usernames, emails, passwords, roles, timestamps)
                .as((id, username, email, password, role, createdAt) -> 
                    User.builder()
                        .id(id)
                        .username(username)
                        .email(email)
                        .password(password)
                        .role(role)
                        .createdAt(createdAt)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<Customer> customers() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> phoneNumbers = phoneNumberArbitrary();
        Arbitrary<String> emails = emailArbitrary().injectNull(0.3);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100).injectNull(0.3);
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary().injectNull(0.1);
        
        return Combinators.combine(ids, phoneNumbers, emails, names, timestamps)
                .as((id, phoneNumber, email, name, createdAt) -> 
                    Customer.builder()
                        .id(id)
                        .phoneNumber(phoneNumber)
                        .email(email)
                        .name(name)
                        .createdAt(createdAt)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<ServiceCategory> serviceCategories() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100);
        Arbitrary<String> descriptions = Arbitraries.strings().ofMinLength(0).ofMaxLength(500).injectNull(0.3);
        
        return Combinators.combine(ids, names, descriptions)
                .as((id, name, description) -> 
                    ServiceCategory.builder()
                        .id(id)
                        .name(name)
                        .description(description)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<Service> services() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200);
        Arbitrary<String> descriptions = Arbitraries.strings().ofMinLength(0).ofMaxLength(500).injectNull(0.3);
        Arbitrary<BigDecimal> prices = Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(100000.00))
                .ofScale(2);
        Arbitrary<Integer> durations = Arbitraries.integers().between(1, 1440);
        Arbitrary<String> imageUrlsArb = Arbitraries.strings().ofMinLength(0).ofMaxLength(500).injectNull(0.5);
        
        return Combinators.combine(ids, names, descriptions, prices, durations, imageUrlsArb)
                .as((id, name, description, basePrice, durationMinutes, imageUrls) -> 
                    Service.builder()
                        .id(id)
                        .name(name)
                        .description(description)
                        .basePrice(basePrice)
                        .durationMinutes(durationMinutes)
                        .imageUrls(imageUrls)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<Booking> bookings() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> bookingReferences = Arbitraries.strings().alpha().numeric()
                .ofMinLength(10).ofMaxLength(20);
        Arbitrary<LocalDateTime> bookingDates = dateTimeArbitrary();
        Arbitrary<BookingStatus> statuses = Arbitraries.of(BookingStatus.values());
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary().injectNull(0.1);
        
        return Combinators.combine(ids, bookingReferences, bookingDates, statuses, timestamps)
                .as((id, bookingReference, bookingDate, status, createdAt) -> 
                    Booking.builder()
                        .id(id)
                        .bookingReference(bookingReference)
                        .bookingDate(bookingDate)
                        .status(status)
                        .createdAt(createdAt)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<RefreshToken> refreshTokens() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> tokens = Arbitraries.strings().ofMinLength(20).ofMaxLength(500);
        Arbitrary<LocalDateTime> expiryDates = dateTimeArbitrary();
        
        return Combinators.combine(ids, tokens, expiryDates)
                .as((id, token, expiryDate) -> 
                    RefreshToken.builder()
                        .id(id)
                        .token(token)
                        .expiryDate(expiryDate)
                        .build()
                );
    }
    
    // ========== Helper Arbitraries ==========
    
    private Arbitrary<String> emailArbitrary() {
        Arbitrary<String> localPart = Arbitraries.strings().alpha().numeric().ofMinLength(1).ofMaxLength(20);
        Arbitrary<String> domain = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20);
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "edu", "io");
        
        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }
    
    private Arbitrary<String> phoneNumberArbitrary() {
        return Arbitraries.strings().numeric().ofMinLength(10).ofMaxLength(15);
    }
    
    private Arbitrary<LocalDateTime> dateTimeArbitrary() {
        return Arbitraries.longs()
                .between(
                    LocalDateTime.of(2020, 1, 1, 0, 0).toEpochSecond(java.time.ZoneOffset.UTC),
                    LocalDateTime.of(2030, 12, 31, 23, 59).toEpochSecond(java.time.ZoneOffset.UTC)
                )
                .map(epochSecond -> LocalDateTime.ofEpochSecond(epochSecond, 0, java.time.ZoneOffset.UTC));
    }
}
