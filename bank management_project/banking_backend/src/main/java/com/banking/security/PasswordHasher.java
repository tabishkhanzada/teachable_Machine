package com.banking.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Salted SHA-256 password hashing using only the JDK (no extra dependency).
 *
 * Stored format:  sha256$&lt;saltHex&gt;$&lt;hashHex&gt;
 *
 * {@link #matches(String, String)} also accepts a legacy plain-text value so
 * existing accounts keep working; the caller can then re-save a hashed value
 * (auto-migration) on the next successful login.
 */
public final class PasswordHasher {

    private static final String PREFIX = "sha256$";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    public static String hash(String raw) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return PREFIX + toHex(salt) + "$" + sha256(salt, raw);
    }

    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (!isHashed(stored)) {
            return constantTimeEquals(stored, raw); // legacy plain text
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 3) {
            return false;
        }
        byte[] salt = fromHex(parts[1]);
        return constantTimeEquals(parts[2], sha256(salt, raw));
    }

    private static String sha256(byte[] salt, String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return toHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static byte[] fromHex(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
