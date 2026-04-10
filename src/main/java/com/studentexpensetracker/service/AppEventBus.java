package com.studentexpensetracker.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class AppEventBus {
    private static final AppEventBus instance = new AppEventBus();

    private final List<Consumer<AppEvent>> listeners = new CopyOnWriteArrayList<>();

    private AppEventBus() {
    }

    public static AppEventBus getInstance() {
        return instance;
    }

    public void subscribe(Consumer<AppEvent> aListener) {
        listeners.add(aListener);
    }

    public void publish(AppEvent anEvent) {
        for (Consumer<AppEvent> aListener : listeners) {
            aListener.accept(anEvent);
        }
    }

    public enum AppEvent {
        EXPENSES_CHANGED,
        SUBSCRIPTIONS_CHANGED,
        BUDGETS_CHANGED
    }
}

