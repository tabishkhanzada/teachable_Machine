package com.banking.dao;

import com.banking.model.Role;
import com.banking.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getString("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role")),
            rs.getBoolean("blocked"),
            rs.getString("account_id")
    );

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByUsername(String username) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT user_id, username, password, role, blocked, account_id FROM users WHERE username = ?",
                    userRowMapper,
                    username
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<User> findById(String userId) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT user_id, username, password, role, blocked, account_id FROM users WHERE user_id = ?",
                    userRowMapper,
                    userId
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<User> findByAccountId(String accountId) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT user_id, username, password, role, blocked, account_id FROM users WHERE account_id = ?",
                    userRowMapper,
                    accountId
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        return jdbcTemplate.query(
                "SELECT user_id, username, password, role, blocked, account_id FROM users",
                userRowMapper
        );
    }

    public void save(User user) {
        jdbcTemplate.update(
                "INSERT INTO users (user_id, username, password, role, blocked, account_id) VALUES (?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE username = VALUES(username), password = VALUES(password), " +
                        "role = VALUES(role), blocked = VALUES(blocked), account_id = VALUES(account_id)",
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().name(),  
                user.isBlocked(),
                user.getAccountId()
        );
    }

    public void deleteById(String userId) {
        jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", userId);
    }
}
