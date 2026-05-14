package com.carshop.mapper;

import com.carshop.dto.response.ProductResponse;
import com.carshop.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final SupplierMapper supplierMapper;

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .sku(product.getSku())
                .quantity(product.getQuantity())
                .reorderLevel(product.getReorderLevel())
                .unitPrice(product.getUnitPrice())
                .supplier(supplierMapper.toResponse(product.getSupplier()))
                .lowStock(product.getQuantity() <= product.getReorderLevel())
                .build();
    }
}
