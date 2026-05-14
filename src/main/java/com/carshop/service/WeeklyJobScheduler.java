package com.carshop.service;

import com.carshop.entity.JobTriggerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyJobScheduler {

    private final WeeklyJobService weeklyJobService;

    @Value("${jobs.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(cron = "${jobs.daily-sales-summary.cron:0 15 0 * * *}")
    public void scheduleDailySalesSummary() {
        if (!schedulerEnabled) {
            return;
        }
        weeklyJobService.runDailySalesSummary(LocalDate.now().minusDays(1), JobTriggerType.SCHEDULED, "scheduler");
    }

    @Scheduled(cron = "${jobs.overdue-invoices.cron:0 0 1 * * *}")
    public void scheduleOverdueInvoiceReminder() {
        if (!schedulerEnabled) {
            return;
        }
        weeklyJobService.runOverdueInvoiceReminder(JobTriggerType.SCHEDULED, "scheduler");
    }

    @Scheduled(cron = "${jobs.low-stock-scan.cron:0 30 1 * * *}")
    public void scheduleLowStockScan() {
        if (!schedulerEnabled) {
            return;
        }
        weeklyJobService.runLowStockScan(JobTriggerType.SCHEDULED, "scheduler");
    }

    @Scheduled(cron = "${jobs.appointment-reminder.cron:0 0 */6 * * *}")
    public void scheduleAppointmentReminder() {
        if (!schedulerEnabled) {
            return;
        }
        weeklyJobService.runAppointmentReminder(JobTriggerType.SCHEDULED, "scheduler");
    }
}
