package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.TransactionType;
import com.example.blockchain.util.GenesisParams;
import com.example.blockchain.validator.BlockValidator;
import com.example.blockchain.validator.TransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);
    private static final long MAX_FUTURE_BLOCK_DRIFT_MS = 120_000L;

    private final List<Block> chain;
    private final List<Transaction> pendingTransactions;
    private final int difficulty;
    private final long blockReward;
    private final int autoMineThreshold;
    private final WalletService walletService;
    private final TransactionValidator transactionValidator = new TransactionValidator();
    private final BlockValidator blockValidator = new BlockValidator();

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
        Block genesis = new Block(
                0,
                GenesisParams.GENESIS_TIMESTAMP,
                List.of(),
                GenesisParams.GENESIS_PREVIOUS_HASH
        );
        genesis.mineBlock(difficulty);
        return genesis;
    }

    public synchronized void addPendingTransaction(Transaction tx) {
        validateIncomingPendingTransaction(tx);
        pendingTransactions.add(tx);
        log.info("Tx agregada al mempool. Total: {}", pendingTransactions.size());

        tryAutoMine();
    }

    private void validateIncomingPendingTransaction(Transaction tx) {
        ensureTransferTransaction(tx);
        ensureValidTransfer(tx);
        ensureNotDuplicatedInMempool(tx);
        ensureNotAlreadyConfirmed(tx);
        ensureSufficientAvailableBalance(tx);
    }


    public List<Transaction> getPendingTransactions() {
        return List.copyOf(pendingTransactions);
    }

    public synchronized Block mineBlock() {
        return mineBlockInternal("manual");
    }

    // Recibe un bloque de otro nodo, lo valida y si es correcto lo agrega
    public synchronized boolean receiveBlock(Block newBlock) {
        if (!isStructurallyValidNextBlock(newBlock)) {
            log.warn("Bloque #{} rechazado por estructura", safeBlockIndex(newBlock));
            return false;
        }

        if (!isEconomicallyValidNextBlock(newBlock)) {
            log.warn("Bloque #{} rechazado por balances", safeBlockIndex(newBlock));
            return false;
        }

        appendBlockToLocalChain(newBlock);
        return true;
    }

    private void appendBlockToLocalChain(Block block) {
        chain.add(block);
        removePendingIncludedIn(block);
        log.info("Bloque #{} agregado: {}", block.getIndex(), block.getHash());
    }

    private boolean isStructurallyValidNextBlock(Block newBlock) {
        return isValidNewBlock(newBlock, getLatestBlock());
    }

    private boolean isEconomicallyValidNextBlock(Block newBlock) {
        return hasValidBalancesForNewBlock(newBlock);
    }

    // Reemplaza la cadena si la nueva es mas larga y es valida
    public synchronized boolean replaceChainIfValid(List<Block> newChain) {
        if (!isLongerThanCurrentChain(newChain)) {
            return false;
        }
        if (!isChainValid(newChain)) {
            return false;
        }
        chain.clear();
        chain.addAll(newChain);
        reconcilePendingTransactions();

        log.info("Cadena reemplazada. Nueva longitud: {}", chain.size());
        return true;
    }

    private synchronized Block mineBlockInternal(String trigger) {
        long blockTimestamp = System.currentTimeMillis();
        String minerAddress = walletService.getAddress();
        logMiningStart(trigger, minerAddress);

        List<Transaction> blockTransactions = buildNextBlockTransactions(minerAddress, blockTimestamp);
        Block candidateBlock = buildCandidateBlock(blockTimestamp, blockTransactions);
        mineAndValidateLocally(candidateBlock);
        appendBlockToLocalChain(candidateBlock);
        return candidateBlock;
    }

    private void logMiningStart(String trigger, String minerAddress) {
        log.info("=== mineBlockInternal trigger={} minerAddress={} ===", trigger, minerAddress);
    }

    private List<Transaction> buildNextBlockTransactions(String minerAddress, long blockTimestamp) {
        Transaction coinbase = Transaction.createCoinbase(minerAddress, blockTimestamp, blockReward);
        List<Transaction> selectedPending = selectValidPendingTransactionsForNextBlock();

        List<Transaction> blockTransactions = new ArrayList<>();
        blockTransactions.add(coinbase);
        blockTransactions.addAll(selectedPending);

        log.info("Coinbase creada -> to={} amount={}", coinbase.getTo(), coinbase.getAmount());
        logBlockTransactions(blockTransactions);

        return blockTransactions;
    }

    private void logBlockTransactions(List<Transaction> transactions) {
        log.info("Transacciones que van al bloque:");
        for (Transaction tx : transactions) {
            log.info(
                    "  TX id={} type={} from={} to={} amount={}",
                    tx.getId(),
                    tx.getType(),
                    tx.getFrom(),
                    tx.getTo(),
                    tx.getAmount()
            );
        }
    }

    private Block buildCandidateBlock(long blockTimestamp, List<Transaction> blockTransactions) {
        return new Block(
                getLatestBlock().getIndex() + 1,
                blockTimestamp,
                blockTransactions,
                getLatestBlock().getHash()
        );
    }

    private void mineAndValidateLocally(Block block) {
        log.info("Minando bloque #{}...", block.getIndex());
        block.mineBlock(difficulty);

        if (!isStructurallyValidNextBlock(block) || !isEconomicallyValidNextBlock(block)) {
            throw new IllegalStateException("INVALID_BLOCK: Bloque minado localmente inválido");
        }
    }


    private void reconcilePendingTransactions() {
        List<Transaction> snapshot = new ArrayList<>(pendingTransactions);
        pendingTransactions.clear();

        for (Transaction tx : snapshot) {
            if (shouldKeepInMempool(tx)) {
                pendingTransactions.add(tx);
            }
        }
    }

    private boolean shouldKeepInMempool(Transaction tx) {
        if (transactionAlreadyInChain(tx)) {
            return false;
        }

        if (!transactionValidator.isValidTransfer(tx)) {
            return false;
        }

        return getAvailableBalance(tx.getFrom()) >= tx.getAmount();
    }

    private boolean transactionAlreadyInChain(Transaction tx) {
        return chain.stream()
                .flatMap(block -> block.getTransactions().stream())
                .anyMatch(existing -> existing.getId().equals(tx.getId()));
    }


    public long getConfirmedBalance(String address) {
        return buildConfirmedBalances(chain).getOrDefault(address, 0L);
    }

    public long getAvailableBalance(String address) {
        return getConfirmedBalance(address) - getPendingOutgoingAmount(address);
    }


    //mira el mempool y suma cuanto ya tiene comprometido esa address en tx pendientes
    private long getPendingOutgoingAmount(String address) {
        return pendingTransactions.stream()
                .filter(Transaction::isTransfer)
                .filter(tx -> address.equalsIgnoreCase(tx.getFrom()))
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        return hasValidBlockLink(newBlock, previousBlock)
                && hasValidBlockTimestamp(newBlock, previousBlock)
                && blockValidator.isValidBlockStructure(newBlock, difficulty, blockReward);
    }

    private boolean hasValidBlockLink(Block newBlock, Block previousBlock) {
        if (newBlock == null || previousBlock == null) {
            return false;
        }

        return previousBlock.getIndex() + 1 == newBlock.getIndex()
                && previousBlock.getHash().equals(newBlock.getPreviousHash());
    }

    private boolean hasValidBlockTimestamp(Block newBlock, Block previousBlock) {
        long now = System.currentTimeMillis();

        return newBlock.getTimestamp() > previousBlock.getTimestamp()
                && newBlock.getTimestamp() <= now + MAX_FUTURE_BLOCK_DRIFT_MS;
    }

    private boolean applyBlockTransactions(Block block, Map<String, Long> balances) {
        List<Transaction> transactions = block.getTransactions();

        if (transactions.isEmpty()) {
            return false;
        }

        Transaction coinbase = transactions.getFirst();
        if (!isValidCoinbaseForBlock(coinbase)) {
            return false;
        }

        if (hasExtraCoinbase(transactions)) {
            return false;
        }

        applyCoinbase(coinbase, balances);

        for (int i = 1; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);

            if (!tx.isTransfer()) {
                return false;
            }

            if (!transactionValidator.isValidTransfer(tx)) {
                return false;
            }

            if (!canApplyTransfer(tx, balances)) {
                return false;
            }

            applyTransfer(tx, balances);
        }

        return true;
    }

    private boolean isValidCoinbaseForBlock(Transaction coinbase) {
        return coinbase.getType() == TransactionType.COINBASE //q la primera tx sea coinbase
            && coinbase.getAmount() == blockReward;
    }

    //Chequea que no haya una segunda coinbase en el mismo bloque
    private boolean hasExtraCoinbase(List<Transaction> transactions) {
        return transactions.stream()
            .skip(1)
            .anyMatch(tx -> tx.getType() == TransactionType.COINBASE);
    }

    private boolean hasValidBalances(List<Block> candidateChain) {
        try {
            buildConfirmedBalances(candidateChain);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private boolean hasValidBalancesForNewBlock(Block newBlock) {
        Map<String, Long> balances = buildConfirmedBalances(chain);
        return applyBlockTransactions(newBlock, balances);
    }

    private Map<String, Long> buildConfirmedBalances(List<Block> confirmedChain) {
        Map<String, Long> balances = new HashMap<>();

        for (int i = 1; i < confirmedChain.size(); i++) {
            Block block = confirmedChain.get(i);

            if (!applyBlockTransactions(block, balances)) {
                throw new IllegalStateException("Cadena confirmada inválida");
            }
        }

        return balances;
    }

    private void applyCoinbase(Transaction coinbase, Map<String, Long> balances) {
        balances.merge(coinbase.getTo(), coinbase.getAmount(), Long::sum);
    }

    private boolean canApplyTransfer(Transaction tx, Map<String, Long> balances) {
        return balances.getOrDefault(tx.getFrom(), 0L) >= tx.getAmount();
    }

    private void applyTransfer(Transaction tx, Map<String, Long> balances) {
        balances.merge(tx.getFrom(), -tx.getAmount(), Long::sum);
        balances.merge(tx.getTo(), tx.getAmount(), Long::sum);
    }

    public boolean isChainValid(List<Block> candidateChain) {
        if (candidateChain == null || candidateChain.isEmpty()) {
            return false;
        }

        if (!isValidGenesis(candidateChain.getFirst())) {
            return false;
        }

        for (int i = 1; i < candidateChain.size(); i++) {
            Block currentBlock = candidateChain.get(i);
            Block previousBlock = candidateChain.get(i - 1);

            if (!isValidNewBlock(currentBlock, previousBlock)) {
                return false;
            }
        }

        return hasValidBalances(candidateChain);
    }

    private boolean isValidGenesis(Block genesis) {
        Block expected = createGenesis();

        return genesis.getIndex() == expected.getIndex()
                && genesis.getTimestamp() == expected.getTimestamp()
                && genesis.getPreviousHash().equals(expected.getPreviousHash())
                && genesis.getHash().equals(expected.getHash())
                && genesis.getNonce() == expected.getNonce()
                && genesis.getTransactions().isEmpty();
    }

    private void removePendingIncludedIn(Block block) {
        List<String> includedIds = block.getTransactions().stream()
                .map(Transaction::getId)
                .toList();

        pendingTransactions.removeIf(tx -> includedIds.contains(tx.getId()));
    }


    public Block getLatestBlock() {
        return chain.getLast();
    }

    public List<Block> getChain() {
        return List.copyOf(chain);
    }

    private List<Transaction> selectValidPendingTransactionsForNextBlock() {
        Map<String, Long> projectedBalances = buildConfirmedBalances(chain);
        List<Transaction> selectedTransactions = new ArrayList<>();

        for (Transaction tx : pendingTransactions) {
            if (canBeIncludedInNextBlock(tx, projectedBalances)) {
                selectedTransactions.add(tx);
                applyTransfer(tx, projectedBalances);
            }
        }

        return selectedTransactions;
    }

    private boolean canBeIncludedInNextBlock(Transaction tx, Map<String, Long> projectedBalances) {
        if (!tx.isTransfer()) {
            return false;
        }

        if (!transactionValidator.isValidTransfer(tx)) {
            return false;
        }

        return canApplyTransfer(tx, projectedBalances);
    }

    private void ensureTransferTransaction(Transaction tx) {
        if (tx == null || !tx.isTransfer()) {
            throw new IllegalArgumentException("INVALID_TRANSACTION: Transacción inválida");
        }
    }

    private void ensureValidTransfer(Transaction tx) {
        if (!transactionValidator.isValidTransfer(tx)) {
            throw new IllegalArgumentException("INVALID_TRANSACTION: Transacción inválida");
        }
    }

    private void ensureNotDuplicatedInMempool(Transaction tx) {
        if (isAlreadyInMempool(tx)) {
            throw new IllegalArgumentException("INVALID_TRANSACTION: Transacción duplicada en mempool");
        }
    }

    private void ensureNotAlreadyConfirmed(Transaction tx) {
        if (transactionAlreadyInChain(tx)) {
            throw new IllegalArgumentException("INVALID_TRANSACTION: Transacción ya confirmada");
        }
    }

    private void ensureSufficientAvailableBalance(Transaction tx) {
        if (getAvailableBalance(tx.getFrom()) < tx.getAmount()) {
            throw new IllegalArgumentException("INVALID_TRANSACTION: Balance insuficiente");
        }
    }

    private boolean isAlreadyInMempool(Transaction tx) {
        return pendingTransactions.stream()
                .anyMatch(existing -> existing.getId().equals(tx.getId()));
    }

    private void tryAutoMine() {
        if (pendingTransactions.size() < autoMineThreshold) {
            return;
        }

        log.info(
                "Threshold alcanzado ({}). Minando automáticamente...",
                autoMineThreshold
        );

        Block minedBlock = mineBlockInternal("auto");
        broadcastBlockIfPossible(minedBlock);
    }

    private void broadcastBlockIfPossible(Block block) {
        if (peerService != null) {
            peerService.broadcastBlock(block);
        }
    }

    private int safeBlockIndex(Block block) {
        return block != null ? block.getIndex() : -1;
    }

    private boolean isLongerThanCurrentChain(List<Block> newChain) {
        return newChain != null && newChain.size() > chain.size();
    }


}