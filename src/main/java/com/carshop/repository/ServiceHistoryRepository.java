package com.carshop.repository;

import com.carshop.entity.ServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, Long> {

    @Query("SELECT sh FROM ServiceHistory sh JOIN FETCH sh.service s " +
           "WHERE sh.vehicle.id = :vehicleId ORDER BY sh.serviceDate DESC")
    List<ServiceHistory> findAllByVehicleIdOrderByServiceDateDesc(@Param("vehicleId") Long vehicleId);
}
