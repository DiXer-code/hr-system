package com.example.hrsystem;

import com.example.hrsystem.entity.Department;
import com.example.hrsystem.entity.Position;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.PositionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;

    public DataInitializer(DepartmentRepository departmentRepository, PositionRepository positionRepository) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            Department it = createDept("IT Department", "Розробка, тестування та підтримка програмного забезпечення.");
            Department hr = createDept("HR Department", "Підбір персоналу, кадровий супровід та розвиток команди.");
            Department sales = createDept("Sales Department", "Продажі, робота з клієнтами та розвиток бізнесу.");
            Department finance = createDept("Finance Department", "Фінансовий облік, бюджетування та аналітика.");
            Department legal = createDept("Legal Department", "Юридичний супровід та договірна робота.");

            createPos("Senior Java Developer", "4500.00", it);
            createPos("Middle Java Developer", "2500.00", it);
            createPos("Frontend Developer", "2300.00", it);
            createPos("QA Engineer", "1800.00", it);
            createPos("DevOps Engineer", "3000.00", it);

            createPos("HR Director", "2000.00", hr);
            createPos("Recruiter", "1200.00", hr);
            createPos("Office Manager", "900.00", hr);

            createPos("Head of Sales", "2500.00", sales);
            createPos("Sales Manager", "1000.00", sales);

            createPos("Chief Accountant", "2200.00", finance);
            createPos("Financial Analyst", "1800.00", finance);

            createPos("Corporate Lawyer", "2100.00", legal);
        }
    }

    private Department createDept(String name, String desc) {
        Department d = new Department();
        d.setName(name);
        d.setDescription(desc);
        return departmentRepository.save(d);
    }

    private void createPos(String name, String salary, Department dept) {
        Position p = new Position();
        p.setName(name);
        p.setSalary(new BigDecimal(salary));
        p.setDepartment(dept);
        positionRepository.save(p);
    }
}
