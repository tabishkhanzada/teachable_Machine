package com.banking.controller;

import com.banking.model.User;
import com.banking.security.TokenStore;
import com.banking.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final TokenStore tokenStore;

    @Autowired
    public AuthController(AuthenticationService authenticationService, TokenStore tokenStore) {
        this.authenticationService = authenticationService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Optional<User> result = authenticationService.authenticate(username, password);
        if (result.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = result.get();
        String token = tokenStore.issue(user.getUsername(), user.getRole().name());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", user.getUserId());
        body.put("username", user.getUsername());
        body.put("role", user.getRole().name());
        body.put("accountId", user.getAccountId());
        body.put("token", token);
        return ResponseEntity.ok(body);
    }
}
