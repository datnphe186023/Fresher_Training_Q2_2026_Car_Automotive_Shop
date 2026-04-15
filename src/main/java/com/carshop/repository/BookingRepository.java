package com.carshop.repository;

import com.carshop.entity.Booking;
import com.carshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Booking entity operations.
 * Provides CRUD operations and custom query methods for booking management.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    /**
     * Find a booking by its unique booking reference.
     * Used for guest order tracking.
     *
     * @param bookingReference the booking reference to search for
     * @return Optional containing the booking if found, empty otherwise
     */
    Optional<Booking> findByBookingReference(String bookingReference);
    
    /**
     * Find all bookings for a specific customer.
     * Used for retrieving customer booking history.
     *
     * @param customer the customer whose bookings to retrieve
     * @return List of bookings for the customer, empty list if none found
     */
    List<Booking> findByCustomer(Customer customer);
}
