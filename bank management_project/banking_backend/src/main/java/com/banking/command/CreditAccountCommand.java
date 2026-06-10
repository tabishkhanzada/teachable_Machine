package com.banking.command;

import com.banking.service.TransactionService;

import java.math.BigDecimal;

public class CreditAccountCommand implements Command {

    private final TransactionService transactionService;
    private final String accountId;
    private final BigDecimal amount;
    private final String description;

    public CreditAccountCommand(TransactionService transactionService, String accountId, BigDecimal amount, String description) {
        this.transactionService = transactionService;
        this.accountId = accountId;
        this.amount = amount;
        this.description = description;
    }

    @Override
    public void execute() {
        transactionService.creditAccount(accountId, amount, description);
    }
}
