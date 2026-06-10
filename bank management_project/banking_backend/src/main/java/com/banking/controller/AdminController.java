package com.banking.controller;

import com.banking.command.CreditAccountCommand;
import com.banking.model.Role;
import com.banking.model.User;
import com.banking.service.CreditRequestService;
import com.banking.service.TransactionAuditService;
import com.banking.service.UserService;
import com.banking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final TransactionAuditService transactionAuditService;
    private final CreditRequestService creditRequestService;

    @Autowired
    public AdminController(UserService userService, TransactionService transactionService,
                           TransactionAuditService transactionAuditService,
                           CreditRequestService creditRequestService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.transactionAuditService = transactionAuditService;
        this.creditRequestService = creditRequestService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : userService.findAllUsers()) {
            String balance = "0.00";
            if (u.getAccountId() != null) {
                balance = userService.findAccount(u.getAccountId())
                        .map(a -> a.getBalance().toPlainString())
                        .orElse("0.00");
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", u.getUserId());
            row.put("username", u.getUsername());
            row.put("accountId", u.getAccountId());
            row.put("blocked", u.isBlocked());
            row.put("initialBalance", balance);   // real account balance
            // password intentionally omitted from the response
            result.add(row);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String username = request.get("username");
        String password = request.get("password");
        String accountId = request.get("accountId");
        BigDecimal initialBalance = parseBalance(request.get("initialBalance"), BigDecimal.ZERO);

        User user = new User(userId, username, password, Role.USER, false, accountId);
        userService.createOrUpdateUser(user, initialBalance);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<Void> blockUser(@PathVariable String userId, @RequestBody Map<String, Boolean> request) {
        userService.blockUser(userId, request.getOrDefault("blocked", true));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> listTransactions() {
        return ResponseEntity.ok(transactionService.findAllTransactions());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<Void> updateUser(@PathVariable String userId, @RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String accountId = request.get("accountId");
        // null = "keep the existing balance" (the Update form doesn't carry a balance).
        BigDecimal initialBalance = parseBalance(request.get("initialBalance"), null);
        User user = new User(userId, username, password, Role.USER, false, accountId);
        userService.createOrUpdateUser(user, initialBalance);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/credit-requests")
    public ResponseEntity<?> listCreditRequests() {
        return ResponseEntity.ok(creditRequestService.findAllRequests());
    }

    @PostMapping("/credit-requests/{requestId}/approve")
    public ResponseEntity<?> approveCreditRequest(@PathVariable String requestId) {
        return ResponseEntity.ok(creditRequestService.approveRequest(requestId));
    }

    @PostMapping("/credit-requests/{requestId}/reject")
    public ResponseEntity<?> rejectCreditRequest(@PathVariable String requestId) {
        return ResponseEntity.ok(creditRequestService.rejectRequest(requestId));
    }

    @PostMapping("/credit")
    public ResponseEntity<Void> creditUser(@RequestBody Map<String, String> request) {
        String accountId = request.get("accountId");
        BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0"));
        String description = request.getOrDefault("description", "Admin credit");
        new CreditAccountCommand(transactionService, accountId, amount, description).execute();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/audit")
    public ResponseEntity<?> getTransactionAuditLog() {
        return ResponseEntity.ok(transactionAuditService.getAuditLog());
    }

    /** Parses an optional balance string; blank/missing returns the fallback. */
    private BigDecimal parseBalance(String raw, BigDecimal fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        return new BigDecimal(raw.trim());
    }
}
