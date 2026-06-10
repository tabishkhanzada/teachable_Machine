package com.banking.validation;

import com.banking.model.Transaction;

public class TransactionValidationChain {

    private TransactionValidator current;
    private TransactionValidationChain next;

    public TransactionValidationChain(TransactionValidator current) {
        this.current = current;
    }

    public TransactionValidationChain append(TransactionValidator nextValidator) {
        TransactionValidationChain nextChain = new TransactionValidationChain(nextValidator);
        this.next = nextChain;
        return this;
    }

    public ValidationResult validate(Transaction transaction) {
        ValidationResult result = current.validate(transaction);
        if (!result.isValid()) {
            return result;
        }
        if (next != null) {
            return next.validate(transaction);
        }
        return ValidationResult.ok();
    }
}
