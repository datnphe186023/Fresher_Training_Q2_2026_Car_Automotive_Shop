package com.carshop.service;

import com.carshop.dto.request.CreateServiceHistoryRequest;
import com.carshop.dto.response.ServiceHistoryResponse;
import com.carshop.entity.Service;
import com.carshop.entity.ServiceHistory;
import com.carshop.entity.Vehicle;
import com.carshop.exception.ResourceNotFoundException;
import com.carshop.mapper.ServiceHistoryMapper;
import com.carshop.repository.ServiceHistoryRepository;
import com.carshop.repository.ServiceRepository;
import com.carshop.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceHistoryServiceTest {

    @Mock private ServiceHistoryRepository serviceHistoryRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ServiceRepository serviceRepository;
    @Mock private ServiceHistoryMapper serviceHistoryMapper;
    @InjectMocks private ServiceHistoryService serviceHistoryService;

    private Vehicle vehicle;
    private Service service;
    private ServiceHistory history;
    private ServiceHistoryResponse historyResponse;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder().id(1L).brand("Toyota").model("Camry").build();
        service = Service.builder().id(1L).name("Film Installation").build();
        history = ServiceHistory.builder().id(1L).vehicle(vehicle).service(service)
                .serviceDate(LocalDate.now()).cost(BigDecimal.valueOf(500000)).build();
        historyResponse = ServiceHistoryResponse.builder().id(1L).vehicleId(1L)
                .serviceName("Film Installation").serviceDate(LocalDate.now())
                .cost(BigDecimal.valueOf(500000)).build();
    }

    @Test
    void createHistory_WithValidData_ReturnsServiceHistoryResponse() {
        CreateServiceHistoryRequest request = CreateServiceHistoryRequest.builder()
                .serviceId(1L).serviceDate(LocalDate.now())
                .cost(BigDecimal.valueOf(500000)).build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(serviceHistoryRepository.save(any())).thenReturn(history);
        when(serviceHistoryMapper.toResponse(history)).thenReturn(historyResponse);

        ServiceHistoryResponse result = serviceHistoryService.createHistory(1L, request);

        assertThat(result).isNotNull();
        assertThat(result.getServiceName()).isEqualTo("Film Installation");
        verify(serviceHistoryRepository).save(any(ServiceHistory.class));
    }

    @Test
    void createHistory_WithNonExistentVehicle_ThrowsResourceNotFoundException() {
        CreateServiceHistoryRequest request = CreateServiceHistoryRequest.builder()
                .serviceId(1L).serviceDate(LocalDate.now()).cost(BigDecimal.valueOf(100000)).build();

        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHistoryService.createHistory(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }

    @Test
    void createHistory_WithNonExistentService_ThrowsResourceNotFoundException() {
        CreateServiceHistoryRequest request = CreateServiceHistoryRequest.builder()
                .serviceId(999L).serviceDate(LocalDate.now()).cost(BigDecimal.valueOf(100000)).build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHistoryService.createHistory(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Service not found");
    }

    @Test
    void createHistory_WithNegativeCost_ThrowsIllegalArgumentException() {
        CreateServiceHistoryRequest request = CreateServiceHistoryRequest.builder()
                .serviceId(1L).serviceDate(LocalDate.now()).cost(BigDecimal.valueOf(-100)).build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> serviceHistoryService.createHistory(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cost cannot be negative");
    }

    @Test
    void getHistoryByVehicleId_WithNonExistentVehicle_ThrowsResourceNotFoundException() {
        when(vehicleRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> serviceHistoryService.getHistoryByVehicleId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vehicle not found");
    }
}
