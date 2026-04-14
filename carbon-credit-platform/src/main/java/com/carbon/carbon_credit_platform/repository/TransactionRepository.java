package com.carbon.carbon_credit_platform.repository;

import com.carbon.carbon_credit_platform.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Seller only
    List<Transaction> findBySellerId(Long sellerId);

    // Buyer only
    List<Transaction> findByBuyerId(Long buyerId);
}