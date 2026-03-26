package com.example.hrsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal; // Не забудь цей імпорт!

@Data
@Entity
@Table(name = "JobHistory")
public class JobHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;

    private LocalDate startDate;
    private LocalDate endDate;

    // --- Додаємо поля, яких не вистачало на скріншоті ---

    private BigDecimal personalSalary; // Індивідуальна зарплата

    private String eventType; // Тип події: "HIRE", "TRANSFER", "DISMISS"
}