package com.carshop.service;

import com.carshop.entity.Customer;
import com.carshop.repository.CustomerRepository;
import com.carshop.util.PhoneNumberValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing customer records in the guest booking system.
 * Handles customer creation, retrieval, and data updates based on phone number.
 * 
 * Validates: Requirements 9.4, 12.1, 12.2, 12.3, 12.4
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    /**
     * Finds an existing customer by phone number or creates a new one.
     * 
     * Business rules:
     * - Phone number is normalized before lookup
     * - If customer exists, returns existing record
     * - If customer doesn't exist, creates new record with provided data
     * - Phone number is the primary identifier
     * 
     * @param phoneNumber the customer's phone number (will be normalized)
     * @param email the customer's email (optional, can be null)
     * @param name the customer's name (optional, can be null)
     * @return the existing or newly created Customer entity
     * @throws IllegalArgumentException if phone number is invalid
     */
    @Transactional
    public Customer findOrCreateCustomer(String phoneNumber, String email, String name) {
        log.debug("Finding or creating customer with phone: {}", phoneNumber);
        
        // Normalize phone number
        String normalizedPhone = PhoneNumberValidator.normalize(phoneNumber);
        log.debug("Normalized phone number: {}", normalizedPhone);
        
        // Try to find existing customer
        return customerRepository.findByPhoneNumber(normalizedPhone)
                .map(existingCustomer -> {
                    log.info("Found existing customer with phone: {}", normalizedPhone);
                    // Update customer info if provided
                    return updateCustomerInfo(existingCustomer, email, name);
                })
                .orElseGet(() -> {
                    log.info("Creating new customer with phone: {}", normalizedPhone);
                    // Create new customer
                    Customer newCustomer = Customer.builder()
                            .phoneNumber(normalizedPhone)
                            .email(email)
                            .name(name)
                            .build();
                    
                    Customer savedCustomer = customerRepository.save(newCustomer);
                    log.info("Customer created successfully with ID: {}", savedCustomer.getId());
                    return savedCustomer;
                });
    }
    
    /**
     * Updates customer information (email and name) only if not already set.
     * 
     * Business rules:
     * - Only updates email if customer's email is null and new email is provided
     * - Only updates name if customer's name is null and new name is provided
     * - Does not overwrite existing email or name
     * - Saves changes to database if any updates are made
     * 
     * @param customer the customer entity to update
     * @param email the new email (optional, can be null)
     * @param name the new name (optional, can be null)
     * @return the updated customer entity
     */
    @Transactional
    public Customer updateCustomerInfo(Customer customer, String email, String name) {
        boolean updated = false;
        
        // Update email only if not already set
        if (customer.getEmail() == null && email != null && !email.trim().isEmpty()) {
            log.debug("Updating customer {} email to: {}", customer.getId(), email);
            customer.setEmail(email);
            updated = true;
        }
        
        // Update name only if not already set
        if (customer.getName() == null && name != null && !name.trim().isEmpty()) {
            log.debug("Updating customer {} name to: {}", customer.getId(), name);
            customer.setName(name);
            updated = true;
        }
        
        // Save if any updates were made
        if (updated) {
            customer = customerRepository.save(customer);
            log.info("Customer {} information updated successfully", customer.getId());
        } else {
            log.debug("No updates needed for customer {}", customer.getId());
        }
        
        return customer;
    }
}
