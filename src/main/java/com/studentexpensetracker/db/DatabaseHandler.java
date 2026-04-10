package com.studentexpensetracker.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

public final class DatabaseHandler {
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(3);
    private static volatile DatabaseHandler instance;

    private final Properties applicationProperties;

    private DatabaseHandler() {
        applicationProperties = new Properties();
        try (InputStream anInputStream = DatabaseHandler.class.getResourceAsStream("/application.properties")) {
            if (anInputStream != null) {
                applicationProperties.load(anInputStream);
            }
        } catch (IOException ignored) {
        }
    }

    public static DatabaseHandler getInstance() {
        DatabaseHandler aLocalInstance = instance;
        if (aLocalInstance != null) {
            return aLocalInstance;
        }
        synchronized (DatabaseHandler.class) {
            if (instance == null) {
                instance = new DatabaseHandler();
            }
            return instance;
        }
    }

    public Connection openConnection() throws SQLException {
        String aUrl = getProperty("db.url");
        String aUsername = getProperty("db.username");
        String aPassword = getProperty("db.password");
        DriverManager.setLoginTimeout((int) CONNECTION_TIMEOUT.toSeconds());
        return DriverManager.getConnection(aUrl, aUsername, aPassword);
    }

    public boolean isConnectionHealthy() {
        try (Connection aConnection = openConnection()) {
            return aConnection.isValid((int) CONNECTION_TIMEOUT.toSeconds());
        } catch (SQLException anException) {
            return false;
        }
    }

    private String getProperty(String aKey) {
        String anEnvironmentKey = aKey.toUpperCase().replace('.', '_');
        String anEnvironmentValue = System.getenv(anEnvironmentKey);
        if (anEnvironmentValue != null && !anEnvironmentValue.isBlank()) {
            return anEnvironmentValue;
        }
        return applicationProperties.getProperty(aKey, "");
    }
}

