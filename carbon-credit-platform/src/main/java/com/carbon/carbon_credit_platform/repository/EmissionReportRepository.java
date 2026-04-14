package com.carbon.carbon_credit_platform.repository;

import com.carbon.carbon_credit_platform.entity.EmissionReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmissionReportRepository extends JpaRepository<EmissionReport, Long> {
}