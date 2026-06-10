package com.banking.service;

import com.banking.command.CreditAccountCommand;
import com.banking.dao.AccountDao;
import com.banking.dao.CreditRequestDao;
import com.banking.dao.UserDao;
import com.banking.model.CreditRequest;
import com.banking.model.CreditRequestStatus;
import com.banking.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreditRequestService {

    private final CreditRequestDao creditRequestDao;
    private final TransactionService transactionService;
    private final UserDao userDao;
    private final AccountDao accountDao;

    @Autowired
    public CreditRequestService(CreditRequestDao creditRequestDao,
                                TransactionService transactionService,
                                UserDao userDao,
                                AccountDao accountDao) {
        this.creditRequestDao = creditRequestDao;
        this.transactionService = transactionService;
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    public List<CreditRequest> findAllRequests() {
        return creditRequestDao.findAll();
    }

    public List<CreditRequest> findRequestsForUser(String userId) {
        return creditRequestDao.findByUserId(userId);
    }

    public Optional<CreditRequest> findById(String requestId) {
        return creditRequestDao.findById(requestId);
    }

    public CreditRequest createRequest(String userId, String accountId, String amount) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID is required.");
        }
        BigDecimal requestedAmount = new BigDecimal(amount);
        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Requested amount must be positive.");
        }

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (user.isBlocked()) {
            throw new IllegalStateException("Blocked users cannot request credit.");
        }

        accountDao.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
        if (!accountId.equals(user.getAccountId())) {
            throw new IllegalArgumentException("Credit request must be made for the user's own account.");
        }

        CreditRequest request = new CreditRequest(UUID.randomUUID().toString(), userId, accountId, requestedAmount, CreditRequestStatus.PENDING);
        creditRequestDao.save(request);
        return request;
    }

    public CreditRequest createRequestByUsername(String username, String amount) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (user.isBlocked()) {
            throw new IllegalStateException("Blocked users cannot request credit.");
        }
        String accountId = user.getAccountId();
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("User has no account.");
        }
        return createRequest(user.getUserId(), accountId, amount);
    }

    /**
     * Crediting the account AND flipping the request to APPROVED now run inside a
     * single transaction. If either step fails, both roll back — so an account can
     * never be credited while the request still shows PENDING (no double-credit).
     */
    @Transactional
    public CreditRequest approveRequest(String requestId) {
        CreditRequest request = creditRequestDao.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Credit request not found."));
        if (request.getStatus() != CreditRequestStatus.PENDING) {
            throw new IllegalStateException("Credit request is not pending.");
        }

        CreditAccountCommand creditCommand = new CreditAccountCommand(transactionService,
                request.getAccountId(), request.getAmount(), "Approved credit request");
        creditCommand.execute();

        request.setStatus(CreditRequestStatus.APPROVED);
        creditRequestDao.save(request);
        return request;
    }

    public CreditRequest rejectRequest(String requestId) {
        CreditRequest request = creditRequestDao.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Credit request not found."));
        if (request.getStatus() != CreditRequestStatus.PENDING) {
            throw new IllegalStateException("Credit request is not pending.");
        }
        request.setStatus(CreditRequestStatus.REJECTED);
        creditRequestDao.save(request);
        return request;
    }
}
