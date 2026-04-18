package com.example.hrsystem.controller;

import com.example.hrsystem.report.ReportDashboard;
import com.example.hrsystem.report.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String showReports(Model model) {
        ReportDashboard dashboard = reportService.buildDashboard();
        model.addAttribute("dashboard", dashboard);
        return "reports";
    }
}
