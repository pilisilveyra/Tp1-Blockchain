package com.example.blockchain.model;

public class Transaction {
    private final String from;
    private final String to;
    private final double amount;
    private final String signature;

    public Transaction(String from, String to, double amount, String signature) {
        this.from = from;
        this.to = to;
        this.amount = amount;
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
        return signature != null && !signature.isBlank();
    }

    public String dataForHash() {
        return from + to + amount + signature;
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

}
