package com.example.hrsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "Employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Основна інформація
    private String firstName;
    private String lastName;
    private String middleName; // По батькові (є на скрінах)

    private String gender; // "Чоловіча"/"Жіноча"
    private String maritalStatus; // "Одружений/а" тощо

    @Column(unique = true)
    private String inn; // ІПН (важливо для HR)

    private String addressRegistration; // Адреса прописки
    private String addressActual;       // Фактична адреса

    private String email;
    private String phoneMain;
    private String phoneWork;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    // Поточний стан (для швидкого доступу)
    @ManyToOne
    @JoinColumn(name = "current_department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "current_position_id")
    private Position position;

    // Зв'язки
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    // НОВЕ: Історія роботи (призначення, переведення)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<JobHistory> jobHistory = new ArrayList<>();

    // НОВЕ: Освіта
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Education> educationList = new ArrayList<>();

    private LocalDate dismissalDate;


    // Поле для фотографії
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] avatar;

    // Допоміжний метод для HTML (перетворює байти в картинку)
    public String getAvatarBase64() {
        if (avatar == null) return null;
        return java.util.Base64.getEncoder().encodeToString(avatar);
    }

    // Додай ось це поле
    private String status = "Активний"; // Значення за замовчуванням

    // ... геттери і сеттери ...
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}