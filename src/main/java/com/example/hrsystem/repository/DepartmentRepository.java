package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    List<Department> findAllByOrderByNameAsc();
}
