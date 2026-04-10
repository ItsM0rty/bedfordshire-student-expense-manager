package com.studentexpensetracker.dao.mysql;

import com.studentexpensetracker.dao.CategoryDao;
import com.studentexpensetracker.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MySqlCategoryDao extends AbstractMySqlDao implements CategoryDao {
    @Override
    public List<Category> findAll(Connection aConnection) throws SQLException {
        String aSql = "SELECT id, name FROM category ORDER BY name";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                List<Category> categories = new ArrayList<>();
                while (aResultSet.next()) {
                    categories.add(new Category(
                            aResultSet.getLong("id"),
                            aResultSet.getString("name")
                    ));
                }
                return categories;
            }
        }
    }

    @Override
    public Optional<Category> findById(Connection aConnection, long anId) throws SQLException {
        String aSql = "SELECT id, name FROM category WHERE id = ?";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setLong(1, anId);
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                if (!aResultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Category(
                        aResultSet.getLong("id"),
                        aResultSet.getString("name")
                ));
            }
        }
    }
}

