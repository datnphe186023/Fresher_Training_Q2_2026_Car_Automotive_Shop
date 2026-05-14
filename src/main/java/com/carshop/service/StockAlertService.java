package com.carshop.service;

import com.carshop.dto.response.StockAlertResponse;
import com.carshop.entity.Product;
import com.carshop.entity.StockAlert;
import com.carshop.entity.StockAlert.AlertStatus;
import com.carshop.entity.StockAlert.AlertType;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.StockAlertMapper;
import com.carshop.repository.StockAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing stock alerts.
 * Auto-generates alerts when product quantity falls below reorder level.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockAlertService {

    private final StockAlertRepository stockAlertRepository;
    private final StockAlertMapper stockAlertMapper;

    /**
     * Auto-generate or update alert based on product quantity.
     * Called after stock movement or product update.
     */
    @Transactional
    public void checkAndCreateAlert(Product product) {
        if (product.getQuantity() <= 0) {
            createOrUpdateAlert(product, AlertType.OUT_OF_STOCK);
        } else if (product.getQuantity() <= product.getReorderLevel()) {
            createOrUpdateAlert(product, AlertType.LOW_STOCK);
        } else {
            // Resolve alert if quantity is now sufficient
            resolveAlertIfExists(product.getId());
        }
    }

    private void createOrUpdateAlert(Product product, AlertType alertType) {
        var existingAlert = stockAlertRepository.findActiveAlertByProductId(product.getId());

        if (existingAlert.isPresent()) {
            StockAlert alert = existingAlert.get();
            // Update alert type if changed (e.g., LOW_STOCK -> OUT_OF_STOCK)
            if (alert.getAlertType() != alertType) {
                alert.setAlertType(alertType);
                stockAlertRepository.save(alert);
                log.info("Alert updated for product {}: {}", product.getId(), alertType);
            }
        } else {
            // Create new alert
            StockAlert newAlert = StockAlert.builder()
                    .product(product)
                    .alertType(alertType)
                    .status(AlertStatus.ACTIVE)
                    .build();
            stockAlertRepository.save(newAlert);
            log.info("New alert created for product {}: {}", product.getId(), alertType);
        }
    }

    @Transactional
    public StockAlertResponse resolveAlert(Long alertId) {
        StockAlert alert = findById(alertId);
        String currentUser = getCurrentUsername();

        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(currentUser);

        StockAlert resolved = stockAlertRepository.save(alert);
        log.info("Alert {} resolved by {}", alertId, currentUser);
        return stockAlertMapper.toResponse(resolved);
    }

    @Transactional
    public StockAlertResponse ignoreAlert(Long alertId, String reason) {
        StockAlert alert = findById(alertId);
        alert.setStatus(AlertStatus.IGNORED);
        alert.setNotes(reason);
        StockAlert updated = stockAlertRepository.save(alert);
        log.info("Alert {} ignored", alertId);
        return stockAlertMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public StockAlertResponse getAlertById(Long id) {
        StockAlert alert = findById(id);
        return stockAlertMapper.toResponse(alert);
    }

    @Transactional(readOnly = true)
    public Page<StockAlertResponse> getAlertsByStatus(AlertStatus status, Pageable pageable) {
        Page<StockAlert> alerts = stockAlertRepository.findByStatus(status, pageable);
        return alerts.map(stockAlertMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> getActiveAlertsByType(AlertType alertType) {
        List<StockAlert> alerts = stockAlertRepository.findActiveAlertsByType(alertType);
        return alerts.stream()
                .map(stockAlertMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> getAlertsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<StockAlert> alerts = stockAlertRepository.findAlertsByDateRange(startDate, endDate);
        return alerts.stream()
                .map(stockAlertMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long countActiveAlerts() {
        return stockAlertRepository.countActiveAlerts();
    }

    public StockAlert findById(Long id) {
        return stockAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock alert not found with id: " + id));
    }

    private void resolveAlertIfExists(Long productId) {
        var existingAlert = stockAlertRepository.findActiveAlertByProductId(productId);
        if (existingAlert.isPresent()) {
            StockAlert alert = existingAlert.get();
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy("SYSTEM");
            stockAlertRepository.save(alert);
            log.info("Alert auto-resolved for product {}", productId);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
