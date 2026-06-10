package com.banking.model;

import java.math.BigDecimal;

public class Account {

    private final String accountId;
    private BigDecimal balance;

    public Account(String accountId, BigDecimal initialBalance) {
        this.accountId = accountId;
        this.balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds.");
        }
        balance = balance.subtract(amount);
    }
}
