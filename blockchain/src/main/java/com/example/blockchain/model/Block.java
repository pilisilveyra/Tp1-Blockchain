package com.example.blockchain.model;

import com.example.blockchain.util.CryptoUtil;

import java.util.List;

public class Block {

    private final int index;
    private final long timestamp;
    private final List<Transaction> transactions;
    private final String previousHash;
    private String hash;
    private long nonce;

    public Block(int index, long timestamp, List<Transaction> transactions, String previousHash) {
        this.index = index;
        this.timestamp = timestamp;
        this.transactions = List.copyOf(transactions); // por seguridad
        this.previousHash = previousHash;
        this.nonce = 0;
        this.hash = calculateHash();
    }
    // Constructor para cuando recibis un bloque de otro nodo (ya viene con hash y nonce)
    public Block(int index, long timestamp, List<Transaction> transactions,
                 String previousHash, String hash, long nonce) {
        this.index = index;
        this.timestamp = timestamp;
        this.transactions = List.copyOf(transactions);
        this.previousHash = previousHash;
        this.hash = hash;
        this.nonce = nonce;
    }

    private String calculateHash() {
        String data = index + "|" + timestamp + "|" + transactionsData() + "|" + previousHash + "|" + nonce;
        return CryptoUtil.sha256(data);
    }

    private String transactionsData() {
        StringBuilder sb = new StringBuilder();
        for (Transaction tx : transactions) {
            sb.append(tx.dataForHash());
        }
        return sb.toString();
    }

    public boolean hasValidTransactions() {
        for (Transaction tx : transactions) {
            if (tx == null || !tx.isValid()) {
                return false;
            }
        }
        return true;
    }

     public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);

        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public boolean isValid(int difficulty) {
        if (index < 0) return false;
        if (timestamp <= 0) return false;
        if (transactions == null) return false;
        if (previousHash == null || previousHash.isBlank()) return false;
        if (hash == null || hash.isBlank()) return false;

        String target = "0".repeat(difficulty);
        return hasValidTransactions()
                && hash.equals(calculateHash())
                && hash.startsWith(target);
    }


    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    public long getNonce() {
        return nonce;
    }
}
