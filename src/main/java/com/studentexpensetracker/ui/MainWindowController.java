package com.studentexpensetracker.ui;

import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.service.DatabaseBootstrapService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public final class MainWindowController {
    @FXML
    private Label databaseStatusLabel;

    @FXML
    private StackPane contentStack;

    @FXML
    private NavBarController navBarController;

    @FXML
    private Node dashboardContent;

    @FXML
    private Node expensesContent;

    @FXML
    private Node subscriptionsContent;

    @FXML
    private Node budgetsContent;

    @FXML
    private void initialize() {
        DatabaseBootstrapService aBootstrapService =
                new DatabaseBootstrapService(DatabaseHandler.getInstance());
        DatabaseBootstrapService.BootstrapResult aResult = aBootstrapService.bootstrap();

        if (aResult.isConnected()) {
            databaseStatusLabel.setText("DB: Connected");
            databaseStatusLabel.getStyleClass().removeAll("db-bad");
            databaseStatusLabel.getStyleClass().add("db-ok");
            if (!aResult.isMigrationsSucceeded()) {
                databaseStatusLabel.setText("DB: Connected (migrations failed)");
            }
        } else {
            databaseStatusLabel.setText("Database unavailable");
            databaseStatusLabel.getStyleClass().removeAll("db-ok");
            databaseStatusLabel.getStyleClass().add("db-bad");
        }

        showPage(NavBarController.NavPage.DASHBOARD);

        if (navBarController != null) {
            navBarController.setNavigationHandler(this::showPage);
        }
    }

    private void showPage(NavBarController.NavPage aPage) {
        hideAll();
        switch (aPage) {
            case DASHBOARD -> dashboardContent.setVisible(true);
            case EXPENSES -> expensesContent.setVisible(true);
            case SUBSCRIPTIONS -> subscriptionsContent.setVisible(true);
            case BUDGETS -> budgetsContent.setVisible(true);
        }
    }

    private void hideAll() {
        if (dashboardContent != null) {
            dashboardContent.setVisible(false);
        }
        if (expensesContent != null) {
            expensesContent.setVisible(false);
        }
        if (subscriptionsContent != null) {
            subscriptionsContent.setVisible(false);
        }
        if (budgetsContent != null) {
            budgetsContent.setVisible(false);
        }
    }
}
