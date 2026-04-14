package com.carbon.carbon_credit_platform.service;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company registerCompany(Company company) {
        return companyRepository.save(company);
    }
}