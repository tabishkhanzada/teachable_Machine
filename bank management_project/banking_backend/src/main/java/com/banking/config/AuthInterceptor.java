package com.banking.config;

import com.banking.security.TokenStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Server-side authorization guard.
 *
 *  - Every /api/** request (except /api/auth/**) must carry a valid token.
 *  - /api/admin/** additionally requires the token's role to be ADMIN.
 *
 * This enforces access on the server, so the desktop UI is no longer the only
 * thing standing between a caller and the admin endpoints.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        // Allow CORS/preflight through untouched.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;

        TokenStore.Session session = tokenStore.validate(token);
        if (session == null) {
            return deny(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required. Please sign in again.");
        }

        if (request.getRequestURI().startsWith("/api/admin") && !"ADMIN".equals(session.role)) {
            return deny(response, HttpServletResponse.SC_FORBIDDEN, "Administrator access required.");
        }

        return true;
    }

    private boolean deny(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
        return false;
    }
}
