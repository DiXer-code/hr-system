package com.example.hrsystem;

import com.example.hrsystem.entity.Department;
import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.JobHistory;
import com.example.hrsystem.entity.Position;
import com.example.hrsystem.entity.Vacation;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.JobHistoryRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.repository.TimesheetRepository;
import com.example.hrsystem.repository.VacationRepository;
import com.example.hrsystem.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeServiceIntegrationTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    @Autowired
    private VacationRepository vacationRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @BeforeEach
    void cleanDatabase() {
        jobHistoryRepository.deleteAllInBatch();
        vacationRepository.deleteAllInBatch();
        timesheetRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        positionRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
    }

    @Test
    void saveEmployeeCreatesHireHistory() throws IOException {
        Department it = createDepartment("ІТ");
        Position qaEngineer = createPosition("QA Engineer", it, "1800.00");

        Employee employee = buildEmployee("Ірина", "Мельник", "3000000001", LocalDate.of(2025, 2, 10), Employee.STATUS_ACTIVE);
        Employee savedEmployee = employeeService.saveEmployee(employee, it.getId(), qaEngineer.getId(), null, null);

        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getDepartment().getId()).isEqualTo(it.getId());
        assertThat(savedEmployee.getPosition().getId()).isEqualTo(qaEngineer.getId());

        List<JobHistory> historyEntries = jobHistoryRepository.findByEmployeeIdOrderByStartDateDescIdDesc(savedEmployee.getId());
        assertThat(historyEntries).hasSize(1);
        assertThat(historyEntries.get(0).getEventType()).isEqualTo("ПРИЙНЯТТЯ");
        assertThat(historyEntries.get(0).getDepartment().getId()).isEqualTo(it.getId());
        assertThat(historyEntries.get(0).getPosition().getId()).isEqualTo(qaEngineer.getId());
        assertThat(historyEntries.get(0).getStartDate()).isEqualTo(LocalDate.of(2025, 2, 10));
    }

    @Test
    void saveEmployeeTransferClosesPreviousHistoryAndAddsTransferEvent() throws IOException {
        Department sales = createDepartment("Продажі");
        Department it = createDepartment("ІТ");
        Position salesManager = createPosition("Sales Manager", sales, "1000.00");
        Position frontendDeveloper = createPosition("Frontend Developer", it, "2300.00");

        Employee savedEmployee = employeeService.saveEmployee(
                buildEmployee("Тарас", "Левченко", "3000000002", LocalDate.of(2024, 11, 1), Employee.STATUS_ACTIVE),
                sales.getId(),
                salesManager.getId(),
                null,
                null
        );

        employeeService.saveEmployee(copyForUpdate(savedEmployee), it.getId(), frontendDeveloper.getId(), null, null);

        List<JobHistory> historyEntries = jobHistoryRepository.findByEmployeeIdOrderByStartDateDescIdDesc(savedEmployee.getId());
        assertThat(historyEntries).hasSize(2);
        assertThat(historyEntries.get(0).getEventType()).isEqualTo("ПЕРЕВЕДЕННЯ");
        assertThat(historyEntries.get(0).getDepartment().getId()).isEqualTo(it.getId());
        assertThat(historyEntries.get(0).getPosition().getId()).isEqualTo(frontendDeveloper.getId());
        assertThat(historyEntries.get(1).getEndDate()).isEqualTo(historyEntries.get(0).getStartDate());
    }

    @Test
    void dismissEmployeeMarksCardAndAddsDismissalEvent() throws IOException {
        Department legal = createDepartment("Юридичний супровід");
        Position lawyer = createPosition("Corporate Lawyer", legal, "2100.00");

        Employee savedEmployee = employeeService.saveEmployee(
                buildEmployee("Оксана", "Романюк", "3000000003", LocalDate.of(2023, 9, 4), Employee.STATUS_ACTIVE),
                legal.getId(),
                lawyer.getId(),
                null,
                null
        );

        employeeService.dismissEmployee(savedEmployee.getId());

        Employee dismissedEmployee = employeeRepository.findById(savedEmployee.getId()).orElseThrow();
        List<JobHistory> historyEntries = jobHistoryRepository.findByEmployeeIdOrderByStartDateDescIdDesc(savedEmployee.getId());

        assertThat(dismissedEmployee.getStatus()).isEqualTo(Employee.STATUS_DISMISSED);
        assertThat(dismissedEmployee.getDismissalDate()).isEqualTo(LocalDate.now());
        assertThat(historyEntries).hasSize(2);
        assertThat(historyEntries.get(0).getEventType()).isEqualTo("ЗВІЛЬНЕННЯ");
        assertThat(historyEntries.get(0).getEndDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void deleteEmployeeFailsWhenVacationExists() throws IOException {
        Department hr = createDepartment("HR");
        Position recruiter = createPosition("Recruiter", hr, "1200.00");

        Employee savedEmployee = employeeService.saveEmployee(
                buildEmployee("Олена", "Коваленко", "3000000004", LocalDate.of(2024, 5, 18), Employee.STATUS_ACTIVE),
                hr.getId(),
                recruiter.getId(),
                null,
                null
        );

        Vacation vacation = new Vacation();
        vacation.setEmployee(savedEmployee);
        vacation.setType("Щорічна");
        vacation.setStartDate(LocalDate.now());
        vacation.setEndDate(LocalDate.now().plusDays(7));
        vacationRepository.save(vacation);

        assertThatThrownBy(() -> employeeService.deleteEmployeeById(savedEmployee.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Неможливо видалити працівника");

        assertThat(employeeRepository.existsById(savedEmployee.getId())).isTrue();
    }

    @Test
    void duplicateInnRollsBackTransaction() throws IOException {
        Department finance = createDepartment("Фінанси");
        Position analyst = createPosition("Financial Analyst", finance, "1800.00");

        employeeService.saveEmployee(
                buildEmployee("Сергій", "Поліщук", "3000000005", LocalDate.of(2024, 8, 8), Employee.STATUS_ACTIVE),
                finance.getId(),
                analyst.getId(),
                null,
                null
        );

        assertThatThrownBy(() -> employeeService.saveEmployee(
                buildEmployee("Марина", "Ткаченко", "3000000005", LocalDate.of(2025, 1, 15), Employee.STATUS_ACTIVE),
                finance.getId(),
                analyst.getId(),
                null,
                null
        )).isInstanceOf(DataIntegrityViolationException.class);

        assertThat(employeeRepository.count()).isEqualTo(1);
        assertThat(jobHistoryRepository.count()).isEqualTo(1);
    }

    private Department createDepartment(String name) {
        Department department = new Department();
        department.setName(name);
        department.setDescription("Тестовий департамент " + name);
        return departmentRepository.save(department);
    }

    private Position createPosition(String name, Department department, String salary) {
        Position position = new Position();
        position.setName(name);
        position.setDepartment(department);
        position.setSalary(new BigDecimal(salary));
        return positionRepository.save(position);
    }

    private Employee buildEmployee(String firstName, String lastName, String inn, LocalDate hireDate, String status) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setMiddleName("Тестович");
        employee.setInn(inn);
        employee.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@test.local");
        employee.setPhoneMain("+380670000000");
        employee.setPhoneWork("+380440000000");
        employee.setBirthDate(hireDate.minusYears(25));
        employee.setHireDate(hireDate);
        employee.setStatus(status);
        employee.setGender("Не вказано");
        employee.setMaritalStatus("Не одружений/а");
        employee.setAddressRegistration("м. Київ");
        employee.setAddressActual("м. Київ");
        return employee;
    }

    private Employee copyForUpdate(Employee source) {
        Employee employee = new Employee();
        employee.setId(source.getId());
        employee.setFirstName(source.getFirstName());
        employee.setLastName(source.getLastName());
        employee.setMiddleName(source.getMiddleName());
        employee.setInn(source.getInn());
        employee.setEmail(source.getEmail());
        employee.setPhoneMain(source.getPhoneMain());
        employee.setPhoneWork(source.getPhoneWork());
        employee.setBirthDate(source.getBirthDate());
        employee.setHireDate(source.getHireDate());
        employee.setStatus(source.getStatus());
        employee.setGender(source.getGender());
        employee.setMaritalStatus(source.getMaritalStatus());
        employee.setAddressRegistration(source.getAddressRegistration());
        employee.setAddressActual(source.getAddressActual());
        return employee;
    }
}
