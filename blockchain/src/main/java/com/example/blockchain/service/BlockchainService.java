package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.TransactionType;
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
    private final long blockReward;
    private final int autoMineThreshold;
    private final WalletService walletService;

    // PeerService se inyecta después para evitar dependencia circular
    private PeerService peerService;

    public BlockchainService(
            @Value("${blockchain.difficulty:4}") int difficulty,
            @Value("${blockchain.block-reward:10}") long blockReward,
            @Value("${blockchain.auto-mine-threshold:3}") int autoMineThreshold,
            WalletService walletService
    ) {
        this.difficulty        = difficulty;
        this.blockReward       = blockReward;
        this.autoMineThreshold = autoMineThreshold;
        this.walletService     = walletService;
        this.chain             = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        chain.add(createGenesis());
        log.info("Blockchain inicializada. Dificultad: {}, génesis: {}", difficulty, getLatestBlock().getHash());
    }

    public void setPeerService(PeerService peerService) {
        this.peerService = peerService;
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
        if (tx.getType() == TransactionType.COINBASE) {
            throw new IllegalArgumentException("Las COINBASE no se agregan al mempool");
        }

        if (!hasSufficientBalance(tx)) {
            throw new IllegalArgumentException("Balance insuficiente");
        }

        boolean duplicate = pendingTransactions.stream()
                .anyMatch(t -> t.getId().equals(tx.getId()));
        if (duplicate) {
            throw new IllegalArgumentException("La transacción ya existe en el mempool");
        }

        if (transactionAlreadyInChain(tx)) {
            throw new IllegalArgumentException("La transacción ya fue confirmada");
        }

        pendingTransactions.add(tx);
        log.info("Tx agregada al mempool. Total: {}", pendingTransactions.size());

        // Minado automatico al llegar al threshold
        if (pendingTransactions.size() >= autoMineThreshold) {
            log.info("Threshold alcanzado ({}). Minando automáticamente...", autoMineThreshold);
            Block mined = mineBlockInternal("auto");
            if (peerService != null) {
                peerService.broadcastBlock(mined);
            }
        }
    }

    public List<Transaction> getPendingTransactions() {
        return List.copyOf(pendingTransactions);
    }

    public synchronized Block mineBlock() {
        return mineBlockInternal("manual");
    }

    private synchronized Block mineBlockInternal(String trigger) {
        long now = System.currentTimeMillis();

        // Armar transacciones del bloque: primero COINBASE, luego las TRANSFER del mempool
        List<Transaction> blockTxs = new ArrayList<>();
        blockTxs.add(Transaction.createCoinbase(walletService.getAddress(), now, blockReward));
        blockTxs.addAll(pendingTransactions);

        Block newBlock = new Block(
                getLatestBlock().getIndex() + 1,
                now,
                blockTxs,
                getLatestBlock().getHash()
        );

        log.info("Minando bloque #{} (trigger: {})...", newBlock.getIndex(), trigger);
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
        removePendingIncludedIn(newBlock);
        log.info("Bloque #{} minado: {}", newBlock.getIndex(), newBlock.getHash());
        return newBlock;
    }

    // Recibe un bloque de otro nodo, lo valida y si es correcto lo agrega
    public synchronized boolean receiveBlock(Block newBlock) {
        if (isValidNewBlock(newBlock, getLatestBlock())) {
            chain.add(newBlock);
            removePendingIncludedIn(newBlock);
            log.info("Bloque #{} recibido y agregado: {}", newBlock.getIndex(), newBlock.getHash());
            return true;
        }
        log.warn("Bloque #{} rechazado", newBlock.getIndex());
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

    private boolean hasSufficientBalance(Transaction tx) {
        if (tx.getType() != TransactionType.TRANSFER) {
            return true;
        }
        long confirmedBalance = getConfirmedBalance(tx.getFrom());
        long pendingOutgoing = getPendingOutgoingAmount(tx.getFrom());
        long availableBalance = confirmedBalance - pendingOutgoing;
        return availableBalance >= tx.getAmount();
    }

    //recorre toda la cadena y calcula el saldo confirmado
    private long getConfirmedBalance(String address) {
        long balance = 0;
        for (Block block : chain) {
            for (Transaction tx : block.getTransactions()) {
                if (address.equalsIgnoreCase(tx.getTo())) {
                    balance += tx.getAmount();
                }
                boolean isOutgoingTransfer =
                    tx.getType() == TransactionType.TRANSFER &&
                        address.equalsIgnoreCase(tx.getFrom());
                if (isOutgoingTransfer) {
                    balance -= tx.getAmount();
                }
            }
        }
        return balance;
    }

    //mira el mempool y suma cuanto ya tiene comprometido esa address en tx pendientes
    private long getPendingOutgoingAmount(String address) {
        return pendingTransactions.stream()
            .filter(tx -> tx.getType() == TransactionType.TRANSFER)
            .filter(tx -> address.equalsIgnoreCase(tx.getFrom()))
            .mapToLong(Transaction::getAmount)
            .sum();
    }


    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (newBlock == null || previousBlock == null) return false;
        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) return false;
        if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) return false;
        if (newBlock.getTimestamp() <= previousBlock.getTimestamp()) return false;
        if (newBlock.getTimestamp() > System.currentTimeMillis() + 120_000) return false;
        return newBlock.isValid(difficulty, blockReward);
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

    private void removePendingIncludedIn(Block block) {
        List<String> minedIds = block.getTransactions().stream()
                .map(Transaction::getId)
                .toList();
        pendingTransactions.removeIf(tx -> minedIds.contains(tx.getId()));
    }


    public Block getLatestBlock() {
        return chain.getLast();
    }

    public List<Block> getChain() {
        return List.copyOf(chain);
    }

}