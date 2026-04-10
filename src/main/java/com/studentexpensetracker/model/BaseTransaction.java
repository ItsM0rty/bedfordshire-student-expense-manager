package com.studentexpensetracker.model;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class BaseTransaction {
    private final long id;
    private final BigDecimal amount;

    protected BaseTransaction(long anId, BigDecimal anAmount) {
        id = anId;
        amount = Objects.requireNonNull(anAmount);
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public abstract String getTransactionLabel();
}
