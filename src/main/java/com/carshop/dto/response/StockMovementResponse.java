package com.carshop.dto.response;

import com.carshop.entity.MovementType;
import com.carshop.entity.StockMovement.MovementReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private MovementType type;
    private MovementReason reason;
    private Integer quantity;
    private LocalDateTime movementDate;
    private String reference;
    private String notes;
    private String createdBy;
}
