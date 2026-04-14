package com.carbon.carbon_credit_platform.service;

import com.carbon.carbon_credit_platform.entity.EmissionReport;
import com.carbon.carbon_credit_platform.repository.EmissionReportRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmissionReportService {

    private final EmissionReportRepository repository;
    private final AIService aiService;

    public EmissionReportService(EmissionReportRepository repository, AIService aiService) {
        this.repository = repository;
        this.aiService = aiService;
    }

    public EmissionReport submitEmission(EmissionReport report) {

        // 1. Call AI Service
        Map<String, Object> aiResponse =
                aiService.verifyEmission(report.getCo2Emission());

        // 2. Extract values from AI response
        int carbonScore = ((Number) aiResponse.get("carbonScore")).intValue();
        double fraudProbability = ((Number) aiResponse.get("fraudProbability")).doubleValue();

        // 3. Set values into report
        report.setCarbonScore(carbonScore);
        report.setFraudProbability(fraudProbability);

        // 4. Save into database
        return repository.save(report);
    }
}