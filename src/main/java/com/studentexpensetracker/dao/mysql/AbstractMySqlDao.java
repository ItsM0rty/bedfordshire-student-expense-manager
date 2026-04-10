package com.studentexpensetracker.dao.mysql;

import com.studentexpensetracker.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractMySqlDao {

    protected void deleteById(Connection aConnection, String aTableName, long anId) throws SQLException {
        String aSql = "DELETE FROM " + aTableName + " WHERE id = ?";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setLong(1, anId);
            aStatement.executeUpdate();
        }
    }

    protected Category mapCategory(ResultSet aResultSet) throws SQLException {
        return new Category(
                aResultSet.getLong("category_id"),
                aResultSet.getString("category_name")
        );
    }
}
