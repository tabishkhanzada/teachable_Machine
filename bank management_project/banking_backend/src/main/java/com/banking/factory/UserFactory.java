package com.banking.factory;

import com.banking.model.Role;
import com.banking.model.User;

public class UserFactory {

    public static User createUser(String userId, String username, String password, String accountId) {
        return new User(userId, username, password, Role.USER, false, accountId);
    }

    public static User createAdmin(String userId, String username, String password, String accountId) {
        return new User(userId, username, password, Role.ADMIN, false, accountId);
    }
}
