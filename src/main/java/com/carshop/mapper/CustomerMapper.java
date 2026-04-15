package com.carshop.mapper;

import com.carshop.dto.response.CustomerResponse;
import com.carshop.entity.Customer;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Customer entity and CustomerResponse DTO.
 */
@Component
public class CustomerMapper {
    
    /**
     * Converts a Customer entity to a CustomerResponse DTO.
     * 
     * @param customer the Customer entity to convert
     * @return CustomerResponse DTO with customer information
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        return CustomerResponse.builder()
                .id(customer.getId())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .name(customer.getName())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
