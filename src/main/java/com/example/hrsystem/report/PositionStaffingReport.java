package com.example.hrsystem.report;

import java.math.BigDecimal;

public record PositionStaffingReport(
        String departmentName,
        String positionName,
        BigDecimal salary,
        Long assignedEmployees
) {
}
