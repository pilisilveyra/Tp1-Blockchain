package com.example.blockchain.dto;

import com.example.blockchain.model.TransactionType;

public record TransactionDto(
        String id,
        String type,
        String from,
        String to,
        long amount,  // sin decimales, ya no mas double
        long timestamp,
        String publicKey,
        String signature
) {
    public TransactionType typeAsEnum() {
        return TransactionType.valueOf(type.toUpperCase());
    }
}
