package com.carshop.dto.response;

import com.carshop.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO for user information.
 * Excludes sensitive data like password.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    /**
     * User ID
     */
    private Long id;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Email address
     */
    private String email;
    
    /**
     * User role (ADMIN or STAFF)
     */
    private Role role;
    
    /**
     * Account creation timestamp
     */
    private LocalDateTime createdAt;
}
