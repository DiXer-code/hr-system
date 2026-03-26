package com.example.hrsystem.service;

import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.JobHistory;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.JobHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Бажано додати транзакційність

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private JobHistoryRepository jobHistoryRepository; // Ось чого не вистачало!

    // Отримати всіх співробітників
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // Знайти одного (знадобиться для редагування)
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    // Зберегти співробітника + логіка історії
    // У файлі EmployeeService.java

    @Transactional
    public void save(Employee employee) {
        boolean isNew = (employee.getId() == null);

        // Логіка для запису історії
        boolean needHistoryRecord = false;

        if (isNew) {
            needHistoryRecord = true; // Новий співробітник -> пишемо в історію
        } else {
            // Якщо старий -> перевіряємо, чи змінилася посада
            Employee oldDbEmployee = employeeRepository.findById(employee.getId()).orElse(null);
            if (oldDbEmployee != null && employee.getPosition() != null) {
                // Якщо стара посада не дорівнює новій
                if (!oldDbEmployee.getPosition().getId().equals(employee.getPosition().getId())) {
                    needHistoryRecord = true;
                }
            }
        }

        // Спочатку зберігаємо самого працівника
        Employee savedEmployee = employeeRepository.save(employee);

        // Якщо треба записати в історію
        if (needHistoryRecord && employee.getPosition() != null) {
            JobHistory history = new JobHistory();
            history.setEmployee(savedEmployee);
            history.setDepartment(employee.getDepartment());
            history.setPosition(employee.getPosition());
            history.setStartDate(LocalDate.now());
            history.setPersonalSalary(employee.getPosition().getSalary());
            history.setEventType(isNew ? "ПРИЙНЯТТЯ" : "ПЕРЕВЕДЕННЯ"); // Пишемо тип події

            jobHistoryRepository.save(history);
        }
    }


    // Видалити співробітника
    public void deleteEmployeeById(Long id) {
        employeeRepository.deleteById(id);
    }
}