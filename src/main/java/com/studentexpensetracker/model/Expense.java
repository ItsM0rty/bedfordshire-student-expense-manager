package com.studentexpensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class Expense extends BaseTransaction {
    private final LocalDate expenseDate;
    private final Category category;
    private final String description;
    private final ExpenseType expenseType;

    public Expense(
            long anId,
            BigDecimal anAmount,
            LocalDate anExpenseDate,
            Category aCategory,
            String aDescription,
            ExpenseType anExpenseType
    ) {
        super(anId, anAmount);
        expenseDate = Objects.requireNonNull(anExpenseDate);
        category = Objects.requireNonNull(aCategory);
        description = Objects.requireNonNull(aDescription);
        expenseType = Objects.requireNonNull(anExpenseType);
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    @Override
    public String getTransactionLabel() {
        return description + " (" + expenseType.name() + ")";
    }
}

