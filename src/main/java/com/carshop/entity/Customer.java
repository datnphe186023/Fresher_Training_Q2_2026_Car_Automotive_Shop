package com.carshop.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer who has made a booking.
 * Customers are identified by phone number and can book services without registration.
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_phone_number", columnList = "phone_number"),
    @Index(name = "idx_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "bookings")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", unique = true, nullable = false, length = 20)
    private String phoneNumber;
    
    @Email(message = "Invalid email format")
    @Column(length = 100)
    private String email;
    
    @Column(length = 100)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(name = "loyalty_points", nullable = false)
    @Builder.Default
    private Integer loyaltyPoints = 0;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("customer-vehicles")
    private List<Vehicle> vehicles = new ArrayList<>();
    
    /**
     * Helper method to add a booking to this customer
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setCustomer(this);
    }
    
    /**
     * Helper method to remove a booking from this customer
     */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setCustomer(null);
    }
}
