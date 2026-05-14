package com.carshop.service;

import com.carshop.dto.request.CreatePaymentRequest;
import com.carshop.dto.response.PaymentResponse;
import com.carshop.entity.*;
import com.carshop.exception.InventoryException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.PaymentMapper;
import com.carshop.repository.InvoiceRepository;
import com.carshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResponse processPayment(CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new InventoryException("Cannot pay a cancelled invoice");
        }

        BigDecimal outstanding = invoice.getOutstandingAmount();
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new InventoryException("Payment amount exceeds outstanding invoice amount");
        }

        String referenceCode = request.getReferenceCode();
        if (referenceCode == null || referenceCode.isBlank()) {
            referenceCode = generateReferenceCode();
        }
        if (paymentRepository.existsByReferenceCode(referenceCode)) {
            throw new InventoryException("Payment reference code already exists");
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .method(request.getMethod())
                .status(PaymentStatus.COMPLETED)
                .referenceCode(referenceCode)
                .notes(request.getNotes())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        invoice.setPaidAmount(invoice.getPaidAmount().add(request.getAmount()));
        invoice.setStatus(invoice.getOutstandingAmount().compareTo(BigDecimal.ZERO) == 0
                ? InvoiceStatus.PAID
                : InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);

        notificationService.notifyPaymentReceived(savedPayment);
        log.info("Payment processed for invoice {} reference {}", invoice.getInvoiceNumber(), referenceCode);
        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoice_IdOrderByPaymentDateDesc(invoiceId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatusOrderByPaymentDateDesc(status).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    private String generateReferenceCode() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
