package com.carshop.service;

import com.carshop.dto.request.CreateStockMovementRequest;
import com.carshop.dto.response.StockMovementResponse;
import com.carshop.entity.MovementType;
import com.carshop.entity.Product;
import com.carshop.entity.StockMovement;
import com.carshop.entity.StockMovement.MovementReason;
import com.carshop.exception.InventoryException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.StockMovementMapper;
import com.carshop.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing stock movements with automatic quantity updates.
 * Ensures ACID compliance when updating product inventory.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductService productService;
    private final StockMovementMapper stockMovementMapper;
    @Lazy
    private final StockAlertService stockAlertService;

    /**
     * Record stock movement and automatically update product quantity.
     * IN movements increase quantity, OUT movements decrease it.
     */
    @Transactional
    public StockMovementResponse recordStockMovement(CreateStockMovementRequest request) {
        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new InventoryException("Quantity must be positive");
        }

        // Get product
        Product product = productService.findById(request.getProductId());

        // Calculate new quantity
        int quantityChange = request.getType() == MovementType.IN 
                ? request.getQuantity() 
                : -request.getQuantity();

        int newQuantity = product.getQuantity() + quantityChange;

        // Validate OUT movement doesn't make quantity negative
        if (newQuantity < 0) {
            throw new InventoryException(
                    "Cannot remove " + request.getQuantity() + " units. " +
                    "Current quantity: " + product.getQuantity());
        }

        String currentUser = getCurrentUsername();

        // Create stock movement record
        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .reason(request.getReason())
                .quantity(request.getQuantity())
                .reference(request.getReference())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        StockMovement savedMovement = stockMovementRepository.save(movement);

        // Update product quantity - ATOMIC operation
        product.setQuantity(newQuantity);
        productService.updateProductQuantity(product.getId(), newQuantity);

        // Check for stock alerts after update
        product.setQuantity(newQuantity);
        stockAlertService.checkAndCreateAlert(product);

        log.info("Stock movement recorded: Product={}, Type={}, Reason={}, Quantity={}, NewQuantity={}",
                product.getId(), request.getType(), request.getReason(), request.getQuantity(), newQuantity);

        return stockMovementMapper.toResponse(savedMovement);
    }

    @Transactional(readOnly = true)
    public StockMovementResponse getStockMovementById(Long id) {
        StockMovement movement = findById(id);
        return stockMovementMapper.toResponse(movement);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getProductMovementHistory(Long productId) {
        // Verify product exists
        productService.findById(productId);
        
        List<StockMovement> movements = stockMovementRepository.findByProductId(productId);
        return movements.stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> getProductMovementHistoryPaginated(
            Long productId, Pageable pageable) {
        // Verify product exists
        productService.findById(productId);
        
        Page<StockMovement> movements = stockMovementRepository.findByProductId(productId, pageable);
        return movements.map(stockMovementMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByDateRange(
            LocalDateTime startDate, LocalDateTime endDate) {
        List<StockMovement> movements = stockMovementRepository.findByDateRange(startDate, endDate);
        return movements.stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovementsByTypeAndReason(
            MovementType type, MovementReason reason) {
        List<StockMovement> movements = stockMovementRepository.findByTypeAndReason(type, reason);
        return movements.stream()
                .map(stockMovementMapper::toResponse)
                .toList();
    }

    public StockMovement findById(Long id) {
        return stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
