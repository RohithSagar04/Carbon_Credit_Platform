package com.carbon.carbon_credit_platform.repository;

import com.carbon.carbon_credit_platform.entity.EmissionReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmissionReportRepository extends JpaRepository<EmissionReport, Long> {

    // NEW: find all emission reports for a specific company (used by /ai/predict)
    List<EmissionReport> findByCompanyId(Long companyId);
}