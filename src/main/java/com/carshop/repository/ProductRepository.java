package com.carshop.repository;

import com.carshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    Page<Product> findByCategory(String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.reorderLevel")
    Page<Product> findAllLowStock(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE (:category IS NULL OR p.category = :category)")
    Page<Product> findAllByOptionalCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.reorderLevel")
    List<Product> findAllLowStockItems();

    @Query("SELECT COUNT(sm) > 0 FROM StockMovement sm WHERE sm.product.id = :productId")
    boolean hasStockMovements(@Param("productId") Long productId);
}
