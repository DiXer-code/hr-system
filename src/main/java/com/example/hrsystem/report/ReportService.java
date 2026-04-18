package com.example.hrsystem.report;

import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.JobHistory;
import com.example.hrsystem.entity.Vacation;
import com.example.hrsystem.repository.DepartmentRepository;
import com.example.hrsystem.repository.DocumentRepository;
import com.example.hrsystem.repository.EmployeeRepository;
import com.example.hrsystem.repository.JobHistoryRepository;
import com.example.hrsystem.repository.PositionRepository;
import com.example.hrsystem.repository.TimesheetRepository;
import com.example.hrsystem.repository.VacationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private static final List<String> STATUS_ORDER = List.of(
            Employee.STATUS_ACTIVE,
            Employee.STATUS_ON_VACATION,
            Employee.STATUS_ON_SICK_LEAVE,
            Employee.STATUS_DISMISSED
    );

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final DocumentRepository documentRepository;
    private final JobHistoryRepository jobHistoryRepository;
    private final VacationRepository vacationRepository;
    private final TimesheetRepository timesheetRepository;

    public ReportService(EmployeeRepository employeeRepository,
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
    public ReportDashboard buildDashboard() {
        LocalDate today = LocalDate.now();

        List<DepartmentHeadcountReport> departmentReports = normalizeDepartmentReports();
        List<StatusDistributionReport> statusReports = normalizeStatusReports();
        List<PositionStaffingReport> positionReports = normalizePositionReports();
        List<Employee> recentHires = employeeRepository.findTop6ByOrderByHireDateDescIdDesc();
        List<JobHistory> recentEvents = jobHistoryRepository.findTop8ByOrderByStartDateDescIdDesc();
        List<Vacation> activeVacations = vacationRepository.findActiveOn(today);

        return new ReportDashboard(
                employeeRepository.count(),
                employeeRepository.countByStatus(Employee.STATUS_ACTIVE),
                departmentRepository.count(),
                documentRepository.count(),
                vacationRepository.countActiveOn(today),
                Optional.ofNullable(timesheetRepository.sumWorkedHoursSince(today.minusDays(30))).orElse(0),
                departmentReports,
                statusReports,
                positionReports,
                recentHires,
                recentEvents,
                activeVacations
        );
    }

    private List<DepartmentHeadcountReport> normalizeDepartmentReports() {
        List<DepartmentHeadcountReport> rows = new ArrayList<>();
        for (DepartmentHeadcountReport row : employeeRepository.buildDepartmentHeadcountReport()) {
            rows.add(sanitizeDepartmentRow(row));
        }

        DepartmentHeadcountReport withoutDepartment = sanitizeDepartmentRow(
                employeeRepository.buildUnassignedDepartmentSummary()
        );
        if (withoutDepartment.totalEmployees() > 0) {
            rows.add(withoutDepartment);
        }

        rows.sort(Comparator
                .comparingLong((DepartmentHeadcountReport row) -> row.totalEmployees() != null ? row.totalEmployees() : 0L)
                .reversed()
                .thenComparing(DepartmentHeadcountReport::departmentName));
        return rows;
    }

    private DepartmentHeadcountReport sanitizeDepartmentRow(DepartmentHeadcountReport row) {
        if (row == null) {
            return new DepartmentHeadcountReport("Без департаменту", 0L, 0L, 0L, 0L, 0L, BigDecimal.ZERO);
        }

        return new DepartmentHeadcountReport(
                row.departmentName() != null ? row.departmentName() : "Без департаменту",
                defaultLong(row.totalEmployees()),
                defaultLong(row.activeEmployees()),
                defaultLong(row.vacationEmployees()),
                defaultLong(row.sickLeaveEmployees()),
                defaultLong(row.dismissedEmployees()),
                row.payrollFund() != null ? row.payrollFund() : BigDecimal.ZERO
        );
    }

    private List<StatusDistributionReport> normalizeStatusReports() {
        List<StatusDistributionReport> rows = new ArrayList<>();
        List<StatusDistributionReport> rawRows = employeeRepository.buildStatusDistribution();

        for (String status : STATUS_ORDER) {
            long count = rawRows.stream()
                    .filter(row -> status.equals(row.status()))
                    .map(StatusDistributionReport::employeeCount)
                    .findFirst()
                    .orElse(0L);
            rows.add(new StatusDistributionReport(status, count));
        }

        for (StatusDistributionReport rawRow : rawRows) {
            if (STATUS_ORDER.contains(rawRow.status())) {
                continue;
            }
            rows.add(new StatusDistributionReport(rawRow.status(), rawRow.employeeCount()));
        }

        return rows;
    }

    private List<PositionStaffingReport> normalizePositionReports() {
        List<PositionStaffingReport> rows = new ArrayList<>();
        for (PositionStaffingReport row : positionRepository.buildPositionStaffingReport()) {
            rows.add(new PositionStaffingReport(
                    row.departmentName() != null ? row.departmentName() : "Без департаменту",
                    row.positionName(),
                    row.salary() != null ? row.salary() : BigDecimal.ZERO,
                    defaultLong(row.assignedEmployees())
            ));
        }
        return rows;
    }

    private long defaultLong(Long value) {
        return value != null ? value : 0L;
    }
}
