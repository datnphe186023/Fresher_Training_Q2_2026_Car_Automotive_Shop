package com.carshop.controller;

import com.carshop.dto.response.StockAlertResponse;
import com.carshop.entity.StockAlert.AlertStatus;
import com.carshop.entity.StockAlert.AlertType;
import com.carshop.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class StockAlertController {

    private final StockAlertService stockAlertService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<StockAlertResponse> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(stockAlertService.getAlertById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<StockAlertResponse>> getAlertsByStatus(
            @RequestParam(defaultValue = "ACTIVE") AlertStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                stockAlertService.getAlertsByStatus(status, PageRequest.of(page, size)));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<StockAlertResponse>> getAlertsByType(@PathVariable AlertType type) {
        return ResponseEntity.ok(stockAlertService.getActiveAlertsByType(type));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<StockAlertResponse>> getAlertsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(stockAlertService.getAlertsByDateRange(startDate, endDate));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<StockAlertResponse> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(stockAlertService.resolveAlert(id));
    }

    @PatchMapping("/{id}/ignore")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<StockAlertResponse> ignoreAlert(
            @PathVariable Long id,
            @RequestParam String reason) {
        return ResponseEntity.ok(stockAlertService.ignoreAlert(id, reason));
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Long> countActiveAlerts() {
        return ResponseEntity.ok(stockAlertService.countActiveAlerts());
    }
}
