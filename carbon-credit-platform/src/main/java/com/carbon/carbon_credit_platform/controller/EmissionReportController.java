package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.EmissionReport;
import com.carbon.carbon_credit_platform.service.EmissionReportService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/emissions")
public class EmissionReportController {

    private final EmissionReportService service;

    public EmissionReportController(EmissionReportService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public EmissionReport submitEmission(@RequestBody EmissionReport report) {
        return service.submitEmission(report);
    }
}