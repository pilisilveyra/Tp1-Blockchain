package com.example.blockchain;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.Block;
import com.example.blockchain.service.BlockchainService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BlockchainServiceFlowTest {

    @Test // test para verificar que el bloque génesis se crea correctamente
    void genesisBlockIsCreatedOnServiceInitialization() {
        int difficulty = 3;
        BlockchainService service = new BlockchainService(difficulty);

        assertEquals(1, service.getChain().size(), "La cadena debe arrancar con solo el bloque génesis");

        Block genesis = service.getLatestBlock();
        assertNotNull(genesis, "Debe existir un bloque génesis");

        assertEquals(0, genesis.getIndex(), "El bloque génesis debe tener index 0");
        assertEquals("0", genesis.getPreviousHash(), "El génesis debe apuntar a previousHash = '0'");
        assertTrue(genesis.isValid(difficulty), "El génesis debe ser válido para la dificultad configurada");
    }

    @Test
    void addingOneTransactionAddsItToPending() {
        int difficulty = 3;
        BlockchainService service = new BlockchainService(difficulty);

        String receiverAddress = TestTxUtils.createNewAddress();

        Transaction tx1 = TestTxUtils.createValidTransaction(50.0, receiverAddress);

        service.addPendingTransaction(tx1);
        assertEquals(1, service.getPendingTransactions().size());
    }

    @Test
    void addingMultipleTransactionsAddsThemToPending() {
        int difficulty = 3;
        BlockchainService service = new BlockchainService(difficulty);

        String receiverAddress = TestTxUtils.createNewAddress();

        Transaction tx1 = TestTxUtils.createValidTransaction(50.0, receiverAddress);
        Transaction tx2 = TestTxUtils.createValidTransaction(20.0, receiverAddress);

        service.addPendingTransaction(tx1);
        assertEquals(1, service.getPendingTransactions().size());

        service.addPendingTransaction(tx2);
        assertEquals(2, service.getPendingTransactions().size());
    }
}

