package com.carshop.repository;

import com.carshop.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE v.customer.phoneNumber = :phoneNumber")
    List<Vehicle> findAllByCustomer_PhoneNumber(@Param("phoneNumber") String phoneNumber);

    List<Vehicle> findAllByCustomerId(Long customerId);

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndIdNot(String plateNumber, Long id);

    @Query("SELECT COUNT(sh) FROM ServiceHistory sh WHERE sh.vehicle.id = :vehicleId")
    long countServiceHistoryByVehicleId(@Param("vehicleId") Long vehicleId);
}
