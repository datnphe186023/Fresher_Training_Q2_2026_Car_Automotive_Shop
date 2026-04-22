package com.carshop.controller;

import com.carshop.dto.request.CreateServiceHistoryRequest;
import com.carshop.dto.response.ServiceHistoryResponse;
import com.carshop.service.ServiceHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles/{vehicleId}/history")
@RequiredArgsConstructor
public class ServiceHistoryController {

    private final ServiceHistoryService serviceHistoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ServiceHistoryResponse> createHistory(
            @PathVariable Long vehicleId,
            @Valid @RequestBody CreateServiceHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(serviceHistoryService.createHistory(vehicleId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ServiceHistoryResponse>> getHistory(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(serviceHistoryService.getHistoryByVehicleId(vehicleId));
    }
}
