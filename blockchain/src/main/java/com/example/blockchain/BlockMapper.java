package com.example.blockchain;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.dto.SendTransactionDto;
import com.example.blockchain.dto.TransactionDto;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.TransactionType;
import com.example.blockchain.service.WalletService;

import java.util.List;

public class BlockMapper {

    private BlockMapper() {}

  // Model -> Dto
  public static TransactionDto toDto(Transaction tx) {
      return new TransactionDto(
              tx.getId(),
              tx.getType().name(),
              tx.getFrom(),
              tx.getTo(),
              tx.getAmount(),
              tx.getTimestamp(),
              tx.getPublicKey(),
              tx.getSignature()
      );
  }

    public static BlockDto toDto(Block block) {
        List<TransactionDto> txDtos = block.getTransactions().stream()
                .map(BlockMapper::toDto)
                .toList();
        return new BlockDto(
                block.getIndex(),
                block.getTimestamp(),
                txDtos,
                block.getPreviousHash(),
                block.getHash(),
                block.getNonce()
        );
    }

    public static ChainDto toDto(List<Block> chain) {
        return ChainDto.ok(chain.stream().map(BlockMapper::toDto).toList());
    }

    // Dto -> Model
    public static Transaction toModel(TransactionDto dto) {
        return new Transaction(
                dto.id(),
                dto.typeAsEnum(),
                dto.from(),
                dto.to(),
                dto.amount(),
                dto.timestamp(),
                dto.publicKey(),
                dto.signature()
        );
    }

    public static Block toModel(BlockDto dto) {
        List<Transaction> txs = dto.transactions().stream()
                .map(BlockMapper::toModel)
                .toList();
        return new Block(
                dto.index(),
                dto.timestamp(),
                txs,
                dto.previousHash(),
                dto.hash(),
                dto.nonce()
        );
    }

  public static TransactionDto toSignedTransferDto(SendTransactionDto sendDto, WalletService walletService) {
    long timestamp = System.currentTimeMillis();
    String from = walletService.getAddress();
    String publicKey = walletService.getPublicKeyHex();
    String signature = walletService.signTransfer(sendDto.to(), sendDto.amount(), timestamp); //firma usando privkey

    return new TransactionDto(
        java.util.UUID.randomUUID().toString(),
        TransactionType.TRANSFER.name(),
        from,
        sendDto.to(),
        sendDto.amount(),
        timestamp,
        publicKey,
        signature
    );
  }
}
