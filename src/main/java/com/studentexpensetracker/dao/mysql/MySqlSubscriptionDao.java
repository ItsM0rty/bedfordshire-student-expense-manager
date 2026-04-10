package com.studentexpensetracker.dao.mysql;

import com.studentexpensetracker.dao.SubscriptionDao;
import com.studentexpensetracker.model.BillingCycle;
import com.studentexpensetracker.model.Subscription;
import com.studentexpensetracker.model.SubscriptionStatus;

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

public final class MySqlSubscriptionDao extends AbstractMySqlDao implements SubscriptionDao {
    @Override
    public Subscription create(Connection aConnection, Subscription aSubscription) throws SQLException {
        String aSql = """
                INSERT INTO subscription(service_name, billing_cycle, cost, next_renewal_date, status)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql, Statement.RETURN_GENERATED_KEYS)) {
            aStatement.setString(1, aSubscription.getServiceName());
            aStatement.setString(2, aSubscription.getBillingCycle().name());
            aStatement.setBigDecimal(3, aSubscription.getCost());
            aStatement.setDate(4, Date.valueOf(aSubscription.getNextRenewalDate()));
            aStatement.setString(5, aSubscription.getStatus().name());
            aStatement.executeUpdate();

            try (ResultSet aGeneratedKeys = aStatement.getGeneratedKeys()) {
                if (!aGeneratedKeys.next()) {
                    throw new SQLException("No generated key returned for subscription insert");
                }
                long aNewId = aGeneratedKeys.getLong(1);
                return new Subscription(
                        aNewId,
                        aSubscription.getServiceName(),
                        aSubscription.getBillingCycle(),
                        aSubscription.getCost(),
                        aSubscription.getNextRenewalDate(),
                        aSubscription.getStatus()
                );
            }
        }
    }

    @Override
    public void update(Connection aConnection, Subscription aSubscription) throws SQLException {
        String aSql = """
                UPDATE subscription
                SET service_name = ?, billing_cycle = ?, cost = ?, next_renewal_date = ?, status = ?
                WHERE id = ?
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setString(1, aSubscription.getServiceName());
            aStatement.setString(2, aSubscription.getBillingCycle().name());
            aStatement.setBigDecimal(3, aSubscription.getCost());
            aStatement.setDate(4, Date.valueOf(aSubscription.getNextRenewalDate()));
            aStatement.setString(5, aSubscription.getStatus().name());
            aStatement.setLong(6, aSubscription.getId());
            aStatement.executeUpdate();
        }
    }

    @Override
    public void deleteById(Connection aConnection, long anId) throws SQLException {
        deleteById(aConnection, "subscription", anId);
    }

    @Override
    public Optional<Subscription> findById(Connection aConnection, long anId) throws SQLException {
        String aSql = "SELECT id, service_name, billing_cycle, cost, next_renewal_date, status FROM subscription WHERE id = ?";
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            aStatement.setLong(1, anId);
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                if (!aResultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapSubscription(aResultSet));
            }
        }
    }

    @Override
    public List<Subscription> findAll(Connection aConnection) throws SQLException {
        String aSql = """
                SELECT id, service_name, billing_cycle, cost, next_renewal_date, status
                FROM subscription
                ORDER BY next_renewal_date ASC, id ASC
                """;
        try (PreparedStatement aStatement = aConnection.prepareStatement(aSql)) {
            try (ResultSet aResultSet = aStatement.executeQuery()) {
                List<Subscription> subscriptions = new ArrayList<>();
                while (aResultSet.next()) {
                    subscriptions.add(mapSubscription(aResultSet));
                }
                return subscriptions;
            }
        }
    }

    private Subscription mapSubscription(ResultSet aResultSet) throws SQLException {
        long anId = aResultSet.getLong("id");
        String aServiceName = aResultSet.getString("service_name");
        BillingCycle aBillingCycle = BillingCycle.valueOf(aResultSet.getString("billing_cycle"));
        BigDecimal aCost = aResultSet.getBigDecimal("cost");
        LocalDate aNextRenewalDate = aResultSet.getDate("next_renewal_date").toLocalDate();
        SubscriptionStatus aStatus = SubscriptionStatus.valueOf(aResultSet.getString("status"));
        return new Subscription(anId, aServiceName, aBillingCycle, aCost, aNextRenewalDate, aStatus);
    }
}

