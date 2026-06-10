package com.banking.builder;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionBuilder {

    private String transactionId = UUID.randomUUID().toString();
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private TransactionType type = TransactionType.TRANSFER;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String description = "";

    public TransactionBuilder sourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
        return this;
    }

    public TransactionBuilder destinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
        return this;
    }

    public TransactionBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionBuilder type(TransactionType type) {
        this.type = type;
        return this;
    }

    public TransactionBuilder timestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TransactionBuilder description(String description) {
        this.description = description;
        return this;
    }

    public Transaction build() {
        return new Transaction(transactionId, sourceAccountId, destinationAccountId, amount, type, timestamp, description);
    }
}
