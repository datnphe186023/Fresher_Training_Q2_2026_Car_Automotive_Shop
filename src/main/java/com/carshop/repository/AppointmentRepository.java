package com.carshop.repository;

import com.carshop.entity.Appointment;
import com.carshop.entity.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByBookingId(Long bookingId);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = :date AND a.timeSlot = :timeSlot AND a.status != 'CANCELLED'")
    List<Appointment> findConflictingAppointments(
            @Param("date") LocalDateTime date,
            @Param("timeSlot") String timeSlot);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate ASC")
    List<Appointment> findAppointmentsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status ORDER BY a.appointmentDate DESC")
    List<Appointment> findByStatus(@Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.appointmentDate = :date AND a.timeSlot = :timeSlot AND a.status IN ('SCHEDULED', 'CONFIRMED')")
    boolean isTimeSlotBooked(@Param("date") LocalDateTime date, @Param("timeSlot") String timeSlot);

    @Query("""
            SELECT a FROM Appointment a
            JOIN FETCH a.booking b
            JOIN FETCH b.customer c
            WHERE a.appointmentDate >= :start
              AND a.appointmentDate < :end
              AND a.status IN ('SCHEDULED', 'CONFIRMED')
            ORDER BY a.appointmentDate ASC
            """)
    List<Appointment> findReminderCandidates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
