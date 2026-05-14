package com.carshop.service;

import com.carshop.dto.request.CreatePurchaseOrderRequest;
import com.carshop.dto.response.PurchaseOrderResponse;
import com.carshop.entity.MovementType;
import com.carshop.entity.Product;
import com.carshop.entity.PurchaseOrder;
import com.carshop.entity.PurchaseOrder.PurchaseOrderStatus;
import com.carshop.entity.PurchaseOrderItem;
import com.carshop.entity.Supplier;
import com.carshop.entity.StockMovement.MovementReason;
import com.carshop.exception.InventoryException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.PurchaseOrderMapper;
import com.carshop.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing purchase orders with auto stock updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SupplierService supplierService;
    private final ProductService productService;
    private final StockMovementService stockMovementService;

    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) {
        Supplier supplier = supplierService.findById(request.getSupplierId());

        String poNumber = generatePoNumber();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .poNumber(poNumber)
                .supplier(supplier)
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(PurchaseOrderStatus.PENDING)
                .notes(request.getNotes())
                .build();

        // Add items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemReq : request.getItems()) {
            Product product = productService.findById(itemReq.getProductId());
            
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .build();
            
            item.calculateLineTotal();
            purchaseOrder.addItem(item);
            totalAmount = totalAmount.add(item.getLineTotal());
        }

        purchaseOrder.setTotalAmount(totalAmount);
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        
        log.info("Purchase order created: {} with {} items", poNumber, request.getItems().size());
        return purchaseOrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        PurchaseOrder purchaseOrder = findById(id);
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderResponse updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status) {
        PurchaseOrder purchaseOrder = findById(id);

        // Validate status transition
        validateStatusTransition(purchaseOrder.getStatus(), status);

        purchaseOrder.setStatus(status);

        // Auto-create stock movement when order is received
        if (status == PurchaseOrderStatus.RECEIVED) {
            handleOrderReceived(purchaseOrder);
        }

        if (status == PurchaseOrderStatus.CANCELLED) {
            purchaseOrder.setActualDeliveryDate(LocalDateTime.now());
        }

        PurchaseOrder updated = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order {} status updated to: {}", id, status);
        return purchaseOrderMapper.toResponse(updated);
    }

    @Transactional
    public void handleOrderReceived(PurchaseOrder purchaseOrder) {
        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            // Create stock movement for received items
            try {
                var request = new com.carshop.dto.request.CreateStockMovementRequest();
                request.setProductId(item.getProduct().getId());
                request.setType(MovementType.IN);
                request.setReason(MovementReason.PURCHASE);
                request.setQuantity(item.getQuantity());
                request.setReference("PO: " + purchaseOrder.getPoNumber());
                request.setNotes("Received from PO: " + purchaseOrder.getPoNumber());

                stockMovementService.recordStockMovement(request);
                log.info("Stock movement created for PO item: Product={}, Quantity={}", 
                        item.getProduct().getId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to create stock movement for PO item", e);
                throw new InventoryException("Failed to update inventory for received items", e);
            }
        }
        purchaseOrder.setActualDeliveryDate(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId, Pageable pageable) {
        supplierService.findById(supplierId);
        Page<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findBySupplier(supplierId, pageable);
        return purchaseOrders.map(purchaseOrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrderResponse> getPurchaseOrdersByStatus(PurchaseOrderStatus status, Pageable pageable) {
        Page<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findByStatus(status, pageable);
        return purchaseOrders.map(purchaseOrderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getPurchaseOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findByOrderDateRange(startDate, endDate);
        return purchaseOrders.stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
    }

    private String generatePoNumber() {
        return "PO-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void validateStatusTransition(PurchaseOrderStatus from, PurchaseOrderStatus to) {
        // Simple validation - can be enhanced
        if (from == PurchaseOrderStatus.CANCELLED) {
            throw new InventoryException("Cannot change status of cancelled purchase order");
        }
        if (from == PurchaseOrderStatus.RECEIVED && to != PurchaseOrderStatus.RECEIVED) {
            throw new InventoryException("Cannot change status of received purchase order");
        }
    }
}
