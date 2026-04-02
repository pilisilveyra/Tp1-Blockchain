/* package com.example.blockchain;

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

    @Test
    void addingNullTransactionThrowsExceptionAndDoesNotChangePending() {
        BlockchainService service = new BlockchainService(3);

        int before = service.getPendingTransactions().size();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addPendingTransaction(null)
        );
        assertEquals("Transacción inválida", ex.getMessage());

        assertEquals(before, service.getPendingTransactions().size());
    }

    @Test
    void addingInvalidTransactionThrowsExceptionAndDoesNotChangePending() {
        BlockchainService service = new BlockchainService(3);

        int before = service.getPendingTransactions().size();

        Transaction invalidTx = TestTxUtils.createInvalidTransaction();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addPendingTransaction(invalidTx)
        );
        assertEquals("Transacción inválida", ex.getMessage());

        assertEquals(before, service.getPendingTransactions().size());
    }

    @Test
    void addingDuplicateTransactionToPendingThrowsAndDoesNotChangePending() {
        BlockchainService service = new BlockchainService(3);

        Transaction tx = TestTxUtils.createValidTransaction(50.0);
        service.addPendingTransaction(tx);

        int before = service.getPendingTransactions().size();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addPendingTransaction(tx)
        );
        assertEquals("La transacción ya existe en el mempool", ex.getMessage());

        assertEquals(before, service.getPendingTransactions().size());
    }

    @Test
    void addingTransactionAlreadyInChainThrowsAndDoesNotChangePending() {
        BlockchainService service = new BlockchainService(3);

        Transaction tx = TestTxUtils.createValidTransaction(50.0);
        service.addPendingTransaction(tx);

        service.mineBlock();

        int before = service.getPendingTransactions().size(); // debería ser 0

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.addPendingTransaction(tx)
        );
        assertEquals("La transacción ya fue confirmada en un bloque", ex.getMessage());

        assertEquals(before, service.getPendingTransactions().size());
    }

    @Test
    void cannotMineBlockWhenThereAreNoPendingTransactions() {
        BlockchainService service = new BlockchainService(3);

        int pendingBefore = service.getPendingTransactions().size();
        int chainBefore = service.getChain().size();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                service::mineBlock
        );
        assertEquals("No hay transacciones pendientes para minar", ex.getMessage());

        assertEquals(pendingBefore, service.getPendingTransactions().size());
        assertEquals(chainBefore, service.getChain().size());
    }

    @Test
    void miningAddsNewBlockToChain() {
        BlockchainService service = new BlockchainService(3);

        Transaction tx = TestTxUtils.createValidTransaction(50.0);
        service.addPendingTransaction(tx);

        int chainBefore = service.getChain().size();

        service.mineBlock();

        assertEquals(chainBefore + 1, service.getChain().size());
    }

    @Test
    void minedBlockPointsToPreviousBlock() {
        BlockchainService service = new BlockchainService(3);

        Block previous = service.getLatestBlock();

        Transaction tx = TestTxUtils.createValidTransaction(50.0);
        service.addPendingTransaction(tx);

        Block mined = service.mineBlock();

        assertEquals(previous.getHash(), mined.getPreviousHash());
        assertEquals(previous.getIndex() + 1, mined.getIndex());
    }
    

    
}

 */

