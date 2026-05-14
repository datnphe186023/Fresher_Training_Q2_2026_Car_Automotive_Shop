package com.carshop.service;

import com.carshop.dto.response.NotificationResponse;
import com.carshop.entity.Notification;
import com.carshop.entity.NotificationStatus;
import com.carshop.entity.NotificationType;
import com.carshop.entity.Invoice;
import com.carshop.entity.Payment;
import com.carshop.mapper.NotificationMapper;
import com.carshop.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponse logNotification(
            String recipient,
            String subject,
            String content,
            NotificationType type,
            String relatedEntityType,
            Long relatedEntityId) {

        Notification notification = Notification.builder()
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .type(type)
                .status(NotificationStatus.SENT)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification logged for recipient {} type {}", recipient, type);
        return notificationMapper.toResponse(saved);
    }

    @Transactional
    public NotificationResponse notifyInvoiceIssued(Invoice invoice) {
        String recipient = resolveRecipient(invoice.getCustomer().getEmail(), invoice.getCustomer().getPhoneNumber());
        String subject = "Invoice issued: " + invoice.getInvoiceNumber();
        String content = "Invoice " + invoice.getInvoiceNumber() + " for booking " + invoice.getBooking().getBookingReference()
                + " has been issued. Total amount: " + invoice.getTotalAmount();
        return logNotification(recipient, subject, content, NotificationType.INVOICE_ISSUED, "INVOICE", invoice.getId());
    }

    @Transactional
    public NotificationResponse notifyPaymentReceived(Payment payment) {
        String recipient = resolveRecipient(payment.getInvoice().getCustomer().getEmail(), payment.getInvoice().getCustomer().getPhoneNumber());
        String subject = "Payment received: " + payment.getReferenceCode();
        String content = "Payment of " + payment.getAmount() + " received for invoice " + payment.getInvoice().getInvoiceNumber();
        return logNotification(recipient, subject, content, NotificationType.PAYMENT_RECEIVED, "PAYMENT", payment.getId());
    }

    @Transactional
    public NotificationResponse notifyPaymentFailed(Payment payment, String errorMessage) {
        String recipient = resolveRecipient(payment.getInvoice().getCustomer().getEmail(), payment.getInvoice().getCustomer().getPhoneNumber());
        String subject = "Payment failed: " + payment.getReferenceCode();
        String content = "Payment failed for invoice " + payment.getInvoice().getInvoiceNumber() + ". Reason: " + errorMessage;
        Notification notification = Notification.builder()
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .type(NotificationType.PAYMENT_FAILED)
                .status(NotificationStatus.FAILED)
                .relatedEntityType("PAYMENT")
                .relatedEntityId(payment.getId())
                .errorMessage(errorMessage)
                .build();
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Transactional
    public NotificationResponse notifyBookingCompleted(Invoice invoice) {
        String recipient = resolveRecipient(invoice.getCustomer().getEmail(), invoice.getCustomer().getPhoneNumber());
        String subject = "Booking completed: " + invoice.getBooking().getBookingReference();
        String content = "Your booking has been completed and invoice " + invoice.getInvoiceNumber() + " was created.";
        return logNotification(recipient, subject, content, NotificationType.BOOKING_COMPLETED, "BOOKING", invoice.getBooking().getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByRecipient(String recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByType(NotificationType type) {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    private String resolveRecipient(String email, String phoneNumber) {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return phoneNumber;
    }
}
