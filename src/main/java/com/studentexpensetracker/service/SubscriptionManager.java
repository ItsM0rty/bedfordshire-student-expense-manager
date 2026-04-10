package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.SubscriptionDao;
import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.model.Subscription;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public final class SubscriptionManager {
    private final DatabaseHandler databaseHandler;
    private final SubscriptionDao subscriptionDao;

    public SubscriptionManager(DatabaseHandler aDatabaseHandler, SubscriptionDao aSubscriptionDao) {
        databaseHandler = Objects.requireNonNull(aDatabaseHandler);
        subscriptionDao = Objects.requireNonNull(aSubscriptionDao);
    }

    public List<Subscription> getAllSubscriptions() throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return subscriptionDao.findAll(aConnection);
        }
    }

    public Subscription addSubscription(Subscription aSubscription) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            return subscriptionDao.create(aConnection, aSubscription);
        }
    }

    public void updateSubscription(Subscription aSubscription) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            subscriptionDao.update(aConnection, aSubscription);
        }
    }

    public void deleteSubscription(long aSubscriptionId) throws SQLException {
        try (Connection aConnection = databaseHandler.openConnection()) {
            subscriptionDao.deleteById(aConnection, aSubscriptionId);
        }
    }
}

