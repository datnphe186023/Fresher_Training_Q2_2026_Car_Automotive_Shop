package com.carshop.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceHistoryResponse {
    private Long id;
    private Long vehicleId;
    private String serviceName;
    private LocalDate serviceDate;
    private String technicianName;
    private String notes;
    private BigDecimal cost;
}
