package com.carshop.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private String brand;
    private String model;
    private Integer year;
    private String plateNumber;
    private String color;
}
