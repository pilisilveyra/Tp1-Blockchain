package com.example.blockchain;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.BlockchainService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockchainServiceAdvancedTest {

  @Test
  void minedBlockContainsPendingTransactions() {
    BlockchainService service = new BlockchainService(3);

    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);

    service.addPendingTransaction(tx1);
    service.addPendingTransaction(tx2);

    Block mined = service.mineBlock();

    assertEquals(2, mined.getTransactions().size());
    assertTrue(mined.getTransactions().contains(tx1));
    assertTrue(mined.getTransactions().contains(tx2));
  }

  @Test
  void miningBlockRemovesTransactionsFromPending() {
    BlockchainService service = new BlockchainService(3);

    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);

    service.addPendingTransaction(tx1);
    service.addPendingTransaction(tx2);

    service.mineBlock();

    assertEquals(0, service.getPendingTransactions().size());
  }

}
