package com.example.blockchain.dto;

import java.util.List;

public record BlockDto(
        int index,
        long timestamp,
        List<TransactionDto> transactions,
        String previousHash,
        String hash,
        long nonce
) {}
