package com.example.blockchain.dto;

import java.util.List;

public record ChainDto(
        String status,
        List<BlockDto> chain,
        int length
) {
    public static ChainDto ok(List<BlockDto> chain) {
        return new ChainDto("ok", chain, chain.size());
    }
}