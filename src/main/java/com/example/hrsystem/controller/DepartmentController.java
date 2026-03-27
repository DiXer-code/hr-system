package com.example.hrsystem.controller;

import com.example.hrsystem.entity.Department;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.PositionRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;

    public DepartmentController(DepartmentRepository departmentRepository,
                                EmployeeRepository employeeRepository,
                                PositionRepository positionRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.positionRepository = positionRepository;
    }

    @GetMapping("/departments")
    public String listDepartments(Model model) {
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        Map<Integer, Long> employeeCountByDepartment = departments.stream()
                .collect(Collectors.toMap(Department::getId, department -> employeeRepository.countByDepartmentId(department.getId())));
        Map<Integer, Long> positionCountByDepartment = departments.stream()
                .collect(Collectors.toMap(Department::getId, department -> positionRepository.countByDepartmentId(department.getId())));

        model.addAttribute("departments", departments);
        model.addAttribute("employeeCountByDepartment", employeeCountByDepartment);
        model.addAttribute("positionCountByDepartment", positionCountByDepartment);
        model.addAttribute("totalEmployees", employeeCountByDepartment.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("totalPositions", positionCountByDepartment.values().stream().mapToLong(Long::longValue).sum());
        return "departments";
    }

    @GetMapping("/departments/new")
    public String showCreateForm(Model model) {
        populateFormModel(model, new Department());
        return "department-details";
    }

    @GetMapping("/departments/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Департамент не знайдено."));
        populateFormModel(model, department);
        return "department-details";
    }

    @PostMapping("/departments/save")
    public String saveDepartment(@Valid @ModelAttribute("department") Department department,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, department);
            return "department-details";
        }

        departmentRepository.save(department);
        redirectAttributes.addFlashAttribute("successMessage", "Департамент успішно збережено.");
        return "redirect:/departments";
    }

    @PostMapping("/departments/{id}/delete")
    public String deleteDepartment(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return performDelete(id, redirectAttributes);
    }

    @GetMapping("/departments/delete/{id}")
    public String deleteDepartmentLegacy(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return performDelete(id, redirectAttributes);
    }

    private String performDelete(Integer id, RedirectAttributes redirectAttributes) {
        long employeeCount = employeeRepository.countByDepartmentId(id);
        long positionCount = positionRepository.countByDepartmentId(id);

        if (employeeCount > 0 || positionCount > 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Неможливо видалити департамент, поки в ньому є співробітники або посади."
            );
            return "redirect:/departments/edit/" + id;
        }

        departmentRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Департамент видалено.");
        return "redirect:/departments";
    }

    private void populateFormModel(Model model, Department department) {
        model.addAttribute("department", department);
        if (department.getId() == null) {
            model.addAttribute("deptEmployees", Collections.emptyList());
            model.addAttribute("deptPositions", Collections.emptyList());
            model.addAttribute("employeeCount", 0L);
            model.addAttribute("positionCount", 0L);
            return;
        }

        model.addAttribute("deptEmployees", employeeRepository.findByDepartmentIdOrderByLastNameAscFirstNameAsc(department.getId()));
        model.addAttribute("deptPositions", positionRepository.findByDepartmentIdOrderByNameAsc(department.getId()));
        model.addAttribute("employeeCount", employeeRepository.countByDepartmentId(department.getId()));
        model.addAttribute("positionCount", positionRepository.countByDepartmentId(department.getId()));
    }
}
