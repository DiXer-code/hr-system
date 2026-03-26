package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Integer> {
}