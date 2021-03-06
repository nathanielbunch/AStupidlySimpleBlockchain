package org.nathanielbunch.ssblockchain.core.ledger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.nathanielbunch.ssblockchain.core.serialization.TransactionDeserializer;
import org.nathanielbunch.ssblockchain.core.utils.BCOHasher;
import org.nathanielbunch.ssblockchain.core.utils.DateTimeUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Every SSBlock is made of n number of SSTransactions. SSTransactions contain
 * information used for identifying a transaction (index), a timestamp for
 * sorting and managing transactions in a block, an origin address, a
 * destination address, an amount of coin transmitted, a transaction note, and
 * the identifying transaction hash. Transactions can be queried for by their
 * hash or their index and timestamp.
 *
 * @since 0.0.1
 * @author nathanielbunch
 */
@JsonInclude
@JsonDeserialize(using = TransactionDeserializer.class)
public class Transaction implements Serializable {

    private final UUID index;
    private final ZonedDateTime timestamp;
    private final String origin;
    private final String destination;
    private final BigDecimal amount;
    private final String note;
    private final byte[] transactionHash;

    protected Transaction(TBuilder builder) throws NoSuchAlgorithmException {
        this.index = builder.index;
        this.timestamp = builder.timestamp;
        this.origin = builder.origin;
        this.destination = builder.destination;
        this.amount = builder.amount;
        this.note = builder.note;
        this.transactionHash = BCOHasher.hash(this);
    }

    public UUID getIndex() {
        return index;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    @Override
    public String toString(){
        return BCOHasher.humanReadableHash(transactionHash);
    }

    /**
     * TBuilder class is the SSTransaction builder. This is to ensure some level
     * of data protection by enforcing non-direct data access and immutable data.
     */
    public static class TBuilder {

        protected UUID index;
        protected ZonedDateTime timestamp;
        protected String origin;
        protected String destination;
        protected BigDecimal amount;
        protected String note;

        private TBuilder(){}

        public TBuilder setOrigin(@JsonProperty("origin") String origin) {
            this.origin = origin;
            return this;
        }

        public TBuilder setDestination(@JsonProperty("destination") String destination) {
            this.destination = destination;
            return this;
        }

        public TBuilder setValue(@JsonProperty("value") BigDecimal value) {
            this.amount = value;
            return this;
        }

        public TBuilder setNote(@JsonProperty("note") String note) {
            this.note = note;
            return this;
        }

        public static TBuilder newSSTransactionBuilder() {
            return new TBuilder();
        }

        public Transaction build() throws NoSuchAlgorithmException {
            this.index = UUID.randomUUID();
            timestamp = DateTimeUtil.getCurrentTimestamp();
            return new Transaction(this);
        }
    }

}
