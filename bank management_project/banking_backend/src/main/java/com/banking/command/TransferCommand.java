package com.banking.command;

import com.banking.service.TransactionService;

import java.math.BigDecimal;

public class TransferCommand implements Command {

    private final TransactionService transactionService;
    private final String sourceAccountId;
    private final String destinationAccountId;
    private final BigDecimal amount;

    public TransferCommand(TransactionService transactionService, String sourceAccountId, String destinationAccountId, BigDecimal amount) {
        this.transactionService = transactionService;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    @Override
    public void execute() {
        transactionService.transfer(sourceAccountId, destinationAccountId, amount);
    }
}
