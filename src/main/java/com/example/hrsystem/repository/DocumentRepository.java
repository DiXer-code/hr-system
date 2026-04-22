package com.example.hrsystem.repository;

import com.example.hrsystem.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEmployeeIdOrderByIdDesc(Long employeeId);

    Optional<Document> findByEmployeeIdAndDocumentCategory(Long employeeId, String documentCategory);
}
