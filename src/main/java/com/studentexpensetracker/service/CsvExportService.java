package com.studentexpensetracker.service;

import com.studentexpensetracker.model.Expense;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class CsvExportService {
    public void exportExpenses(Path aPath, List<Expense> expenses) throws IOException {
        Objects.requireNonNull(aPath);
        Objects.requireNonNull(expenses);

        try (BufferedWriter aWriter = Files.newBufferedWriter(aPath, StandardCharsets.UTF_8)) {
            aWriter.write("id,date,category,type,description,amount");
            aWriter.newLine();
            for (Expense anExpense : expenses) {
                aWriter.write(toCsvLine(anExpense));
                aWriter.newLine();
            }
        }
    }

    private String toCsvLine(Expense anExpense) {
        long anId = anExpense.getId();
        LocalDate aDate = anExpense.getExpenseDate();
        String aCategory = anExpense.getCategory().getName();
        String aType = anExpense.getExpenseType().name();
        String aDescription = anExpense.getDescription();
        BigDecimal anAmount = anExpense.getAmount();

        return anId
                + "," + escape(String.valueOf(aDate))
                + "," + escape(aCategory)
                + "," + escape(aType)
                + "," + escape(aDescription)
                + "," + escape(anAmount.toPlainString());
    }

    private String escape(String aValue) {
        if (aValue == null) {
            return "\"\"";
        }
        String aSanitised = aValue.replace("\"", "\"\"");
        return "\"" + aSanitised + "\"";
    }
}

