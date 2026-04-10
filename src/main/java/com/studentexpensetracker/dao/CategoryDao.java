package com.studentexpensetracker.dao;

import com.studentexpensetracker.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    List<Category> findAll(Connection aConnection) throws SQLException;

    Optional<Category> findById(Connection aConnection, long anId) throws SQLException;
}

