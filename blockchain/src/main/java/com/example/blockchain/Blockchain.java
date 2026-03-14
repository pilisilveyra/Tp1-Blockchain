package com.example.blockchain;

import java.util.ArrayList;
import java.util.List;


public class Blockchain {
    private final List<Block> chain;
    private final int difficulty;

    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty;
        chain.add(createGenesis());
    }

    private Block createGenesis() {
        Block genesis = new Block(
                0,
                1700000000L, // pongo cualquiera para que sea deterministico y no lo cree cada bloque
                new ArrayList<>(),
                "0"
        );
        genesis.mineBlock(difficulty);
        return genesis;
    }

    public Block getLatestBlock() {
        return chain.getLast();
    }

    // yo (nodo) creo, mino y agrego un bloque y lo mando a los demas
    public Block createAndMineBlock(List<Transaction> transactions) throws IllegalArgumentException {
        if (!areValidTransactions(transactions)) {
            throw new IllegalArgumentException("Las transacciones del bloque no son válidas");
        }

        Block newBlock = createNewBlock(transactions);
        newBlock.mineBlock(difficulty);
        chain.add(newBlock);
        return newBlock;
    }

    private Block createNewBlock(List<Transaction> transactions) {
        return new Block(
                getLatestBlock().getIndex() + 1,
                System.currentTimeMillis(),
                transactions,
                getLatestBlock().getHash()
        );
    }

    // yo recibo un bloque de otro nodo, lo valido y si es correcto lo agrego a mi cadena
    public boolean addBlockIfValid(Block newBlock) {
            if (isValidNewBlock(newBlock, getLatestBlock())) {
                chain.add(newBlock);
                return true;
            }
            return false;
    }

    public boolean isValidNewBlock(Block newBlock, Block previousBlock) {
        if (newBlock == null || previousBlock == null) {
            return false;
        }
        if (previousBlock.getIndex() + 1 != newBlock.getIndex()) {
            return false;
        }
        if (!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
            return false;
        }
        if (!newBlock.hasValidTransactions()) {
            return false;
        }
        return newBlock.isValid(difficulty);
    }

    // yo (nodo) recibo una cadena de otro nodo, la valido y verifico si es correcta

    public boolean isChainValid(List<Block> chain) {

        if (chain == null || chain.isEmpty()) {
            return false;
        }

        if (!isValidGenesis(chain.getFirst())) {
            return false;
        }

        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            if (!isValidNewBlock(currentBlock, previousBlock)) {
                return false;
            }
        }
        return true;
        //falaria verificar bloque genesiss y quizas algo con los hashes
    }

    // cuando recibo cadena me fijo si es mejor (proof of work y mas larga que la mia) y si es asi la reemplazo
    public boolean replaceChainIfValid(List<Block> newChain) {
        if (newChain.size() > chain.size() && isChainValid(newChain)) {
            chain.clear();
            chain.addAll(newChain);
            return true;
        }
        return false;
    }

    private boolean isValidGenesis(Block genesis) {
        Block expected = createGenesis();
        return genesis.getIndex() == expected.getIndex()
                && genesis.getTimestamp() == expected.getTimestamp()
                && genesis.getPreviousHash().equals(expected.getPreviousHash())
                && genesis.getHash().equals(expected.getHash())
                && genesis.getNonce() == expected.getNonce();
    }

    private boolean areValidTransactions(List<Transaction> transactions) {
        if (transactions == null) {
            return false;
        }
        for (Transaction tx : transactions) {
            if (tx == null || !tx.isValid()) {
                return false;
            }
        }
        return true;
    }

    public List<Block> getChain() {
        return List.copyOf(chain);
    }
}



