package com.studentexpensetracker.model;

public enum ExpenseType {
    ONE_TIME("One-time"),
    RECURRING("Recurring");

    private final String displayName;

    ExpenseType(String aDisplayName) {
        displayName = aDisplayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

