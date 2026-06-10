package com.banking.service;

import com.banking.dao.TransactionDao;
import com.banking.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionAuditService {

    private final TransactionDao transactionDao;

    @Autowired
    public TransactionAuditService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public void log(Transaction transaction) {
        // Audit is backed by the transactions store.
        // No separate in-memory log is required when transactions are persisted.
    }

    public List<Transaction> getAuditLog() {
        return transactionDao.findAll();
    }
}
