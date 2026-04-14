package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // ✅ Register company
    @PostMapping("/register")
    public Company registerCompany(@RequestBody Company company) {
        return companyRepository.save(company);
    }
    @GetMapping("/{id}")
public Company getCompanyById(@PathVariable Long id) {
    return companyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Company not found"));
}
    // ✅ Get all companies
    @GetMapping("/all")
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }
}