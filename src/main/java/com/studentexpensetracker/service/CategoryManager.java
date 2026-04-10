package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.CategoryDao;
import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.model.Category;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public final class CategoryManager {
    private final DatabaseHandler databaseHandler;
    private final CategoryDao categoryDao;

    public CategoryManager(DatabaseHandler aDatabaseHandler, CategoryDao aCategoryDao) {
        databaseHandler = Objects.requireNonNull(aDatabaseHandler);
        categoryDao = Objects.requireNonNull(aCategoryDao);
    }

    public List<Category> getAllCategories() throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return categoryDao.findAll(aConnection);
        }
    }
}

