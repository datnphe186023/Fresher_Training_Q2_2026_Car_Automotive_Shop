package com.carshop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a service history record for a vehicle.
 */
@Entity
@Table(name = "service_history", indexes = {
    @Index(name = "idx_sh_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_sh_service_date", columnList = "service_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"vehicle", "service"})
public class ServiceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonBackReference("vehicle-history")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @NotNull(message = "Service date is required")
    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "technician_name", length = 100)
    private String technicianName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotNull(message = "Cost is required")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;
}
