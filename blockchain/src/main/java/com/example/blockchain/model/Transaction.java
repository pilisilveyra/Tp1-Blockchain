package com.example.blockchain.model;

import java.util.UUID;

public class Transaction {

    private final String id;
    private final TransactionType type;
    private final String from;
    private final String to;
    private final long amount;  // entero, sin decimales
    private final long timestamp;
    private final String publicKey;
    private final String signature;

    // Constructor para TRANSFER (recibe los campos del usuario)
    public Transaction(String id, String from, String to, long amount,
                       long timestamp, String publicKey, String signature) {
        this.id        = id != null ? id : UUID.randomUUID().toString();
        this.type      = TransactionType.TRANSFER;
        this.from      = from;
        this.to        = to;
        this.amount    = amount;
        this.timestamp = timestamp;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    // Constructor para COINBASE (la crea el nodo minero al armar el bloque)
    public static Transaction createCoinbase(String minerAddress, long blockTimestamp, long blockReward) {
        return new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.COINBASE,
                "SYSTEM",
                minerAddress,
                blockReward,
                blockTimestamp,
                "0".repeat(64),
                "0".repeat(64)
        );
    }

    // Constructor interno completo
    public Transaction(String id, TransactionType type, String from, String to, long amount,
                       long timestamp, String publicKey, String signature) {
        this.id        = id;
        this.type      = type;
        this.from      = from;
        this.to        = to;
        this.amount    = amount;
        this.timestamp = timestamp;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public boolean isCoinbase() {
        return type == TransactionType.COINBASE;
    }
    public boolean isTransfer() {
        return type == TransactionType.TRANSFER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String canonicalPayload() {
        return "TRANSFER|" + from + "|" + to + "|" + amount + "|" + timestamp;
    }

    public String getId()        { return id; }
    public TransactionType getType()        { return type; }
    public String getFrom()      { return from; }
    public String getTo()        { return to; }
    public long getAmount()      { return amount; }
    public long getTimestamp()   { return timestamp; }
    public String getPublicKey() { return publicKey; }
    public String getSignature() { return signature; }
}
