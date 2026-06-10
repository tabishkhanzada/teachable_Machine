package com.banking.frontend.service;

import java.util.prefs.Preferences;

public class SessionManager {
    private static final String PREF_USER_ID = "userId";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_ROLE = "role";
    private static final String PREF_ACCOUNT_ID = "accountId";
    private final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);

    /**
     * The auth token is kept in memory only (shared across all SessionManager
     * instances) and is intentionally NOT persisted. It dies when the app
     * closes, so each launch requires a fresh login and a fresh valid token.
     */
    private static volatile String authToken;

    public void setToken(String token) {
        authToken = token;
    }

    public String getToken() {
        return authToken;
    }

    public void saveSession(String userId, String username, String role, String accountId) {
        prefs.put(PREF_USER_ID, userId);
        prefs.put(PREF_USERNAME, username);
        prefs.put(PREF_ROLE, role);
        prefs.put(PREF_ACCOUNT_ID, accountId != null ? accountId : "");
    }

    public String getSavedUserId() {
        return prefs.get(PREF_USER_ID, null);
    }

    public String getSavedUsername() {
        return prefs.get(PREF_USERNAME, null);
    }

    public String getSavedRole() {
        return prefs.get(PREF_ROLE, null);
    }

    public String getSavedAccountId() {
        String saved = prefs.get(PREF_ACCOUNT_ID, null);
        return saved != null && saved.isBlank() ? null : saved;
    }

    public boolean isLoggedIn() {
        // A valid in-memory token is required, so a restarted app always
        // returns to the login screen (no stale auto-login without a token).
        return authToken != null && getSavedUserId() != null && getSavedRole() != null;
    }

    public void clearSession() {
        authToken = null;
        prefs.remove(PREF_USER_ID);
        prefs.remove(PREF_USERNAME);
        prefs.remove(PREF_ROLE);
        prefs.remove(PREF_ACCOUNT_ID);
    }
}
