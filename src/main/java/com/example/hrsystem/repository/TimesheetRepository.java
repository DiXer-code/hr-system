package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimesheetRepository extends JpaRepository<Timesheet, Integer> {
    boolean existsByEmployeeId(Long employeeId);
}
