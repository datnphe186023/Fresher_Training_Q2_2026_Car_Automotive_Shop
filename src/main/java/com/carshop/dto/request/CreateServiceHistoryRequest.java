package com.carshop.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceHistoryRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Service date is required")
    private LocalDate serviceDate;

    private String technicianName;

    private String notes;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    private BigDecimal cost;
}
