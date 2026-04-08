package com.example.blockchain.util;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

public class WalletGenerator {

  public static void main(String[] args) throws Exception {
    ECKeyPair keyPair = Keys.createEcKeyPair();

    String privateKeyHex = Numeric.toHexStringNoPrefixZeroPadded(keyPair.getPrivateKey(), 64);
    String publicKeyHex = CryptoUtil.publicKeyToHex(keyPair);
    String address = CryptoUtil.addressFromPublicKey(publicKeyHex);

    System.out.println("PRIVATE KEY HEX:");
    System.out.println(privateKeyHex);

    System.out.println("\nPUBLIC KEY HEX:");
    System.out.println(publicKeyHex);

    System.out.println("\nADDRESS:");
    System.out.println(address);
  }
}