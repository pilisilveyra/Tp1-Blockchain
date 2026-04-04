//package com.example.blockchain.util;
//
//import java.security.KeyPair;
//
//public class ManualTxGenerator {
//
//    public static void main(String[] args) {
//
//        KeyPair sender = CryptoUtil.generateKeyPair();
//        KeyPair receiver = CryptoUtil.generateKeyPair();
//
//        String senderPublic = CryptoUtil.publicKeyToBase64(sender.getPublic());
//        String senderAddress = CryptoUtil.addressFromPublicKey(senderPublic);
//
//        String receiverPublic = CryptoUtil.publicKeyToBase64(receiver.getPublic());
//        String receiverAddress = CryptoUtil.addressFromPublicKey(receiverPublic);
//
//        double amount = 50.0;
//
//        String data = senderAddress + "|" + receiverAddress + "|" + amount;
//        String signature = CryptoUtil.sign(sender.getPrivate(), data);
//
//        System.out.println("\n===== TRANSACTION JSON =====\n");
//
//        System.out.println("""
//{
//  "from": "%s",
//  "to": "%s",
//  "amount": %s,
//  "publicKey": "%s",
//  "signature": "%s"
//}
//""".formatted(senderAddress, receiverAddress, amount, senderPublic, signature));
//    }
//}
