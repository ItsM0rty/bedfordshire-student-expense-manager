package com.studentexpensetracker.model;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

public final class Budget {
    private final long id;
    private final Category category;
    private final YearMonth month;
    private final BigDecimal monthlyLimit;

    public Budget(long anId, Category aCategory, YearMonth aMonth, BigDecimal aMonthlyLimit) {
        id = anId;
        category = Objects.requireNonNull(aCategory);
        month = Objects.requireNonNull(aMonth);
        monthlyLimit = Objects.requireNonNull(aMonthlyLimit);
    }

    public long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }
}

