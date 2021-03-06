package org.nathanielbunch.ssblockchain.node.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nathanielbunch.ssblockchain.core.ledger.Transaction;
import org.nathanielbunch.ssblockchain.core.ledger.Wallet;
import org.nathanielbunch.ssblockchain.node.model.BlockResponse;
import org.nathanielbunch.ssblockchain.node.service.BlockchainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

/**
 * Provides the rest interface for interacting with the SSBlockchain.
 *
 * @since 0.0.1
 * @author nathanielbunch
 */
@RestController
public class BlockchainController {

    Logger logger = LoggerFactory.getLogger(BlockchainController.class);

    private ObjectMapper objectMapper;

    @Autowired
    BlockchainService service;

    @PostConstruct
    private void init() {
        this.objectMapper = new ObjectMapper();
    }

    @RequestMapping(value = "/transaction", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> putSSTransaction(@RequestBody JsonNode data) throws JsonProcessingException {
        logger.trace("Received new data: {}", data);
        Transaction transaction = objectMapper.treeToValue(data, Transaction.class);
        this.service.addNewTransaction(transaction);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/transaction", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Transaction> getSSTransaction() throws Exception {
        return new ResponseEntity<>(this.service.getTransaction(), HttpStatus.OK);
    }

    @RequestMapping(value = "/wallet", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Wallet> getSSWallet() throws Exception {
        return new ResponseEntity<>(this.service.getWallet(), HttpStatus.OK);
    }

    @RequestMapping(value = "/mine", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BlockResponse> mineBlock() throws Exception {
        return new ResponseEntity<>(this.service.mineBlock(), HttpStatus.CREATED);
    }

}
