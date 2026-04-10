package com.studentexpensetracker.dao.mysql;

import com.studentexpensetracker.dao.BudgetDao;
import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.model.Category;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MySqlBudgetDao extends AbstractMySqlDao implements BudgetDao {
    @Override
    public Budget upsert(Connection aConnection, Budget aBudget) throws SQLException {
        String aSql = """
                INSERT INTO budget(category_id, budget_month, monthly_limit)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE monthly_limit = VALUES(monthly_limit)
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql, Statement.RETURN_GENERATED_KEYS)) {
            aStatement.setLong(1, aBudget.getCategory().getId());
            aStatement.setString(2, formatMonth(aBudget.getMonth()));
            aStatement.setBigDecimal(3, aBudget.getMonthlyLimit());
            aStatement.executeUpdate();
        }

        Optional<Budget> persistedBudget = findByCategoryAndMonth(aConnection, aBudget.getCategory().getId(), aBudget.getMonth());
        if (persistedBudget.isEmpty()) {
            throw new SQLException("Upsert budget failed to load persisted row");
        }
        return persistedBudget.get();
    }

    @Override
    public Optional<Budget> findByCategoryAndMonth(Connection aConnection, long aCategoryId, YearMonth aMonth) throws SQLException {
        String aSql = """
                SELECT
                    b.id,
                    b.budget_month,
                    b.monthly_limit,
                    c.id AS category_id,
                    c.name AS category_name
                FROM budget b
                INNER JOIN category c ON c.id = b.category_id
                WHERE b.category_id = ? AND b.budget_month = ?
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setLong(1, aCategoryId);
            aStatement.setString(2, formatMonth(aMonth));
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                if (!aResultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapBudget(aResultSet));
            }
        }
    }

    @Override
    public List<Budget> findByMonth(Connection aConnection, YearMonth aMonth) throws SQLException {
        String aSql = """
                SELECT
                    b.id,
                    b.budget_month,
                    b.monthly_limit,
                    c.id AS category_id,
                    c.name AS category_name
                FROM budget b
                INNER JOIN category c ON c.id = b.category_id
                WHERE b.budget_month = ?
                ORDER BY c.name
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setString(1, formatMonth(aMonth));
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                List<Budget> budgets = new ArrayList<>();
                while (aResultSet.next()) {
                    budgets.add(mapBudget(aResultSet));
                }
                return budgets;
            }
        }
    }

    private Budget mapBudget(ResultSet aResultSet) throws SQLException {
        long anId = aResultSet.getLong("id");
        YearMonth aMonth = parseMonth(aResultSet.getString("budget_month"));
        BigDecimal aMonthlyLimit = aResultSet.getBigDecimal("monthly_limit");
        Category aCategory = mapCategory(aResultSet);
        return new Budget(anId, aCategory, aMonth, aMonthlyLimit);
    }

    private String formatMonth(YearMonth aMonth) {
        return aMonth.toString();
    }

    private YearMonth parseMonth(String aMonthText) {
        return YearMonth.parse(aMonthText);
    }
}

