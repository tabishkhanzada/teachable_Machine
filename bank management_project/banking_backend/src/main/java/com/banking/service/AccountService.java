package com.banking.service;

import com.banking.model.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public Account createAccount(String accountId, BigDecimal initialBalance) {
        Account account = new Account(accountId, initialBalance);
        accounts.put(accountId, account);
        return account;
    }

    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    public void deposit(String accountId, BigDecimal amount) {
        Account account = getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        account.deposit(amount);
    }

    public void withdraw(String accountId, BigDecimal amount) {
        Account account = getAccount(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        account.withdraw(amount);
    }
}
