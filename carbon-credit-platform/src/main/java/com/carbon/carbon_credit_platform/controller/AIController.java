package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.entity.EmissionReport;
import com.carbon.carbon_credit_platform.entity.Transaction;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import com.carbon.carbon_credit_platform.repository.EmissionReportRepository;
import com.carbon.carbon_credit_platform.repository.TransactionRepository;
import com.carbon.carbon_credit_platform.service.AIService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private final AIService aiService;
    private final EmissionReportRepository emissionReportRepository;
    private final CompanyRepository companyRepository;
    private final TransactionRepository transactionRepository;

    public AIController(AIService aiService,
                        EmissionReportRepository emissionReportRepository,
                        CompanyRepository companyRepository,
                        TransactionRepository transactionRepository) {
        this.aiService = aiService;
        this.emissionReportRepository = emissionReportRepository;
        this.companyRepository = companyRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping("/predict")
    public Map<String, Object> predictEmissions(@RequestBody PredictionRequest request) {
        if (request.getCompanyId() == null) {
            throw new RuntimeException("companyId is required");
        }

        List<Double> history = request.getHistoricalEmissions();
        if (history == null || history.isEmpty()) {
            history = emissionReportRepository.findByCompanyId(request.getCompanyId())
                    .stream()
                    .map(EmissionReport::getCo2Emission)
                    .collect(Collectors.toList());
        }

        if (history == null || history.isEmpty()) {
            history = List.of(40.0, 42.0, 41.0, 39.0);
        }

        int steps = request.getSteps() == null ? 3 : Math.max(1, Math.min(12, request.getSteps()));
        return aiService.predictEmissions(request.getCompanyId(), history, steps);
    }

    @GetMapping("/price")
    public Map<String, Object> recommendPrice(
            @RequestParam(required = false) Integer availableCredits,
            @RequestParam(required = false) Integer buyRequests,
            @RequestParam(required = false) Double avgCarbonScore,
            @RequestParam(required = false) Double previousPrice
    ) {
        List<Company> companies = companyRepository.findAll();
        List<Transaction> transactions = transactionRepository.findAll();

        int computedSupply = companies.stream()
                .mapToInt(Company::getCreditBalance)
                .sum();

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        int computedDemand = (int) transactions.stream()
                .filter(tx -> tx.getTransactionTime() != null && tx.getTransactionTime().isAfter(last24h))
                .count();

        double computedAvgScore = companies.isEmpty()
                ? 50.0
                : companies.stream().mapToInt(Company::getCarbonScore).average().orElse(50.0);

        return aiService.recommendPrice(
                availableCredits != null ? availableCredits : computedSupply,
                buyRequests != null ? buyRequests : computedDemand,
                avgCarbonScore != null ? avgCarbonScore : computedAvgScore,
                previousPrice
        );
    }

    @PostMapping("/detect")
    public Map<String, Object> detectTransaction(@RequestBody DetectRequest request) {
        if (request.getCredits() == null || request.getBuyerId() == null || request.getSellerId() == null) {
            throw new RuntimeException("credits, buyerId, and sellerId are required");
        }

        int txCountLast24h = resolvePairTxCountLast24h(request.getBuyerId(), request.getSellerId());
        double avgCreditsPerTx = resolvePairAvgCredits(request.getBuyerId(), request.getSellerId());

        return aiService.detectSuspicious(
                request.getTransactionId(),
                request.getCredits(),
                request.getBuyerId(),
                request.getSellerId(),
                txCountLast24h > 0 ? txCountLast24h : (request.getTxCountLast24h() == null ? 1 : request.getTxCountLast24h()),
                avgCreditsPerTx > 0 ? avgCreditsPerTx : (request.getAvgCreditsPerTx() == null ? 50.0 : request.getAvgCreditsPerTx())
        );
    }

    @GetMapping("/detect/all")
    public List<Map<String, Object>> detectAllTransactions() {
        List<Transaction> all = transactionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Transaction tx : all) {
            int txCountLast24h = resolvePairTxCountLast24h(tx.getBuyerId(), tx.getSellerId());
            double avgCreditsPerTx = resolvePairAvgCredits(tx.getBuyerId(), tx.getSellerId());

            Map<String, Object> aiResult = aiService.detectSuspicious(
                    tx.getId(),
                    tx.getCredits(),
                    tx.getBuyerId(),
                    tx.getSellerId(),
                    Math.max(1, txCountLast24h),
                    avgCreditsPerTx > 0 ? avgCreditsPerTx : tx.getCredits()
            );

            aiResult.put("transactionId", tx.getId());
            aiResult.put("buyerId", tx.getBuyerId());
            aiResult.put("sellerId", tx.getSellerId());
            aiResult.put("credits", tx.getCredits());
            aiResult.put("transactionTime", tx.getTransactionTime());
            result.add(aiResult);
        }

        return result;
    }

    private int resolvePairTxCountLast24h(Long buyerId, Long sellerId) {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        return (int) transactionRepository.findAll().stream()
                .filter(tx -> Objects.equals(tx.getBuyerId(), buyerId))
                .filter(tx -> Objects.equals(tx.getSellerId(), sellerId))
                .filter(tx -> tx.getTransactionTime() != null && tx.getTransactionTime().isAfter(last24h))
                .count();
    }

    private double resolvePairAvgCredits(Long buyerId, Long sellerId) {
        return transactionRepository.findAll().stream()
                .filter(tx -> Objects.equals(tx.getBuyerId(), buyerId))
                .filter(tx -> Objects.equals(tx.getSellerId(), sellerId))
                .mapToInt(Transaction::getCredits)
                .average()
                .orElse(0.0);
    }

    public static class PredictionRequest {
        private Long companyId;
        private List<Double> historicalEmissions;
        private Integer steps;

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        public List<Double> getHistoricalEmissions() {
            return historicalEmissions;
        }

        public void setHistoricalEmissions(List<Double> historicalEmissions) {
            this.historicalEmissions = historicalEmissions;
        }

        public Integer getSteps() {
            return steps;
        }

        public void setSteps(Integer steps) {
            this.steps = steps;
        }
    }

    public static class DetectRequest {
        private Long transactionId;
        private Double credits;
        private Long buyerId;
        private Long sellerId;
        private Integer txCountLast24h;
        private Double avgCreditsPerTx;

        public Long getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(Long transactionId) {
            this.transactionId = transactionId;
        }

        public Double getCredits() {
            return credits;
        }

        public void setCredits(Double credits) {
            this.credits = credits;
        }

        public Long getBuyerId() {
            return buyerId;
        }

        public void setBuyerId(Long buyerId) {
            this.buyerId = buyerId;
        }

        public Long getSellerId() {
            return sellerId;
        }

        public void setSellerId(Long sellerId) {
            this.sellerId = sellerId;
        }

        public Integer getTxCountLast24h() {
            return txCountLast24h;
        }

        public void setTxCountLast24h(Integer txCountLast24h) {
            this.txCountLast24h = txCountLast24h;
        }

        public Double getAvgCreditsPerTx() {
            return avgCreditsPerTx;
        }

        public void setAvgCreditsPerTx(Double avgCreditsPerTx) {
            this.avgCreditsPerTx = avgCreditsPerTx;
        }
    }
}
