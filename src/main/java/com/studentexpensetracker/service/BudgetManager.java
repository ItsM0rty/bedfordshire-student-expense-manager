package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.BudgetDao;
import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.model.Budget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BudgetManager {
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.80");
    private static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("1.00");

    private final DatabaseHandler databaseHandler;
    private final BudgetDao budgetDao;

    public BudgetManager(DatabaseHandler aDatabaseHandler, BudgetDao aBudgetDao) {
        databaseHandler = Objects.requireNonNull(aDatabaseHandler);
        budgetDao = Objects.requireNonNull(aBudgetDao);
    }

    public Budget setMonthlyBudget(Budget aBudget) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return budgetDao.upsert(aConnection, aBudget);
        }
    }

    public Optional<Budget> getBudgetForMonth(long aCategoryId, YearMonth aMonth) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return budgetDao.findByCategoryAndMonth(aConnection, aCategoryId, aMonth);
        }
    }

    public List<Budget> getBudgetsForMonth(YearMonth aMonth) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return budgetDao.findByMonth(aConnection, aMonth);
        }
    }

    public BigDecimal calculateUtilisation(BigDecimal aSpentAmount, BigDecimal aLimit) {
        if (aLimit == null || aLimit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeSpent = aSpentAmount == null ? BigDecimal.ZERO : aSpentAmount;
        return safeSpent.divide(aLimit, 4, RoundingMode.HALF_UP);
    }

    public BudgetAlertLevel classifyUtilisation(BigDecimal aUtilisationRatio) {
        BigDecimal safeRatio = aUtilisationRatio == null ? BigDecimal.ZERO : aUtilisationRatio;
        if (safeRatio.compareTo(CRITICAL_THRESHOLD) >= 0) {
            return BudgetAlertLevel.CRITICAL;
        }
        if (safeRatio.compareTo(WARNING_THRESHOLD) >= 0) {
            return BudgetAlertLevel.WARNING;
        }
        return BudgetAlertLevel.OK;
    }

    public enum BudgetAlertLevel {
        OK,
        WARNING,
        CRITICAL
    }
}

