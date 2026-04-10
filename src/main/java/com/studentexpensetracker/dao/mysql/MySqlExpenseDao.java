package com.studentexpensetracker.dao.mysql;

import com.studentexpensetracker.dao.ExpenseDao;
import com.studentexpensetracker.model.Category;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.ExpenseType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MySqlExpenseDao extends AbstractMySqlDao implements ExpenseDao {
    @Override
    public Expense create(Connection aConnection, Expense anExpense) throws SQLException {
        String aSql = """
                INSERT INTO expense(amount, expense_date, category_id, description, expense_type, recurrence_interval_days, recurrence_end_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql, Statement.RETURN_GENERATED_KEYS)) {
            aStatement.setBigDecimal(1, anExpense.getAmount());
            aStatement.setDate(2, Date.valueOf(anExpense.getExpenseDate()));
            aStatement.setLong(3, anExpense.getCategory().getId());
            aStatement.setString(4, anExpense.getDescription());
            aStatement.setString(5, anExpense.getExpenseType().name());
            aStatement.setObject(6, null);
            aStatement.setObject(7, null);

            aStatement.executeUpdate();
            try (ResultSet aGeneratedKeys = aStatement.getGeneratedKeys()) {
                if (!aGeneratedKeys.next()) {
                    throw new SQLException("No generated key returned for expense insert");
                }
                long aNewId = aGeneratedKeys.getLong(1);
                return new Expense(
                        aNewId,
                        anExpense.getAmount(),
                        anExpense.getExpenseDate(),
                        anExpense.getCategory(),
                        anExpense.getDescription(),
                        anExpense.getExpenseType()
                );
            }
        }
    }

    @Override
    public void update(Connection aConnection, Expense anExpense) throws SQLException {
        String aSql = """
                UPDATE expense
                SET amount = ?, expense_date = ?, category_id = ?, description = ?, expense_type = ?
                WHERE id = ?
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setBigDecimal(1, anExpense.getAmount());
            aStatement.setDate(2, Date.valueOf(anExpense.getExpenseDate()));
            aStatement.setLong(3, anExpense.getCategory().getId());
            aStatement.setString(4, anExpense.getDescription());
            aStatement.setString(5, anExpense.getExpenseType().name());
            aStatement.setLong(6, anExpense.getId());
            aStatement.executeUpdate();
        }
    }

    @Override
    public void deleteById(Connection aConnection, long anId) throws SQLException {
        deleteById(aConnection, "expense", anId);
    }

    @Override
    public Optional<Expense> findById(Connection aConnection, long anId) throws SQLException {
        String aSql = baseSelectSql() + " WHERE e.id = ?";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setLong(1, anId);
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                if (!aResultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapExpense(aResultSet));
            }
        }
    }

    @Override
    public List<Expense> findAll(Connection aConnection) throws SQLException {
        String aSql = baseSelectSql() + " ORDER BY e.expense_date DESC, e.id DESC";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                List<Expense> expenses = new ArrayList<>();
                while (aResultSet.next()) {
                    expenses.add(mapExpense(aResultSet));
                }
                return expenses;
            }
        }
    }

    @Override
    public List<Expense> findByDateRange(Connection aConnection, LocalDate aStartDate, LocalDate anEndDate) throws SQLException {
        String aSql = baseSelectSql() + " WHERE e.expense_date BETWEEN ? AND ? ORDER BY e.expense_date DESC, e.id DESC";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setDate(1, Date.valueOf(aStartDate));
            aStatement.setDate(2, Date.valueOf(anEndDate));
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                List<Expense> expenses = new ArrayList<>();
                while (aResultSet.next()) {
                    expenses.add(mapExpense(aResultSet));
                }
                return expenses;
            }
        }
    }

    private String baseSelectSql() {
        return """
                SELECT
                    e.id,
                    e.amount,
                    e.expense_date,
                    e.description,
                    e.expense_type,
                    c.id AS category_id,
                    c.name AS category_name
                FROM expense e
                INNER JOIN category c ON c.id = e.category_id
                """;
    }

    private Expense mapExpense(ResultSet aResultSet) throws SQLException {
        long anExpenseId = aResultSet.getLong("id");
        BigDecimal anAmount = aResultSet.getBigDecimal("amount");
        LocalDate anExpenseDate = aResultSet.getDate("expense_date").toLocalDate();
        String aDescription = aResultSet.getString("description");
        ExpenseType anExpenseType = ExpenseType.valueOf(aResultSet.getString("expense_type"));
        Category aCategory = mapCategory(aResultSet);
        return new Expense(anExpenseId, anAmount, anExpenseDate, aCategory, aDescription, anExpenseType);
    }
}

