package com.carbon.carbon_credit_platform.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    private static final String AI_URL = "http://127.0.0.1:8000/verify";

    public Map<String, Object> verifyEmission(double co2Emission) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("co2Emission", co2Emission);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(AI_URL, entity, Map.class);
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
}