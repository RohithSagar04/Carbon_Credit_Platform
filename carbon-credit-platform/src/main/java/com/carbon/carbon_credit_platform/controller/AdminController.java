package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.service.AdminService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Load all companies
    @GetMapping("/companies")
    public List<Company> getAllCompanies() {
        return adminService.getAllCompanies();
    }

    // Search company by ID
    @GetMapping("/company/{id}")
    public Company getCompanyById(@PathVariable Long id) {
        return adminService.getCompanyById(id);
    }

    // Assign credits
    @PostMapping("/assignCredits")
    public String assignCredits(
            @RequestParam Long companyId,
            @RequestParam int credits) {

        adminService.assignCredits(companyId, credits);
        return "Credits assigned successfully";
    }

    // Sell credits from admin
    @PostMapping("/sellCredits")
    public String sellCredits(
            @RequestParam Long adminId,
            @RequestParam Long buyerId,
            @RequestParam int credits) {

        adminService.adminSellCredits(adminId, buyerId, credits);
        return "Admin sold credits successfully";
    }
}