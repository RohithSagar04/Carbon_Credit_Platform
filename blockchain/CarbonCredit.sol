// SPDX-License-Identifier: MIT
pragma solidity ^0.8.7;

contract CarbonCredit {

    struct Transaction {
        address buyer;
        address seller;
        uint credits;
        uint timestamp;
    }

    Transaction[] public transactions;

    function addTransaction(address buyer, address seller, uint credits) public {
        transactions.push(Transaction(buyer, seller, credits, block.timestamp));
    }

    function getTransaction(uint index) public view returns (
        address, address, uint, uint
    ) {
        Transaction memory txn = transactions[index];
        return (txn.buyer, txn.seller, txn.credits, txn.timestamp);
    }
}