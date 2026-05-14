package com.carshop.mapper;

import com.carshop.dto.response.AppointmentResponse;
import com.carshop.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .bookingId(appointment.getBooking().getId())
                .appointmentDate(appointment.getAppointmentDate())
                .timeSlot(appointment.getTimeSlot())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .createdBy(appointment.getCreatedBy())
                .build();
    }
}
