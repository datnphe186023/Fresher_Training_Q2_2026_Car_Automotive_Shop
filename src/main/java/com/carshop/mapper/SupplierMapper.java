package com.carshop.mapper;

import com.carshop.dto.response.SupplierResponse;
import com.carshop.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {
        if (supplier == null) return null;
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactPhone(supplier.getContactPhone())
                .contactEmail(supplier.getContactEmail())
                .address(supplier.getAddress())
                .build();
    }
}
