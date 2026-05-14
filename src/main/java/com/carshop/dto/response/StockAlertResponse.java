package com.carshop.dto.response;

import com.carshop.entity.StockAlert.AlertStatus;
import com.carshop.entity.StockAlert.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockAlertResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer currentQuantity;
    private Integer reorderLevel;
    private AlertType alertType;
    private AlertStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private String notes;
}
