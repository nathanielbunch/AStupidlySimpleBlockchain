package org.nathanielbunch.ssblockchain.node.service;

import org.nathanielbunch.ssblockchain.core.ledger.Block;
import org.nathanielbunch.ssblockchain.core.ledger.Blockchain;
import org.nathanielbunch.ssblockchain.core.ledger.Transaction;
import org.nathanielbunch.ssblockchain.core.ledger.Wallet;
import org.nathanielbunch.ssblockchain.core.utils.BCOHasher;
import org.nathanielbunch.ssblockchain.core.utils.BCOKeyGenerator;
import org.nathanielbunch.ssblockchain.node.controller.BlockchainController;
import org.nathanielbunch.ssblockchain.node.model.BlockResponse;
import org.nathanielbunch.ssblockchain.node.network.Node;
import org.openjdk.jol.info.GraphLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.security.auth.DestroyFailedException;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles lower-level operation with the SSBlockchain. Used
 * to serve functionality to the rest endpoint.
 *
 * @since 0.0.1
 * @see BlockchainController
 * @author nathanielbunch
 */
@Service
public class BlockchainService {

    private final Logger logger = LoggerFactory.getLogger(BlockchainService.class);
    private final Object lock = new Object();

    @Autowired
    private Node node;
    @Autowired
    private Blockchain blockchain;
    @Autowired
    private BCOKeyGenerator keyGenerator;

    private List<Transaction> transactions;
    private Wallet currentWallet;

    @PostConstruct
    private void init(){
        this.transactions = new ArrayList<>();
    }

    /**
     * Returns a transaction given a set of identifying parameters.
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public Transaction getTransaction() throws NoSuchAlgorithmException {
        return Transaction.TBuilder.newSSTransactionBuilder()
                .setOrigin("TestAddress")
                .setDestination("TestDestination")
                .setValue(new BigDecimal("0.1234"))
                .setNote("Test transaction")
                .build();
    }

    /**
     * Adds a new transaction to execute on the blockchain.
     *
     * @param transaction
     */
    public void addNewTransaction(Transaction transaction) {
        synchronized (lock) {
            logger.info("New transaction: {} [{} -> {} = {}]", transaction.toString(), transaction.getOrigin(), transaction.getDestination(), transaction.getAmount());
            this.transactions.add(transaction);
        }
    }

    /**
     * Returns the currently loaded wallet.
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public Wallet getWallet() throws NoSuchAlgorithmException, DestroyFailedException {
        logger.info("Generating new wallet...");
        KeyPair newKeyPair = keyGenerator.generatePublicPrivateKeys();
        Wallet newWallet = Wallet.WBuilder.newSSWalletBuilder().setPublicKey(newKeyPair.getPublic()).build();
        logger.info("New wallet generated with the private key: {}", BCOHasher.humanReadableHash(newKeyPair.getPrivate().getEncoded()));
        this.currentWallet = newWallet;
        return newWallet;
    }

    /**
     * Returns a most recent block response.
     *
     * @return
     */
    public BlockResponse mineBlock() throws Exception {

        logger.info("Mining new block...");

        BlockResponse lastMinedBlock;

        if(blockchain.getBlocks().length == 0){
            Block genesisBlock = Block.BBuilder.newSSBlockBuilder()
                    .setTransactions("In the beginning...there was light.")
                    .setPreviousBlock(null)
                    .build();
            blockchain.addBlocks(List.of(genesisBlock));
        }

        Block lastBlock = blockchain.getBlocks()[blockchain.getBlocks().length-1];

        logger.info("Last block record: {}", lastBlock.toString());

        int lastProof;
        if(lastBlock.getTransactions() instanceof String){
            lastProof = 1;
        } else {
            lastProof = ((Transaction[]) lastBlock.getTransactions()).length;
        }

        this.proofOfWork(lastProof);

        Transaction blockMineAward = Transaction.TBuilder.newSSTransactionBuilder()
                .setOrigin("SSBlockchainNetwork")
                .setDestination(currentWallet.getHumanReadableAddress())
                .setValue(new BigDecimal(1))
                .setNote("Happy mining!")
                .build();

        logger.info("Block mine awarded, transaction: {} @ {}", blockMineAward.toString(), blockMineAward.getAmount());

        Block newBlock;
        synchronized (lock) {
            this.addNewTransaction(blockMineAward);

            newBlock = Block.BBuilder.newSSBlockBuilder()
                    .setTransactions(this.transactions.toArray(Transaction[]::new))
                    .setPreviousBlock(this.blockchain.getBlocks()[this.blockchain.getBlocks().length - 1].getBlockHash())
                    .build();

            this.blockchain.addBlocks(List.of(newBlock));
            this.transactions = new ArrayList<>();
        }

        logger.info("New block: {}", newBlock.toString());

        return BlockResponse.Builder.builder()
                .setIndex(newBlock.getIndex())
                .setTimestamp(newBlock.getTimestamp())
                .setSize(GraphLayout.parseInstance(newBlock).totalSize())
                .setBlockhash(newBlock.getBlockHash())
                .build();
    }

    private int proofOfWork(int work) {
        int incrememt = work + 1;
        while(work % 23 == 0 && incrememt % work == 0) {
            incrememt++;
        }
        return incrememt;
    }

}
