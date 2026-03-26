package com.example.blockchain;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.util.CryptoUtil;

import java.security.KeyPair;

/**
 * para armar transacciones válidas en tests.
 */
public final class TestTxUtils {

    private TestTxUtils() {}

    public static String createNewAddress() {
        return CryptoUtil.addressFromPublicKey(createNewPublicKeyBase64());
    }

    private static String createNewPublicKeyBase64() {
        KeyPair keyPair = CryptoUtil.generateKeyPair();
        return CryptoUtil.publicKeyToBase64(keyPair.getPublic());
    }

    public static Transaction createValidTransaction(double amount, String receiverAddress) {
        KeyPair sender = CryptoUtil.generateKeyPair();

        String senderPublic = CryptoUtil.publicKeyToBase64(sender.getPublic());
        String senderAddress = CryptoUtil.addressFromPublicKey(senderPublic);

        String dataToSign = senderAddress + "|" + receiverAddress + "|" + amount;
        String signature = CryptoUtil.sign(sender.getPrivate(), dataToSign);

        Transaction tx = new Transaction(senderAddress, receiverAddress, amount, senderPublic, signature);
        if (!tx.isValid()) {
            throw new IllegalStateException("La transacción generada no es válida");
        }
        return tx;
    }

    public static Transaction createValidTransaction(double amount) {
        return createValidTransaction(amount, createNewAddress());
    }

    public static Transaction createInvalidTransaction() {
        // amount <= 0 hace que Transaction.isValid() devuelva false antes de validar firma
        return new Transaction(
                "0xSender",
                "0xReceiver",
                0.0,
                "publicKey",
                "signature"
        );
    }
}

