package com.example.blockchain.util;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoUtil {

    private CryptoUtil() {
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el par de claves", e);
        }
    }

    public static String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static PublicKey publicKeyFromBase64(String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519");
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo reconstruir la public key", e);
        }
    }

    // no implementamos para private key porque no es necesario para el funcionamiento de la blockchain,
    // y así evitamos exponer código que manipule claves privadas

    public static String sign(PrivateKey privateKey, String data) {
        try {
            Signature signature = Signature.getInstance("Ed25519");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo firmar", e);
        }
    }

    public static boolean verifySignature(String publicKeyBase64, String data, String signatureBase64) {
        try {
            PublicKey publicKey = publicKeyFromBase64(publicKeyBase64);

            Signature verifier = Signature.getInstance("Ed25519");
            verifier.initVerify(publicKey);
            verifier.update(data.getBytes(StandardCharsets.UTF_8)); //paso los datos firmados

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return verifier.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }

    public static String addressFromPublicKey(String publicKeyBase64) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKeyBase64.getBytes(StandardCharsets.UTF_8));
            return "0x" + bytesToHex(hash).substring(0, 40);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo derivar la address", e);
        }
    }

    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular SHA-256", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}
