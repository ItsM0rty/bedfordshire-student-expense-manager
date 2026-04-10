package com.studentexpensetracker.dao;

import com.studentexpensetracker.model.Subscription;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface SubscriptionDao {
    Subscription create(Connection aConnection, Subscription aSubscription) throws SQLException;

    void update(Connection aConnection, Subscription aSubscription) throws SQLException;

    void deleteById(Connection aConnection, long anId) throws SQLException;

    Optional<Subscription> findById(Connection aConnection, long anId) throws SQLException;

    List<Subscription> findAll(Connection aConnection) throws SQLException;
}

