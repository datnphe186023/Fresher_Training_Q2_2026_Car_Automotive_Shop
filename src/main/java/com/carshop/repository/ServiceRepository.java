package com.carshop.repository;

import com.carshop.entity.Service;
import com.carshop.entity.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Service entity operations.
 * Provides CRUD operations and custom query methods for service management.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    /**
     * Find all services in a specific category with pagination support.
     * Used for filtering services by category in the service catalog.
     *
     * @param category the service category to filter by
     * @param pageable pagination and sorting parameters
     * @return Page of services in the specified category
     */
    Page<Service> findByCategory(ServiceCategory category, Pageable pageable);
}
