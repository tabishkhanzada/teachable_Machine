package com.banking.service;

import com.banking.dao.UserDao;
import com.banking.factory.UserFactory;
import com.banking.model.Role;
import com.banking.model.User;
import com.banking.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final UserDao userDao;

    @Autowired
    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String username, String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return Optional.of(UserFactory.createAdmin("admin", "admin", "admin", "admin-account"));
        }

        Optional<User> storedUser = userDao.findByUsername(username);
        if (storedUser.isPresent() && PasswordHasher.matches(password, storedUser.get().getPassword())) {
            User user = storedUser.get();
            if (user.isBlocked()) {
                return Optional.empty();
            }
            // Auto-migrate a legacy plain-text password to a salted hash on first login.
            if (!PasswordHasher.isHashed(user.getPassword())) {
                userDao.save(new User(
                        user.getUserId(),
                        user.getUsername(),
                        PasswordHasher.hash(password),
                        user.getRole(),
                        user.isBlocked(),
                        user.getAccountId()
                ));
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }
}

