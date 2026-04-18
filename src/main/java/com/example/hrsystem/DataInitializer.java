package com.example.hrsystem;

import com.example.hrsystem.entity.Department;
import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.Position;
import com.example.hrsystem.entity.Role;
import com.example.hrsystem.entity.Timesheet;
import com.example.hrsystem.entity.User;
import com.example.hrsystem.entity.Vacation;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.repository.RoleRepository;
import com.example.hrsystem.repository.TimesheetRepository;
import com.example.hrsystem.repository.UserRepository;
import com.example.hrsystem.repository.VacationRepository;
import com.example.hrsystem.service.EmployeeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final VacationRepository vacationRepository;
    private final TimesheetRepository timesheetRepository;
    private final EmployeeService employeeService;

    public DataInitializer(DepartmentRepository departmentRepository,
                           PositionRepository positionRepository,
                           EmployeeRepository employeeRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           VacationRepository vacationRepository,
                           TimesheetRepository timesheetRepository,
                           EmployeeService employeeService) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.vacationRepository = vacationRepository;
        this.timesheetRepository = timesheetRepository;
        this.employeeService = employeeService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (departmentRepository.count() == 0) {
            seedReferenceData();
        }

        ensureDefaultUser();

        if (employeeRepository.count() == 0) {
            seedDemoEmployees();
        }
    }

    private void seedReferenceData() {
        Department it = createDept("ІТ", "Розробка, тестування, інфраструктура та підтримка внутрішніх систем.");
        Department hr = createDept("HR", "Підбір персоналу, кадровий супровід, адаптація та розвиток команди.");
        Department sales = createDept("Продажі", "Пошук клієнтів, комерційні пропозиції та утримання партнерів.");
        Department finance = createDept("Фінанси", "Бюджетування, фінансовий контроль, аналітика та звітність.");
        Department legal = createDept("Юридичний супровід", "Договори, комплаєнс, перевірка ризиків та захист інтересів компанії.");

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

    private void ensureDefaultUser() {
        Role adminRole = roleRepository.findAll().stream()
                .filter(role -> "ADMIN".equalsIgnoreCase(role.getName()))
                .findFirst()
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    role.setDescription("Повний доступ до кадрової системи.");
                    return roleRepository.save(role);
                });

        if (userRepository.count() > 0) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("12345");
        admin.setEmail("admin@hr.local");
        admin.setRole(adminRole);
        userRepository.save(admin);
    }

    private void seedDemoEmployees() throws IOException {
        Position seniorJava = findPosition("Senior Java Developer");
        Position qaEngineer = findPosition("QA Engineer");
        Position hrDirector = findPosition("HR Director");
        Position financialAnalyst = findPosition("Financial Analyst");
        Position corporateLawyer = findPosition("Corporate Lawyer");
        Position salesManager = findPosition("Sales Manager");
        Position frontendDeveloper = findPosition("Frontend Developer");

        if (seniorJava == null
                || qaEngineer == null
                || hrDirector == null
                || financialAnalyst == null
                || corporateLawyer == null
                || salesManager == null
                || frontendDeveloper == null) {
            return;
        }

        LocalDate today = LocalDate.now();

        Employee dmytro = createEmployee(
                "Дмитро",
                "Бондаренко",
                "Ігорович",
                "d.bondarenko@hr.local",
                "+380671112233",
                "3012345678",
                seniorJava.getDepartment(),
                seniorJava,
                today.minusYears(2).minusMonths(3),
                Employee.STATUS_ACTIVE
        );

        Employee olena = createEmployee(
                "Олена",
                "Коваленко",
                "Петрівна",
                "o.kovalenko@hr.local",
                "+380672223344",
                "3023456789",
                hrDirector.getDepartment(),
                hrDirector,
                today.minusYears(3).minusMonths(1),
                Employee.STATUS_ACTIVE
        );

        Employee iryna = createEmployee(
                "Ірина",
                "Мельник",
                "Сергіївна",
                "i.melnyk@hr.local",
                "+380673334455",
                "3034567890",
                qaEngineer.getDepartment(),
                qaEngineer,
                today.minusYears(1).minusMonths(8),
                Employee.STATUS_ON_VACATION
        );

        Employee serhii = createEmployee(
                "Сергій",
                "Поліщук",
                "Васильович",
                "s.polishchuk@hr.local",
                "+380674445566",
                "3045678901",
                financialAnalyst.getDepartment(),
                financialAnalyst,
                today.minusYears(1).minusMonths(2),
                Employee.STATUS_ON_SICK_LEAVE
        );

        Employee oksana = createEmployee(
                "Оксана",
                "Романюк",
                "Андріївна",
                "o.romaniuk@hr.local",
                "+380675556677",
                "3056789012",
                corporateLawyer.getDepartment(),
                corporateLawyer,
                today.minusYears(4),
                Employee.STATUS_ACTIVE
        );

        Employee taras = createEmployee(
                "Тарас",
                "Левченко",
                "Олегович",
                "t.levchenko@hr.local",
                "+380676667788",
                "3067890123",
                salesManager.getDepartment(),
                salesManager,
                today.minusYears(2).minusMonths(7),
                Employee.STATUS_ACTIVE
        );

        transferEmployee(taras, frontendDeveloper.getDepartment(), frontendDeveloper);
        employeeService.dismissEmployee(oksana.getId());

        createVacation(iryna, today.minusDays(4), today.plusDays(10), "Щорічна");
        createTimesheet(dmytro, today.minusDays(1), 8);
        createTimesheet(dmytro, today.minusDays(2), 8);
        createTimesheet(dmytro, today.minusDays(3), 7);
        createTimesheet(olena, today.minusDays(1), 8);
        createTimesheet(serhii, today.minusDays(6), 5);
    }

    private Employee createEmployee(String firstName,
                                    String lastName,
                                    String middleName,
                                    String email,
                                    String phone,
                                    String inn,
                                    Department department,
                                    Position position,
                                    LocalDate hireDate,
                                    String status) throws IOException {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setMiddleName(middleName);
        employee.setEmail(email);
        employee.setPhoneMain(phone);
        employee.setPhoneWork(phone);
        employee.setInn(inn);
        employee.setBirthDate(hireDate.minusYears(25));
        employee.setHireDate(hireDate);
        employee.setStatus(status);
        employee.setGender("Не вказано");
        employee.setMaritalStatus("Не одружений/а");
        employee.setAddressRegistration("м. Київ");
        employee.setAddressActual("м. Київ");
        return employeeService.saveEmployee(employee, department.getId(), position.getId(), null, null);
    }

    private void transferEmployee(Employee employee, Department targetDepartment, Position targetPosition) throws IOException {
        Employee draft = new Employee();
        draft.setId(employee.getId());
        draft.setFirstName(employee.getFirstName());
        draft.setLastName(employee.getLastName());
        draft.setMiddleName(employee.getMiddleName());
        draft.setEmail(employee.getEmail());
        draft.setPhoneMain(employee.getPhoneMain());
        draft.setPhoneWork(employee.getPhoneWork());
        draft.setInn(employee.getInn());
        draft.setBirthDate(employee.getBirthDate());
        draft.setHireDate(employee.getHireDate());
        draft.setStatus(employee.getStatus());
        draft.setGender(employee.getGender());
        draft.setMaritalStatus(employee.getMaritalStatus());
        draft.setAddressRegistration(employee.getAddressRegistration());
        draft.setAddressActual(employee.getAddressActual());
        employeeService.saveEmployee(draft, targetDepartment.getId(), targetPosition.getId(), null, null);
    }

    private Position findPosition(String positionName) {
        return positionRepository.findAll().stream()
                .filter(position -> positionName.equalsIgnoreCase(position.getName()))
                .findFirst()
                .orElse(null);
    }

    private void createVacation(Employee employee, LocalDate startDate, LocalDate endDate, String type) {
        Vacation vacation = new Vacation();
        vacation.setEmployee(employee);
        vacation.setStartDate(startDate);
        vacation.setEndDate(endDate);
        vacation.setType(type);
        vacationRepository.save(vacation);
    }

    private void createTimesheet(Employee employee, LocalDate workDate, int workedHours) {
        Timesheet timesheet = new Timesheet();
        timesheet.setEmployee(employee);
        timesheet.setWorkDate(workDate);
        timesheet.setWorkedHours(workedHours);
        timesheetRepository.save(timesheet);
    }

    private Department createDept(String name, String description) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    private void createPos(String name, String salary, Department department) {
        Position position = new Position();
        position.setName(name);
        position.setSalary(new BigDecimal(salary));
        position.setDepartment(department);
        positionRepository.save(position);
    }
}
