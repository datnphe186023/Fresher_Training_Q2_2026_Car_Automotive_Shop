package com.carshop.service;

import com.carshop.dto.request.CreateAppointmentRequest;
import com.carshop.dto.response.AppointmentResponse;
import com.carshop.entity.Appointment;
import com.carshop.entity.Booking;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.exception.TimeSlotConflictException;
import com.carshop.mapper.AppointmentMapper;
import com.carshop.repository.AppointmentRepository;
import com.carshop.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing appointments with time slot conflict detection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BookingRepository bookingRepository;
    private final AppointmentMapper appointmentMapper;

    /**
     * Create appointment with conflict detection.
     * Throws TimeSlotConflictException if slot is already booked.
     */
    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        // Check if time slot is available
        if (appointmentRepository.isTimeSlotBooked(request.getAppointmentDate(), request.getTimeSlot())) {
            throw new TimeSlotConflictException(
                    "Time slot " + request.getTimeSlot() + 
                    " is already booked on " + request.getAppointmentDate());
        }

        // Get booking and verify it exists
        Booking booking = bookingRepository.findById(request.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Booking not found with id: " + request.getBookingId()));

        // Check if booking already has appointment
        if (appointmentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new IllegalStateException("Booking already has an appointment");
        }

        String currentUser = getCurrentUsername();
        
        Appointment appointment = Appointment.builder()
                .booking(booking)
                .appointmentDate(request.getAppointmentDate())
                .timeSlot(request.getTimeSlot())
                .status(Appointment.AppointmentStatus.SCHEDULED)
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment created for booking: {}", request.getBookingId());
        return appointmentMapper.toResponse(savedAppointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = findById(id);
        return appointmentMapper.toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment appointment = findById(id);
        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} status updated to: {}", id, status);
        return appointmentMapper.toResponse(updated);
    }

    /**
     * Cancel appointment and release time slot.
     * Called when booking is cancelled.
     */
    @Transactional
    public void cancelAppointment(Long bookingId) {
        appointmentRepository.findByBookingId(bookingId).ifPresent(appointment -> {
            appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            log.info("Appointment cancelled for booking: {}", bookingId);
        });
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Appointment> appointments = appointmentRepository.findAppointmentsByDateRange(startDate, endDate);
        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        List<Appointment> appointments = appointmentRepository.findByStatus(status);
        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
