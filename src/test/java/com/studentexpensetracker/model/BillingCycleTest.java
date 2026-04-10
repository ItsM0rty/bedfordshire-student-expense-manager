package com.studentexpensetracker.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BillingCycleTest {
    @Test
    void weeklyToMonthlyEquivalentUses433Factor() {
        BigDecimal aMonthly = BillingCycle.WEEKLY.toMonthlyEquivalent(new BigDecimal("10.00"));
        assertEquals(new BigDecimal("43.30"), aMonthly.setScale(2));
    }

    @Test
    void monthlyToMonthlyEquivalentIsIdentity() {
        BigDecimal aMonthly = BillingCycle.MONTHLY.toMonthlyEquivalent(new BigDecimal("15.50"));
        assertEquals(new BigDecimal("15.50"), aMonthly.setScale(2));
    }

    @Test
    void quarterlyToMonthlyEquivalentDividesByThree() {
        BigDecimal aMonthly = BillingCycle.QUARTERLY.toMonthlyEquivalent(new BigDecimal("30.00"));
        assertEquals(new BigDecimal("10.00"), aMonthly.setScale(2));
    }

    @Test
    void annuallyToMonthlyEquivalentDividesByTwelve() {
        BigDecimal aMonthly = BillingCycle.ANNUALLY.toMonthlyEquivalent(new BigDecimal("120.00"));
        assertEquals(new BigDecimal("10.00"), aMonthly.setScale(2));
    }
}

