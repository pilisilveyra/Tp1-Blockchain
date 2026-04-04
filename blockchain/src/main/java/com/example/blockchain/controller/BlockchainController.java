package com.example.blockchain.controller;

import com.example.blockchain.BlockMapper;
import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.dto.StatusDto;
import com.example.blockchain.dto.TransactionDto;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.BlockchainService;
import com.example.blockchain.service.PeerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final PeerService peerService;
    // agregar walletservice

    public BlockchainController(BlockchainService blockchainService, PeerService peerService) {
        this.blockchainService = blockchainService;
        this.peerService = peerService;
    }
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    /*  cuando este el wallet service se va a poder
    @GetMapping("/status")
    public StatusDto status() {
        Block latest = blockchainService.getLatestBlock();
        return new StatusDto(
                "ok",
                new StatusDto.NodeInfo(peerService.getMyUrl(), walletService.getAddress(), walletService.getPublicKeyHex()),
                new StatusDto.ChainInfo(blockchainService.getChain().size(), latest.getHash()),
                new StatusDto.PeersInfo(peerService.getPeers().size())
        );
    }
     */

    // Devuelve la cadena completa
    @GetMapping("/chain")
    public ChainDto getChain() {
        return BlockMapper.toDto(blockchainService.getChain());
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> addTransaction(@RequestBody TransactionDto dto) {
        if ("COINBASE".equalsIgnoreCase(dto.type())) {
            throw new IllegalArgumentException("Las COINBASE no se aceptan por este endpoint");
        }
        Transaction tx = BlockMapper.toModel(dto);
        blockchainService.addPendingTransaction(tx);
        peerService.broadcastTransaction(dto);
        return ResponseEntity.status(202).body(Map.of(
                "status", "ok",
                "accepted", true,
                "txId", tx.getId()
        ));
    }

    @GetMapping("/transactions/pending")
    public ResponseEntity<Map<String, Object>> getPendingTransactions() {
        var pending = blockchainService.getPendingTransactions().stream()
                .map(BlockMapper::toDto)
                .toList();
        return ResponseEntity.ok(Map.of("status", "ok", "pending", pending, "count", pending.size()));
    }

    @PostMapping("/mine")
    public ResponseEntity<Map<String, Object>> mine() {
        Block minedBlock = blockchainService.mineBlock();
        peerService.broadcastBlock(minedBlock);
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "mined", true,
                "trigger", "manual",
                "block", BlockMapper.toDto(minedBlock)
        ));
    }

    @PostMapping("/blocks")
    public ResponseEntity<Map<String, Object>> receiveBlock(@RequestBody BlockDto dto) {
        Block block = BlockMapper.toModel(dto);
        boolean added = blockchainService.receiveBlock(block);
        if (!added) {
            throw new IllegalArgumentException("Bloque inválido o ya conocido");
        }
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "accepted", true,
                "action", "appended",
                "chainLength", blockchainService.getChain().size()
        ));
    }

    @PostMapping("/peers")
    public ResponseEntity<Map<String, Object>> registerPeer(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Falta el campo 'url'");
        }
        peerService.registerPeer(url);
        peerService.broadcastNewPeer(url);
        var peerList = peerService.getPeers().stream().toList();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "registered", url,
                "peers", peerList
        ));
    }

    @GetMapping("/peers")
    public Map<String, Object> getPeers() {
        var peerList = peerService.getPeers().stream().toList();
        return Map.of("status", "ok", "peers", peerList, "count", peerList.size());
    }

    @PostMapping("/peers/sync")
    public ResponseEntity<Map<String, Object>> syncWithPeers() {
        peerService.syncWithPeers();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "chainLength", blockchainService.getChain().size()
        ));
    }
    /*
    faltan:
     @PostMapping("/wallet/send")
     @GetMapping("/wallet")
     */
}