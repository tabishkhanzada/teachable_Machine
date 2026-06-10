package com.banking.model;

import java.math.BigDecimal;

public class CreditRequest {

    private final String requestId;
    private final String userId;
    private final String accountId;
    private final BigDecimal amount;
    private CreditRequestStatus status;

    public CreditRequest(String requestId, String userId, String accountId, BigDecimal amount, CreditRequestStatus status) {
        this.requestId = requestId;
        this.userId = userId;
        this.accountId = accountId;
        this.amount = amount;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CreditRequestStatus getStatus() {
        return status;
    }

    public void setStatus(CreditRequestStatus status) {
        this.status = status;
    }
}
