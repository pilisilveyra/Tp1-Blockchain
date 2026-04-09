package com.example.blockchain.controller;

import com.example.blockchain.BlockMapper;
import com.example.blockchain.dto.*;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.BlockchainService;
import com.example.blockchain.service.PeerService;
import com.example.blockchain.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final PeerService peerService;
    private final WalletService walletService;
    // agregar walletservice

    public BlockchainController(BlockchainService blockchainService, PeerService peerService, WalletService walletService) {
        this.blockchainService = blockchainService;
        this.peerService = peerService;
        this.walletService = walletService;
    }
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

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

    // Devuelve la cadena completa
    @GetMapping("/chain")
    public ChainDto getChain() {
        return BlockMapper.toDto(blockchainService.getChain());
    }

    @PostMapping("/transactions")
    public ResponseEntity<Map<String, Object>> addTransaction(@RequestBody TransactionDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("INVALID_REQUEST: body requerido");
        }
        Transaction tx = BlockMapper.toModel(dto);
        blockchainService.addPendingTransaction(tx);

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
            throw new IllegalArgumentException("INVALID_BLOCK: Bloque inválido o ya conocido");
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
            throw new IllegalArgumentException("INVALID_REQUEST: Falta el campo url");
        }
        boolean added = peerService.registerPeer(url);
        if (added) {
            peerService.broadcastNewPeer(url);
        }
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

    @GetMapping("/wallet")
    public ResponseEntity<Map<String, Object>> getWallet() {
        String address = walletService.getAddress();
        String publicKey = walletService.getPublicKeyHex();

        long confirmedBalance = blockchainService.getConfirmedBalance(address);
        long availableBalance = blockchainService.getAvailableBalance(address);

        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "wallet", Map.of(
                "address", address,
                "publicKey", publicKey,
                "confirmedBalance", confirmedBalance,
                "availableBalance", availableBalance
            )
        ));
    }

    @PostMapping("/wallet/send")
    public ResponseEntity<Map<String, Object>> sendTransaction(@RequestBody SendTransactionDto dto) {
        try {
            TransactionDto tx = BlockMapper.toSignedTransferDto(dto, walletService);

            Transaction model = BlockMapper.toModel(tx);

            blockchainService.addPendingTransaction(model);
            System.out.println("Agregada al mempool OK");

            return ResponseEntity.status(202).body(Map.of(
                "status", "ok",
                "accepted", true,
                "txId", tx.id()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}