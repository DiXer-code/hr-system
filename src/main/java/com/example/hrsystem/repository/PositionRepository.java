package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Integer> {
    List<Position> findAllByOrderByNameAsc();

    List<Position> findByDepartmentIdOrderByNameAsc(Integer departmentId);

    long countByDepartmentId(Integer departmentId);
}
