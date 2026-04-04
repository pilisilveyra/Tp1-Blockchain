package com.example.blockchain.service;

import com.example.blockchain.model.TransactionType;
import com.example.blockchain.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.ECKeyPair;

@Service
public class WalletService {

    private static final TransactionType transferType = TransactionType.TRANSFER;

    private final ECKeyPair keyPair;
    private final String publicKeyHex;
    private final String address;

    public WalletService(@Value("${blockchain.wallet.private-key}") String privateKeyHex) {
        this.keyPair = CryptoUtil.keyPairFromPrivateKeyHex(privateKeyHex);
        this.publicKeyHex = CryptoUtil.publicKeyToHex(keyPair);
        this.address = CryptoUtil.addressFromPublicKey(publicKeyHex);
    }

    public String getAddress() {
        return address;
    }

    public String getPublicKeyHex() {
        return publicKeyHex;
    }

    public String signTransfer(String to, long amount, long timestamp) {
        String payload = buildTransferPayload(to, amount, timestamp); //string exacto para firmar
        return CryptoUtil.sign(keyPair, payload);
    }

    private String buildTransferPayload(String to, long amount, long timestamp) {
        return transferType.name() + "|" + address + "|" + to + "|" + amount + "|" + timestamp;
    }
}