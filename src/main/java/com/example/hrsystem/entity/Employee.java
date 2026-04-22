package com.example.hrsystem.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "Employee")
public class Employee {
    public static final String STATUS_ACTIVE = "Активний";
    public static final String STATUS_ON_VACATION = "У відпустці";
    public static final String STATUS_DISMISSED = "Звільнений";
    public static final String STATUS_ON_SICK_LEAVE = "Лікарняний";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Вкажіть ім'я")
    private String firstName;

    @NotBlank(message = "Вкажіть прізвище")
    private String lastName;

    private String middleName;
    private String gender;
    private String maritalStatus;

    @Column(unique = true)
    private String inn;

    private String addressRegistration;
    private String addressActual;

    @Email(message = "Вкажіть коректний email")
    private String email;

    private String phoneMain;
    private String phoneWork;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;

    @ManyToOne
    @JoinColumn(name = "current_department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "current_position_id")
    private Position position;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id DESC")
    private List<Document> documents = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC, id DESC")
    private List<JobHistory> jobHistory = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("graduationYear DESC, id DESC")
    private List<Education> educationList = new ArrayList<>();

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dismissalDate;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] avatar;

    private String avatarContentType;

    @Column(nullable = false)
    private String status = STATUS_ACTIVE;

    public String getAvatarBase64() {
        if (avatar == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(avatar);
    }

    public String getAvatarDataUri() {
        if (avatar == null) {
            return null;
        }
        String contentType = (avatarContentType == null || avatarContentType.isBlank())
                ? "image/jpeg"
                : avatarContentType;
        return "data:" + contentType + ";base64," + getAvatarBase64();
    }

    public String getFullName() {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, lastName);
        appendPart(builder, firstName);
        appendPart(builder, middleName);
        return builder.length() == 0 ? "Без імені" : builder.toString();
    }

    public String getInitials() {
        String firstInitial = firstName != null && !firstName.isBlank()
                ? firstName.substring(0, 1).toUpperCase()
                : "";
        String lastInitial = lastName != null && !lastName.isBlank()
                ? lastName.substring(0, 1).toUpperCase()
                : "";
        String initials = firstInitial + lastInitial;
        return initials.isBlank() ? "HR" : initials;
    }

    public Document getHiringDocument() {
        return getDocumentByCategory(Document.CATEGORY_HIRING);
    }

    public Document getDismissalDocument() {
        return getDocumentByCategory(Document.CATEGORY_DISMISSAL);
    }

    public Document getTransferDocument() {
        return getDocumentByCategory(Document.CATEGORY_TRANSFER);
    }

    public List<Document> getOtherDocuments() {
        return documents.stream()
                .filter(document -> !Document.PERSONNEL_CATEGORIES.contains(document.getDocumentCategory()))
                .collect(Collectors.toList());
    }

    private Document getDocumentByCategory(String category) {
        return documents.stream()
                .filter(document -> category.equals(document.getDocumentCategory()))
                .findFirst()
                .orElse(null);
    }

    private void appendPart(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(value.trim());
    }
}
