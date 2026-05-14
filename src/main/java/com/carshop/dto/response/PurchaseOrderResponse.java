package com.carshop.dto.response;

import com.carshop.entity.PurchaseOrder.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderResponse {

    private Long id;
    private String poNumber;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private BigDecimal totalAmount;
    private PurchaseOrderStatus status;
    private String notes;
    private List<PurchaseOrderItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseOrderItemResponse {

        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private Integer receivedQuantity;
    }
}
