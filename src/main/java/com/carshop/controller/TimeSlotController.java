package com.carshop.controller;

import com.carshop.dto.request.CreateTimeSlotRequest;
import com.carshop.dto.response.TimeSlotResponse;
import com.carshop.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TimeSlotResponse> createTimeSlot(@Valid @RequestBody CreateTimeSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timeSlotService.createTimeSlot(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<TimeSlotResponse>> getTimeSlotsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(timeSlotService.getTimeSlotsByDate(date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TimeSlotResponse> updateTimeSlot(
            @PathVariable Long id,
            @Valid @RequestBody CreateTimeSlotRequest request) {
        return ResponseEntity.ok(timeSlotService.updateTimeSlot(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        return ResponseEntity.noContent().build();
    }
}
