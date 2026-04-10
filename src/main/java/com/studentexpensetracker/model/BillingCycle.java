package com.studentexpensetracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum BillingCycle {
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY;

    public BigDecimal toMonthlyEquivalent(BigDecimal aCost) {
        if (aCost == null) {
            return BigDecimal.ZERO;
        }
        return switch (this) {
            case WEEKLY -> aCost.multiply(new BigDecimal("4.33"));
            case MONTHLY -> aCost;
            case QUARTERLY -> aCost.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
            case ANNUALLY -> aCost.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        };
    }
}

