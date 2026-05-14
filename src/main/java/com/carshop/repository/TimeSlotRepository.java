package com.carshop.repository;

import com.carshop.entity.BookingStatus;
import com.carshop.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    boolean existsByDateAndStartTime(LocalDate date, LocalTime startTime);

    List<TimeSlot> findAllByDate(LocalDate date);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.timeSlot.id = :timeSlotId AND b.status <> :status")
    boolean existsActiveBookingForTimeSlot(@Param("timeSlotId") Long timeSlotId,
                                           @Param("status") BookingStatus status);
}
