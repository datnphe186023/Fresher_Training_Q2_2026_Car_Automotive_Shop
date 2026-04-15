package com.carshop.mapper;

import com.carshop.dto.response.BookingResponse;
import com.carshop.entity.Booking;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Booking entity and BookingResponse DTO.
 */
@Component
public class BookingMapper {
    
    private final CustomerMapper customerMapper;
    private final ServiceMapper serviceMapper;
    
    public BookingMapper(CustomerMapper customerMapper, ServiceMapper serviceMapper) {
        this.customerMapper = customerMapper;
        this.serviceMapper = serviceMapper;
    }
    
    /**
     * Converts a Booking entity to a BookingResponse DTO.
     * 
     * @param booking the Booking entity to convert
     * @return BookingResponse DTO with booking information
     */
    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }
        
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .customer(customerMapper.toResponse(booking.getCustomer()))
                .service(serviceMapper.toResponse(booking.getService()))
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
