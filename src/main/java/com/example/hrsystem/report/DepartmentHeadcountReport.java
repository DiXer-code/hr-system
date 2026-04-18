package com.example.hrsystem.report;

import java.math.BigDecimal;

public record DepartmentHeadcountReport(
        String departmentName,
        Long totalEmployees,
        Long activeEmployees,
        Long vacationEmployees,
        Long sickLeaveEmployees,
        Long dismissedEmployees,
        BigDecimal payrollFund
) {
}
