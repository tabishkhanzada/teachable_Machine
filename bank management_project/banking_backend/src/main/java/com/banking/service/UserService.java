package com.banking.service;

import com.banking.dao.AccountDao;
import com.banking.dao.UserDao;
import com.banking.model.Account;
import com.banking.model.User;
import com.banking.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;
    private final AccountDao accountDao;

    @Autowired
    public UserService(UserDao userDao, AccountDao accountDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    public Optional<User> findById(String userId) {
        return userDao.findById(userId);
    }

    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public List<User> findAllUsers() {
        return userDao.findAll();
    }

    public void createOrUpdateUser(User user, BigDecimal initialBalance) {
        String newAccount = blankToNull(user.getAccountId());
        Optional<User> existingUser = userDao.findById(user.getUserId());

        if (existingUser.isPresent()) {
            User old = existingUser.get();

            // Keep the stored password if none was supplied; otherwise hash the new one.
            String password;
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                password = old.getPassword();
            } else {
                password = PasswordHasher.isHashed(user.getPassword())
                        ? user.getPassword()
                        : PasswordHasher.hash(user.getPassword());
            }

            // Username and account may change; role + blocked are preserved
            // (use the dedicated Block/Unblock action to change blocked).
            User merged = new User(
                    user.getUserId(),
                    user.getUsername(),
                    password,
                    old.getRole(),
                    old.isBlocked(),
                    newAccount
            );

            String oldAccount = blankToNull(old.getAccountId());

            if (newAccount != null) {
                BigDecimal balance;
                if (initialBalance != null) {
                    balance = initialBalance;
                } else if (newAccount.equals(oldAccount)) {
                    balance = accountDao.findByAccountId(newAccount)
                            .map(Account::getBalance).orElse(BigDecimal.ZERO);
                } else {
                    balance = BigDecimal.ZERO;
                }
                // Create/update the (possibly new) account first to satisfy the FK.
                accountDao.save(new Account(newAccount, balance));
            }

            userDao.save(merged);

            // Remove the orphaned old account after the user no longer points at it.
            if (oldAccount != null && !oldAccount.equals(newAccount)) {
                accountDao.deleteById(oldAccount);
            }
        } else {
            // New user — always store a hashed password.
            String password = (user.getPassword() == null || user.getPassword().isBlank())
                    ? ""
                    : PasswordHasher.hash(user.getPassword());

            User created = new User(user.getUserId(), user.getUsername(), password,
                    user.getRole(), user.isBlocked(), newAccount);

            if (newAccount != null) {
                accountDao.save(new Account(newAccount, initialBalance != null ? initialBalance : BigDecimal.ZERO));
            }
            userDao.save(created);
        }
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public void deleteUser(String userId) {
        userDao.findById(userId).ifPresent(user -> {
            if (user.getAccountId() != null) {
                accountDao.deleteById(user.getAccountId());
            }
        });
        userDao.deleteById(userId);
    }

    public void blockUser(String userId, boolean blocked) {
        userDao.findById(userId).ifPresent(user -> {
            user.setBlocked(blocked);
            userDao.save(user);
        });
    }

    public Optional<Account> findAccount(String accountId) {
        return accountDao.findByAccountId(accountId);
    }
}
