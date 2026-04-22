package com.carshop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a vehicle owned by a customer.
 */
@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicle_customer_id", columnList = "customer_id"),
    @Index(name = "idx_vehicle_plate_number", columnList = "plate_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"customer", "serviceHistories"})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("customer-vehicles")
    private Customer customer;

    @NotBlank(message = "Brand is required")
    @Column(nullable = false, length = 100)
    private String brand;

    @NotBlank(message = "Model is required")
    @Column(nullable = false, length = 100)
    private String model;

    @NotNull(message = "Year is required")
    @Column(nullable = false)
    private Integer year;

    @NotBlank(message = "Plate number is required")
    @Column(name = "plate_number", unique = true, nullable = false, length = 20)
    private String plateNumber;

    @Column(length = 50)
    private String color;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("vehicle-history")
    private List<ServiceHistory> serviceHistories = new ArrayList<>();
}
