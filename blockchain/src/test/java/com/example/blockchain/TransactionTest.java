package com.example.blockchain;

import com.example.blockchain.model.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionTest {

  @Test
  void validTransactionIsValid() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    assertTrue(tx.isValid());
  }

  @Test
  void transactionWithNullFromIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        null,
        validTx.getTo(),
        validTx.getAmount(),
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }



}
