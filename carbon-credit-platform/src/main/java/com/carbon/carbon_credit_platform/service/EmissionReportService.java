package com.carbon.carbon_credit_platform.service;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.entity.EmissionReport;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import com.carbon.carbon_credit_platform.repository.EmissionReportRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmissionReportService {

    private final EmissionReportRepository repository;
    private final CompanyRepository companyRepository;
    private final AIService aiService;

    public EmissionReportService(EmissionReportRepository repository, 
                                 CompanyRepository companyRepository, 
                                 AIService aiService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.aiService = aiService;
    }

    public EmissionReport submitEmission(EmissionReport report) {

        // 1. Call AI Service to verify emission
        Map<String, Object> aiResponse = aiService.verifyEmission(report.getCo2Emission());

        // 2. Extract values from AI response
        int carbonScore = ((Number) aiResponse.get("carbonScore")).intValue();
        double fraudProbability = ((Number) aiResponse.get("fraudProbability")).doubleValue();

        // 3. Set values into report
        report.setCarbonScore(carbonScore);
        report.setFraudProbability(fraudProbability);

        // 4. Update the Company's carbon score and allocate credits
        Company company = companyRepository.findById(report.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found with ID: " + report.getCompanyId()));

        company.setCarbonScore(carbonScore);
        
        // Allocate credits based on score (e.g., higher score gets more credits)
        int allocatedCredits = carbonScore * 10;
        company.setCreditBalance(company.getCreditBalance() + allocatedCredits);

        companyRepository.save(company);

        // 5. Save report into database
        return repository.save(report);
    }
}