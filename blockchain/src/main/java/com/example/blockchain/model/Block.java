package com.example.blockchain.model;

import com.example.blockchain.util.CryptoUtil;

import java.util.List;
import java.util.stream.Collectors;

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

    public String calculateHash() {
        String txIds = transactions.stream()
                .map(Transaction::getId)
                .collect(Collectors.joining(","));
        String data = index + "|" + timestamp + "|" + previousHash + "|" + nonce + "|" + txIds;
        return CryptoUtil.sha256(data);
    }

     public void mineBlock(int difficulty) {
        String target = "0".repeat(difficulty);

        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public boolean isGenesis() {
        return index == 0;
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
