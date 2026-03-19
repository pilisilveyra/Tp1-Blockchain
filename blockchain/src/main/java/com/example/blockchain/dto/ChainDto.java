package com.example.blockchain.dto;

import java.util.List;

public record ChainDto(List<BlockDto> chain, int length) {}
