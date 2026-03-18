package com.example.blockchain.controller;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.dto.TransactionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BlockchainController {

    // GET /api/chain -> devuelve tu cadena completa
    @GetMapping("/chain")
    public ChainDto getChain() {
        return null;
    }

    // POST /api/block -> recibis un bloque minado por otro nodo
    @PostMapping("/block")
    public ResponseEntity<String> receiveBlock(@RequestBody BlockDto blockDto) {
        return ResponseEntity.ok("Bloque recibido");
    }

    // POST /api/mine -> creas/minas un bloque a partir de transacciones
    @PostMapping("/mine")
    public BlockDto mineBlock(@RequestBody List<TransactionDto> transactions) {
        return null;
    }
    // agregar una transaccion: @PostMapping("/transaction")
    // ver las transacciones pendientes: @GetMapping("/transactions/pending")
}