package com.carshop.mapper;

import com.carshop.dto.response.BookingResponse;
import com.carshop.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    private final CustomerMapper customerMapper;
    private final ServiceMapper serviceMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final VehicleMapper vehicleMapper;

    public BookingMapper(CustomerMapper customerMapper, ServiceMapper serviceMapper,
                         TimeSlotMapper timeSlotMapper, VehicleMapper vehicleMapper) {
        this.customerMapper = customerMapper;
        this.serviceMapper = serviceMapper;
        this.timeSlotMapper = timeSlotMapper;
        this.vehicleMapper = vehicleMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) return null;
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .customer(customerMapper.toResponse(booking.getCustomer()))
                .service(serviceMapper.toResponse(booking.getService()))
                .timeSlot(timeSlotMapper.toResponse(booking.getTimeSlot()))
                .vehicle(vehicleMapper.toResponse(booking.getVehicle()))
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .discountPercent(booking.getDiscountPercent())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
