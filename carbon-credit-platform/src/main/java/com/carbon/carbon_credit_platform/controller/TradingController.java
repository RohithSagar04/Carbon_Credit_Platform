package com.carbon.carbon_credit_platform.controller;

import com.carbon.carbon_credit_platform.entity.Transaction;
import com.carbon.carbon_credit_platform.repository.TransactionRepository;
import com.carbon.carbon_credit_platform.service.TradingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")   // ✅ Fix CORS
@RestController
@RequestMapping("/trading")
public class TradingController {

    private final TradingService tradingService;
    private final TransactionRepository transactionRepository;

    // ✅ Constructor Injection
    public TradingController(TradingService tradingService,
                             TransactionRepository transactionRepository) {
        this.tradingService = tradingService;
        this.transactionRepository = transactionRepository;
    }

    // ================= TRANSFER CREDITS =================
    @PostMapping("/transfer")
    public ResponseEntity<String> transferCredits(
            @RequestParam Long sellerId,
            @RequestParam Long buyerId,
            @RequestParam int credits
    ) {
        tradingService.transferCredits(sellerId, buyerId, credits);
        return ResponseEntity.ok("Transaction successful");
    }

    // ================= GET TRANSACTION HISTORY =================
    @GetMapping("/history")
    public Object getHistory(
            @RequestParam Long companyId,
            @RequestParam String role) {
    
        return tradingService.getTransactionsForUser(companyId, role);
    }
}