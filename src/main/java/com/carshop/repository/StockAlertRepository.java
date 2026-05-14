package com.carshop.repository;

import com.carshop.entity.StockAlert;
import com.carshop.entity.StockAlert.AlertStatus;
import com.carshop.entity.StockAlert.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    @Query("SELECT a FROM StockAlert a WHERE a.product.id = :productId AND a.status = 'ACTIVE'")
    Optional<StockAlert> findActiveAlertByProductId(@Param("productId") Long productId);

    @Query("SELECT a FROM StockAlert a WHERE a.status = :status ORDER BY a.createdAt DESC")
    Page<StockAlert> findByStatus(@Param("status") AlertStatus status, Pageable pageable);

    @Query("SELECT a FROM StockAlert a WHERE a.alertType = :alertType AND a.status = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<StockAlert> findActiveAlertsByType(@Param("alertType") AlertType alertType);

    @Query("SELECT a FROM StockAlert a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<StockAlert> findAlertsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM StockAlert a WHERE a.status = 'ACTIVE'")
    Long countActiveAlerts();
}
