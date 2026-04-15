package com.carshop.dto.response;

import com.carshop.entity.BookingStatus;
import com.carshop.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for DTO serialization round-trip.
 * Feature: week-1-foundation-setup, Property 8: DTO serialization round-trip
 * **Validates: Requirements 18.6**
 */
class DTOSerializationPropertyTest {
    
    private final ObjectMapper objectMapper;
    
    public DTOSerializationPropertyTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Property 8: DTO serialization round-trip
     * For any DTO instance used in API responses, serializing to JSON and deserializing back
     * should produce an equivalent object.
     */
    
    @Property
    @Label("AuthResponse DTO serialization round-trip")
    void authResponseSerializationRoundTrip(
            @ForAll("authResponses") AuthResponse authResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(authResponse);
        
        // Deserialize back to DTO
        AuthResponse deserialized = objectMapper.readValue(json, AuthResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(authResponse.getAccessToken(), deserialized.getAccessToken());
        assertEquals(authResponse.getRefreshToken(), deserialized.getRefreshToken());
        assertEquals(authResponse.getTokenType(), deserialized.getTokenType());
        assertEquals(authResponse.getExpiresIn(), deserialized.getExpiresIn());
        
        // Verify nested UserResponse
        if (authResponse.getUser() != null) {
            assertNotNull(deserialized.getUser());
            assertEquals(authResponse.getUser().getId(), deserialized.getUser().getId());
            assertEquals(authResponse.getUser().getUsername(), deserialized.getUser().getUsername());
            assertEquals(authResponse.getUser().getEmail(), deserialized.getUser().getEmail());
            assertEquals(authResponse.getUser().getRole(), deserialized.getUser().getRole());
            assertEquals(authResponse.getUser().getCreatedAt(), deserialized.getUser().getCreatedAt());
        } else {
            assertNull(deserialized.getUser());
        }
    }
    
    @Property
    @Label("UserResponse DTO serialization round-trip")
    void userResponseSerializationRoundTrip(
            @ForAll("userResponses") UserResponse userResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(userResponse);
        
        // Deserialize back to DTO
        UserResponse deserialized = objectMapper.readValue(json, UserResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(userResponse.getId(), deserialized.getId());
        assertEquals(userResponse.getUsername(), deserialized.getUsername());
        assertEquals(userResponse.getEmail(), deserialized.getEmail());
        assertEquals(userResponse.getRole(), deserialized.getRole());
        assertEquals(userResponse.getCreatedAt(), deserialized.getCreatedAt());
    }
    
    @Property
    @Label("ServiceResponse DTO serialization round-trip")
    void serviceResponseSerializationRoundTrip(
            @ForAll("serviceResponses") ServiceResponse serviceResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(serviceResponse);
        
        // Deserialize back to DTO
        ServiceResponse deserialized = objectMapper.readValue(json, ServiceResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(serviceResponse.getId(), deserialized.getId());
        assertEquals(serviceResponse.getName(), deserialized.getName());
        assertEquals(serviceResponse.getDescription(), deserialized.getDescription());
        assertEquals(serviceResponse.getBasePrice(), deserialized.getBasePrice());
        assertEquals(serviceResponse.getDurationMinutes(), deserialized.getDurationMinutes());
        
        // Verify nested CategoryResponse
        if (serviceResponse.getCategory() != null) {
            assertNotNull(deserialized.getCategory());
            assertEquals(serviceResponse.getCategory().getId(), deserialized.getCategory().getId());
            assertEquals(serviceResponse.getCategory().getName(), deserialized.getCategory().getName());
            assertEquals(serviceResponse.getCategory().getDescription(), deserialized.getCategory().getDescription());
        } else {
            assertNull(deserialized.getCategory());
        }
        
        // Verify image URLs list
        if (serviceResponse.getImageUrls() != null) {
            assertNotNull(deserialized.getImageUrls());
            assertEquals(serviceResponse.getImageUrls().size(), deserialized.getImageUrls().size());
            assertEquals(serviceResponse.getImageUrls(), deserialized.getImageUrls());
        } else {
            assertNull(deserialized.getImageUrls());
        }
    }
    
    @Property
    @Label("CategoryResponse DTO serialization round-trip")
    void categoryResponseSerializationRoundTrip(
            @ForAll("categoryResponses") CategoryResponse categoryResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(categoryResponse);
        
        // Deserialize back to DTO
        CategoryResponse deserialized = objectMapper.readValue(json, CategoryResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(categoryResponse.getId(), deserialized.getId());
        assertEquals(categoryResponse.getName(), deserialized.getName());
        assertEquals(categoryResponse.getDescription(), deserialized.getDescription());
    }
    
    @Property
    @Label("BookingResponse DTO serialization round-trip")
    void bookingResponseSerializationRoundTrip(
            @ForAll("bookingResponses") BookingResponse bookingResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(bookingResponse);
        
        // Deserialize back to DTO
        BookingResponse deserialized = objectMapper.readValue(json, BookingResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(bookingResponse.getId(), deserialized.getId());
        assertEquals(bookingResponse.getBookingReference(), deserialized.getBookingReference());
        assertEquals(bookingResponse.getBookingDate(), deserialized.getBookingDate());
        assertEquals(bookingResponse.getStatus(), deserialized.getStatus());
        assertEquals(bookingResponse.getCreatedAt(), deserialized.getCreatedAt());
        
        // Verify nested CustomerResponse
        if (bookingResponse.getCustomer() != null) {
            assertNotNull(deserialized.getCustomer());
            assertEquals(bookingResponse.getCustomer().getId(), deserialized.getCustomer().getId());
            assertEquals(bookingResponse.getCustomer().getPhoneNumber(), deserialized.getCustomer().getPhoneNumber());
            assertEquals(bookingResponse.getCustomer().getEmail(), deserialized.getCustomer().getEmail());
            assertEquals(bookingResponse.getCustomer().getName(), deserialized.getCustomer().getName());
            assertEquals(bookingResponse.getCustomer().getCreatedAt(), deserialized.getCustomer().getCreatedAt());
        } else {
            assertNull(deserialized.getCustomer());
        }
        
        // Verify nested ServiceResponse
        if (bookingResponse.getService() != null) {
            assertNotNull(deserialized.getService());
            assertEquals(bookingResponse.getService().getId(), deserialized.getService().getId());
            assertEquals(bookingResponse.getService().getName(), deserialized.getService().getName());
        } else {
            assertNull(deserialized.getService());
        }
    }
    
    @Property
    @Label("CustomerResponse DTO serialization round-trip")
    void customerResponseSerializationRoundTrip(
            @ForAll("customerResponses") CustomerResponse customerResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(customerResponse);
        
        // Deserialize back to DTO
        CustomerResponse deserialized = objectMapper.readValue(json, CustomerResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(customerResponse.getId(), deserialized.getId());
        assertEquals(customerResponse.getPhoneNumber(), deserialized.getPhoneNumber());
        assertEquals(customerResponse.getEmail(), deserialized.getEmail());
        assertEquals(customerResponse.getName(), deserialized.getName());
        assertEquals(customerResponse.getCreatedAt(), deserialized.getCreatedAt());
    }
    
    @Property
    @Label("ErrorResponse DTO serialization round-trip")
    void errorResponseSerializationRoundTrip(
            @ForAll("errorResponses") ErrorResponse errorResponse
    ) throws Exception {
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(errorResponse);
        
        // Deserialize back to DTO
        ErrorResponse deserialized = objectMapper.readValue(json, ErrorResponse.class);
        
        // Verify field-by-field equivalence
        assertEquals(errorResponse.getTimestamp(), deserialized.getTimestamp());
        assertEquals(errorResponse.getStatus(), deserialized.getStatus());
        assertEquals(errorResponse.getError(), deserialized.getError());
        assertEquals(errorResponse.getMessage(), deserialized.getMessage());
        assertEquals(errorResponse.getPath(), deserialized.getPath());
        
        // Verify field errors list
        if (errorResponse.getErrors() != null) {
            assertNotNull(deserialized.getErrors());
            assertEquals(errorResponse.getErrors().size(), deserialized.getErrors().size());
            for (int i = 0; i < errorResponse.getErrors().size(); i++) {
                assertEquals(errorResponse.getErrors().get(i).getField(), 
                           deserialized.getErrors().get(i).getField());
                assertEquals(errorResponse.getErrors().get(i).getMessage(), 
                           deserialized.getErrors().get(i).getMessage());
            }
        } else {
            assertNull(deserialized.getErrors());
        }
    }
    
    // ========== Arbitraries (Generators) ==========
    
    @Provide
    Arbitrary<AuthResponse> authResponses() {
        Arbitrary<String> accessTokens = Arbitraries.strings().ofMinLength(20).ofMaxLength(500);
        Arbitrary<String> refreshTokens = Arbitraries.strings().ofMinLength(20).ofMaxLength(500);
        Arbitrary<String> tokenTypes = Arbitraries.of("Bearer");
        Arbitrary<Long> expiresIn = Arbitraries.longs().between(1L, 86400L);
        Arbitrary<UserResponse> users = userResponses().injectNull(0.1);
        
        return Combinators.combine(accessTokens, refreshTokens, tokenTypes, expiresIn, users)
                .as((accessToken, refreshToken, tokenType, expires, user) -> 
                    AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType(tokenType)
                        .expiresIn(expires)
                        .user(user)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<UserResponse> userResponses() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> usernames = Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50);
        Arbitrary<String> emails = emailArbitrary();
        Arbitrary<Role> roles = Arbitraries.of(Role.ADMIN, Role.STAFF);
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary().injectNull(0.1);
        
        return Combinators.combine(ids, usernames, emails, roles, timestamps)
                .as((id, username, email, role, createdAt) -> 
                    UserResponse.builder()
                        .id(id)
                        .username(username)
                        .email(email)
                        .role(role)
                        .createdAt(createdAt)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<ServiceResponse> serviceResponses() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200);
        Arbitrary<CategoryResponse> categories = categoryResponses().injectNull(0.1);
        Arbitrary<String> descriptions = Arbitraries.strings().ofMinLength(0).ofMaxLength(500).injectNull(0.3);
        Arbitrary<BigDecimal> prices = Arbitraries.bigDecimals()
                .between(BigDecimal.valueOf(0.01), BigDecimal.valueOf(100000.00))
                .ofScale(2);
        Arbitrary<Integer> durations = Arbitraries.integers().between(1, 1440);
        Arbitrary<List<String>> imageUrls = imageUrlListArbitrary().injectNull(0.2);
        
        return Combinators.combine(ids, names, categories, descriptions, prices, durations, imageUrls)
                .as((id, name, category, description, basePrice, durationMinutes, urls) -> 
                    ServiceResponse.builder()
                        .id(id)
                        .name(name)
                        .category(category)
                        .description(description)
                        .basePrice(basePrice)
                        .durationMinutes(durationMinutes)
                        .imageUrls(urls)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<CategoryResponse> categoryResponses() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100);
        Arbitrary<String> descriptions = Arbitraries.strings().ofMinLength(0).ofMaxLength(500).injectNull(0.3);
        
        return Combinators.combine(ids, names, descriptions)
                .as((id, name, description) -> 
                    CategoryResponse.builder()
                        .id(id)
                        .name(name)
                        .description(description)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<BookingResponse> bookingResponses() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> bookingReferences = Arbitraries.strings().alpha().numeric()
                .ofMinLength(10).ofMaxLength(20);
        Arbitrary<CustomerResponse> customers = customerResponses().injectNull(0.1);
        Arbitrary<ServiceResponse> services = serviceResponses().injectNull(0.1);
        Arbitrary<LocalDateTime> bookingDates = dateTimeArbitrary();
        Arbitrary<BookingStatus> statuses = Arbitraries.of(BookingStatus.values());
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary().injectNull(0.1);
        
        return Combinators.combine(ids, bookingReferences, customers, services, bookingDates, statuses, timestamps)
                .as((id, bookingReference, customer, service, bookingDate, status, createdAt) -> 
                    BookingResponse.builder()
                        .id(id)
                        .bookingReference(bookingReference)
                        .customer(customer)
                        .service(service)
                        .bookingDate(bookingDate)
                        .status(status)
                        .createdAt(createdAt)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<CustomerResponse> customerResponses() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L).injectNull(0.1);
        Arbitrary<String> phoneNumbers = phoneNumberArbitrary();
        Arbitrary<String> emails = emailArbitrary().injectNull(0.3);
        Arbitrary<String> names = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100).injectNull(0.3);
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary().injectNull(0.1);
        
        return Combinators.combine(ids, phoneNumbers, emails, names, timestamps)
                .as((id, phoneNumber, email, name, createdAt) -> 
                    CustomerResponse.builder()
                        .id(id)
                        .phoneNumber(phoneNumber)
                        .email(email)
                        .name(name)
                        .createdAt(createdAt)
                        .build()
                );
    }
    
    @Provide
    Arbitrary<ErrorResponse> errorResponses() {
        Arbitrary<LocalDateTime> timestamps = dateTimeArbitrary();
        Arbitrary<Integer> statuses = Arbitraries.integers().between(400, 599);
        Arbitrary<String> errors = Arbitraries.of("Bad Request", "Unauthorized", "Forbidden", 
                                                   "Not Found", "Conflict", "Internal Server Error");
        Arbitrary<String> messages = Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(200);
        Arbitrary<String> paths = Arbitraries.of("/api/auth/register", "/api/auth/login", 
                                                  "/api/bookings", "/api/services", "/api/categories");
        Arbitrary<List<ErrorResponse.FieldError>> fieldErrors = fieldErrorListArbitrary().injectNull(0.3);
        
        return Combinators.combine(timestamps, statuses, errors, messages, paths, fieldErrors)
                .as((timestamp, status, error, message, path, errs) -> 
                    ErrorResponse.builder()
                        .timestamp(timestamp)
                        .status(status)
                        .error(error)
                        .message(message)
                        .path(path)
                        .errors(errs)
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
    
    private Arbitrary<List<String>> imageUrlListArbitrary() {
        Arbitrary<String> url = Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(50)
                .map(s -> "https://example.com/images/" + s + ".jpg");
        return Arbitraries.integers().between(0, 5)
                .flatMap(size -> {
                    List<Arbitrary<String>> urlArbitraries = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        urlArbitraries.add(url);
                    }
                    return Combinators.combine(urlArbitraries).as(urls -> new ArrayList<>(urls));
                });
    }
    
    private Arbitrary<List<ErrorResponse.FieldError>> fieldErrorListArbitrary() {
        Arbitrary<ErrorResponse.FieldError> fieldError = Combinators.combine(
                Arbitraries.of("username", "email", "password", "phoneNumber", "serviceId", "bookingDate"),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(100)
        ).as((field, message) -> 
            ErrorResponse.FieldError.builder()
                .field(field)
                .message(message)
                .build()
        );
        
        return Arbitraries.integers().between(0, 5)
                .flatMap(size -> {
                    List<Arbitrary<ErrorResponse.FieldError>> errorArbitraries = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        errorArbitraries.add(fieldError);
                    }
                    return Combinators.combine(errorArbitraries).as(errors -> new ArrayList<>(errors));
                });
    }
}
