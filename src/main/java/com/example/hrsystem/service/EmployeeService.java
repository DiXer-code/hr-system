package com.example.hrsystem.service;

import com.example.hrsystem.entity.Department;
import com.example.hrsystem.entity.Document;
import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.JobHistory;
import com.example.hrsystem.entity.Position;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.DocumentRepository;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.JobHistoryRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.repository.TimesheetRepository;
import com.example.hrsystem.repository.VacationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class EmployeeService {
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            Employee.STATUS_ACTIVE,
            Employee.STATUS_ON_VACATION,
            Employee.STATUS_DISMISSED,
            Employee.STATUS_ON_SICK_LEAVE
    );

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final DocumentRepository documentRepository;
    private final JobHistoryRepository jobHistoryRepository;
    private final VacationRepository vacationRepository;
    private final TimesheetRepository timesheetRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           PositionRepository positionRepository,
                           DocumentRepository documentRepository,
                           JobHistoryRepository jobHistoryRepository,
                           VacationRepository vacationRepository,
                           TimesheetRepository timesheetRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.documentRepository = documentRepository;
        this.jobHistoryRepository = jobHistoryRepository;
        this.vacationRepository = vacationRepository;
        this.timesheetRepository = timesheetRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return findAll();
        }
        return employeeRepository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Працівника не знайдено."));
    }

    @Transactional(readOnly = true)
    public Employee createEmptyEmployee() {
        Employee employee = new Employee();
        employee.setStatus(Employee.STATUS_ACTIVE);
        employee.setHireDate(LocalDate.now());
        return employee;
    }

    @Transactional(readOnly = true)
    public List<String> getAvailableStatuses() {
        return List.of(
                Employee.STATUS_ACTIVE,
                Employee.STATUS_ON_VACATION,
                Employee.STATUS_ON_SICK_LEAVE,
                Employee.STATUS_DISMISSED
        );
    }

    @Transactional
    public Employee saveEmployee(Employee formEmployee,
                                 Integer departmentId,
                                 Integer positionId,
                                 MultipartFile avatarFile,
                                 MultipartFile hireDocumentFile,
                                 MultipartFile dismissalDocumentFile,
                                 MultipartFile transferDocumentFile) throws IOException {
        boolean isNew = formEmployee.getId() == null;
        Employee employee = isNew ? new Employee() : findById(formEmployee.getId());

        Department previousDepartment = employee.getDepartment();
        Position previousPosition = employee.getPosition();
        String previousStatus = normalizeStatus(employee.getStatus());

        Department department = resolveDepartment(departmentId);
        Position position = resolvePosition(positionId);
        if (department == null && position != null && position.getDepartment() != null) {
            department = position.getDepartment();
        }

        validateDepartmentAndPosition(department, position);
        copyEditableFields(formEmployee, employee);

        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setStatus(normalizeStatus(formEmployee.getStatus()));

        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }

        if (Employee.STATUS_DISMISSED.equals(employee.getStatus())) {
            if (employee.getDismissalDate() == null) {
                employee.setDismissalDate(LocalDate.now());
            }
        } else {
            employee.setDismissalDate(null);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            employee.setAvatar(avatarFile.getBytes());
            employee.setAvatarContentType(
                    StringUtils.hasText(avatarFile.getContentType()) ? avatarFile.getContentType() : "image/jpeg"
            );
        }

        Employee savedEmployee = employeeRepository.save(employee);

        upsertPersonnelDocument(savedEmployee, hireDocumentFile, Document.CATEGORY_HIRING);
        upsertPersonnelDocument(savedEmployee, dismissalDocumentFile, Document.CATEGORY_DISMISSAL);
        upsertPersonnelDocument(savedEmployee, transferDocumentFile, Document.CATEGORY_TRANSFER);

        syncJobHistory(savedEmployee, previousDepartment, previousPosition, previousStatus, isNew);
        return savedEmployee;
    }

    @Transactional
    public void dismissEmployee(Long id) {
        Employee employee = findById(id);
        if (Employee.STATUS_DISMISSED.equals(normalizeStatus(employee.getStatus()))) {
            return;
        }

        employee.setStatus(Employee.STATUS_DISMISSED);
        employee.setDismissalDate(LocalDate.now());
        createHistoryEntry(employee, "ЗВІЛЬНЕННЯ", employee.getDismissalDate(), true);
    }

    @Transactional
    public void deleteEmployeeById(Long id) {
        if (vacationRepository.existsByEmployeeId(id) || timesheetRepository.existsByEmployeeId(id)) {
            throw new IllegalStateException("Неможливо видалити працівника, поки для нього існують відпустки або табелі.");
        }

        Employee employee = findById(id);
        employeeRepository.delete(employee);
    }

    private void copyEditableFields(Employee source, Employee target) {
        target.setFirstName(trimToNull(source.getFirstName()));
        target.setLastName(trimToNull(source.getLastName()));
        target.setMiddleName(trimToNull(source.getMiddleName()));
        target.setGender(trimToNull(source.getGender()));
        target.setMaritalStatus(trimToNull(source.getMaritalStatus()));
        target.setInn(trimToNull(source.getInn()));
        target.setAddressRegistration(trimToNull(source.getAddressRegistration()));
        target.setAddressActual(trimToNull(source.getAddressActual()));
        target.setEmail(trimToNull(source.getEmail()));
        target.setPhoneMain(trimToNull(source.getPhoneMain()));
        target.setPhoneWork(trimToNull(source.getPhoneWork()));
        target.setBirthDate(source.getBirthDate());
        target.setHireDate(source.getHireDate());
    }

    private Department resolveDepartment(Integer departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Обраний департамент не існує."));
    }

    private Position resolvePosition(Integer positionId) {
        if (positionId == null) {
            return null;
        }
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new IllegalArgumentException("Обрана посада не існує."));
    }

    private void validateDepartmentAndPosition(Department department, Position position) {
        if (department == null || position == null || position.getDepartment() == null) {
            return;
        }

        if (!position.getDepartment().getId().equals(department.getId())) {
            throw new IllegalArgumentException("Посада не належить до вибраного департаменту.");
        }
    }

    private void syncJobHistory(Employee employee,
                                Department previousDepartment,
                                Position previousPosition,
                                String previousStatus,
                                boolean isNew) {
        if (isNew) {
            createHistoryEntry(employee, "ПРИЙНЯТТЯ", employee.getHireDate(), false);
            if (Employee.STATUS_DISMISSED.equals(employee.getStatus())) {
                createHistoryEntry(employee, "ЗВІЛЬНЕННЯ", employee.getDismissalDate(), true);
            }
            return;
        }

        String currentStatus = normalizeStatus(employee.getStatus());
        if (Employee.STATUS_DISMISSED.equals(currentStatus) && !Employee.STATUS_DISMISSED.equals(previousStatus)) {
            createHistoryEntry(employee, "ЗВІЛЬНЕННЯ", employee.getDismissalDate(), true);
            return;
        }

        if (hasDifferentDepartment(previousDepartment, employee.getDepartment())
                || hasDifferentPosition(previousPosition, employee.getPosition())) {
            createHistoryEntry(employee, "ПЕРЕВЕДЕННЯ", LocalDate.now(), true);
        }
    }

    private void createHistoryEntry(Employee employee, String eventType, LocalDate eventDate, boolean closePreviousEntry) {
        LocalDate effectiveDate = eventDate != null ? eventDate : LocalDate.now();
        if (closePreviousEntry) {
            closeOpenHistory(employee.getId(), effectiveDate);
        }

        JobHistory history = new JobHistory();
        history.setEmployee(employee);
        history.setDepartment(employee.getDepartment());
        history.setPosition(employee.getPosition());
        history.setStartDate(effectiveDate);
        history.setEndDate("ЗВІЛЬНЕННЯ".equals(eventType) ? effectiveDate : null);
        history.setPersonalSalary(resolveSalary(employee.getPosition()));
        history.setEventType(eventType);
        jobHistoryRepository.save(history);
    }

    private void closeOpenHistory(Long employeeId, LocalDate eventDate) {
        for (JobHistory history : jobHistoryRepository.findByEmployeeIdAndEndDateIsNullOrderByStartDateDescIdDesc(employeeId)) {
            LocalDate safeEndDate = eventDate;
            if (history.getStartDate() != null && eventDate.isBefore(history.getStartDate())) {
                safeEndDate = history.getStartDate();
            }
            history.setEndDate(safeEndDate);
        }
    }

    private void upsertPersonnelDocument(Employee employee, MultipartFile documentFile, String category) throws IOException {
        if (documentFile == null || documentFile.isEmpty()) {
            return;
        }

        Document document = documentRepository.findByEmployeeIdAndDocumentCategory(employee.getId(), category)
                .orElseGet(Document::new);
        document.setEmployee(employee);
        document.setDocumentCategory(category);
        document.setFileName(
                buildStoredFileName(category, documentFile.getOriginalFilename())
        );
        document.setFileType(
                StringUtils.hasText(documentFile.getContentType()) ? documentFile.getContentType() : "application/octet-stream"
        );
        document.setData(documentFile.getBytes());
        documentRepository.save(document);
    }

    private String buildStoredFileName(String category, String originalFileName) {
        String safeName = StringUtils.hasText(originalFileName) ? originalFileName : "document";
        return switch (category) {
            case Document.CATEGORY_HIRING -> "hire-" + safeName;
            case Document.CATEGORY_DISMISSAL -> "dismissal-" + safeName;
            case Document.CATEGORY_TRANSFER -> "transfer-" + safeName;
            default -> safeName;
        };
    }

    private BigDecimal resolveSalary(Position position) {
        return position != null ? position.getSalary() : null;
    }

    private boolean hasDifferentDepartment(Department previousDepartment, Department currentDepartment) {
        if (previousDepartment == null && currentDepartment == null) {
            return false;
        }
        if (previousDepartment == null || currentDepartment == null) {
            return true;
        }
        return !previousDepartment.getId().equals(currentDepartment.getId());
    }

    private boolean hasDifferentPosition(Position previousPosition, Position currentPosition) {
        if (previousPosition == null && currentPosition == null) {
            return false;
        }
        if (previousPosition == null || currentPosition == null) {
            return true;
        }
        return !previousPosition.getId().equals(currentPosition.getId());
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return Employee.STATUS_ACTIVE;
        }

        String trimmed = status.trim();
        return ALLOWED_STATUSES.contains(trimmed) ? trimmed : Employee.STATUS_ACTIVE;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
