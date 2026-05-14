package com.carshop.service;

import com.carshop.dto.response.BookingResponse;
import com.carshop.entity.*;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.BookingMapper;
import com.carshop.repository.BookingRepository;
import com.carshop.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingStatusUpdateTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private CustomerService customerService;
    @Mock private BookingMapper bookingMapper;
    @InjectMocks private BookingService bookingService;

    private Customer customer;
    private Service service;
    private Booking booking;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).phoneNumber("0901234567").loyaltyPoints(0).build();
        service = Service.builder().id(1L).name("Film Installation")
                .basePrice(BigDecimal.valueOf(500000)).build();
        booking = Booking.builder().id(1L).customer(customer).service(service)
                .bookingReference("REF001").status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now().plusDays(1)).build();
        bookingResponse = BookingResponse.builder().id(1L).bookingReference("REF001").build();
    }

    @Test
    void updateBookingStatus_ToCompleted_AwardsLoyaltyPoints() {
        // 500000 / 10000 = 50 points
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

        bookingService.updateBookingStatus(1L, BookingStatus.COMPLETED);

        verify(customerService).addLoyaltyPoints(1L, 50);
        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void updateBookingStatus_AlreadyCompleted_ThrowsDuplicateResourceException() {
        booking.setStatus(BookingStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateBookingStatus(1L, BookingStatus.COMPLETED))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Booking is already completed");

        verify(customerService, never()).addLoyaltyPoints(anyLong(), anyInt());
    }

    @Test
    void updateBookingStatus_ToCancelled_DoesNotAwardPoints() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

        bookingService.updateBookingStatus(1L, BookingStatus.CANCELLED);

        verify(customerService, never()).addLoyaltyPoints(anyLong(), anyInt());
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void updateBookingStatus_ToInProgress_DoesNotAwardPoints() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

        bookingService.updateBookingStatus(1L, BookingStatus.IN_PROGRESS);

        verify(customerService, never()).addLoyaltyPoints(anyLong(), anyInt());
    }

    @Test
    void updateBookingStatus_WithNonExistentBooking_ThrowsResourceNotFoundException() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBookingStatus(999L, BookingStatus.COMPLETED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Booking not found");
    }

    @Test
    void updateBookingStatus_LoyaltyPointsCalculation_FloorDivision() {
        // 150000 / 10000 = 15 points (floor)
        service.setBasePrice(BigDecimal.valueOf(150000));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingMapper.toResponse(any())).thenReturn(bookingResponse);

        bookingService.updateBookingStatus(1L, BookingStatus.COMPLETED);

        verify(customerService).addLoyaltyPoints(1L, 15);
    }
}
