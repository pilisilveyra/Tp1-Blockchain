package com.example.blockchain.model;

import com.example.blockchain.util.CryptoUtil;

public class Transaction {
    private final String from;
    private final String to;
    private final double amount;
    private final String publicKey;
    private final String signature;

    public Transaction(String from, String to, double amount, String publicKey, String signature) {
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.publicKey = publicKey;
        this.signature = signature;
    }

    public boolean isValid() {
        if (from == null || from.isBlank()) {
            return false;
        }
        if (to == null || to.isBlank()) {
            return false;
        }
        if (from.equals(to)) {
            return false;
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            return false;
        }
        if (publicKey == null || publicKey.isBlank()) return false;
        if (signature == null || signature.isBlank()) return false;

        try {
            // La dirección del emisor debe salir de su public key
            String derivedAddress = CryptoUtil.addressFromPublicKey(publicKey);
            if (!from.equals(derivedAddress)) {
                return false;
            }

            // La firma debe ser válida para el contenido de la tx
            return CryptoUtil.verifySignature(publicKey, dataToSign(), signature);
        } catch (Exception e){
            return false;
        }
    }

    public String dataToSign() {
        return from + "|" + to + "|" + amount;
    }

    public String dataForHash() {
        return from + "|" + to + "|" + amount + "|" + publicKey + "|" + signature;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }

    public String getSignature() {
        return signature;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
