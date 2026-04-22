package com.carshop.controller;

import com.carshop.dto.response.LoyaltyResponse;
import com.carshop.dto.response.ServiceHistoryResponse;
import com.carshop.dto.response.VehicleResponse;
import com.carshop.entity.Customer;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.repository.CustomerRepository;
import com.carshop.service.ServiceHistoryService;
import com.carshop.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoints accessible without authentication.
 * Allows guests to look up vehicles, service history, and loyalty points by phone number.
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final VehicleService vehicleService;
    private final ServiceHistoryService serviceHistoryService;
    private final CustomerRepository customerRepository;

    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByPhone(
            @RequestParam String phoneNumber) {
        return ResponseEntity.ok(vehicleService.getVehiclesByPhoneNumber(phoneNumber));
    }

    @GetMapping("/vehicles/{vehicleId}/history")
    public ResponseEntity<List<ServiceHistoryResponse>> getVehicleHistory(
            @PathVariable Long vehicleId) {
        return ResponseEntity.ok(serviceHistoryService.getHistoryByVehicleId(vehicleId));
    }

    @GetMapping("/customers/loyalty")
    public ResponseEntity<LoyaltyResponse> getLoyaltyBalance(
            @RequestParam String phoneNumber) {
        Customer customer = customerRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        return ResponseEntity.ok(LoyaltyResponse.builder()
                .customerName(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .build());
    }
}
