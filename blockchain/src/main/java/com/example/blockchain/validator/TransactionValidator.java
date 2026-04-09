package com.example.blockchain.validator;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.util.CryptoUtil;

import java.util.UUID;

public class TransactionValidator {

    public boolean isValid(Transaction tx) {
        if (tx == null || tx.getType() == null) return false;

        return switch (tx.getType()) {
            case TRANSFER -> isValidTransfer(tx);
            case COINBASE -> isValidCoinbase(tx);
        };
    }

    public boolean isValidTransfer(Transaction tx) {
        if (tx.getId() == null || tx.getId().isBlank()) return false;
        if (!isUuidV4(tx.getId())) return false;
        if (tx.getFrom() == null || tx.getFrom().isBlank()) return false;
        if (tx.getTo() == null || tx.getTo().isBlank()) return false;
        if (tx.getFrom().equalsIgnoreCase(tx.getTo())) return false;
        if (tx.getAmount() <= 0) return false;
        if (tx.getTimestamp() <= 0) return false;
        if (tx.getPublicKey() == null || tx.getPublicKey().isBlank()) return false;
        if (tx.getSignature() == null || tx.getSignature().isBlank()) return false;

        try {
            String derivedAddress = CryptoUtil.addressFromPublicKey(tx.getPublicKey());
            if (!tx.getFrom().equalsIgnoreCase(derivedAddress)) return false;

            return CryptoUtil.verifySignature(
                    tx.getPublicKey(),
                    tx.canonicalPayload(),
                    tx.getSignature()
            );
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidCoinbase(Transaction tx) {
        return tx.getId() != null
                && !tx.getId().isBlank()
                && isUuidV4(tx.getId())
                && "SYSTEM".equals(tx.getFrom())
                && tx.getTo() != null
                && !tx.getTo().isBlank()
                && tx.getAmount() > 0
                && tx.getTimestamp() > 0
                && isZeroValue(tx.getPublicKey())
                && isZeroValue(tx.getSignature());
    }

    private boolean isUuidV4(String value) {
        try {
            UUID uuid = UUID.fromString(value);
            return uuid.version() == 4;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isZeroValue(String value) {
        return value != null && value.chars().allMatch(c -> c == '0');
    }


}
