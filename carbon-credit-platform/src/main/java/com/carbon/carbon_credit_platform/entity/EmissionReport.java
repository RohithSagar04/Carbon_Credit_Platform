package com.carbon.carbon_credit_platform.entity;

import jakarta.persistence.*;

@Entity
public class EmissionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private double co2Emission;

    // NEW FIELDS (AI results)
    private int carbonScore;

    private double fraudProbability;

    public EmissionReport() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public double getCo2Emission() {
        return co2Emission;
    }

    public void setCo2Emission(double co2Emission) {
        this.co2Emission = co2Emission;
    }

    public int getCarbonScore() {
        return carbonScore;
    }

    public void setCarbonScore(int carbonScore) {
        this.carbonScore = carbonScore;
    }

    public double getFraudProbability() {
        return fraudProbability;
    }

    public void setFraudProbability(double fraudProbability) {
        this.fraudProbability = fraudProbability;
    }
}