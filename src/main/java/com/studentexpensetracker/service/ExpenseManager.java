package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.ExpenseDao;
import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.model.Expense;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class ExpenseManager {
    private final DatabaseHandler databaseHandler;
    private final ExpenseDao expenseDao;

    public ExpenseManager(DatabaseHandler aDatabaseHandler, ExpenseDao anExpenseDao) {
        databaseHandler = Objects.requireNonNull(aDatabaseHandler);
        expenseDao = Objects.requireNonNull(anExpenseDao);
    }

    public List<Expense> getAllExpenses() throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return expenseDao.findAll(aConnection);
        }
    }

    public List<Expense> getExpensesByDateRange(LocalDate aStartDate, LocalDate anEndDate) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return expenseDao.findByDateRange(aConnection, aStartDate, anEndDate);
        }
    }

    public Expense addExpense(Expense anExpense) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return expenseDao.create(aConnection, anExpense);
        }
    }

    public void updateExpense(Expense anExpense) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            expenseDao.update(aConnection, anExpense);
        }
    }

    public void deleteExpense(long anExpenseId) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            expenseDao.deleteById(aConnection, anExpenseId);
        }
    }
}

