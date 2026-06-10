package com.banking.validation;

import com.banking.model.Transaction;

public interface TransactionValidator {
    ValidationResult validate(Transaction transaction);
}
