package org.nathanielbunch.ssblockchain.core.ledger;

import org.nathanielbunch.ssblockchain.core.utils.BCOHasher;
import org.nathanielbunch.ssblockchain.core.utils.DateTimeUtil;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * The SSBlock is the main unit of data in the SSBlockchain. SSBlocks contain
 * an identifier known as the index, a timestamp for data management / sorting,
 * an object that contains transactions, a link to a previous block (previousBlockHash)
 * and the current block hash (blockHash). Blocks can be queried by hash or index.
 *
 * @since 0.0.1
 * @author nathanielbunch
 */
public final class Block implements Serializable {

    // Make the different fields of the block immutable
    private final UUID index;
    private final ZonedDateTime timestamp;
    private final Object transactions;
    private final byte[] previousBlockHash;
    private byte[] blockHash;
    private int nonce;

    private Block(BBuilder blockBuilder) throws Exception {
        this.index = blockBuilder.index;
        this.timestamp = blockBuilder.timestamp;
        this.transactions = blockBuilder.transactions;
        this.previousBlockHash = blockBuilder.previousBlock;
        this.blockHash = BCOHasher.hash(this);
    }

    public UUID getIndex() {
        return index;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public Object getTransactions() {
        return transactions;
    }

    public byte[] getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void setBlockHash(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public void incrementNonce() {
        this.nonce++;
    }

    @Override
    public String toString() {
        return BCOHasher.humanReadableHash(blockHash);
    }

    /**
     * BBuilder class is the SSBlock builder. This is to ensure some level
     * of data protection by enforcing non-direct data access and immutable data.
     */
    public static class BBuilder {

        private UUID index;
        private ZonedDateTime timestamp;
        private Object transactions;
        private byte[] previousBlock;

        private BBuilder(){}

        public static BBuilder newSSBlockBuilder(){
            return new BBuilder();
        }

        public BBuilder setTransactions(Object transactions){
            this.transactions = transactions;
            return this;
        }

        public BBuilder setPreviousBlock(byte[] previousBlock){
            this.previousBlock = previousBlock;
            return this;
        }

        public Block build() throws Exception {
            this.index = UUID.randomUUID();
            timestamp = DateTimeUtil.getCurrentTimestamp();
            return new Block(this);
        }

    }

}
