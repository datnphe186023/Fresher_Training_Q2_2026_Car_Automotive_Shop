package com.carshop.controller;

import com.carshop.dto.request.LoginRequest;
import com.carshop.dto.request.RefreshTokenRequest;
import com.carshop.dto.request.RegisterRequest;
import com.carshop.dto.response.AuthResponse;
import com.carshop.dto.response.UserResponse;
import com.carshop.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user registration, login, and token refresh for ADMIN and STAFF users.
 * 
 * Validates: Requirements 5.1, 5.3, 5.4, 6.1, 6.6, 7.1, 7.4
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Registers a new ADMIN or STAFF user.
     * 
     * @param request the registration request containing username, email, password, and role
     * @return HTTP 201 with UserResponse on success
     * @throws DuplicateResourceException if username or email already exists (HTTP 409)
     * @throws InvalidRoleException if role is not ADMIN or STAFF (HTTP 400)
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for username: {}", request.getUsername());
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse.getUser());
    }
    
    /**
     * Authenticates a user and returns JWT tokens.
     * 
     * @param request the login request containing username and password
     * @return HTTP 200 with AuthResponse containing tokens and user information
     * @throws InvalidCredentialsException if credentials are invalid (HTTP 401)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for username: {}", request.getUsername());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }
    
    /**
     * Refreshes an access token using a valid refresh token.
     * 
     * @param request the refresh token request containing the refresh token
     * @return HTTP 200 with AuthResponse containing new access token
     * @throws InvalidTokenException if refresh token is invalid or expired (HTTP 401)
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received token refresh request");
        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(authResponse);
    }
}
