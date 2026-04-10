package com.studentexpensetracker.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MigrationsRunner {
    private final DatabaseHandler databaseHandler;

    public MigrationsRunner(DatabaseHandler aDatabaseHandler) {
        databaseHandler = Objects.requireNonNull(aDatabaseHandler);
    }

    public void runSchemaAndSeed() throws SQLException, IOException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            executeSqlResource(aConnection, "/db/schema.sql");
            executeSqlResource(aConnection, "/db/seed.sql");
        }
    }

    private void executeSqlResource(Connection aConnection, String aResourcePath) throws IOException, SQLException {
        String sqlContent = readResourceAsString(aResourcePath);
        List<String> statements = splitStatements(sqlContent);
        try (Statement aStatement = aConnection.createStatement()) {
            for (String aSqlStatement : statements) {
                String trimmedStatement = aSqlStatement.trim();
                if (trimmedStatement.isEmpty()) {
                    continue;
                }
                aStatement.execute(trimmedStatement);
            }
        }
    }

    private String readResourceAsString(String aResourcePath) throws IOException {
        InputStream anInputStream = MigrationsRunner.class.getResourceAsStream(aResourcePath);
        if (anInputStream == null) {
            throw new IOException("Missing resource: " + aResourcePath);
        }
        try (BufferedReader aReader = new BufferedReader(
                new InputStreamReader(anInputStream, StandardCharsets.UTF_8)
        )) {
            StringBuilder aStringBuilder = new StringBuilder();
            String aLine;
            while ((aLine = aReader.readLine()) != null) {
                aStringBuilder.append(aLine).append('\n');
            }
            return aStringBuilder.toString();
        }
    }

    private List<String> splitStatements(String aSqlContent) {
        List<String> statements = new ArrayList<>();
        StringBuilder aCurrentStatement = new StringBuilder();

        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        for (int anIndex = 0; anIndex < aSqlContent.length(); anIndex += 1) {
            char aCharacter = aSqlContent.charAt(anIndex);

            if (aCharacter == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (aCharacter == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            }

            if (aCharacter == ';' && !inSingleQuotes && !inDoubleQuotes) {
                statements.add(aCurrentStatement.toString());
                aCurrentStatement.setLength(0);
                continue;
            }

            aCurrentStatement.append(aCharacter);
        }

        if (!aCurrentStatement.isEmpty()) {
            statements.add(aCurrentStatement.toString());
        }

        return statements;
    }
}

