package com.studentexpensetracker.db;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class DatabaseHandlerSmokeTest {
    @Disabled("Enable when MySQL is configured via application.properties or env vars")
    @Test
    void connectionHealthCheckReturnsTrueWhenConfigured() {
        assertTrue(DatabaseHandler.getInstance().isConnectionHealthy());
    }
}

