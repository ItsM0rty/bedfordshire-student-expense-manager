package com.studentexpensetracker.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public final class NavBarController {
    @FXML
    private Button navDashboard;

    @FXML
    private Button navExpenses;

    @FXML
    private Button navSubscriptions;

    @FXML
    private Button navBudgets;

    private NavigationHandler navigationHandler;

    public void setNavigationHandler(NavigationHandler aHandler) {
        navigationHandler = aHandler;
    }

    @FXML
    private void onDashboard() {
        setActive(navDashboard);
        if (navigationHandler != null) {
            navigationHandler.navigateTo(NavPage.DASHBOARD);
        }
    }

    @FXML
    private void onExpenses() {
        setActive(navExpenses);
        if (navigationHandler != null) {
            navigationHandler.navigateTo(NavPage.EXPENSES);
        }
    }

    @FXML
    private void onSubscriptions() {
        setActive(navSubscriptions);
        if (navigationHandler != null) {
            navigationHandler.navigateTo(NavPage.SUBSCRIPTIONS);
        }
    }

    @FXML
    private void onBudgets() {
        setActive(navBudgets);
        if (navigationHandler != null) {
            navigationHandler.navigateTo(NavPage.BUDGETS);
        }
    }

    private void setActive(Button anActiveButton) {
        for (Button aButton : new Button[]{navDashboard, navExpenses, navSubscriptions, navBudgets}) {
            aButton.getStyleClass().removeAll("nav-btn-active");
            if (!aButton.getStyleClass().contains("nav-btn")) {
                aButton.getStyleClass().add("nav-btn");
            }
        }
        anActiveButton.getStyleClass().add("nav-btn-active");
    }

    public enum NavPage {
        DASHBOARD,
        EXPENSES,
        SUBSCRIPTIONS,
        BUDGETS
    }

    public interface NavigationHandler {
        void navigateTo(NavPage aPage);
    }
}
