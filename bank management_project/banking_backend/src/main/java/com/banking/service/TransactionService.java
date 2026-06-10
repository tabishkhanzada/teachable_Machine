package com.banking.service;

import com.banking.builder.TransactionBuilder;
import com.banking.dao.AccountDao;
import com.banking.dao.TransactionDao;
import com.banking.dao.UserDao;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import com.banking.validation.AccountExistsValidator;
import com.banking.validation.AmountValidator;
import com.banking.validation.BlockedValidator;
import com.banking.validation.BalanceValidator;
import com.banking.validation.TransactionValidationChain;
import com.banking.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final UserDao userDao;
    private final TransactionAuditService auditService;

    @Autowired
    public TransactionService(AccountDao accountDao, TransactionDao transactionDao, UserDao userDao,
                              TransactionAuditService auditService) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.userDao = userDao;
        this.auditService = auditService;
    }

    private void validateTransaction(Transaction transaction) {
        TransactionValidationChain chain = new TransactionValidationChain(new AmountValidator())
                .append(new AccountExistsValidator(accountDao))
                .append(new BlockedValidator(userDao));

        if (transaction.getType() == TransactionType.TRANSFER) {
            chain.append(new BalanceValidator(accountDao));
        }

        ValidationResult result = chain.validate(transaction);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getMessage());
        }
    }

    public List<Transaction> findAllTransactions() {
        return transactionDao.findAll();
    }

    public List<Transaction> findTransactionsForAccount(String accountId) {
        return transactionDao.findByAccountId(accountId);
    }

    @Transactional
    public void transfer(String sourceAccountId, String destinationAccountId, BigDecimal amount) {
        Transaction transaction = new TransactionBuilder()
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .description("User transfer")
                .build();

        validateTransaction(transaction);

        Account sourceAccount = accountDao.findByAccountId(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found."));
        Account destinationAccount = accountDao.findByAccountId(destinationAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found."));

        sourceAccount.withdraw(amount);
        destinationAccount.deposit(amount);
        accountDao.save(sourceAccount);
        accountDao.save(destinationAccount);

        transactionDao.save(transaction);
        auditService.log(transaction);
    }

    public void creditAccountByUsername(String username, BigDecimal amount, String description) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        String accountId = userDao.findByUsername(username)
                .map(user -> user.getAccountId())
                .filter(account -> account != null && !account.isBlank())
                .orElseThrow(() -> new IllegalArgumentException("User or account not found for username: " + username));
        creditAccount(accountId, amount, description);
    }

    @Transactional
    public void creditAccount(String accountId, BigDecimal amount, String description) {
        Transaction transaction = new TransactionBuilder()
                .destinationAccountId(accountId)
                .amount(amount)
                .type(TransactionType.CREDIT)
                .description(description)
                .build();

        validateTransaction(transaction);

        Account account = accountDao.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        account.deposit(amount);
        accountDao.save(account);

        transactionDao.save(transaction);
        auditService.log(transaction);
    }
}
