package com.banking.frontend.service;

import com.banking.frontend.model.CreditRequest;
import com.banking.frontend.model.LoginResponse;
import com.banking.frontend.model.Transaction;
import com.banking.frontend.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ApiService {
    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public LoginResponse login(String username, String password) throws IOException, InterruptedException {
        Map<String, String> payload = Map.of(
                "username", username,
                "password", password
        );
        String response = apiClient.sendPost("/api/auth/login", payload);
        return gson.fromJson(response, LoginResponse.class);
    }

    public List<User> getAllUsers() throws IOException, InterruptedException {
        String response = apiClient.sendGet("/api/admin/users");
        Type type = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(response, type);
    }

    public List<Transaction> getAllTransactions() throws IOException, InterruptedException {
        String response = apiClient.sendGet("/api/admin/transactions");
        Type type = new TypeToken<List<Transaction>>() {}.getType();
        return gson.fromJson(response, type);
    }

    public List<Transaction> getAuditLog() throws IOException, InterruptedException {
        // Currently returns the same transaction set; backend audit endpoint can be swapped later.
        return getAllTransactions();
    }

    public List<CreditRequest> getCreditRequests() throws IOException, InterruptedException {
        String response = apiClient.sendGet("/api/admin/credit-requests");
        Type type = new TypeToken<List<CreditRequest>>() {}.getType();
        return gson.fromJson(response, type);
    }

    public List<Transaction> getUserTransactions(String userId, String accountId) throws IOException, InterruptedException {
        String endpoint = "/api/user/transactions";
        if (accountId != null && !accountId.isBlank()) {
            endpoint += "?accountId=" + accountId;
        }
        String response = apiClient.sendGet(endpoint);
        Type type = new TypeToken<List<Transaction>>() {}.getType();
        return gson.fromJson(response, type);
    }

    public String requestCredit(CreditRequest request) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/user/request-credit", request);
    }

    public String requestCredit(Map<String, String> payload) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/user/request-credit", payload);
    }

    public String transferMoney(Map<String, String> payload) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/user/transfer", payload);
    }

    public String createUser(User user) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/admin/users", user);
    }

    public String updateUser(String userId, User user) throws IOException, InterruptedException {
        return apiClient.sendPut("/api/admin/users/" + userId, user);
    }

    public String deleteUser(String userId) throws IOException, InterruptedException {
        return apiClient.sendDelete("/api/admin/users/" + userId);
    }

    public String blockUser(String userId, boolean blocked) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/admin/users/" + userId + "/block", Map.of("blocked", String.valueOf(blocked)));
    }

    public String approveCreditRequest(String requestId) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/admin/credit-requests/" + requestId + "/approve", Map.of());
    }

    public String rejectCreditRequest(String requestId) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/admin/credit-requests/" + requestId + "/reject", Map.of());
    }

    public String creditAccount(Map<String, String> payload) throws IOException, InterruptedException {
        return apiClient.sendPost("/api/admin/credit", payload);
    }

    public String getUserBalance(String userId) throws IOException, InterruptedException {
        return apiClient.sendGet("/api/user/balance/" + userId);
    }

    /**
     * Returns the numeric account balance. The endpoint replies with a JSON
     * object like {"accountId":"ahsan001","balance":500.0}; we parse the
     * "balance" field directly instead of scraping digits from the whole
     * string (which previously pulled digits out of the account id).
     */
    public double getUserBalanceValue(String userId) throws IOException, InterruptedException {
        String response = apiClient.sendGet("/api/user/balance/" + userId);
        if (response == null || response.isBlank()) {
            return 0.0;
        }
        try {
            JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
            if (obj.has("balance") && !obj.get("balance").isJsonNull()) {
                return obj.get("balance").getAsBigDecimal().doubleValue();
            }
        } catch (Exception ex) {
            // Fall through to 0.0 on any unexpected shape.
        }
        return 0.0;
    }

    public List<CreditRequest> getUserCreditRequests(String userId) throws IOException, InterruptedException {
        String response = apiClient.sendGet("/api/user/credit-requests?userId=" + userId);
        Type type = new TypeToken<List<CreditRequest>>() {}.getType();
        return gson.fromJson(response, type);
    }
}
