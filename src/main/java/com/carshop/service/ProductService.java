package com.carshop.service;

import com.carshop.dto.request.CreateProductRequest;
import com.carshop.dto.response.ProductResponse;
import com.carshop.entity.Product;
import com.carshop.entity.Supplier;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.ProductMapper;
import com.carshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierService supplierService;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU already exists");
        }
        Supplier supplier = resolveSupplier(request.getSupplierId());
        Product product = Product.builder()
                .name(request.getName())
                .category(request.getCategory())
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .reorderLevel(request.getReorderLevel())
                .unitPrice(request.getUnitPrice())
                .supplier(supplier)
                .build();
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String category, boolean lowStock, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        if (lowStock) {
            return productRepository.findAllLowStock(pageable).map(productMapper::toResponse);
        }
        return productRepository.findAllByOptionalCategory(category, pageable).map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return productMapper.toResponse(findById(id));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = findById(id);
        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("SKU already exists");
        }
        Supplier supplier = resolveSupplier(request.getSupplierId());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setSku(request.getSku());
        product.setQuantity(request.getQuantity());
        product.setReorderLevel(request.getReorderLevel());
        product.setUnitPrice(request.getUnitPrice());
        product.setSupplier(supplier);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        findById(id);
        if (productRepository.hasStockMovements(id)) {
            throw new IllegalStateException("Cannot delete product with existing stock movements");
        }
        productRepository.deleteById(id);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public void updateProductQuantity(Long productId, Integer newQuantity) {
        Product product = findById(productId);
        if (newQuantity < 0) {
            throw new IllegalStateException("Product quantity cannot be negative");
        }
        product.setQuantity(newQuantity);
        productRepository.save(product);
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) return null;
        return supplierService.findById(supplierId);
    }
}
