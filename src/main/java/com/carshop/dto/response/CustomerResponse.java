package com.carshop.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for customer information.
 * Contains customer details for guest bookings.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    
    /**
     * Customer ID
     */
    private Long id;
    
    /**
     * Customer phone number (primary identifier)
     */
    private String phoneNumber;
    
    /**
     * Customer email address
     */
    private String email;
    
    /**
     * Customer name
     */
    private String name;

    /**
     * Customer address
     */
    private String address;

    /**
     * Customer loyalty points balance
     */
    private Integer loyaltyPoints;
    
    /**
     * Customer record creation timestamp
     */
    private LocalDateTime createdAt;
}
