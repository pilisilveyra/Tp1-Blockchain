/* package com.example.blockchain;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.dto.TransactionDto;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockMapperTest {

  @Test
  void transactionToDtoMapsAllFieldsCorrectly() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    TransactionDto dto = BlockMapper.toDto(tx);

    assertEquals(tx.getFrom(), dto.from());
    assertEquals(tx.getTo(), dto.to());
    assertEquals(tx.getAmount(), dto.amount());
    assertEquals(tx.getPublicKey(), dto.publicKey());
    assertEquals(tx.getSignature(), dto.signature());
  }

  @Test
  void transactionDtoToModelMapsAllFieldsCorrectly() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);
    TransactionDto dto = BlockMapper.toDto(tx);

    Transaction mapped = BlockMapper.toModel(dto);

    assertEquals(dto.from(), mapped.getFrom());
    assertEquals(dto.to(), mapped.getTo());
    assertEquals(dto.amount(), mapped.getAmount());
    assertEquals(dto.publicKey(), mapped.getPublicKey());
    assertEquals(dto.signature(), mapped.getSignature());
  }

  @Test
  void blockToDtoMapsAllFieldsCorrectly() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    Block block = new Block(
        1,
        System.currentTimeMillis(),
        List.of(tx),
        "previous hash"
    );
    block.mineBlock(3);

    BlockDto dto = BlockMapper.toDto(block);

    assertEquals(block.getIndex(), dto.index());
    assertEquals(block.getTimestamp(), dto.timestamp());
    assertEquals(block.getPreviousHash(), dto.previousHash());
    assertEquals(block.getHash(), dto.hash());
    assertEquals(block.getNonce(), dto.nonce());
    assertEquals(1, dto.transactions().size());
    assertEquals(tx.getFrom(), dto.transactions().get(0).from());
  }

  @Test
  void blockDtoToModelMapsAllFieldsCorrectly() {
    Transaction tx = TestTxUtils.createValidTransaction(50.0);

    Block original = new Block(
        1,
        System.currentTimeMillis(),
        List.of(tx),
        "previous hash"
    );
    original.mineBlock(3);

    BlockDto dto = BlockMapper.toDto(original);
    Block mapped = BlockMapper.toModel(dto);

    assertEquals(dto.index(), mapped.getIndex());
    assertEquals(dto.timestamp(), mapped.getTimestamp());
    assertEquals(dto.previousHash(), mapped.getPreviousHash());
    assertEquals(dto.hash(), mapped.getHash());
    assertEquals(dto.nonce(), mapped.getNonce());
    assertEquals(dto.transactions().size(), mapped.getTransactions().size());
  }


}

 */
