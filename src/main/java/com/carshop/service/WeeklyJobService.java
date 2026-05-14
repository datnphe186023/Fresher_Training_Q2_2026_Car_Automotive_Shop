package com.carshop.service;

import com.carshop.dto.response.JobExecutionResponse;
import com.carshop.entity.*;
import com.carshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyJobService {

    public static final String DAILY_SALES_SUMMARY = "DAILY_SALES_SUMMARY";
    public static final String OVERDUE_INVOICE_REMINDER = "OVERDUE_INVOICE_REMINDER";
    public static final String LOW_STOCK_SCAN = "LOW_STOCK_SCAN";
    public static final String APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER";

    private static final String SYSTEM_USER = "SYSTEM";

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final JobExecutionHistoryRepository jobExecutionHistoryRepository;
    private final NotificationService notificationService;
    private final StockAlertService stockAlertService;

    @Transactional
    public JobExecutionResponse runDailySalesSummary(LocalDate date, JobTriggerType triggerType, String triggeredBy) {
        LocalDate targetDate = date == null ? LocalDate.now().minusDays(1) : date;
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        JobExecutionHistory execution = startExecution(DAILY_SALES_SUMMARY, triggerType, triggeredBy);
        try {
            BigDecimal totalRevenue = paymentRepository.sumCompletedPaymentsInRange(start, end);
            long completedPayments = paymentRepository.countCompletedPaymentsInRange(start, end);
            long completedBookings = bookingRepository.countCompletedInRange(start, end);

            BigDecimal averageTicket = BigDecimal.ZERO;
            if (completedPayments > 0) {
                averageTicket = totalRevenue.divide(BigDecimal.valueOf(completedPayments), 2, RoundingMode.HALF_UP);
            }

            String summary = String.format(
                    "Date=%s, totalRevenue=%s, completedPayments=%d, completedBookings=%d, averageTicket=%s",
                    targetDate,
                    totalRevenue,
                    completedPayments,
                    completedBookings,
                    averageTicket
            );

            notificationService.logNotification(
                    "admin@carshop.local",
                    "Daily sales summary " + targetDate,
                    summary,
                    NotificationType.DAILY_SALES_SUMMARY,
                    "DAILY_SALES",
                    targetDate.toEpochDay());

            return completeSuccess(execution, completedPayments, completedPayments, 0, summary);
        } catch (Exception ex) {
            log.error("Daily sales summary job failed", ex);
            return completeFailure(execution, ex, "Failed to generate daily sales summary");
        }
    }

    @Transactional
    public JobExecutionResponse runOverdueInvoiceReminder(JobTriggerType triggerType, String triggeredBy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        JobExecutionHistory execution = startExecution(OVERDUE_INVOICE_REMINDER, triggerType, triggeredBy);
        try {
            List<Invoice> candidates = invoiceRepository.findOverdueCandidates(now);
            long notified = 0;
            long updated = 0;

            for (Invoice invoice : candidates) {
                if (invoice.getStatus() != InvoiceStatus.OVERDUE) {
                    invoice.setStatus(InvoiceStatus.OVERDUE);
                    updated++;
                }

                boolean alreadyNotified = notificationRepository
                        .existsByTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtBetween(
                                NotificationType.OVERDUE_INVOICE_REMINDER,
                                "INVOICE",
                                invoice.getId(),
                                startOfDay,
                                endOfDay);
                if (alreadyNotified) {
                    continue;
                }

                String recipient = resolveRecipient(invoice.getCustomer().getEmail(), invoice.getCustomer().getPhoneNumber());
                String subject = "Overdue invoice reminder: " + invoice.getInvoiceNumber();
                String content = "Invoice " + invoice.getInvoiceNumber() + " is overdue since " + invoice.getDueDate()
                        + ". Outstanding amount: " + invoice.getOutstandingAmount();

                notificationService.logNotification(
                        recipient,
                        subject,
                        content,
                        NotificationType.OVERDUE_INVOICE_REMINDER,
                        "INVOICE",
                        invoice.getId());
                notified++;
            }

            if (updated > 0) {
                invoiceRepository.saveAll(candidates);
            }

            String summary = String.format(
                    "Overdue invoices=%d, statusUpdated=%d, remindersSent=%d",
                    candidates.size(),
                    updated,
                    notified
            );
            return completeSuccess(execution, candidates.size(), notified, candidates.size() - notified, summary);
        } catch (Exception ex) {
            log.error("Overdue invoice reminder job failed", ex);
            return completeFailure(execution, ex, "Failed to process overdue invoices");
        }
    }

    @Transactional
    public JobExecutionResponse runLowStockScan(JobTriggerType triggerType, String triggeredBy) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        JobExecutionHistory execution = startExecution(LOW_STOCK_SCAN, triggerType, triggeredBy);
        try {
            List<Product> lowStockProducts = productRepository.findAllLowStockItems();
            long notificationsSent = 0;

            for (Product product : lowStockProducts) {
                stockAlertService.checkAndCreateAlert(product);

                boolean alreadyNotified = notificationRepository
                        .existsByTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtBetween(
                                NotificationType.LOW_STOCK_ALERT,
                                "PRODUCT",
                                product.getId(),
                                startOfDay,
                                endOfDay);
                if (alreadyNotified) {
                    continue;
                }

                String content = "Low stock detected for product " + product.getName()
                        + " (SKU: " + product.getSku() + ") quantity=" + product.getQuantity()
                        + ", reorderLevel=" + product.getReorderLevel();
                notificationService.logNotification(
                        "inventory@carshop.local",
                        "Low stock alert: " + product.getSku(),
                        content,
                        NotificationType.LOW_STOCK_ALERT,
                        "PRODUCT",
                        product.getId());
                notificationsSent++;
            }

            String summary = String.format(
                    "Low stock products=%d, notificationsSent=%d",
                    lowStockProducts.size(),
                    notificationsSent
            );
            return completeSuccess(execution, lowStockProducts.size(), notificationsSent, lowStockProducts.size() - notificationsSent, summary);
        } catch (Exception ex) {
            log.error("Low stock scan job failed", ex);
            return completeFailure(execution, ex, "Failed to process low stock scan");
        }
    }

    @Transactional
    public JobExecutionResponse runAppointmentReminder(JobTriggerType triggerType, String triggeredBy) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.plusHours(23);
        LocalDateTime end = now.plusHours(25);
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        JobExecutionHistory execution = startExecution(APPOINTMENT_REMINDER, triggerType, triggeredBy);
        try {
            List<Appointment> candidates = appointmentRepository.findReminderCandidates(start, end);
            long remindersSent = 0;

            for (Appointment appointment : candidates) {
                boolean alreadyNotified = notificationRepository
                        .existsByTypeAndRelatedEntityTypeAndRelatedEntityIdAndCreatedAtBetween(
                                NotificationType.APPOINTMENT_REMINDER,
                                "APPOINTMENT",
                                appointment.getId(),
                                startOfDay,
                                endOfDay);
                if (alreadyNotified) {
                    continue;
                }

                String recipient = resolveRecipient(
                        appointment.getBooking().getCustomer().getEmail(),
                        appointment.getBooking().getCustomer().getPhoneNumber());
                String subject = "Appointment reminder: " + appointment.getBooking().getBookingReference();
                String content = "You have an appointment at " + appointment.getAppointmentDate()
                        + " (time slot: " + appointment.getTimeSlot() + ") for booking "
                        + appointment.getBooking().getBookingReference() + ".";

                notificationService.logNotification(
                        recipient,
                        subject,
                        content,
                        NotificationType.APPOINTMENT_REMINDER,
                        "APPOINTMENT",
                        appointment.getId());
                remindersSent++;
            }

            String summary = String.format(
                    "Appointment candidates=%d, remindersSent=%d",
                    candidates.size(),
                    remindersSent
            );
            return completeSuccess(execution, candidates.size(), remindersSent, candidates.size() - remindersSent, summary);
        } catch (Exception ex) {
            log.error("Appointment reminder job failed", ex);
            return completeFailure(execution, ex, "Failed to send appointment reminders");
        }
    }

    @Transactional(readOnly = true)
    public Page<JobExecutionResponse> getExecutionHistory(Pageable pageable) {
        return jobExecutionHistoryRepository.findAllByOrderByStartedAtDesc(pageable)
                .map(JobExecutionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<JobExecutionResponse> getExecutionHistoryByJobName(String jobName, Pageable pageable) {
        return jobExecutionHistoryRepository.findByJobNameOrderByStartedAtDesc(jobName, pageable)
                .map(JobExecutionResponse::from);
    }

    public String defaultManualUser() {
        return SYSTEM_USER;
    }

    private JobExecutionHistory startExecution(String jobName, JobTriggerType triggerType, String triggeredBy) {
        JobExecutionHistory execution = JobExecutionHistory.builder()
                .jobName(jobName)
                .triggerType(triggerType)
                .triggeredBy(triggeredBy == null || triggeredBy.isBlank() ? SYSTEM_USER : triggeredBy)
                .status(JobExecutionStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();
        return jobExecutionHistoryRepository.save(execution);
    }

    private JobExecutionResponse completeSuccess(
            JobExecutionHistory execution,
            long processed,
            long success,
            long failed,
            String summary) {
        execution.setStatus(JobExecutionStatus.SUCCESS);
        execution.setFinishedAt(LocalDateTime.now());
        execution.setProcessedCount(processed);
        execution.setSuccessCount(success);
        execution.setFailedCount(failed);
        execution.setSummary(summary);
        return JobExecutionResponse.from(jobExecutionHistoryRepository.save(execution));
    }

    private JobExecutionResponse completeFailure(JobExecutionHistory execution, Exception ex, String summary) {
        execution.setStatus(JobExecutionStatus.FAILED);
        execution.setFinishedAt(LocalDateTime.now());
        execution.setErrorMessage(ex.getMessage());
        execution.setSummary(summary);
        return JobExecutionResponse.from(jobExecutionHistoryRepository.save(execution));
    }

    private String resolveRecipient(String email, String phoneNumber) {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber : "unknown";
    }
}
