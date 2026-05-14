package com.carshop.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierResponse {
    private Long id;
    private String name;
    private String contactPhone;
    private String contactEmail;
    private String address;
}
