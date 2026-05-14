package com.carshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String name;

    private String contactPhone;

    private String contactEmail;

    private String address;
}
