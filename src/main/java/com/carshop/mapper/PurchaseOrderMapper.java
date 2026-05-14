package com.carshop.mapper;

import com.carshop.dto.response.PurchaseOrderResponse;
import com.carshop.entity.PurchaseOrder;
import com.carshop.entity.PurchaseOrderItem;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        if (purchaseOrder == null) {
            return null;
        }
        return PurchaseOrderResponse.builder()
                .id(purchaseOrder.getId())
                .poNumber(purchaseOrder.getPoNumber())
                .supplierId(purchaseOrder.getSupplier().getId())
                .supplierName(purchaseOrder.getSupplier().getName())
                .orderDate(purchaseOrder.getOrderDate())
                .expectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate())
                .actualDeliveryDate(purchaseOrder.getActualDeliveryDate())
                .totalAmount(purchaseOrder.getTotalAmount())
                .status(purchaseOrder.getStatus())
                .notes(purchaseOrder.getNotes())
                .items(purchaseOrder.getItems().stream()
                        .map(this::toItemResponse)
                        .toList())
                .build();
    }

    private PurchaseOrderResponse.PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        return PurchaseOrderResponse.PurchaseOrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .receivedQuantity(item.getReceivedQuantity())
                .build();
    }
}
