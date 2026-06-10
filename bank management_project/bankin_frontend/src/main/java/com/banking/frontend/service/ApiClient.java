package com.banking.frontend.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final SessionManager sessionManager = new SessionManager();

    /** Attaches the bearer token (when signed in) to an outgoing request. */
    private HttpRequest.Builder authed(HttpRequest.Builder builder) {
        String token = sessionManager.getToken();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private String send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new IOException(extractError(response));
    }

    public String sendPost(String endpoint, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest request = authed(HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(request);
    }

    public String sendGet(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = authed(HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json"))
                .GET()
                .build();
        return send(request);
    }

    public String sendDelete(String endpoint) throws IOException, InterruptedException {
        HttpRequest request = authed(HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json"))
                .DELETE()
                .build();
        return send(request);
    }

    public String sendPut(String endpoint, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest request = authed(HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json"))
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(request);
    }

    /**
     * Turns a non-2xx response into a readable message. The backend now returns
     * {"error":"Insufficient balance..."} for validation failures, so we surface
     * that text directly instead of dumping the raw HTTP status + JSON body.
     */
    private String extractError(HttpResponse<String> response) {
        String body = response.body();
        if (body != null && !body.isBlank()) {
            try {
                JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
                if (obj.has("error") && !obj.get("error").isJsonNull()) {
                    return obj.get("error").getAsString();
                }
                if (obj.has("message") && !obj.get("message").isJsonNull()) {
                    return obj.get("message").getAsString();
                }
            } catch (Exception ignored) {
                // Not JSON — fall back to a generic message below.
            }
        }
        return "Server error (HTTP " + response.statusCode() + ")";
    }
}
