package com.carbon.carbon_credit_platform.service;

import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Service
public class BlockchainService {

    private final Web3j web3j;

    public BlockchainService() {
        this.web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));
    }

    public void logTransaction(Long sellerId, Long buyerId, int credits) {
        System.out.println("🔗 Blockchain log:");
        System.out.println("Seller: " + sellerId);
        System.out.println("Buyer: " + buyerId);
        System.out.println("Credits: " + credits);
    }
}