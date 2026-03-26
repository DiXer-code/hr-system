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
    public void run(String... args) throws Exception {
        // ПЕРЕВІРКА: Якщо відділів немає - створюємо базу заново
        if (departmentRepository.count() == 0) {

            System.out.println("⏳ Починаю наповнення бази даних...");

            // 1. Створюємо відділи
            Department it = createDept("IT Відділ", "Розробка ПЗ");
            Department hr = createDept("HR Відділ", "Робота з персоналом");
            Department sales = createDept("Відділ Продажу", "Комерція");
            Department finance = createDept("Фінансовий Відділ", "Бухгалтерія та аудит");
            Department legal = createDept("Юридичний Відділ", "Правова підтримка");

            // 2. Створюємо посади для IT
            createPos("Java Senior Developer", "4500.00", it);
            createPos("Java Middle Developer", "2500.00", it);
            createPos("Frontend Developer", "2300.00", it);
            createPos("QA Engineer", "1800.00", it);
            createPos("DevOps Engineer", "3000.00", it);

            // 3. Створюємо посади для HR
            createPos("HR Director", "2000.00", hr);
            createPos("Recruiter", "1200.00", hr);
            createPos("Office Manager", "900.00", hr);

            // 4. Створюємо посади для Sales
            createPos("Head of Sales", "2500.00", sales);
            createPos("Sales Manager", "1000.00", sales);

            // 5. Створюємо посади для Finance
            createPos("Chief Accountant", "2200.00", finance);
            createPos("Financial Analyst", "1800.00", finance);

            // 6. Створюємо посади для Legal
            createPos("Corporate Lawyer", "2100.00", legal);

            System.out.println("✅ База даних успішно наповнена!");
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