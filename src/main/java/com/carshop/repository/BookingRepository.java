package com.carshop.repository;

import com.carshop.entity.Booking;
import com.carshop.entity.BookingStatus;
import com.carshop.entity.Customer;
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
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomer(Customer customer);

    boolean existsByTimeSlot_IdAndStatusNot(Long timeSlotId, BookingStatus status);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.customer c
        JOIN FETCH b.service s
        WHERE (:status IS NULL OR b.status = :status)
          AND (:startDate IS NULL OR b.bookingDate >= :startDate)
          AND (:endDate IS NULL OR b.bookingDate <= :endDate)
          AND (:phoneNumber IS NULL OR c.phoneNumber = :phoneNumber)
        ORDER BY b.createdAt DESC
        """)
    Page<Booking> findAllWithFilters(
            @Param("status") BookingStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable);

        @Query("""
          SELECT COUNT(b)
          FROM Booking b
          WHERE b.status = 'COMPLETED'
            AND b.createdAt >= :start
            AND b.createdAt < :end
          """)
        long countCompletedInRange(
          @Param("start") LocalDateTime start,
          @Param("end") LocalDateTime end);
}
