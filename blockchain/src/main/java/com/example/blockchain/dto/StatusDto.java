package com.example.blockchain.dto;

public record StatusDto(
        String status,
        NodeInfo node,
        ChainInfo chain,
        PeersInfo peers
) {
    public record NodeInfo(String url, String address, String publicKey) {}
    public record ChainInfo(int length, String latestHash) {}
    public record PeersInfo(int count) {}
}