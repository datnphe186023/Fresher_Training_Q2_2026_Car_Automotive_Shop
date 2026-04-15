package com.carshop.mapper;

import com.carshop.dto.response.UserResponse;
import com.carshop.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between User entity and UserResponse DTO.
 * Excludes sensitive data like password from responses.
 */
@Component
public class UserMapper {
    
    /**
     * Converts a User entity to a UserResponse DTO.
     * 
     * @param user the User entity to convert
     * @return UserResponse DTO with user information (excluding password)
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
