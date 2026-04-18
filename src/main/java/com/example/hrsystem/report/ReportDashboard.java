package com.example.hrsystem.report;

import com.example.hrsystem.entity.Employee;
import com.example.hrsystem.entity.JobHistory;
import com.example.hrsystem.entity.Vacation;

import java.util.List;

public record ReportDashboard(
        long totalEmployees,
        long activeEmployees,
        long departmentCount,
        long documentCount,
        long activeVacationCount,
        int workedHoursLast30Days,
        List<DepartmentHeadcountReport> departmentReports,
        List<StatusDistributionReport> statusReports,
        List<PositionStaffingReport> positionReports,
        List<Employee> recentHires,
        List<JobHistory> recentEvents,
        List<Vacation> activeVacations
) {
}
