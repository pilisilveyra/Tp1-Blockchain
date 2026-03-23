package com.example.blockchain.dto;

public record TransactionDto(
        String from,
        String to,
        double amount,
        String publicKey,
        String signature
) {}
