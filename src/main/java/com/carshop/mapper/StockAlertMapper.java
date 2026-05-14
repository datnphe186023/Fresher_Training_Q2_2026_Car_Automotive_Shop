package com.carshop.mapper;

import com.carshop.dto.response.StockAlertResponse;
import com.carshop.entity.StockAlert;
import org.springframework.stereotype.Component;

@Component
public class StockAlertMapper {

    public StockAlertResponse toResponse(StockAlert alert) {
        if (alert == null) {
            return null;
        }
        return StockAlertResponse.builder()
                .id(alert.getId())
                .productId(alert.getProduct().getId())
                .productName(alert.getProduct().getName())
                .currentQuantity(alert.getProduct().getQuantity())
                .reorderLevel(alert.getProduct().getReorderLevel())
                .alertType(alert.getAlertType())
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .resolvedAt(alert.getResolvedAt())
                .resolvedBy(alert.getResolvedBy())
                .notes(alert.getNotes())
                .build();
    }
}
