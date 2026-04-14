package com.carbon.carbon_credit_platform.service;

import com.carbon.carbon_credit_platform.entity.Company;
import com.carbon.carbon_credit_platform.entity.Transaction;
import com.carbon.carbon_credit_platform.repository.CompanyRepository;
import com.carbon.carbon_credit_platform.repository.TransactionRepository;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradingService {

    private final CompanyRepository companyRepository;
    private final TransactionRepository transactionRepository;

    public TradingService(CompanyRepository companyRepository,
                          TransactionRepository transactionRepository) {
        this.companyRepository = companyRepository;
        this.transactionRepository = transactionRepository;
    }

    // ================= SELL FLOW =================
    public void transferCredits(Long sellerId, Long buyerId, int credits) {

        Company seller = companyRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        // BUYER cannot sell
        if ("BUYER".equalsIgnoreCase(seller.getRole())) {
            throw new RuntimeException("Buyers cannot sell credits");
        }

        // Check balance
        if (seller.getCreditBalance() < credits) {
            throw new RuntimeException("Insufficient credits");
        }

        // Transfer
        seller.setCreditBalance(seller.getCreditBalance() - credits);
        buyer.setCreditBalance(buyer.getCreditBalance() + credits);

        companyRepository.save(seller);
        companyRepository.save(buyer);

        // Save transaction
        Transaction tx = new Transaction();
        tx.setSellerId(sellerId);
        tx.setBuyerId(buyerId);
        tx.setCredits(credits);

        transactionRepository.save(tx);
    }

    // ================= BUY FLOW =================
    public void buyCredits(Long buyerId, Long sellerId, int credits) {

        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        Company seller = companyRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Admin cannot buy
        if ("ADMIN".equalsIgnoreCase(buyer.getRole())) {
            throw new RuntimeException("Admin cannot buy");
        }

        // Seller must have credits
        if (seller.getCreditBalance() < credits) {
            throw new RuntimeException("Seller has insufficient credits");
        }

        // Transfer
        seller.setCreditBalance(seller.getCreditBalance() - credits);
        buyer.setCreditBalance(buyer.getCreditBalance() + credits);

        companyRepository.save(seller);
        companyRepository.save(buyer);

        // Save transaction
        Transaction tx = new Transaction();
        tx.setSellerId(sellerId);
        tx.setBuyerId(buyerId);
        tx.setCredits(credits);

        transactionRepository.save(tx);
    }

    // ================= ROLE BASED HISTORY =================
    public Object getTransactionsForUser(Long companyId, String role) {

        if (role == null) {
            throw new RuntimeException("Role is required");
        }

        // ADMIN → separate histories
        if (role.equalsIgnoreCase("ADMIN")) {

            Map<String, List<Transaction>> adminHistory = new HashMap<>();

            adminHistory.put("sellingHistory", transactionRepository.findAll());
            adminHistory.put("buyingHistory", transactionRepository.findAll());

            return adminHistory;
        }

        // SELLER → only sold transactions
        if (role.equalsIgnoreCase("SELLER")) {
            return transactionRepository.findBySellerId(companyId);
        }

        // BUYER → only bought transactions
        if (role.equalsIgnoreCase("BUYER")) {
            return transactionRepository.findByBuyerId(companyId);
        }

        throw new RuntimeException("Invalid role");
    }
}