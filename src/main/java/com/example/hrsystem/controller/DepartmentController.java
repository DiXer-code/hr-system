package com.example.hrsystem.controller;

import com.example.hrsystem.entity.Department;
import com.example.hrsystem.repository.DepartmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/departments")
    public String listDepartments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        return "departments";
    }

    @GetMapping("/departments/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        return "department-details";
    }

    // Зверни увагу: ID у відділів зазвичай Integer, якщо у тебе Long - зміни Integer на Long
    @GetMapping("/departments/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Department department = departmentRepository.findById(id).orElseThrow();
        model.addAttribute("department", department);
        return "department-details";
    }

    @PostMapping("/departments/save")
    public String saveDepartment(@ModelAttribute("department") Department department) {
        departmentRepository.save(department);
        return "redirect:/departments";
    }

    @GetMapping("/departments/delete/{id}")
    public String deleteDepartment(@PathVariable Integer id) {
        departmentRepository.deleteById(id);
        return "redirect:/departments";
    }
}