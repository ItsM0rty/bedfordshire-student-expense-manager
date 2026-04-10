package com.studentexpensetracker.service;

import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.model.Category;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.Subscription;
import com.studentexpensetracker.model.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AnalyticsService {
    private final ExpenseManager expenseManager;
    private final SubscriptionManager subscriptionManager;
    private final BudgetManager budgetManager;

    public AnalyticsService(
            ExpenseManager anExpenseManager,
            SubscriptionManager aSubscriptionManager,
            BudgetManager aBudgetManager
    ) {
        expenseManager = Objects.requireNonNull(anExpenseManager);
        subscriptionManager = Objects.requireNonNull(aSubscriptionManager);
        budgetManager = Objects.requireNonNull(aBudgetManager);
    }

    public BigDecimal getMonthSpend(YearMonth aMonth) throws Exception {
        LocalDate aStartDate = aMonth.atDay(1);
        LocalDate anEndDate = aMonth.atEndOfMonth();
        List<Expense> expenses = expenseManager.getExpensesByDateRange(aStartDate, anEndDate);
        return sumExpenses(expenses);
    }

    public Map<Category, BigDecimal> getSpendByCategory(YearMonth aMonth) throws Exception {
        LocalDate aStartDate = aMonth.atDay(1);
        LocalDate anEndDate = aMonth.atEndOfMonth();
        List<Expense> expenses = expenseManager.getExpensesByDateRange(aStartDate, anEndDate);

        Map<Category, BigDecimal> spendByCategory = new HashMap<>();
        for (Expense anExpense : expenses) {
            Category aCategory = anExpense.getCategory();
            spendByCategory.putIfAbsent(aCategory, BigDecimal.ZERO);
            spendByCategory.put(aCategory, spendByCategory.get(aCategory).add(anExpense.getAmount()));
        }
        return spendByCategory;
    }

    public List<DailySpend> getSpendLastDays(int aDays) throws Exception {
        LocalDate anEndDate = LocalDate.now();
        LocalDate aStartDate = anEndDate.minusDays(aDays - 1L);
        List<Expense> expenses = expenseManager.getExpensesByDateRange(aStartDate, anEndDate);

        Map<LocalDate, BigDecimal> spendByDay = new HashMap<>();
        for (Expense anExpense : expenses) {
            spendByDay.putIfAbsent(anExpense.getExpenseDate(), BigDecimal.ZERO);
            spendByDay.put(anExpense.getExpenseDate(), spendByDay.get(anExpense.getExpenseDate()).add(anExpense.getAmount()));
        }

        List<DailySpend> dailySpends = new ArrayList<>();
        for (int dayIndex = 0; dayIndex < aDays; dayIndex += 1) {
            LocalDate aDate = aStartDate.plusDays(dayIndex);
            BigDecimal aTotal = spendByDay.getOrDefault(aDate, BigDecimal.ZERO);
            dailySpends.add(new DailySpend(aDate, aTotal));
        }
        return dailySpends;
    }

    public int countUpcomingRenewals(int aDays) throws Exception {
        LocalDate aToday = LocalDate.now();
        LocalDate aLimitDate = aToday.plusDays(aDays);
        List<Subscription> subscriptions = subscriptionManager.getAllSubscriptions();
        int count = 0;
        for (Subscription aSubscription : subscriptions) {
            if (aSubscription.getStatus() != SubscriptionStatus.ACTIVE) {
                continue;
            }
            if (!aSubscription.getNextRenewalDate().isBefore(aToday) && !aSubscription.getNextRenewalDate().isAfter(aLimitDate)) {
                count += 1;
            }
        }
        return count;
    }

    public int countBudgetAlerts(YearMonth aMonth) throws Exception {
        Map<Category, BigDecimal> spendByCategory = getSpendByCategory(aMonth);
        int alertCount = 0;
        for (Map.Entry<Category, BigDecimal> entry : spendByCategory.entrySet()) {
            Category aCategory = entry.getKey();
            BigDecimal aSpend = entry.getValue();
            Budget aBudget = budgetManager.getBudgetForMonth(aCategory.getId(), aMonth).orElse(null);
            if (aBudget == null) {
                continue;
            }
            BigDecimal aUtilisation = budgetManager.calculateUtilisation(aSpend, aBudget.getMonthlyLimit());
            BudgetManager.BudgetAlertLevel aLevel = budgetManager.classifyUtilisation(aUtilisation);
            if (aLevel != BudgetManager.BudgetAlertLevel.OK) {
                alertCount += 1;
            }
        }
        return alertCount;
    }

    public List<CategorySpend> getTopCategorySpends(YearMonth aMonth, int aLimit) throws Exception {
        Map<Category, BigDecimal> spendByCategory = getSpendByCategory(aMonth);
        List<CategorySpend> categorySpends = new ArrayList<>();
        for (Map.Entry<Category, BigDecimal> entry : spendByCategory.entrySet()) {
            categorySpends.add(new CategorySpend(entry.getKey(), entry.getValue()));
        }
        categorySpends.sort(Comparator.comparing(CategorySpend::getAmount).reversed());
        if (categorySpends.size() <= aLimit) {
            return categorySpends;
        }
        return categorySpends.subList(0, aLimit);
    }

    private BigDecimal sumExpenses(List<Expense> expenses) {
        BigDecimal total = BigDecimal.ZERO;
        for (Expense anExpense : expenses) {
            total = total.add(anExpense.getAmount());
        }
        return total;
    }

    public static final class DailySpend {
        private final LocalDate date;
        private final BigDecimal amount;

        public DailySpend(LocalDate aDate, BigDecimal anAmount) {
            date = Objects.requireNonNull(aDate);
            amount = Objects.requireNonNull(anAmount);
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    public static final class CategorySpend {
        private final Category category;
        private final BigDecimal amount;

        public CategorySpend(Category aCategory, BigDecimal anAmount) {
            category = Objects.requireNonNull(aCategory);
            amount = Objects.requireNonNull(anAmount);
        }

        public Category getCategory() {
            return category;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}

