package com.example.hrsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Position")
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private BigDecimal salary;

    // Зв'язок: Посада прив'язана до Відділу
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}