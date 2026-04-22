package com.carshop.service;

import com.carshop.dto.request.CreateBookingRequest;
import com.carshop.dto.response.BookingResponse;
import com.carshop.entity.Booking;
import com.carshop.entity.BookingStatus;
import com.carshop.entity.Customer;
import com.carshop.entity.Service;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.BookingMapper;
import com.carshop.repository.BookingRepository;
import com.carshop.repository.ServiceRepository;
import com.carshop.util.BookingReferenceGenerator;
import com.carshop.util.EmailValidator;
import com.carshop.util.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing guest bookings in the car enhancement shop system.
 * Handles booking creation, retrieval by reference, and tracking by phone number.
 * 
 * Validates: Requirements 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.10, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.8
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerService customerService;
    private final BookingMapper bookingMapper;
    
    /**
     * Creates a new booking for a guest customer.
     * 
     * Business rules:
     * - Validates phone number format
     * - Validates email format if provided
     * - Finds or creates customer by phone number
     * - Generates unique booking reference
     * - Creates booking with PENDING status
     * - Updates customer email/name if provided and not already set
     * 
     * @param request the booking creation request containing customer and booking details
     * @return BookingResponse with the created booking details including booking reference
     * @throws IllegalArgumentException if phone number or email format is invalid
     * @throws ResourceNotFoundException if service ID does not exist
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for phone: {}, serviceId: {}", request.getPhoneNumber(), request.getServiceId());
        
        // Validate phone number format
        if (!PhoneNumberValidator.isValid(request.getPhoneNumber())) {
            log.warn("Invalid phone number format: {}", request.getPhoneNumber());
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        // Validate email format if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!EmailValidator.isValid(request.getEmail())) {
                log.warn("Invalid email format: {}", request.getEmail());
                throw new IllegalArgumentException("Invalid email format");
            }
        }
        
        // Find or create customer
        Customer customer = customerService.findOrCreateCustomer(
            request.getPhoneNumber(),
            request.getEmail(),
            request.getName()
        );
        log.debug("Customer resolved with ID: {}", customer.getId());
        
        // Find service
        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> {
                    log.error("Service not found with ID: {}", request.getServiceId());
                    return new IllegalArgumentException("Service not found with ID: " + request.getServiceId());
                });
        log.debug("Service found: {}", service.getName());
        
        // Generate unique booking reference
        String bookingReference = BookingReferenceGenerator.generateReference();
        log.debug("Generated booking reference: {}", bookingReference);
        
        // Create booking with PENDING status
        Booking booking = Booking.builder()
                .customer(customer)
                .service(service)
                .bookingReference(bookingReference)
                .bookingDate(request.getBookingDate())
                .status(BookingStatus.PENDING)
                .build();
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully with reference: {}, ID: {}", 
                savedBooking.getBookingReference(), savedBooking.getId());
        
        // Convert to response DTO
        return bookingMapper.toResponse(savedBooking);
    }
    
    /**
     * Retrieves a booking by its unique booking reference.
     * Used for guest order tracking.
     * 
     * @param bookingReference the unique booking reference to search for
     * @return BookingResponse with the booking details
     * @throws IllegalArgumentException if booking reference is not found
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        log.debug("Retrieving booking by reference: {}", bookingReference);
        
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> {
                    log.warn("Booking not found with reference: {}", bookingReference);
                    return new IllegalArgumentException("Booking not found with reference: " + bookingReference);
                });
        
        log.info("Booking found with reference: {}", bookingReference);
        return bookingMapper.toResponse(booking);
    }
    
    /**
     * Retrieves all bookings for a customer by phone number.
     * Used for guest order tracking by phone number.
     * 
     * @param phoneNumber the customer's phone number
     * @return List of BookingResponse with all bookings for the customer
     * @throws IllegalArgumentException if phone number format is invalid
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByPhone(String phoneNumber) {
        log.debug("Retrieving bookings by phone: {}", phoneNumber);
        
        // Validate and normalize phone number
        if (!PhoneNumberValidator.isValid(phoneNumber)) {
            log.warn("Invalid phone number format: {}", phoneNumber);
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        String normalizedPhone = PhoneNumberValidator.normalize(phoneNumber);
        log.debug("Normalized phone number: {}", normalizedPhone);
        
        // Find customer by phone number
        return customerService.findOrCreateCustomer(normalizedPhone, null, null)
                .getBookings()
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a booking. When status is COMPLETED, awards loyalty points.
     *
     * @param bookingId the booking ID
     * @param newStatus the new status to set
     * @return updated BookingResponse
     */
    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        log.info("Updating booking {} status to {}", bookingId, newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (newStatus == BookingStatus.COMPLETED) {
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                throw new DuplicateResourceException("Booking is already completed");
            }
            int points = booking.getService().getBasePrice()
                    .divide(BigDecimal.valueOf(10000), RoundingMode.FLOOR).intValue();
            customerService.addLoyaltyPoints(booking.getCustomer().getId(), points);
            log.info("Awarded {} loyalty points to customer {}", points, booking.getCustomer().getId());
        }

        booking.setStatus(newStatus);
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }
}
