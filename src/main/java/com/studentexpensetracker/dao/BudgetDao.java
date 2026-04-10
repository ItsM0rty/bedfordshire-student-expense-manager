package com.studentexpensetracker.dao;

import com.studentexpensetracker.model.Budget;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetDao {
    Budget upsert(Connection aConnection, Budget aBudget) throws SQLException;

    Optional<Budget> findByCategoryAndMonth(Connection aConnection, long aCategoryId, YearMonth aMonth) throws SQLException;

    List<Budget> findByMonth(Connection aConnection, YearMonth aMonth) throws SQLException;
}

