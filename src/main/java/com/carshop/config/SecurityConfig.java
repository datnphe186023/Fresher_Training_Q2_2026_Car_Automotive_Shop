package com.carshop.config;

import com.carshop.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the Car Shop application.
 * Configures JWT-based authentication, authorization rules, CORS, and password encoding.
 * 
 * Validates: Requirements 17.1, 17.2, 17.3, 17.4, 17.5, 17.6, 17.7, 17.8, 17.9, 17.10, 17.12
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    
    /**
     * Configures the security filter chain with JWT authentication and authorization rules.
     * 
     * Public endpoints (no authentication required):
     * - /api/auth/** (register, login, refresh)
     * - GET /api/services/**
     * - GET /api/categories/**
     * - POST /api/bookings (guest booking creation)
     * - GET /api/bookings/track (guest order tracking)
     * 
     * Protected endpoints (ADMIN/STAFF authentication required):
     * - POST/PUT/DELETE /api/services/**
     * - POST/PUT/DELETE /api/categories/**
     * 
     * @param http the HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API (Requirement 17.2)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS (Requirement 17.3)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public authentication endpoints (Requirement 17.4)
                .requestMatchers("/api/auth/**").permitAll()
                
                // Public service endpoints - GET only (Requirement 17.5)
                .requestMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                
                // Public category endpoints - GET only (Requirement 17.6)
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Public booking creation endpoint (Requirement 17.7)
                .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
                
                // Public booking tracking endpoint (Requirement 17.8)
                .requestMatchers(HttpMethod.GET, "/api/bookings/track").permitAll()

                // Public endpoints for guest vehicle/history/loyalty lookup
                .requestMatchers("/api/public/**").permitAll()

                // Protected service management endpoints (Requirement 17.9)
                .requestMatchers(HttpMethod.POST, "/api/services/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasAnyRole("ADMIN", "STAFF")
                
                // Protected category management endpoints (Requirement 17.9)
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAnyRole("ADMIN", "STAFF")

                // Vehicle endpoints
                .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/vehicles/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/vehicles/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")

                // Booking status update
                .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/status").hasAnyRole("ADMIN", "STAFF")

                // Time slot management
                .requestMatchers(HttpMethod.GET, "/api/time-slots/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/time-slots/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/time-slots/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.DELETE, "/api/time-slots/**").hasRole("ADMIN")

                // Supplier management
                .requestMatchers(HttpMethod.GET, "/api/suppliers/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/suppliers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/suppliers/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/suppliers/**").hasRole("ADMIN")

                // Product management
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                
                // All other endpoints require authentication (Requirement 17.10)
                .anyRequest().authenticated()
            )
            
            // Stateless session management for JWT (Requirement 17.1)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Set authentication provider
            .authenticationProvider(authenticationProvider())
            
            // Add JWT authentication filter (Requirement 17.11)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configures CORS to allow requests from specified origins.
     * 
     * @return CorsConfigurationSource with allowed origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from frontend origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React development server
            "http://localhost:5173",  // Vite development server
            "http://localhost:4200"   // Angular development server
        ));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Creates a BCryptPasswordEncoder bean with strength 10.
     * 
     * @return PasswordEncoder for hashing passwords (Requirement 17.12)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
    
    /**
     * Creates an AuthenticationProvider that uses UserDetailsService and PasswordEncoder.
     * 
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    /**
     * Exposes the AuthenticationManager bean for use in authentication services.
     * 
     * @param config the AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
