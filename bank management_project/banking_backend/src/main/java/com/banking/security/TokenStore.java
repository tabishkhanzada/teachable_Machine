package com.banking.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of active login tokens. A token is issued at login and must
 * be presented (Authorization: Bearer &lt;token&gt;) on every protected request.
 * Tokens live only while the server is running, so a restart forces re-login.
 */
@Component
public class TokenStore {

    /** Minimal identity carried by a token. */
    public static final class Session {
        public final String username;
        public final String role;

        public Session(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }

    private final Map<String, Session> tokens = new ConcurrentHashMap<>();

    public String issue(String username, String role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, new Session(username, role));
        return token;
    }

    public Session validate(String token) {
        return (token == null) ? null : tokens.get(token);
    }

    public void revoke(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
