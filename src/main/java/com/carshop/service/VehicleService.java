package com.carshop.service;

import com.carshop.dto.request.CreateVehicleRequest;
import com.carshop.dto.request.UpdateVehicleRequest;
import com.carshop.dto.response.VehicleResponse;
import com.carshop.entity.Customer;
import com.carshop.entity.Vehicle;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.VehicleMapper;
import com.carshop.repository.CustomerRepository;
import com.carshop.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        log.info("Creating vehicle with plate: {}", request.getPlateNumber());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (vehicleRepository.existsByPlateNumber(request.getPlateNumber())) {
            throw new DuplicateResourceException("Plate number already registered");
        }

        validateYear(request.getYear());

        Vehicle vehicle = Vehicle.builder()
                .customer(customer)
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .plateNumber(request.getPlateNumber())
                .color(request.getColor())
                .build();

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return vehicleMapper.toResponse(findById(id));
    }

    @Transactional
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        log.info("Updating vehicle id: {}", id);
        Vehicle vehicle = findById(id);

        if (vehicleRepository.existsByPlateNumberAndIdNot(request.getPlateNumber(), id)) {
            throw new DuplicateResourceException("Plate number already registered");
        }

        validateYear(request.getYear());

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setPlateNumber(request.getPlateNumber());
        vehicle.setColor(request.getColor());

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle id: {}", id);
        Vehicle vehicle = findById(id);

        if (vehicleRepository.countServiceHistoryByVehicleId(id) > 0) {
            throw new IllegalStateException("Cannot delete vehicle with existing service history");
        }

        vehicleRepository.delete(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByPhoneNumber(String phoneNumber) {
        return vehicleRepository.findAllByCustomer_PhoneNumber(phoneNumber)
                .stream().map(vehicleMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomerId(Long customerId) {
        return vehicleRepository.findAllByCustomerId(customerId)
                .stream().map(vehicleMapper::toResponse).collect(Collectors.toList());
    }

    private Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    private void validateYear(int year) {
        int maxYear = Year.now().getValue() + 1;
        if (year < 1900 || year > maxYear) {
            throw new IllegalArgumentException("Year must be between 1900 and " + maxYear);
        }
    }
}
