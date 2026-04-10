package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.BudgetDao;
import com.studentexpensetracker.db.DatabaseHandler;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class BudgetManagerTest {
    @Test
    void utilisationRatioIsZeroWhenLimitIsZero() {
        BudgetManager aBudgetManager = new BudgetManager(DatabaseHandler.getInstance(), new NoOpBudgetDao());
        BigDecimal aRatio = aBudgetManager.calculateUtilisation(new BigDecimal("50"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0"), aRatio.setScale(0));
    }

    @Test
    void classifyAt80PercentIsWarning() {
        BudgetManager aBudgetManager = new BudgetManager(DatabaseHandler.getInstance(), new NoOpBudgetDao());
        BudgetManager.BudgetAlertLevel aLevel = aBudgetManager.classifyUtilisation(new BigDecimal("0.80"));
        assertEquals(BudgetManager.BudgetAlertLevel.WARNING, aLevel);
    }

    @Test
    void classifyAt100PercentIsCritical() {
        BudgetManager aBudgetManager = new BudgetManager(DatabaseHandler.getInstance(), new NoOpBudgetDao());
        BudgetManager.BudgetAlertLevel aLevel = aBudgetManager.classifyUtilisation(new BigDecimal("1.00"));
        assertEquals(BudgetManager.BudgetAlertLevel.CRITICAL, aLevel);
    }

    private static final class NoOpBudgetDao implements BudgetDao {
        @Override
        public com.studentexpensetracker.model.Budget upsert(java.sql.Connection aConnection, com.studentexpensetracker.model.Budget aBudget) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.Optional<com.studentexpensetracker.model.Budget> findByCategoryAndMonth(java.sql.Connection aConnection, long aCategoryId, java.time.YearMonth aMonth) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.List<com.studentexpensetracker.model.Budget> findByMonth(java.sql.Connection aConnection, java.time.YearMonth aMonth) {
            throw new UnsupportedOperationException();
        }
    }
}

