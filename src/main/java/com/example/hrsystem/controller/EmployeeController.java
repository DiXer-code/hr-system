package com.example.hrsystem.controller;

import com.example.hrsystem.entity.Document;
import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.DocumentRepository;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Controller
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 1. Головна сторінка (Список)
    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employees";
    }

    // 2. СТОРІНКА: Додати нового (ОСЬ ЦЬОГО МЕТОДУ НЕ ВИСТАЧАЛО!)
    @GetMapping("/employees/new")
    public String showCreateForm(Model model) {
        model.addAttribute("employee", new Employee());
        // Завантажуємо списки відділів та посад для випадаючих списків
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", positionRepository.findAll());
        return "employee-details"; // Посилається на твій файл employee-details.html
    }

    // 3. СТОРІНКА: Редагувати існуючого
    @GetMapping("/employees/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.findById(id);
        model.addAttribute("employee", employee);
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("positions", positionRepository.findAll());
        return "employee-details";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute("employee") Employee employee,
                               @RequestParam(value = "departmentId", required = false) Integer departmentId,
                               @RequestParam(value = "positionId", required = false) Integer positionId,
                               @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                               @RequestParam(value = "documentFile", required = false) MultipartFile documentFile) throws IOException {

        if (departmentId != null) {
            employee.setDepartment(departmentRepository.findById(departmentId).orElse(null));
        } else {
            employee.setDepartment(null);
        }

        if (positionId != null) {
            employee.setPosition(positionRepository.findById(positionId).orElse(null));
        } else {
            employee.setPosition(null);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            employee.setAvatar(avatarFile.getBytes());
        } else if (employee.getId() != null) {
            Employee oldVersion = employeeService.findById(employee.getId());
            if (oldVersion != null) {
                employee.setAvatar(oldVersion.getAvatar());
            }
        }

        employeeService.save(employee);

        if (documentFile != null && !documentFile.isEmpty()) {
            Document doc = new Document();
            doc.setFileName(documentFile.getOriginalFilename());
            doc.setFileType(documentFile.getContentType());
            doc.setData(documentFile.getBytes());
            doc.setEmployee(employee);
            documentRepository.save(doc);
        }

        return "redirect:/employees";
    }

    // 5. ДІЯ: Видалення
    @GetMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployeeById(id);
        return "redirect:/employees";
    }

    // --- Робота з документами ---

    @PostMapping("/employees/{id}/upload")
    public String uploadFile(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        Document doc = new Document();
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(file.getContentType());
        doc.setData(file.getBytes());
        doc.setEmployee(employee);
        documentRepository.save(doc);
        return "redirect:/employees/edit/" + id;
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        Document doc = documentRepository.findById(id).orElseThrow();
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getFileName() + "\"")
                .body(doc.getData());
    }





    @GetMapping("/employees/search")
    public String searchEmployees(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("employees", employeeService.searchByNameOrLastName(keyword));
        model.addAttribute("keyword", keyword);
        return "employees";
    }

}
