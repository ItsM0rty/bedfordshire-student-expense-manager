package com.studentexpensetracker.dao;

import com.studentexpensetracker.model.Expense;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseDao {
    Expense create(Connection aConnection, Expense anExpense) throws SQLException;

    void update(Connection aConnection, Expense anExpense) throws SQLException;

    void deleteById(Connection aConnection, long anId) throws SQLException;

    Optional<Expense> findById(Connection aConnection, long anId) throws SQLException;

    List<Expense> findAll(Connection aConnection) throws SQLException;

    List<Expense> findByDateRange(Connection aConnection, LocalDate aStartDate, LocalDate anEndDate) throws SQLException;
}

