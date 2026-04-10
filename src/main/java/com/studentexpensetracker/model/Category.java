package com.studentexpensetracker.model;

import java.util.Objects;

public final class Category {
    private final long id;
    private final String name;

    public Category(long anId, String aName) {
        id = anId;
        name = Objects.requireNonNull(aName);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (!(anObject instanceof Category that)) {
            return false;
        }
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}

