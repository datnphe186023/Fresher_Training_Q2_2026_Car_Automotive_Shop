package com.carshop.repository;

import com.carshop.entity.Payment;
import com.carshop.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReferenceCode(String referenceCode);

    List<Payment> findByInvoice_IdOrderByPaymentDateDesc(Long invoiceId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.id = :invoiceId AND p.status = 'COMPLETED'")
    BigDecimal sumCompletedPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.referenceCode = :referenceCode")
    boolean existsByReferenceCode(@Param("referenceCode") String referenceCode);

    List<Payment> findByStatusOrderByPaymentDateDesc(PaymentStatus status);

        @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.status = 'COMPLETED'
              AND p.paymentDate >= :start
              AND p.paymentDate < :end
            """)
        BigDecimal sumCompletedPaymentsInRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

        @Query("""
            SELECT COUNT(p)
            FROM Payment p
            WHERE p.status = 'COMPLETED'
              AND p.paymentDate >= :start
              AND p.paymentDate < :end
            """)
        long countCompletedPaymentsInRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
