package com.example.blockchain.controller;

import com.example.blockchain.BlockMapper;
import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.dto.TransactionDto;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.BlockchainService;
import com.example.blockchain.service.PeerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final PeerService peerService;

    public BlockchainController(BlockchainService blockchainService, PeerService peerService) {
        this.blockchainService = blockchainService;
        this.peerService = peerService;
    }

    // GET /api/chain -> Devuelve la cadena completa
    @GetMapping("/chain")
    public ChainDto getChain() {
        return BlockMapper.toDto(blockchainService.getChain());
    }

    // POST /api/transaction -> agrega transaccion al mempool local
    // Body: { "from": "0xAlice", "to": "0xBob", "amount": 50, "signature": "0x..." }
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, String>> addTransaction(@RequestBody TransactionDto dto) {
        Transaction tx = BlockMapper.toModel(dto);
        blockchainService.addPendingTransaction(tx);
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Transacción agregada al mempool"
        ));
    }

    // GET /api/transactions/pending -> ver que hay en el mempool
    @GetMapping("/transactions/pending")
    public ResponseEntity<?> getPendingTransactions() {
        var pending = blockchainService.getPendingTransactions().stream()
                .map(BlockMapper::toDto)
                .toList();
        return ResponseEntity.ok(Map.of("pending", pending, "count", pending.size()));
    }

    // POST /api/mine -> Mina las transacciones pendientes, agrega el bloque y lo propaga a los peers
    @PostMapping("/mine")
    public ResponseEntity<?> mine() {
        Block minedBlock = blockchainService.mineBlock();
        peerService.broadcastBlock(minedBlock);
        return ResponseEntity.ok(BlockMapper.toDto(minedBlock));
    }

    // POST /api/block -> Recibe bloque minado por otro nodo, si es valido lo agrega
    @PostMapping("/block")
    public ResponseEntity<Map<String, String>> receiveBlock(@RequestBody BlockDto dto) {
        Block block = BlockMapper.toModel(dto);
        boolean added = blockchainService.receiveBlock(block);

        if (!added) {
            throw new IllegalArgumentException("Bloque inválido");
        }

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Bloque aceptado"
        ));
    }

    // POST /api/peers -> registra un nuevo peer
    // Body: { "url": "http://192.168.1.10:8080" }
    @PostMapping("/peers")
    public ResponseEntity<Map<String, Object>> registerPeer(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Falta el campo 'url'");
        }
        peerService.registerPeer(url);
        return ResponseEntity.ok(Map.of("status", "ok", "peers", peerService.getPeers()));
    }

    //* GET /api/peers -> Lista los peers conocidos
    @GetMapping("/peers")
    public ResponseEntity<Map<String, Object>> getPeers() {
        return ResponseEntity.ok(Map.of("peers", peerService.getPeers()));
    }

    // POST /api/peers/sync -> Pide la cadena a todos los peers y reemplaza la local si alguno tiene una más larga
    @PostMapping("/peers/sync")
    public ResponseEntity<Map<String, Object>> syncWithPeers() {
        peerService.syncWithPeers();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "chainLength", blockchainService.getChain().size()
        ));
    }

    @PostMapping("/peers/join")
    public ResponseEntity<Map<String, Object>> join(@RequestBody Map<String, String> body) {
        String newPeerUrl = body.get("url");
        if (newPeerUrl == null || newPeerUrl.isBlank()) {
            throw new IllegalArgumentException("Falta el campo 'url'");
        }
        // Le avisa a todos los peers existentes que llegó uno nuevo
        peerService.broadcastNewPeer(newPeerUrl);
        // Lo agrega a su propia lista
        peerService.registerPeer(newPeerUrl);
        // Le devuelve la lista de todos los peers para que los agregue
        return ResponseEntity.ok(Map.of(
                "peers", peerService.getPeers()
        ));
    }
}