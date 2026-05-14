package com.carshop.service;

import com.carshop.dto.request.CreateBookingRequest;
import com.carshop.dto.response.BookingResponse;
import com.carshop.entity.*;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.InvalidStatusTransitionException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.BookingMapper;
import com.carshop.repository.BookingRepository;
import com.carshop.repository.ServiceRepository;
import com.carshop.repository.TimeSlotRepository;
import com.carshop.repository.VehicleRepository;
import com.carshop.util.BookingReferenceGenerator;
import com.carshop.util.EmailValidator;
import com.carshop.util.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    // Allowed status transitions
    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatus.PENDING, Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
            BookingStatus.CONFIRMED, Set.of(BookingStatus.IN_PROGRESS, BookingStatus.CANCELLED),
            BookingStatus.IN_PROGRESS, Set.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED),
            BookingStatus.COMPLETED, Set.of(),
            BookingStatus.CANCELLED, Set.of()
    );

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final BookingMapper bookingMapper;
    private final InvoiceService invoiceService;
    @Lazy
    private final AppointmentService appointmentService;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for phone: {}, serviceId: {}", request.getPhoneNumber(), request.getServiceId());

        if (!PhoneNumberValidator.isValid(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!EmailValidator.isValid(request.getEmail())) {
                throw new IllegalArgumentException("Invalid email format");
            }
        }

        Customer customer = customerService.findOrCreateCustomer(
                request.getPhoneNumber(), request.getEmail(), request.getName());

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found with ID: " + request.getServiceId()));

        // Validate discount
        BigDecimal discount = request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO;
        validateDiscount(discount);

        // Calculate total price
        BigDecimal totalPrice = service.getBasePrice()
                .multiply(BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);

        // Resolve optional time slot
        TimeSlot timeSlot = null;
        if (request.getTimeSlotId() != null) {
            timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Time slot not found"));
            if (bookingRepository.existsByTimeSlot_IdAndStatusNot(request.getTimeSlotId(), BookingStatus.CANCELLED)) {
                throw new DuplicateResourceException("This time slot is already booked");
            }
            timeSlot.setAvailable(false);
            timeSlotRepository.save(timeSlot);
        }

        // Resolve optional vehicle
        Vehicle vehicle = null;
        if (request.getVehicleId() != null) {
            vehicle = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        }

        Booking booking = Booking.builder()
                .customer(customer)
                .service(service)
                .timeSlot(timeSlot)
                .vehicle(vehicle)
                .bookingReference(BookingReferenceGenerator.generateReference())
                .bookingDate(request.getBookingDate())
                .status(BookingStatus.PENDING)
                .discountPercent(discount)
                .totalPrice(totalPrice)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: {}", saved.getBookingReference());
        return bookingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        return bookingMapper.toResponse(
                bookingRepository.findByBookingReference(bookingReference)
                        .orElseThrow(() -> new IllegalArgumentException("Booking not found with reference: " + bookingReference)));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByPhone(String phoneNumber) {
        if (!PhoneNumberValidator.isValid(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        String normalized = PhoneNumberValidator.normalize(phoneNumber);
        return customerService.findOrCreateCustomer(normalized, null, null)
                .getBookings().stream().map(bookingMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsWithFilters(BookingStatus status, LocalDateTime startDate,
                                                         LocalDateTime endDate, String phoneNumber,
                                                         int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findAllWithFilters(status, startDate, endDate, phoneNumber, pageable)
                .map(bookingMapper::toResponse);
    }

    @Transactional
    public BookingResponse updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        log.info("Updating booking {} status to {}", bookingId, newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        BookingStatus current = booking.getStatus();
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(current, newStatus);
        }

        // Award loyalty points on COMPLETED
        if (newStatus == BookingStatus.COMPLETED) {
            int points = booking.getService().getBasePrice()
                    .divide(BigDecimal.valueOf(10000), RoundingMode.FLOOR).intValue();
            customerService.addLoyaltyPoints(booking.getCustomer().getId(), points);
            log.info("Awarded {} loyalty points to customer {}", points, booking.getCustomer().getId());
        }

        // Release time slot on CANCELLED
        if (newStatus == BookingStatus.CANCELLED && booking.getTimeSlot() != null) {
            booking.getTimeSlot().setAvailable(true);
            timeSlotRepository.save(booking.getTimeSlot());
            
            // Cancel associated appointment
            appointmentService.cancelAppointment(bookingId);
        }

        booking.setStatus(newStatus);
        Booking saved = bookingRepository.save(booking);

        if (newStatus == BookingStatus.COMPLETED) {
            // Auto-issue invoice after the booking has been persisted as completed
            invoiceService.createInvoiceForBooking(bookingId);
        }

        return bookingMapper.toResponse(saved);
    }

    private void validateDiscount(BigDecimal discount) {
        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
    }
}
