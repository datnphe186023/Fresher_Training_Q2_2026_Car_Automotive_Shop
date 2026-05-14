package com.carshop.repository;

import com.carshop.entity.PurchaseOrder;
import com.carshop.entity.PurchaseOrder.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId ORDER BY po.orderDate DESC")
    Page<PurchaseOrder> findBySupplier(@Param("supplierId") Long supplierId, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.status = :status ORDER BY po.orderDate DESC")
    Page<PurchaseOrder> findByStatus(@Param("status") PurchaseOrderStatus status, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findByOrderDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.id = :supplierId AND po.status IN ('PENDING', 'ORDERED')")
    List<PurchaseOrder> findActiveOrdersBySupplier(@Param("supplierId") Long supplierId);

    @Query("SELECT COUNT(po) > 0 FROM PurchaseOrder po WHERE po.supplier.id = :supplierId AND po.status IN ('PENDING', 'ORDERED', 'PARTIALLY_RECEIVED')")
    boolean hasActivePurchaseOrders(@Param("supplierId") Long supplierId);
}
