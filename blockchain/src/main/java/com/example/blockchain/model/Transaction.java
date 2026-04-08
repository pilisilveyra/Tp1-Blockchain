package com.example.blockchain.model;

import com.example.blockchain.util.CryptoUtil;

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

    public boolean isValid() {
        return switch (type) {
            case TRANSFER -> isValidTransfer();
            case COINBASE -> isValidCoinbase();
        };
    }

    private boolean isValidTransfer() {
        if (id == null || id.isBlank()) return false;
        if (from == null || from.isBlank()) return false;
        if (to == null || to.isBlank()) return false;
        if (from.equals(to)) return false;
        if (amount <= 0) return false;
        if (timestamp <= 0) return false;
        if (publicKey == null || publicKey.isBlank()) return false;
        if (signature == null || signature.isBlank()) return false;

        // La address del from debe coincidir con la derivada de la publicKey
        try {
            String derivedAddress = CryptoUtil.addressFromPublicKey(publicKey);
            if (!from.equalsIgnoreCase(derivedAddress)) return false;

            return CryptoUtil.verifySignature(publicKey, canonicalPayload(), signature);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidCoinbase() {
        return id != null && !id.isBlank()
            && "SYSTEM".equals(from)
            && to != null && !to.isBlank()
            && amount > 0
            && timestamp > 0
            && isZeroValue(publicKey)
            && isZeroValue(signature);
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

    private boolean isZeroValue(String value) {
        return value != null && value.chars().allMatch(c -> c == '0');
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
