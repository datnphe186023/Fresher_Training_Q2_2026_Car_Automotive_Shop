package com.carshop.controller;

import com.carshop.dto.response.JobExecutionResponse;
import com.carshop.entity.JobTriggerType;
import com.carshop.service.WeeklyJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final WeeklyJobService weeklyJobService;

    @PostMapping("/daily-sales-summary/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<JobExecutionResponse> runDailySalesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        String user = authentication != null ? authentication.getName() : weeklyJobService.defaultManualUser();
        return ResponseEntity.ok(weeklyJobService.runDailySalesSummary(date, JobTriggerType.MANUAL, user));
    }

    @PostMapping("/overdue-invoices/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<JobExecutionResponse> runOverdueInvoiceReminder(Authentication authentication) {
        String user = authentication != null ? authentication.getName() : weeklyJobService.defaultManualUser();
        return ResponseEntity.ok(weeklyJobService.runOverdueInvoiceReminder(JobTriggerType.MANUAL, user));
    }

    @PostMapping("/low-stock-scan/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<JobExecutionResponse> runLowStockScan(Authentication authentication) {
        String user = authentication != null ? authentication.getName() : weeklyJobService.defaultManualUser();
        return ResponseEntity.ok(weeklyJobService.runLowStockScan(JobTriggerType.MANUAL, user));
    }

    @PostMapping("/appointment-reminder/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<JobExecutionResponse> runAppointmentReminder(Authentication authentication) {
        String user = authentication != null ? authentication.getName() : weeklyJobService.defaultManualUser();
        return ResponseEntity.ok(weeklyJobService.runAppointmentReminder(JobTriggerType.MANUAL, user));
    }

    @GetMapping("/executions")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<JobExecutionResponse>> getExecutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(weeklyJobService.getExecutionHistory(PageRequest.of(page, size)));
    }

    @GetMapping("/executions/{jobName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<JobExecutionResponse>> getExecutionsByJobName(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(weeklyJobService.getExecutionHistoryByJobName(jobName, PageRequest.of(page, size)));
    }
}
