package com.banking.dao;

import com.banking.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Account> accountRowMapper = (rs, rowNum) -> new Account(
            rs.getString("account_id"),
            rs.getBigDecimal("balance")
    );

    @Autowired
    public AccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Account> findByAccountId(String accountId) {
        try {
            Account account = jdbcTemplate.queryForObject(
                    "SELECT account_id, balance FROM accounts WHERE account_id = ?",
                    accountRowMapper,
                    accountId
            );
            return Optional.ofNullable(account);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<Account> findAll() {
        return jdbcTemplate.query("SELECT account_id, balance FROM accounts", accountRowMapper);
    }

    public void save(Account account) {
        jdbcTemplate.update(
                "INSERT INTO accounts (account_id, balance) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE balance = VALUES(balance)",
                account.getAccountId(),
                account.getBalance()
        );
    }

    public void deleteById(String accountId) {
        jdbcTemplate.update("DELETE FROM accounts WHERE account_id = ?", accountId);
    }
}
