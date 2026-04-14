package com.carbon.carbon_credit_platform.repository;

import com.carbon.carbon_credit_platform.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}