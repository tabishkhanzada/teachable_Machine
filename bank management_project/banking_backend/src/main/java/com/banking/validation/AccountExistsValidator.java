package com.banking.validation;

import com.banking.dao.AccountDao;
import com.banking.model.Transaction;

public class AccountExistsValidator implements TransactionValidator {

    private final AccountDao accountDao;

    public AccountExistsValidator(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.getSourceAccountId() != null && !accountDao.findByAccountId(transaction.getSourceAccountId()).isPresent()) {
            return ValidationResult.fail("Source account does not exist.");
        }
        if (transaction.getDestinationAccountId() != null && !accountDao.findByAccountId(transaction.getDestinationAccountId()).isPresent()) {
            return ValidationResult.fail("Destination account does not exist.");
        }
        return ValidationResult.ok();
    }
}
