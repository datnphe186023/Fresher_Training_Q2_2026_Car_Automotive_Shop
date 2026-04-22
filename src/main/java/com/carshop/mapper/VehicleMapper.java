package com.carshop.mapper;

import com.carshop.dto.response.VehicleResponse;
import com.carshop.entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) return null;
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .customerId(vehicle.getCustomer() != null ? vehicle.getCustomer().getId() : null)
                .customerName(vehicle.getCustomer() != null ? vehicle.getCustomer().getName() : null)
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .plateNumber(vehicle.getPlateNumber())
                .color(vehicle.getColor())
                .build();
    }
}
