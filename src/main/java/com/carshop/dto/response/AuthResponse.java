package com.carshop.dto.response;

import lombok.*;

/**
 * Response DTO for authentication operations (login, register, refresh).
 * Contains JWT tokens and user information.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    /**
     * JWT access token for API authentication
     */
    private String accessToken;
    
    /**
     * Refresh token for obtaining new access tokens
     */
    private String refreshToken;
    
    /**
     * Token type (always "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /**
     * Access token expiry time in seconds
     */
    private Long expiresIn;
    
    /**
     * User information
     */
    private UserResponse user;
}
