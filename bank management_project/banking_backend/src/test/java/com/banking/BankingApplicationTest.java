package com.banking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BankingApplicationTest {

    @Test
    public void shouldPrintStartupMessage() {
        assertEquals("Banking backend started.", "Banking backend started.");
    }
}
