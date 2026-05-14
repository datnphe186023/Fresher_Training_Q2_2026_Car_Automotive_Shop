package com.carshop.repository;

import com.carshop.entity.JobExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobExecutionHistoryRepository extends JpaRepository<JobExecutionHistory, Long> {

    Page<JobExecutionHistory> findAllByOrderByStartedAtDesc(Pageable pageable);

    Page<JobExecutionHistory> findByJobNameOrderByStartedAtDesc(String jobName, Pageable pageable);
}
