package com.carshop.controller;

import com.carshop.dto.response.NotificationResponse;
import com.carshop.entity.NotificationStatus;
import com.carshop.entity.NotificationType;
import com.carshop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/recipient/{recipient}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<NotificationResponse>> getByRecipient(@PathVariable String recipient) {
        return ResponseEntity.ok(notificationService.getNotificationsByRecipient(recipient));
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<NotificationResponse>> getByType(@PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.getNotificationsByType(type));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<NotificationResponse>> getByStatus(
            @RequestParam(defaultValue = "PENDING") NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getNotificationsByStatus(status, PageRequest.of(page, size)));
    }
}
