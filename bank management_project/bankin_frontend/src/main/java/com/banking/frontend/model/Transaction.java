package com.banking.frontend.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction {
    private String transactionId;
    private String sourceAccountId;
    private String destinationAccountId;
    private String amount;
    private String date;
    private String timestamp;
    private String description;
    private String status;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        String rawDate = (date != null && !date.isBlank()) ? date : timestamp;
        return formatDate(rawDate);
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceDisplay() {
        return (sourceAccountId != null && !sourceAccountId.isBlank()) ? sourceAccountId : "System";
    }

    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return rawDate;
        }
        String trimmed = rawDate.trim();
        DateTimeFormatter output = DateTimeFormatter.ofPattern("dd MMMM yyyy h:mma");
        try {
            // Try ISO offset / instant formats.
            if (trimmed.endsWith("Z") || trimmed.contains("+")) {
                OffsetDateTime odt = OffsetDateTime.parse(trimmed);
                return output.format(odt).toLowerCase();
            }
            // Try local date-time formats.
            LocalDateTime ldt = LocalDateTime.parse(trimmed);
            return output.format(ldt).toLowerCase();
        } catch (DateTimeParseException ex) {
            try {
                Instant instant = Instant.parse(trimmed);
                return output.format(LocalDateTime.ofInstant(instant, ZoneId.systemDefault())).toLowerCase();
            } catch (DateTimeParseException ex2) {
                return rawDate;
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
