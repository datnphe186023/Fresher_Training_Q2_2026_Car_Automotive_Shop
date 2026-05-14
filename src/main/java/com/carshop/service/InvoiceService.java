package com.carshop.service;

import com.carshop.dto.request.CreateInvoiceRequest;
import com.carshop.dto.response.InvoiceResponse;
import com.carshop.entity.*;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.InvoiceMapper;
import com.carshop.repository.BookingRepository;
import com.carshop.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceMapper invoiceMapper;
    private final NotificationService notificationService;

    @Transactional
    public InvoiceResponse createInvoiceForBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Invoice can only be created for completed bookings");
        }

        return invoiceMapper.toResponse(createOrGetInvoice(booking, null));
    }

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));
        return invoiceMapper.toResponse(createOrGetInvoice(booking, request.getDueDate(), request.getNotes()));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        return invoiceMapper.toResponse(findById(invoiceId));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        return invoiceMapper.toResponse(invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber)));
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByBookingReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));
        return invoiceMapper.toResponse(invoiceRepository.findByBooking_Id(booking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for booking reference: " + bookingReference)));
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findAllWithStatus(status, pageable).map(invoiceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByCustomerPhone(String phoneNumber) {
        return invoiceRepository.findByCustomerPhoneNumberOrderByIssueDateDesc(phoneNumber).stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    public Invoice findById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));
    }

    private Invoice createOrGetInvoice(Booking booking, LocalDateTime dueDate) {
        return createOrGetInvoice(booking, dueDate, null);
    }

    private Invoice createOrGetInvoice(Booking booking, LocalDateTime dueDate, String notes) {
        return invoiceRepository.findByBooking_Id(booking.getId())
                .orElseGet(() -> {
                    BigDecimal total = booking.getTotalPrice() != null ? booking.getTotalPrice() : booking.getService().getBasePrice();
                    Invoice invoice = Invoice.builder()
                            .invoiceNumber(generateInvoiceNumber())
                            .booking(booking)
                            .customer(booking.getCustomer())
                            .dueDate(dueDate != null ? dueDate : LocalDateTime.now().plusDays(7))
                            .subtotalAmount(total)
                            .taxAmount(BigDecimal.ZERO)
                            .totalAmount(total)
                            .paidAmount(BigDecimal.ZERO)
                            .status(InvoiceStatus.ISSUED)
                            .notes(notes)
                            .build();

                    InvoiceItem item = InvoiceItem.builder()
                            .serviceId(booking.getService().getId())
                            .serviceName(booking.getService().getName())
                            .quantity(1)
                            .unitPrice(total)
                            .build();
                    item.calculateLineTotal();
                    invoice.addItem(item);

                    Invoice saved = invoiceRepository.save(invoice);
                    notificationService.notifyInvoiceIssued(saved);
                    notificationService.notifyBookingCompleted(saved);
                    log.info("Invoice created for booking {}", booking.getBookingReference());
                    return saved;
                });
    }

    private String generateInvoiceNumber() {
        return "INV-" + LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 14) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
