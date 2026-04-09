package com.example.blockchain.util;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CryptoUtil {

    private static final int PUBLIC_KEY_HEX_LENGTH = 128;
    private static final int SIGNATURE_PART_LENGTH = 32;
    private static final int SIGNATURE_TOTAL_LENGTH = 65;
    private static final int RECOVERY_ID_INDEX = 64;

    private static final String ADDRESS_PREFIX = "0x";
    private static final String HASH_ALGORITHM = "SHA-256";

    private CryptoUtil() {
    }

    public static ECKeyPair keyPairFromPrivateKeyHex(String privateKeyHex) {
        return ECKeyPair.create(parseHexToBigInteger(privateKeyHex));
    }

    public static String publicKeyToHex(ECKeyPair keyPair) {
        return Numeric.toHexStringNoPrefixZeroPadded(
            keyPair.getPublicKey(),
            PUBLIC_KEY_HEX_LENGTH
        );
    }

    public static String sign(ECKeyPair keyPair, String data) {
        Sign.SignatureData signature = Sign.signMessage(toUtf8Bytes(data), keyPair, false);
        byte[] encodedSignature = encodeSignature(signature);
        return Base64.getEncoder().encodeToString(encodedSignature);
    }

    public static String addressFromPublicKey(String publicKeyHex) {
        BigInteger publicKey = parseHexToBigInteger(publicKeyHex);
        return ADDRESS_PREFIX + Keys.getAddress(publicKey);
    }


    public static boolean verifySignature(String publicKeyHex, String data, String signatureBase64) {
        try {
            byte[] encodedSignature = Base64.getDecoder().decode(signatureBase64);
            if (!hasExpectedLength(encodedSignature)) {
                return false;
            }

            Sign.SignatureData signature = decodeSignature(encodedSignature);

            BigInteger recoveredPublicKey =
                    Sign.signedMessageToKey(toUtf8Bytes(data), signature);

            String recoveredAddress = ADDRESS_PREFIX + Keys.getAddress(recoveredPublicKey);
            String expectedAddress = addressFromPublicKey(publicKeyHex);

            return recoveredAddress.equalsIgnoreCase(expectedAddress);
        } catch (Exception e) {
            return false;
        }
    }

    public static String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(toUtf8Bytes(data));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular SHA-256", e);
        }
    }

    private static BigInteger parseHexToBigInteger(String hexValue) {
        String cleanHex = Numeric.cleanHexPrefix(hexValue);
        return new BigInteger(cleanHex, 16);
    }


    private static byte[] encodeSignature(Sign.SignatureData signature) {
        byte[] encoded = new byte[SIGNATURE_TOTAL_LENGTH];
        copy(signature.getR(), encoded, 0);
        copy(signature.getS(), encoded, SIGNATURE_PART_LENGTH);
        encoded[RECOVERY_ID_INDEX] = signature.getV()[0];
        return encoded;
    }

    private static Sign.SignatureData decodeSignature(byte[] encodedSignature) {
        byte[] r = extractSignaturePart(encodedSignature, 0);
        byte[] s = extractSignaturePart(encodedSignature, SIGNATURE_PART_LENGTH);
        byte v = encodedSignature[RECOVERY_ID_INDEX];
        return new Sign.SignatureData(v, r, s);
    }

    private static byte[] extractSignaturePart(byte[] encodedSignature, int startIndex) {
        byte[] part = new byte[SIGNATURE_PART_LENGTH];
        System.arraycopy(encodedSignature, startIndex, part, 0, SIGNATURE_PART_LENGTH);
        return part;
    }


    private static boolean hasExpectedLength(byte[] encodedSignature) {
        return encodedSignature.length == SIGNATURE_TOTAL_LENGTH;
    }

    private static byte[] toUtf8Bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static void copy(byte[] source, byte[] target, int targetStartIndex) {
        System.arraycopy(source, 0, target, targetStartIndex, source.length);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte currentByte : bytes) {
            hex.append(String.format("%02x", currentByte));
        }
        return hex.toString();
    }
}