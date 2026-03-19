package com.example.blockchain;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.dto.TransactionDto;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;

import java.util.List;

public class BlockMapper {

    private BlockMapper() {}

  // Model -> Dto
    public static TransactionDto toDto(Transaction tx) {
        return new TransactionDto(tx.getFrom(), tx.getTo(), tx.getAmount(), tx.getSignature());
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
        List<BlockDto> blockDtos = chain.stream().map(BlockMapper::toDto).toList();
        return new ChainDto(blockDtos, blockDtos.size());
    }

    // Dto -> Model
    public static Transaction toModel(TransactionDto Dto) {
        return new Transaction(Dto.from(), Dto.to(), Dto.amount(), Dto.signature());
    }

    public static Block toModel(BlockDto Dto) {
        List<Transaction> txs = Dto.transactions().stream()
                .map(BlockMapper::toModel)
                .toList();
        // constructor que ya viene con hash y nonce calculados
        return new Block(
                Dto.index(),
                Dto.timestamp(),
                txs,
                Dto.previousHash(),
                Dto.hash(),
                Dto.nonce()
        );
    }
}
