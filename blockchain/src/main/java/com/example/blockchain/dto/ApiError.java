package com.example.blockchain.dto;

public record ApiError(
        String error,
        long timestamp,
        String path
) {}