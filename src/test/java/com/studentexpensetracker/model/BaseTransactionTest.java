package com.studentexpensetracker.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class BaseTransactionTest {

    @Test
    void expenseInheritsIdAndAmountFromBaseTransaction() {
        Category aCategory = new Category(1, "Food");
        Expense anExpense = new Expense(42, new BigDecimal("19.99"), LocalDate.of(2026, 3, 1),
                aCategory, "Lunch", ExpenseType.ONE_TIME);

        assertInstanceOf(BaseTransaction.class, anExpense);
        assertEquals(42L, anExpense.getId());
        assertEquals(new BigDecimal("19.99"), anExpense.getAmount());
    }

    @Test
    void subscriptionInheritsIdAndAmountFromBaseTransaction() {
        Subscription aSubscription = new Subscription(7, "Netflix", BillingCycle.MONTHLY,
                new BigDecimal("15.99"), LocalDate.of(2026, 4, 1), SubscriptionStatus.ACTIVE);

        assertInstanceOf(BaseTransaction.class, aSubscription);
        assertEquals(7L, aSubscription.getId());
        assertEquals(new BigDecimal("15.99"), aSubscription.getAmount());
        assertEquals(new BigDecimal("15.99"), aSubscription.getCost());
    }

    @Test
    void polymorphicTransactionLabelResolvesCorrectSubtype() {
        Category aCategory = new Category(1, "Transport");
        BaseTransaction anExpense = new Expense(1, new BigDecimal("5.00"), LocalDate.of(2026, 3, 1),
                aCategory, "Bus fare", ExpenseType.RECURRING);
        BaseTransaction aSubscription = new Subscription(2, "Spotify", BillingCycle.MONTHLY,
                new BigDecimal("9.99"), LocalDate.of(2026, 4, 1), SubscriptionStatus.ACTIVE);

        assertEquals("Bus fare (RECURRING)", anExpense.getTransactionLabel());
        assertEquals("Spotify (MONTHLY)", aSubscription.getTransactionLabel());
    }

    @Test
    void polymorphicListCanHoldBothExpenseAndSubscription() {
        Category aCategory = new Category(1, "Entertainment");
        List<BaseTransaction> transactions = Arrays.asList(
                new Expense(1, new BigDecimal("20.00"), LocalDate.of(2026, 3, 1),
                        aCategory, "Cinema", ExpenseType.ONE_TIME),
                new Subscription(2, "HBO Max", BillingCycle.MONTHLY,
                        new BigDecimal("14.99"), LocalDate.of(2026, 4, 1), SubscriptionStatus.ACTIVE)
        );

        BigDecimal total = BigDecimal.ZERO;
        for (BaseTransaction aTransaction : transactions) {
            total = total.add(aTransaction.getAmount());
        }
        assertEquals(new BigDecimal("34.99"), total);
    }
}
