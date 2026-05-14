package com.carshop.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private String sku;
    private Integer quantity;
    private Integer reorderLevel;
    private BigDecimal unitPrice;
    private SupplierResponse supplier;
    private boolean lowStock;
}
