package com.banking.validation;

import com.banking.dao.AccountDao;
import com.banking.model.Transaction;
import com.banking.model.Account;

import java.math.BigDecimal;

public class BalanceValidator implements TransactionValidator {

    private final AccountDao accountDao;

    public BalanceValidator(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.getSourceAccountId() == null) {
            return ValidationResult.ok();
        }
        Account sourceAccount = accountDao.findByAccountId(transaction.getSourceAccountId()).orElse(null);
        if (sourceAccount == null) {
            return ValidationResult.fail("Source account does not exist.");
        }
        BigDecimal amount = transaction.getAmount();
        if (amount == null || sourceAccount.getBalance().compareTo(amount) < 0) {
            return ValidationResult.fail("Insufficient funds.");
        }
        return ValidationResult.ok();
    }
}
