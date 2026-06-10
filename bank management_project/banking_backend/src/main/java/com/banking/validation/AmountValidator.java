package com.banking.validation;

import com.banking.model.Transaction;

import java.math.BigDecimal;

public class AmountValidator implements TransactionValidator {

    @Override
    public ValidationResult validate(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.fail("Amount must be positive.");
        }
        return ValidationResult.ok();
    }
}
