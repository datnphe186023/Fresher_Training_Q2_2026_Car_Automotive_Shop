package com.carshop.service;

import com.carshop.dto.request.CreateVehicleRequest;
import com.carshop.dto.request.UpdateVehicleRequest;
import com.carshop.dto.response.VehicleResponse;
import com.carshop.entity.Customer;
import com.carshop.entity.Vehicle;
import com.carshop.exception.DuplicateResourceException;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.VehicleMapper;
import com.carshop.repository.CustomerRepository;
import com.carshop.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private VehicleMapper vehicleMapper;
    @InjectMocks private VehicleService vehicleService;

    private Customer customer;
    private Vehicle vehicle;
    private VehicleResponse vehicleResponse;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).name("John").phoneNumber("0901234567").build();
        vehicle = Vehicle.builder().id(1L).customer(customer).brand("Toyota")
                .model("Camry").year(2020).plateNumber("51A-12345").color("White").build();
        vehicleResponse = VehicleResponse.builder().id(1L).customerId(1L).brand("Toyota")
                .model("Camry").year(2020).plateNumber("51A-12345").color("White").build();
    }

    @Test
    void createVehicle_WithValidData_ReturnsVehicleResponse() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(1L).brand("Toyota").model("Camry").year(2020)
                .plateNumber("51A-12345").color("White").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByPlateNumber("51A-12345")).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(vehicleResponse);

        VehicleResponse result = vehicleService.createVehicle(request);

        assertThat(result).isNotNull();
        assertThat(result.getPlateNumber()).isEqualTo("51A-12345");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_WithNonExistentCustomer_ThrowsResourceNotFoundException() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(999L).brand("Toyota").model("Camry").year(2020)
                .plateNumber("51A-12345").build();

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    void createVehicle_WithDuplicatePlate_ThrowsDuplicateResourceException() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(1L).brand("Toyota").model("Camry").year(2020)
                .plateNumber("51A-12345").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByPlateNumber("51A-12345")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Plate number already registered");
    }

    @Test
    void createVehicle_WithYear1899_ThrowsIllegalArgumentException() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(1L).brand("Toyota").model("Camry").year(1899)
                .plateNumber("51A-12345").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByPlateNumber(any())).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Year must be between");
    }

    @Test
    void createVehicle_WithYearCurrentPlusTwo_ThrowsIllegalArgumentException() {
        int invalidYear = Year.now().getValue() + 2;
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(1L).brand("Toyota").model("Camry").year(invalidYear)
                .plateNumber("51A-12345").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByPlateNumber(any())).thenReturn(false);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createVehicle_WithYear1900_Succeeds() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(1L).brand("Ford").model("T").year(1900)
                .plateNumber("51A-99999").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByPlateNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(vehicleMapper.toResponse(any())).thenReturn(vehicleResponse);

        assertThatCode(() -> vehicleService.createVehicle(request)).doesNotThrowAnyException();
    }

    @Test
    void createVehicle_WithYearCurrentPlusOne_Succeeds() {
        int validYear = Year.now().getValue() + 1;
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .customerId(1L).brand("Toyota").model("Future").year(validYear)
                .plateNumber("51A-88888").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByPlateNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);
        when(vehicleMapper.toResponse(any())).thenReturn(vehicleResponse);

        assertThatCode(() -> vehicleService.createVehicle(request)).doesNotThrowAnyException();
    }

    @Test
    void deleteVehicle_WithExistingServiceHistory_ThrowsIllegalStateException() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.countServiceHistoryByVehicleId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> vehicleService.deleteVehicle(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete vehicle with existing service history");
    }

    @Test
    void deleteVehicle_WithNoServiceHistory_DeletesSuccessfully() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.countServiceHistoryByVehicleId(1L)).thenReturn(0L);

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).delete(vehicle);
    }

    @Test
    void getVehicleById_WithNonExistentId_ThrowsResourceNotFoundException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void updateVehicle_WithDuplicatePlateOnOtherVehicle_ThrowsDuplicateResourceException() {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .brand("Honda").model("Civic").year(2021).plateNumber("51A-99999").build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByPlateNumberAndIdNot("51A-99999", 1L)).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.updateVehicle(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
