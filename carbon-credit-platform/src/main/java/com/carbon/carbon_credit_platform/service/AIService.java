package com.carbon.carbon_credit_platform.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIService – bridges Spring Boot to the Python FastAPI AI engine.
 *
 * Existing endpoint  : /verify     (emission verification)
 * NEW endpoints      : /predict    (emission prediction – Linear Regression)
 *                      /price      (credit price recommendation)
 *                      /detect     (suspicious transaction – Isolation Forest)
 *
 * All new methods have a local fallback so the app keeps working
 * if the AI engine is offline.
 */
@Service
public class AIService {

    private static final String AI_BASE = "http://127.0.0.1:8000";

    // ─────────────────────────────────────────────────────────────────
    // EXISTING: /verify
    // ─────────────────────────────────────────────────────────────────
    public Map<String, Object> verifyEmission(double co2Emission) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("co2Emission", co2Emission);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(AI_BASE + "/verify", entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null || body.get("carbonScore") == null || body.get("fraudProbability") == null) {
                return localHeuristic(co2Emission);
            }
            return body;
        } catch (RestClientException ex) {
            return localHeuristic(co2Emission);
        }
    }

    private Map<String, Object> localHeuristic(double co2Emission) {
        int carbonScore = (int) Math.max(0, Math.min(100, 100 - co2Emission));
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("carbonScore", carbonScore);
        fallback.put("fraudProbability", 0.05);
        return fallback;
    }

    // ─────────────────────────────────────────────────────────────────
    // NEW: /predict  –  Emission Prediction (Linear Regression)
    // ─────────────────────────────────────────────────────────────────
    /**
     * Predict future carbon emissions based on historical data.
     *
     * @param companyId           company whose emissions are being forecast
     * @param historicalEmissions ordered list of past CO2 values (oldest → newest)
     * @param steps               number of future steps to forecast (1–12)
     * @return AI prediction result or local fallback
     */
    public Map<String, Object> predictEmissions(Long companyId,
                                                List<Double> historicalEmissions,
                                                int steps) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("companyId", companyId);
        request.put("historicalEmissions", historicalEmissions);
        request.put("steps", steps);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(AI_BASE + "/predict", entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return localPredictionFallback(historicalEmissions, steps);
            return body;
        } catch (RestClientException ex) {
            return localPredictionFallback(historicalEmissions, steps);
        }
    }

    /** Simple linear extrapolation fallback when AI engine is offline */
    private Map<String, Object> localPredictionFallback(List<Double> history, int steps) {
        double last = history.isEmpty() ? 50.0 : history.get(history.size() - 1);
        double prev = history.size() > 1 ? history.get(history.size() - 2) : last;
        double slope = last - prev;

        List<Double> predictions = new java.util.ArrayList<>();
        for (int i = 1; i <= steps; i++) {
            predictions.add(Math.max(0.0, Math.round((last + slope * i) * 100.0) / 100.0));
        }

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("predictedEmissions", predictions);
        fallback.put("trend", slope > 0.5 ? "increasing" : slope < -0.5 ? "decreasing" : "stable");
        fallback.put("slope", slope);
        fallback.put("reductionStrategies", List.of("Switch to renewable energy", "Optimise logistics"));
        fallback.put("modelUsed", "Local linear extrapolation (AI engine offline)");
        return fallback;
    }

    // ─────────────────────────────────────────────────────────────────
    // NEW: /price  –  Credit Price Recommendation
    // ─────────────────────────────────────────────────────────────────
    /**
     * Recommend a dynamic carbon credit price based on supply, demand, and scores.
     *
     * @param availableCredits total credits currently available (supply)
     * @param buyRequests      number of pending buy requests (demand)
     * @param avgCarbonScore   platform-wide average carbon score
     * @param previousPrice    last known market price (nullable)
     * @return recommended price result
     */
    public Map<String, Object> recommendPrice(int availableCredits,
                                              int buyRequests,
                                              double avgCarbonScore,
                                              Double previousPrice) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("availableCredits", availableCredits);
        request.put("buyRequests", buyRequests);
        request.put("avgCarbonScore", avgCarbonScore);
        if (previousPrice != null) {
            request.put("previousPrice", previousPrice);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(AI_BASE + "/price", entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return localPriceFallback(availableCredits, buyRequests, avgCarbonScore);
            return body;
        } catch (RestClientException ex) {
            return localPriceFallback(availableCredits, buyRequests, avgCarbonScore);
        }
    }

    private Map<String, Object> localPriceFallback(int supply, int demand, double score) {
        double ratio = (double) Math.max(demand, 1) / Math.max(supply, 1);
        double price = Math.round(25.0 * (1 + ratio * 0.3 + (score - 50) / 200.0) * 100) / 100.0;
        price = Math.max(5.0, Math.min(200.0, price));

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("recommendedPrice", price);
        fallback.put("currency", "USD");
        fallback.put("priceTrend", "stable");
        fallback.put("confidence", 0.6);
        fallback.put("explanation", "Local fallback – AI engine offline.");
        fallback.put("modelUsed", "Local heuristic (AI engine offline)");
        return fallback;
    }

    // ─────────────────────────────────────────────────────────────────
    // NEW: /detect  –  Suspicious Transaction Detection (Isolation Forest)
    // ─────────────────────────────────────────────────────────────────
    /**
     * Detect whether a transaction is suspicious.
     *
     * @param transactionId    ID of the transaction being evaluated
     * @param credits          number of credits in this transaction
     * @param buyerId          buyer company ID
     * @param sellerId         seller company ID
     * @param txCountLast24h   total transactions by this pair in last 24 h
     * @param avgCreditsPerTx  historical average credits per transaction
     * @return risk score + flags
     */
    public Map<String, Object> detectSuspicious(Long transactionId,
                                                double credits,
                                                Long buyerId,
                                                Long sellerId,
                                                int txCountLast24h,
                                                double avgCreditsPerTx) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("transactionId", transactionId);
        request.put("credits", credits);
        request.put("buyerId", buyerId);
        request.put("sellerId", sellerId);
        request.put("txCountLast24h", txCountLast24h);
        request.put("avgCreditsPerTx", avgCreditsPerTx);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(AI_BASE + "/detect", entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) return localDetectFallback(credits, buyerId, sellerId, txCountLast24h, avgCreditsPerTx);
            return body;
        } catch (RestClientException ex) {
            return localDetectFallback(credits, buyerId, sellerId, txCountLast24h, avgCreditsPerTx);
        }
    }

    private Map<String, Object> localDetectFallback(double credits, Long buyerId, Long sellerId,
                                                     int txCountLast24h, double avgCreditsPerTx) {
        boolean highVolume = credits > 2000;
        boolean highFreq = txCountLast24h > 20;
        boolean selfTx = buyerId.equals(sellerId);
        boolean spike = avgCreditsPerTx > 0 && credits > avgCreditsPerTx * 10;

        double risk = 0.1;
        java.util.List<String> flags = new java.util.ArrayList<>();

        if (highVolume) { risk += 0.3; flags.add("Unusually large credit volume (>2000)"); }
        if (highFreq)   { risk += 0.3; flags.add("High transaction frequency (>20 in 24h)"); }
        if (selfTx)     { risk += 0.4; flags.add("Self-transaction detected"); }
        if (spike)      { risk += 0.25; flags.add("Volume >10× historical average"); }

        risk = Math.min(1.0, risk);

        Map<String, Object> fallback = new HashMap<>();
        fallback.put("riskScore", Math.round(risk * 1000.0) / 1000.0);
        fallback.put("isSuspicious", risk > 0.4);
        fallback.put("flags", flags);
        fallback.put("modelUsed", "Local rule-based heuristic (AI engine offline)");
        return fallback;
    }
}