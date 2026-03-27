package com.example.hrsystem.controller;

import com.example.hrsystem.entity.Document;
import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.DocumentRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final DocumentRepository documentRepository;

    public EmployeeController(EmployeeService employeeService,
                              DepartmentRepository departmentRepository,
                              PositionRepository positionRepository,
                              DocumentRepository documentRepository) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.documentRepository = documentRepository;
    }

    @GetMapping({"/employees", "/employees/search"})
    public String listEmployees(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("employees", employeeService.findAll(keyword));
        model.addAttribute("keyword", keyword);
        return "employees";
    }

    @GetMapping("/employees/new")
    public String showCreateForm(Model model) {
        populateFormModel(model, employeeService.createEmptyEmployee());
        return "employee-details";
    }

    @GetMapping({"/employees/edit/{id}", "/employees/{id}"})
    public String showEditForm(@PathVariable Long id, Model model) {
        populateFormModel(model, employeeService.findById(id));
        return "employee-details";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                               BindingResult bindingResult,
                               @RequestParam(value = "departmentId", required = false) Integer departmentId,
                               @RequestParam(value = "positionId", required = false) Integer positionId,
                               @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                               @RequestParam(value = "documentFile", required = false) MultipartFile documentFile,
                               Model model,
                               RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            applySelections(employee, departmentId, positionId);
            populateFormModel(model, employee);
            return "employee-details";
        }

        try {
            Employee savedEmployee = employeeService.saveEmployee(employee, departmentId, positionId, avatarFile, documentFile);
            redirectAttributes.addFlashAttribute("successMessage", "Дані працівника успішно збережено.");
            return "redirect:/employees/edit/" + savedEmployee.getId();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            bindingResult.reject("saveError", ex.getMessage());
            applySelections(employee, departmentId, positionId);
            populateFormModel(model, employee);
            return "employee-details";
        }
    }

    @PostMapping("/employees/{id}/dismiss")
    public String dismissEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.dismissEmployee(id);
        redirectAttributes.addFlashAttribute("successMessage", "Працівника позначено як звільненого.");
        return "redirect:/employees/edit/" + id;
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return performDelete(id, redirectAttributes);
    }

    @GetMapping("/employees/delete/{id}")
    public String deleteEmployeeLegacy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return performDelete(id, redirectAttributes);
    }

    private String performDelete(Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deleteEmployeeById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Працівника видалено.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Документ не знайдено."));

        MediaType mediaType;
        try {
            mediaType = document.getFileType() != null
                    ? MediaType.parseMediaType(document.getFileType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (IllegalArgumentException ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        boolean inlineView = mediaType.isCompatibleWith(MediaType.APPLICATION_PDF)
                || "image".equalsIgnoreCase(mediaType.getType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        (inlineView ? "inline" : "attachment") + "; filename=\"" + document.getFileName() + "\""
                )
                .body(document.getData());
    }

    private void populateFormModel(Model model, Employee employee) {
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentRepository.findAllByOrderByNameAsc());
        model.addAttribute("positions", positionRepository.findAllByOrderByNameAsc());
        model.addAttribute("statuses", employeeService.getAvailableStatuses());
    }

    private void applySelections(Employee employee, Integer departmentId, Integer positionId) {
        employee.setDepartment(
                departmentId != null ? departmentRepository.findById(departmentId).orElse(null) : null
        );
        employee.setPosition(
                positionId != null ? positionRepository.findById(positionId).orElse(null) : null
        );
    }
}
