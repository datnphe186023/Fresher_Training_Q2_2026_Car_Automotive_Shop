package com.carshop.controller;

import com.carshop.dto.request.CreateStockMovementRequest;
import com.carshop.dto.response.StockMovementResponse;
import com.carshop.entity.MovementType;
import com.carshop.entity.StockMovement.MovementReason;
import com.carshop.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<StockMovementResponse> recordStockMovement(
            @Valid @RequestBody CreateStockMovementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.recordStockMovement(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<StockMovementResponse> getStockMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(stockMovementService.getStockMovementById(id));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Page<StockMovementResponse>> getProductMovementHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                stockMovementService.getProductMovementHistoryPaginated(
                        productId, PageRequest.of(page, size)));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(stockMovementService.getMovementsByDateRange(startDate, endDate));
    }

    @GetMapping("/type-reason")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<StockMovementResponse>> getMovementsByTypeAndReason(
            @RequestParam MovementType type,
            @RequestParam MovementReason reason) {
        return ResponseEntity.ok(stockMovementService.getMovementsByTypeAndReason(type, reason));
    }
}
