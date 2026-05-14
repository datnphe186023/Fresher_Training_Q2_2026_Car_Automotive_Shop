package com.carshop.dto.request;

import com.carshop.entity.MovementType;
import com.carshop.entity.StockMovement.MovementReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStockMovementRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Movement type is required")
    private MovementType type;

    @NotNull(message = "Movement reason is required")
    private MovementReason reason;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String reference;

    private String notes;
}
