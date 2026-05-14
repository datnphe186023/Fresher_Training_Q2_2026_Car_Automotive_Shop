package com.carshop.service;

import com.carshop.dto.request.CreateSupplierRequest;
import com.carshop.dto.response.SupplierResponse;
import com.carshop.entity.Supplier;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.SupplierMapper;
import com.carshop.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .address(request.getAddress())
                .build();
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        return supplierMapper.toResponse(findById(id));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, CreateSupplierRequest request) {
        Supplier supplier = findById(id);
        supplier.setName(request.getName());
        supplier.setContactPhone(request.getContactPhone());
        supplier.setContactEmail(request.getContactEmail());
        supplier.setAddress(request.getAddress());
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        findById(id);
        if (supplierRepository.hasAssociatedProducts(id) || supplierRepository.hasAssociatedPurchaseOrders(id)) {
            throw new IllegalStateException("Cannot delete supplier with associated products or purchase orders");
        }
        supplierRepository.deleteById(id);
    }

    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
    }
}
