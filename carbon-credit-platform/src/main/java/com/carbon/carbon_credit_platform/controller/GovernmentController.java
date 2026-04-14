package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/government")
public class GovernmentController {

    private final CompanyRepository companyRepository;

    public GovernmentController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostMapping("/approve")
    public Company approveCompany(@RequestParam Long companyId,
                                  @RequestParam int carbonScore) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Set official carbon score
        company.setCarbonScore(carbonScore);

        // Credit allocation logic
        if (carbonScore >= 80) {
            company.setCreditBalance(company.getCreditBalance() + 100);
        } else if (carbonScore >= 50) {
            company.setCreditBalance(company.getCreditBalance() + 50);
        } else {
            company.setCreditBalance(company.getCreditBalance() + 10);
        }

        return companyRepository.save(company);
    }
}