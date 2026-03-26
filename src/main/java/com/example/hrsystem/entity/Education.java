package com.example.hrsystem.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private String institution; // ВНЗ
    private String faculty;
    private String degree;      // Бакалавр/Магістр
    private String graduationYear;
}