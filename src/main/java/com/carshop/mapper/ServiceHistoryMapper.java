package com.carshop.mapper;

import com.carshop.dto.response.ServiceHistoryResponse;
import com.carshop.entity.ServiceHistory;
import org.springframework.stereotype.Component;

@Component
public class ServiceHistoryMapper {

    public ServiceHistoryResponse toResponse(ServiceHistory history) {
        if (history == null) return null;
        return ServiceHistoryResponse.builder()
                .id(history.getId())
                .vehicleId(history.getVehicle() != null ? history.getVehicle().getId() : null)
                .serviceName(history.getService() != null ? history.getService().getName() : null)
                .serviceDate(history.getServiceDate())
                .technicianName(history.getTechnicianName())
                .notes(history.getNotes())
                .cost(history.getCost())
                .build();
    }
}
