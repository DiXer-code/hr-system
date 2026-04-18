package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface TimesheetRepository extends JpaRepository<Timesheet, Integer> {
    boolean existsByEmployeeId(Long employeeId);

    @Query("""
            select coalesce(sum(t.workedHours), 0)
            from Timesheet t
            where t.workDate >= :fromDate
            """)
    Integer sumWorkedHoursSince(@Param("fromDate") LocalDate fromDate);
}
