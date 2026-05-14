package com.carshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_sm_product_id", columnList = "product_id"),
    @Index(name = "idx_sm_movement_date", columnList = "movement_date"),
    @Index(name = "idx_sm_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "product")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementReason reason;

    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "movement_date", nullable = false, updatable = false)
    private LocalDateTime movementDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum MovementReason {
        PURCHASE, USAGE, ADJUSTMENT, DAMAGED, RETURN, OTHER
    }
}
