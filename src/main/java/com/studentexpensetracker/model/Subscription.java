package com.studentexpensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public final class Subscription extends BaseTransaction {
    private final String serviceName;
    private final BillingCycle billingCycle;
    private final LocalDate nextRenewalDate;
    private final SubscriptionStatus status;

    public Subscription(
            long anId,
            String aServiceName,
            BillingCycle aBillingCycle,
            BigDecimal aCost,
            LocalDate aNextRenewalDate,
            SubscriptionStatus aStatus
    ) {
        super(anId, aCost);
        serviceName = Objects.requireNonNull(aServiceName);
        billingCycle = Objects.requireNonNull(aBillingCycle);
        nextRenewalDate = Objects.requireNonNull(aNextRenewalDate);
        status = Objects.requireNonNull(aStatus);
    }

    public String getServiceName() {
        return serviceName;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public BigDecimal getCost() {
        return getAmount();
    }

    public LocalDate getNextRenewalDate() {
        return nextRenewalDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public BigDecimal getMonthlyEquivalentCost() {
        return billingCycle.toMonthlyEquivalent(getAmount());
    }

    @Override
    public String getTransactionLabel() {
        return serviceName + " (" + billingCycle.name() + ")";
    }
}

