package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    private final List<Block> chain;
    private final List<Transaction> pendingTransactions;
    private final int difficulty;

    public BlockchainService(@Value("${blockchain.difficulty:3}") int difficulty) {
        this.difficulty = difficulty;
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        chain.add(createGenesis());
        log.info("Blockchain inicializada con dificultad {} y bloque génesis {}", difficulty, getLatestBlock().getHash());
    }

    private Block createGenesis() {
        Block genesis = new Block(0, 1700000000L, new ArrayList<>(), "0");
        genesis.mineBlock(difficulty);
        return genesis;
    }

    public synchronized void addPendingTransaction(Transaction tx) {
        if (tx == null || !tx.isValid()) {
            throw new IllegalArgumentException("Transacción inválida");
        }

        boolean alreadyPending = pendingTransactions.stream()
                .anyMatch(existing -> existing.getId().equals(tx.getId()));

        if (alreadyPending) {
            throw new IllegalArgumentException("La transacción ya existe en el mempool");
        }

        if (transactionAlreadyInChain(tx)) {
            throw new IllegalArgumentException("La transacción ya fue confirmada en un bloque");
        }

        pendingTransactions.add(tx);
        log.info("Transacción pendiente agregada. Total en mempool: {}", pendingTransactions.size());
    }

    private void removePendingTransactionsIncludedIn(Block block) {
        List<String> minedTxIds = block.getTransactions().stream()
                .map(Transaction::getId)
                .toList();

        pendingTransactions.removeIf(tx -> minedTxIds.contains(tx.getId()));
    }

    public List<Transaction> getPendingTransactions() {
        return List.copyOf(pendingTransactions);
    }


    // Mina las transacciones pendientes, agrega al bloque a la chain y borra las transacciones pendientes
    public synchronized Block mineBlock() {
        if (pendingTransactions.isEmpty()) {
            throw new IllegalStateException("No hay transacciones pendientes para minar");
        }
        Block newBlock = new Block(
                getLatestBlock().getIndex() + 1,
                System.currentTimeMillis(),
                new ArrayList<>(pendingTransactions),
                getLatestBlock().getHash()
        );
        log.info("Minando bloque #{}...", newBlock.getIndex());
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
        removePendingTransactionsIncludedIn(newBlock);
        log.info("Bloque #{} minado: {}", newBlock.getIndex(), newBlock.getHash());
        return newBlock;
    }

    // Recibe un bloque de otro nodo, lo valida y si es correcto lo agrega
    public synchronized boolean receiveBlock(Block newBlock) {
        if (isValidNewBlock(newBlock, getLatestBlock())) {
            chain.add(newBlock);
            removePendingTransactionsIncludedIn(newBlock);
            log.info("Bloque #{} recibido y agregado: {}", newBlock.getIndex(), newBlock.getHash());
            return true;
        }
        log.warn("Bloque #{} rechazado (inválido)", newBlock.getIndex());
        return false;
    }


    // Reemplaza la cadena si la nueva es mas larga y es valida
    public synchronized boolean replaceChainIfValid(List<Block> newChain) {
        if (newChain.size() > chain.size() && isChainValid(newChain)) {
            chain.clear();
            chain.addAll(newChain);
            log.info("Cadena reemplazada. Nueva longitud: {}", chain.size());
            return true;
        }
        return false;
    }

    private boolean transactionAlreadyInChain(Transaction tx) {
        return chain.stream()
                .flatMap(block -> block.getTransactions().stream())
                .anyMatch(existing -> existing.getId().equals(tx.getId()));
    }


    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (newBlock == null || previousBlock == null) return false;
        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) return false;
        if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) return false;
        if (newBlock.getTimestamp() <= previousBlock.getTimestamp()) return false;
        if (newBlock.getTimestamp() > System.currentTimeMillis() + 120_000) return false;
        if (!newBlock.hasValidTransactions()) return false;
        return newBlock.isValid(difficulty);
    }

    public boolean isChainValid(List<Block> chain) {
        if (chain == null || chain.isEmpty()) return false;
        if (!isValidGenesis(chain.getFirst())) return false;
        for (int i = 1; i < chain.size(); i++) {
            if (!isValidNewBlock(chain.get(i), chain.get(i - 1))) return false;
        }
        return true;
    }

    private boolean isValidGenesis(Block genesis) {
        Block expected = createGenesis();
        return genesis.getIndex() == expected.getIndex()
                && genesis.getTimestamp() == expected.getTimestamp()
                && genesis.getPreviousHash().equals(expected.getPreviousHash())
                && genesis.getHash().equals(expected.getHash())
                && genesis.getNonce() == expected.getNonce();
    }


    public Block getLatestBlock() {
        return chain.getLast();
    }

    public List<Block> getChain() {
        return List.copyOf(chain);
    }

    public int getDifficulty() {
        return difficulty;
    }
}