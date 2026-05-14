package com.carshop.repository;

import com.carshop.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.supplier.id = :supplierId")
    boolean hasAssociatedProducts(@Param("supplierId") Long supplierId);

    @Query("SELECT COUNT(po) > 0 FROM PurchaseOrder po WHERE po.supplier.id = :supplierId")
    boolean hasAssociatedPurchaseOrders(@Param("supplierId") Long supplierId);
}
