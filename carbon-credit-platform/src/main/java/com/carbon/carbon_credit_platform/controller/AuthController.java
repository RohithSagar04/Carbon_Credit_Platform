package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import com.carbon.carbon_credit_platform.security.JwtUtil;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final CompanyRepository companyRepository;

    public AuthController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Company company) {

        Optional<Company> existing =
                companyRepository.findAll().stream()
                        .filter(c -> c.getEmail().equals(company.getEmail())
                                && c.getPassword().equals(company.getPassword()))
                        .findFirst();

        if (existing.isPresent()) {

            Company user = existing.get();

            String token = JwtUtil.generateToken(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRole());
            response.put("companyId", user.getId());

            return response;

        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    // ================= REGISTER =================
    @PostMapping("/register")
public Company register(@RequestBody Company company) {

    if (company.getRole() == null || company.getRole().isEmpty()) {
        throw new RuntimeException("Role is required");
    }

    String role = company.getRole().toUpperCase();

    // ❌ BLOCK ADMIN REGISTRATION
    if (role.equals("ADMIN")) {
        throw new RuntimeException("Admin cannot be registered");
    }

    company.setRole(role);

    return companyRepository.save(company);

    }
}