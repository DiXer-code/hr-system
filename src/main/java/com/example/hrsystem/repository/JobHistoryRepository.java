package com.example.hrsystem.repository;

import com.example.hrsystem.entity.JobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobHistoryRepository extends JpaRepository<JobHistory, Long> {
    List<JobHistory> findByEmployeeIdOrderByStartDateDescIdDesc(Long employeeId);

    List<JobHistory> findByEmployeeIdAndEndDateIsNullOrderByStartDateDescIdDesc(Long employeeId);
}
