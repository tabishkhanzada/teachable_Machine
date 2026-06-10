package com.banking.dao;

import com.banking.model.Transaction;
import com.banking.model.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TransactionDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Transaction> transactionRowMapper = (rs, rowNum) -> new Transaction(
            rs.getString("transaction_id"),
            rs.getString("source_account_id"),
            rs.getString("destination_account_id"),
            rs.getBigDecimal("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getTimestamp("timestamp").toLocalDateTime(),
            rs.getString("description")
    );

    @Autowired
    public TransactionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Transaction> findAll() {
        return jdbcTemplate.query("SELECT transaction_id, source_account_id, destination_account_id, amount, type, timestamp, description FROM transactions ORDER BY timestamp DESC", transactionRowMapper);
    }

    public List<Transaction> findByAccountId(String accountId) {
        return jdbcTemplate.query(
                "SELECT transaction_id, source_account_id, destination_account_id, amount, type, timestamp, description FROM transactions " +
                        "WHERE source_account_id = ? OR destination_account_id = ? ORDER BY timestamp DESC",
                transactionRowMapper,
                accountId,
                accountId
        );
    }

    public void save(Transaction transaction) {
        jdbcTemplate.update(
                "INSERT INTO transactions (transaction_id, source_account_id, destination_account_id, amount, type, timestamp, description) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE source_account_id = VALUES(source_account_id), destination_account_id = VALUES(destination_account_id), amount = VALUES(amount), type = VALUES(type), timestamp = VALUES(timestamp), description = VALUES(description)",
                transaction.getTransactionId(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getType().name(),
                java.sql.Timestamp.valueOf(transaction.getTimestamp()),
                transaction.getDescription()
        );
    }
}
