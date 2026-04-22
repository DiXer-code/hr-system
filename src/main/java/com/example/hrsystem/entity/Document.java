package com.example.hrsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Document")
public class Document {
    public static final String CATEGORY_HIRING = "HIRING";
    public static final String CATEGORY_DISMISSAL = "DISMISSAL";
    public static final String CATEGORY_TRANSFER = "TRANSFER";
    public static final Set<String> PERSONNEL_CATEGORIES = Set.of(
            CATEGORY_HIRING,
            CATEGORY_DISMISSAL,
            CATEGORY_TRANSFER
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String fileType;

    private String documentCategory;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public String getDocumentCategoryLabel() {
        return switch (documentCategory) {
            case CATEGORY_HIRING -> "Наказ про прийняття";
            case CATEGORY_DISMISSAL -> "Наказ про звільнення";
            case CATEGORY_TRANSFER -> "Наказ про переведення";
            default -> "Інший документ";
        };
    }
}
