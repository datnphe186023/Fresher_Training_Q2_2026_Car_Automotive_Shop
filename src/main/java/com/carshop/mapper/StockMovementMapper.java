package com.carshop.mapper;

import com.carshop.dto.response.StockMovementResponse;
import com.carshop.entity.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        if (movement == null) {
            return null;
        }
        return StockMovementResponse.builder()
                .id(movement.getId())
                .productId(movement.getProduct().getId())
                .productName(movement.getProduct().getName())
                .type(movement.getType())
                .reason(movement.getReason())
                .quantity(movement.getQuantity())
                .movementDate(movement.getMovementDate())
                .reference(movement.getReference())
                .notes(movement.getNotes())
                .createdBy(movement.getCreatedBy())
                .build();
    }
}
