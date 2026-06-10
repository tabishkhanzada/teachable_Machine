package com.banking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {

    private String userId;
    private String username;
    private String password;
    private Role role;
    private boolean blocked;
    private String accountId;

    public User(String userId, String username, String password, Role role, boolean blocked, String accountId) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.blocked = blocked;
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    /** Never serialize the password/hash in any API response. */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
