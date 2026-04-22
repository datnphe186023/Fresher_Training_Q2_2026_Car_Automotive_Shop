package com.carshop.service;

import com.carshop.dto.request.CreateServiceHistoryRequest;
import com.carshop.dto.response.ServiceHistoryResponse;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceHistory;
import com.carshop.entity.Vehicle;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.ServiceHistoryMapper;
import com.carshop.repository.ServiceHistoryRepository;
import com.carshop.repository.ServiceRepository;
import com.carshop.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceHistoryService {

    private final ServiceHistoryRepository serviceHistoryRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceHistoryMapper serviceHistoryMapper;

    @Transactional
    public ServiceHistoryResponse createHistory(Long vehicleId, CreateServiceHistoryRequest request) {
        log.info("Creating service history for vehicle id: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (request.getCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }

        ServiceHistory history = ServiceHistory.builder()
                .vehicle(vehicle)
                .service(service)
                .serviceDate(request.getServiceDate())
                .technicianName(request.getTechnicianName())
                .notes(request.getNotes())
                .cost(request.getCost())
                .build();

        return serviceHistoryMapper.toResponse(serviceHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public List<ServiceHistoryResponse> getHistoryByVehicleId(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found");
        }
        return serviceHistoryRepository.findAllByVehicleIdOrderByServiceDateDesc(vehicleId)
                .stream().map(serviceHistoryMapper::toResponse).collect(Collectors.toList());
    }
}
