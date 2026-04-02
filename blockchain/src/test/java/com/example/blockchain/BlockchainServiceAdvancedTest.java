/*package com.example.blockchain;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.BlockchainService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

  @Test
  void minedBlockSatisfiesProofOfWork() {
    BlockchainService service = new BlockchainService(3);

    Transaction tx = TestTxUtils.createValidTransaction(50.0);
    service.addPendingTransaction(tx);

    Block mined = service.mineBlock();

    assertTrue(mined.getHash().startsWith("000"));
    assertTrue(mined.isValid(3));
  }

  @Test
  void receiveValidBlockFromAnotherNode() {
    BlockchainService service = new BlockchainService(3);

    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    Block externalBlock = new Block(
        service.getLatestBlock().getIndex() + 1,
        System.currentTimeMillis(),
        List.of(tx),
        service.getLatestBlock().getHash()
    );
    externalBlock.mineBlock(3);

    boolean added = service.receiveBlock(externalBlock);

    assertTrue(added);
    assertEquals(2, service.getChain().size());
  }

  @Test
  void receiveBlockWithWrongPreviousHashIsRejected() {
    BlockchainService service = new BlockchainService(3);

    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    Block externalBlock = new Block(
        service.getLatestBlock().getIndex() + 1,
        System.currentTimeMillis(),
        List.of(tx),
        "hash incorrecto"
    );
    externalBlock.mineBlock(3);

    boolean added = service.receiveBlock(externalBlock);

    assertFalse(added);
    assertEquals(1, service.getChain().size());
  }

  @Test
  void replaceWithLongerValidChain() {
    BlockchainService local = new BlockchainService(3);
    BlockchainService remote = new BlockchainService(3);

    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    remote.addPendingTransaction(tx1);
    remote.mineBlock();

    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);
    remote.addPendingTransaction(tx2);
    remote.mineBlock();

    boolean replaced = local.replaceChainIfValid(remote.getChain());

    assertTrue(replaced);
    assertEquals(remote.getChain().size(), local.getChain().size());
  }

  @Test
  void doesNotReplaceWithShorterChain() {
    BlockchainService local = new BlockchainService(3);
    BlockchainService remote = new BlockchainService(3);

    Transaction tx = TestTxUtils.createValidTransaction(50.0);
    local.addPendingTransaction(tx);
    local.mineBlock();

    boolean replaced = local.replaceChainIfValid(remote.getChain());

    assertFalse(replaced);
    assertEquals(2, local.getChain().size());
  }

  @Test
  void doesNotReplaceWithChainOfSameLength() {
    BlockchainService local = new BlockchainService(3);
    BlockchainService remote = new BlockchainService(3);

    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    local.addPendingTransaction(tx1);
    local.mineBlock();

    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);
    remote.addPendingTransaction(tx2);
    remote.mineBlock();

    boolean replaced = local.replaceChainIfValid(remote.getChain());

    assertFalse(replaced);
    assertEquals(2, local.getChain().size());
  }

  @Test
  void doesNotReplaceWithLongerButInvalidChain() {
    BlockchainService local = new BlockchainService(3);
    BlockchainService remote = new BlockchainService(3);

    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    remote.addPendingTransaction(tx1);
    remote.mineBlock();

    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);
    remote.addPendingTransaction(tx2);
    remote.mineBlock();

    List<Block> invalidChain = new ArrayList<>(remote.getChain());
    Block lastBlock = invalidChain.get(invalidChain.size() - 1);

    Block tamperedBlock = new Block(
        lastBlock.getIndex(),
        lastBlock.getTimestamp(),
        lastBlock.getTransactions(),
        "previous-hash-trucho",
        lastBlock.getHash(),
        lastBlock.getNonce()
    );

    invalidChain.set(invalidChain.size() - 1, tamperedBlock);

    boolean replaced = local.replaceChainIfValid(invalidChain);

    assertFalse(replaced);
    assertEquals(1, local.getChain().size());
  }

  @Test
  void getPendingTransactionsReturnsCopyNotLiveReference() {
    BlockchainService service = new BlockchainService(3);

    Transaction tx1 = TestTxUtils.createValidTransaction(50.0);
    service.addPendingTransaction(tx1);

    List<Transaction> snapshot = service.getPendingTransactions();

    Transaction tx2 = TestTxUtils.createValidTransaction(20.0);
    service.addPendingTransaction(tx2);

    assertEquals(1, snapshot.size());
    assertEquals(2, service.getPendingTransactions().size());
  }


}

 */
