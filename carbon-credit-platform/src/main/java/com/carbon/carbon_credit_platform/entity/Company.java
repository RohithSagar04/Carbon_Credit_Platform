package com.carbon.carbon_credit_platform.entity;

import jakarta.persistence.*;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String role; // ADMIN / BUYER / SELLER
    private String companyName;
    private String email;
    private String password;
    private int carbonScore;
    private int creditBalance;

    public Company() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }
    public String getRole() {
    return role;
    }

    public void setRole(String role) {
    this.role = role;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCarbonScore() {
        return carbonScore;
    }

    public void setCarbonScore(int carbonScore) {
        this.carbonScore = carbonScore;
    }

    public int getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(int creditBalance) {
        this.creditBalance = creditBalance;
    }
}