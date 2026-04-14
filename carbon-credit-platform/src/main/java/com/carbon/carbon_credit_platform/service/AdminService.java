package com.carbon.carbon_credit_platform.service;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final CompanyRepository companyRepository;

    public AdminService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

    public void assignCredits(Long companyId, int credits) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        company.setCreditBalance(company.getCreditBalance() + credits);
        companyRepository.save(company);
    }

    public void adminSellCredits(Long adminId, Long buyerId, int credits) {
        Company admin = companyRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (!admin.getRole().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("Only admin can sell credits");
        }

        if (admin.getCreditBalance() < credits) {
            throw new RuntimeException("Insufficient admin credits");
        }

        admin.setCreditBalance(admin.getCreditBalance() - credits);
        buyer.setCreditBalance(buyer.getCreditBalance() + credits);

        companyRepository.save(admin);
        companyRepository.save(buyer);
    }
}