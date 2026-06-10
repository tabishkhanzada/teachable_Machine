package com.banking.controller;

import com.banking.command.TransferCommand;
import com.banking.service.CreditRequestService;
import com.banking.service.TransactionService;
import com.banking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final CreditRequestService creditRequestService;

    @Autowired
    public UserController(UserService userService, TransactionService transactionService, CreditRequestService creditRequestService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.creditRequestService = creditRequestService;
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> getBalance(@PathVariable String userId) {
        return userService.findById(userId)
                .map(user -> {
                    if (user.getAccountId() == null) {
                        return ResponseEntity.badRequest().body(Map.of("error", "User has no account."));
                    }
                    return userService.findAccount(user.getAccountId())
                            .map(account -> ResponseEntity.ok(Map.of(
                                    "accountId", account.getAccountId(),
                                    "balance", account.getBalance()
                            )))
                            .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Account not found.")));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, String> request) {
        String sourceAccountId = request.get("sourceAccountId");
        String destinationAccountId = request.get("destinationAccountId");
        BigDecimal amount = new BigDecimal(request.getOrDefault("amount", "0"));
        new TransferCommand(transactionService, sourceAccountId, destinationAccountId, amount).execute();
        return ResponseEntity.ok(Map.of("status", "transfer submitted"));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@RequestParam(required = false) String accountId) {
        // Scoped to a single account. Without an accountId we return nothing
        // rather than leaking every account's transactions; admins use
        // /api/admin/transactions for the full list.
        if (accountId == null || accountId.isBlank()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
        return ResponseEntity.ok(transactionService.findTransactionsForAccount(accountId));
    }

    @PostMapping("/request-credit")
    public ResponseEntity<?> requestCredit(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String amount = request.get("amount");
        return ResponseEntity.ok(Map.of(
                "request", creditRequestService.createRequestByUsername(username, amount),
                "status", "PENDING"
        ));
    }

    @GetMapping("/credit-requests")
    public ResponseEntity<?> getCreditRequests(@RequestParam String userId) {
        return ResponseEntity.ok(creditRequestService.findRequestsForUser(userId));
    }
}
