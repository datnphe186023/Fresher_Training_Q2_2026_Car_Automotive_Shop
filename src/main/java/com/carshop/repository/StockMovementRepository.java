package com.carshop.repository;

import com.carshop.entity.MovementType;
import com.carshop.entity.StockMovement;
import com.carshop.entity.StockMovement.MovementReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId ORDER BY sm.movementDate DESC")
    List<StockMovement> findByProductId(@Param("productId") Long productId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId ORDER BY sm.movementDate DESC")
    Page<StockMovement> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementDate BETWEEN :startDate AND :endDate ORDER BY sm.movementDate DESC")
    List<StockMovement> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.type = :type AND sm.reason = :reason ORDER BY sm.movementDate DESC")
    List<StockMovement> findByTypeAndReason(
            @Param("type") MovementType type,
            @Param("reason") MovementReason reason);

    @Query("SELECT SUM(CASE WHEN sm.type = 'IN' THEN sm.quantity ELSE -sm.quantity END) FROM StockMovement sm WHERE sm.product.id = :productId")
    Integer calculateNetQuantity(@Param("productId") Long productId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id = :productId AND sm.movementDate BETWEEN :startDate AND :endDate ORDER BY sm.movementDate DESC")
    Page<StockMovement> findProductMovementsByDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
