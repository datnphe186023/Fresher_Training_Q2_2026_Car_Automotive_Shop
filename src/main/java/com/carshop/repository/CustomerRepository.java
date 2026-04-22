package com.carshop.repository;

import com.carshop.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Customer entity operations.
 * Provides CRUD operations and custom query methods for customer management.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * Find a customer by phone number.
     * Used for customer lookup during booking creation and tracking.
     *
     * @param phoneNumber the phone number to search for
     * @return Optional containing the customer if found, empty otherwise
     */
    Optional<Customer> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find a customer by email address.
     * Used for optional customer lookup by email.
     *
     * @param email the email address to search for
     * @return Optional containing the customer if found, empty otherwise
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find a customer by phone number with vehicles eagerly fetched.
     */
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.vehicles WHERE c.phoneNumber = :phoneNumber")
    Optional<Customer> findByPhoneNumberWithVehicles(@Param("phoneNumber") String phoneNumber);
}
