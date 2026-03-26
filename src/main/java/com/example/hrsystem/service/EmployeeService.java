package com.example.hrsystem.service;

import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.JobHistory;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.JobHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    public List<Employee> searchByNameOrLastName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return employeeRepository.findAll();
        }
        return employeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(keyword.trim(), keyword.trim());
    }

    @Transactional
    public void save(Employee employee) {
        boolean isNew = (employee.getId() == null);
        boolean needHistoryRecord = false;

        if (isNew) {
            needHistoryRecord = true;
        } else {
            Employee oldDbEmployee = employeeRepository.findById(employee.getId()).orElse(null);
            if (oldDbEmployee != null && employee.getPosition() != null && oldDbEmployee.getPosition() != null) {
                if (!oldDbEmployee.getPosition().getId().equals(employee.getPosition().getId())) {
                    needHistoryRecord = true;
                }
            }
        }

        Employee savedEmployee = employeeRepository.save(employee);

        if (needHistoryRecord && employee.getPosition() != null) {
            JobHistory history = new JobHistory();
            history.setEmployee(savedEmployee);
            history.setDepartment(employee.getDepartment());
            history.setPosition(employee.getPosition());
            history.setStartDate(LocalDate.now());
            history.setPersonalSalary(employee.getPosition().getSalary());
            history.setEventType(isNew ? "ПРИЙНЯТТЯ" : "ПЕРЕВЕДЕННЯ");

            jobHistoryRepository.save(history);
        }
    }

    public void deleteEmployeeById(Long id) {
        employeeRepository.deleteById(id);
    }
}