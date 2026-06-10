package com.banking.dao;

import com.banking.model.CreditRequest;
import com.banking.model.CreditRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CreditRequestDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<CreditRequest> creditRequestRowMapper = (rs, rowNum) -> new CreditRequest(
            rs.getString("request_id"),
            rs.getString("user_id"),
            rs.getString("account_id"),
            rs.getBigDecimal("amount"),
            CreditRequestStatus.valueOf(rs.getString("status"))
    );

    @Autowired
    public CreditRequestDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CreditRequest> findAll() {
        return jdbcTemplate.query("SELECT request_id, user_id, account_id, amount, status FROM credit_requests", creditRequestRowMapper);
    }

    public Optional<CreditRequest> findById(String requestId) {
        try {
            CreditRequest request = jdbcTemplate.queryForObject(
                    "SELECT request_id, user_id, account_id, amount, status FROM credit_requests WHERE request_id = ?",
                    creditRequestRowMapper,
                    requestId
            );
            return Optional.ofNullable(request);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public void save(CreditRequest request) {
        jdbcTemplate.update(
                "INSERT INTO credit_requests (request_id, user_id, account_id, amount, status) VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), account_id = VALUES(account_id), amount = VALUES(amount), status = VALUES(status)",
                request.getRequestId(),
                request.getUserId(),
                request.getAccountId(),
                request.getAmount(),
                request.getStatus().name()
        );
    }

    public List<CreditRequest> findByUserId(String userId) {
        return jdbcTemplate.query(
                "SELECT request_id, user_id, account_id, amount, status FROM credit_requests WHERE user_id = ?",
                creditRequestRowMapper,
                userId
        );
    }
}
