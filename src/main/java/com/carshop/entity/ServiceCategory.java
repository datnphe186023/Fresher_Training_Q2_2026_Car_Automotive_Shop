package com.carshop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a service category that groups related car enhancement services.
 * Categories help organize the service catalog for easier browsing.
 */
@Entity
@Table(name = "service_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "services")
public class ServiceCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Column(unique = true, nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Service> services = new ArrayList<>();
    
    /**
     * Helper method to add a service to this category
     */
    public void addService(Service service) {
        services.add(service);
        service.setCategory(this);
    }
    
    /**
     * Helper method to remove a service from this category
     */
    public void removeService(Service service) {
        services.remove(service);
        service.setCategory(null);
    }
}
