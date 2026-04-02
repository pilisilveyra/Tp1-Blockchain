/*package com.example.blockchain;

import com.example.blockchain.model.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void transactionWithBlankFromIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        "   ",
        validTx.getTo(),
        validTx.getAmount(),
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithNullToIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        null,
        validTx.getAmount(),
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithBlankToIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        "   ",
        validTx.getAmount(),
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithSameFromAndToIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getFrom(),
        validTx.getAmount(),
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithZeroAmountIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        0.0,
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithNegativeAmountIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        -10.0,
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }


  @Test
  void transactionWithNaNAmountIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        Double.NaN,
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }


  @Test
  void transactionWithInfiniteAmountIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        Double.POSITIVE_INFINITY,
        validTx.getPublicKey(),
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithNullPublicKeyIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        validTx.getAmount(),
        null,
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithBlankPublicKeyIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        validTx.getAmount(),
        "   ",
        validTx.getSignature()
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithNullSignatureIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        validTx.getAmount(),
        validTx.getPublicKey(),
        null
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithBlankSignatureIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        validTx.getAmount(),
        validTx.getPublicKey(),
        "   "
    );

    assertFalse(tx.isValid());
  }

  @Test
  void transactionWithPublicKeyThatDoesNotMatchFromIsInvalid() {
    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);

    Transaction tampered = new Transaction(
        tx1.getFrom(),
        tx1.getTo(),
        tx1.getAmount(),
        tx2.getPublicKey(),
        tx1.getSignature()
    );

    assertFalse(tampered.isValid());
  }

  @Test
  void transactionWithTamperedSignatureIsInvalid() {
    Transaction validTx = TestTxUtils.createValidTransaction(50.0);

    Transaction tx = new Transaction(
        validTx.getFrom(),
        validTx.getTo(),
        validTx.getAmount(),
        validTx.getPublicKey(),
        validTx.getSignature() + "alterada"
    );

    assertFalse(tx.isValid());
  }

  @Test
  void dataToSignReturnsExpectedFormat() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    String expected = tx.getFrom() + "|" + tx.getTo() + "|" + tx.getAmount();

    assertEquals(expected, tx.dataToSign());
  }

  @Test
  void dataForHashReturnsExpectedFormat() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    String expected = tx.getFrom() + "|" + tx.getTo() + "|" + tx.getAmount() + "|" +
        tx.getPublicKey() + "|" + tx.getSignature();

    assertEquals(expected, tx.dataForHash());
  }

  @Test
  void getIdReturnsSameHashForTransactionsWithSameData() {
    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);

    Transaction tx2 = new Transaction(
        tx1.getFrom(),
        tx1.getTo(),
        tx1.getAmount(),
        tx1.getPublicKey(),
        tx1.getSignature()
    );

    assertEquals(tx1.getId(), tx2.getId());
  }

  @Test
  void gettersReturnConstructorValues() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    assertNotNull(tx.getFrom());
    assertNotNull(tx.getTo());
    assertTrue(tx.getAmount() > 0);
    assertNotNull(tx.getPublicKey());
    assertNotNull(tx.getSignature());
  }

}


 */