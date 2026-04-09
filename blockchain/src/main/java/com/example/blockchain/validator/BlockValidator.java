package com.example.blockchain.validator;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;

import java.util.List;

public class BlockValidator {

    private final TransactionValidator transactionValidator = new TransactionValidator();

    public boolean isValidBlockStructure(Block block, int difficulty, long blockReward) {
        if (block == null) return false;
        if (block.getIndex() < 0) return false;
        if (block.getTimestamp() <= 0) return false;
        if (block.getPreviousHash() == null || block.getPreviousHash().isBlank()) return false;
        if (block.getHash() == null || block.getHash().isBlank()) return false;
        if (block.getNonce() < 0) return false;

        if (!block.getHash().equals(block.calculateHash())) return false;
        if (!block.getHash().startsWith("0".repeat(difficulty))) return false;

        if (block.isGenesis()) {
            return "0".equals(block.getPreviousHash()) && block.getTransactions().isEmpty();
        }

        return hasValidTransactions(block, blockReward);
    }

    public boolean hasValidTransactions(Block block, long blockReward) {
        List<Transaction> transactions = block.getTransactions();
        if (transactions == null || transactions.isEmpty()) return false;

        long coinbaseCount = transactions.stream().filter(Transaction::isCoinbase).count();
        if (coinbaseCount != 1) return false;
        if (!transactions.getFirst().isCoinbase()) return false;

        Transaction coinbase = transactions.getFirst();
        if (!transactionValidator.isValidCoinbase(coinbase)) return false;
        if (coinbase.getAmount() != blockReward) return false;
        if (coinbase.getTimestamp() != block.getTimestamp()) return false;

        return transactions.stream()
                .skip(1)
                .allMatch(tx -> tx.isTransfer() && transactionValidator.isValidTransfer(tx));
    }
}
