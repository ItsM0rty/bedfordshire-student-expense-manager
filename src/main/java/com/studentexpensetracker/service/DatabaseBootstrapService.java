package com.studentexpensetracker.service;

import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.db.MigrationsRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class DatabaseBootstrapService {
    private final DatabaseHandler databaseHandler;

    public DatabaseBootstrapService(DatabaseHandler aDatabaseHandler) {
        databaseHandler = Objects.requireNonNull(aDatabaseHandler);
    }

    public BootstrapResult bootstrap() {
        if (!databaseHandler.isConnectionHealthy()) {
            return BootstrapResult.notConnected();
        }

        try {
            new MigrationsRunner(databaseHandler).runSchemaAndSeed();
            return BootstrapResult.connected();
        } catch (SQLException | IOException ignored) {
            return BootstrapResult.connectedButMigrationsFailed();
        }
    }

    public static final class BootstrapResult {
        private final boolean connected;
        private final boolean migrationsSucceeded;

        private BootstrapResult(boolean aConnected, boolean aMigrationsSucceeded) {
            connected = aConnected;
            migrationsSucceeded = aMigrationsSucceeded;
        }

        public static BootstrapResult connected() {
            return new BootstrapResult(true, true);
        }

        public static BootstrapResult notConnected() {
            return new BootstrapResult(false, false);
        }

        public static BootstrapResult connectedButMigrationsFailed() {
            return new BootstrapResult(true, false);
        }

        public boolean isConnected() {
            return connected;
        }

        public boolean isMigrationsSucceeded() {
            return migrationsSucceeded;
        }
    }
}

