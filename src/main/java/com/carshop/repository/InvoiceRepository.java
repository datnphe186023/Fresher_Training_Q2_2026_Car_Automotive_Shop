package com.carshop.repository;

import com.carshop.entity.Invoice;
import com.carshop.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByBooking_Id(Long bookingId);

    boolean existsByBooking_Id(Long bookingId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.customer JOIN FETCH i.booking WHERE i.customer.phoneNumber = :phoneNumber ORDER BY i.issueDate DESC")
    List<Invoice> findByCustomerPhoneNumberOrderByIssueDateDesc(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT i FROM Invoice i WHERE (:status IS NULL OR i.status = :status) ORDER BY i.issueDate DESC")
    Page<Invoice> findAllWithStatus(@Param("status") InvoiceStatus status, Pageable pageable);

        @Query("""
                        SELECT i FROM Invoice i
                        JOIN FETCH i.customer c
                        JOIN FETCH i.booking b
                        WHERE i.dueDate < :asOf
                            AND i.status IN ('ISSUED', 'PARTIALLY_PAID')
                        ORDER BY i.dueDate ASC
                        """)
        List<Invoice> findOverdueCandidates(@Param("asOf") LocalDateTime asOf);
}
