package com.example.blockchain.dto;

public record ApiError(
        String status,
        ErrorDetail error
) {
    public record ErrorDetail(
            String code,
            String message
    ) {}
}