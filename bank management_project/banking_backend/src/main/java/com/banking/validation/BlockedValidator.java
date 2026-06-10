package com.banking.validation;

import com.banking.dao.UserDao;
import com.banking.model.Transaction;
import com.banking.model.User;

public class BlockedValidator implements TransactionValidator {

    private final UserDao userDao;

    public BlockedValidator(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.getSourceAccountId() == null) {
            return ValidationResult.ok();
        }
        User user = userDao.findByAccountId(transaction.getSourceAccountId()).orElse(null);
        if (user != null && user.isBlocked()) {
            return ValidationResult.fail("Source user is blocked.");
        }
        return ValidationResult.ok();
    }
}
