package com.example.blockchain.dto;

// Lo que el usuario manda a POST /wallet/send
// La app pone el from, timestamp, publicKey y firma sola
public record SendTransactionDto(
        String to,
        long amount
) {}