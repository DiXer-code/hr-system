package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacationRepository extends JpaRepository<Vacation, Integer> {
    List<Vacation> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);
}
