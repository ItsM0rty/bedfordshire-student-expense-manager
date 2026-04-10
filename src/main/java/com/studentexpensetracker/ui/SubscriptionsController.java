package com.studentexpensetracker.ui;

import com.studentexpensetracker.app.AppModule;
import com.studentexpensetracker.model.BillingCycle;
import com.studentexpensetracker.model.Subscription;
import com.studentexpensetracker.model.SubscriptionStatus;
import com.studentexpensetracker.service.AppEventBus;
import com.studentexpensetracker.service.SubscriptionManager;
import com.studentexpensetracker.util.MoneyUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SubscriptionsController {
    @FXML
    private TextField searchTextField;

    @FXML
    private CheckBox activeOnlyCheckBox;

    @FXML
    private TableView<Subscription> subscriptionsTable;

    @FXML
    private TableColumn<Subscription, String> serviceColumn;

    @FXML
    private TableColumn<Subscription, String> billingCycleColumn;

    @FXML
    private TableColumn<Subscription, String> costColumn;

    @FXML
    private TableColumn<Subscription, String> monthlyEquivalentColumn;

    @FXML
    private TableColumn<Subscription, String> nextRenewalColumn;

    @FXML
    private TableColumn<Subscription, String> statusColumn;

    @FXML
    private Label statusLabel;

    private final SubscriptionManager subscriptionManager = AppModule.getInstance().getSubscriptionManager();
    private final ObservableList<Subscription> subscriptions = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureTable();
        searchTextField.textProperty().addListener((anObs, anOldValue, aNewValue) -> applyClientFilters());
        activeOnlyCheckBox.selectedProperty().addListener((anObs, anOldValue, aNewValue) -> applyClientFilters());
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAddSubscription() {
        SubscriptionInput input = showSubscriptionDialog("Add subscription", null);
        if (input == null) {
            return;
        }
        try {
            Subscription aSubscription = new Subscription(
                    0,
                    input.serviceName,
                    input.billingCycle,
                    input.cost,
                    input.nextRenewalDate,
                    input.status
            );
            subscriptionManager.addSubscription(aSubscription);
            refresh();
            statusLabel.setText("Subscription added");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.SUBSCRIPTIONS_CHANGED);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to add subscription");
        }
    }

    @FXML
    private void onEditSelected() {
        Subscription aSelected = subscriptionsTable.getSelectionModel().getSelectedItem();
        if (aSelected == null) {
            statusLabel.setText("Select a subscription to edit");
            return;
        }
        SubscriptionInput input = showSubscriptionDialog("Edit subscription", aSelected);
        if (input == null) {
            return;
        }
        try {
            Subscription anUpdated = new Subscription(
                    aSelected.getId(),
                    input.serviceName,
                    input.billingCycle,
                    input.cost,
                    input.nextRenewalDate,
                    input.status
            );
            subscriptionManager.updateSubscription(anUpdated);
            refresh();
            statusLabel.setText("Subscription updated");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.SUBSCRIPTIONS_CHANGED);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to update subscription");
        }
    }

    @FXML
    private void onToggleStatus() {
        Subscription aSelected = subscriptionsTable.getSelectionModel().getSelectedItem();
        if (aSelected == null) {
            statusLabel.setText("Select a subscription to toggle");
            return;
        }
        SubscriptionStatus aNewStatus = aSelected.getStatus() == SubscriptionStatus.ACTIVE
                ? SubscriptionStatus.INACTIVE
                : SubscriptionStatus.ACTIVE;
        try {
            Subscription updated = new Subscription(
                    aSelected.getId(),
                    aSelected.getServiceName(),
                    aSelected.getBillingCycle(),
                    aSelected.getCost(),
                    aSelected.getNextRenewalDate(),
                    aNewStatus
            );
            subscriptionManager.updateSubscription(updated);
            refresh();
            statusLabel.setText("Subscription status updated");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.SUBSCRIPTIONS_CHANGED);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to toggle status");
        }
    }

    @FXML
    private void onDeleteSelected() {
        Subscription aSelected = subscriptionsTable.getSelectionModel().getSelectedItem();
        if (aSelected == null) {
            statusLabel.setText("Select a subscription to delete");
            return;
        }
        try {
            subscriptionManager.deleteSubscription(aSelected.getId());
            refresh();
            statusLabel.setText("Subscription deleted");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.SUBSCRIPTIONS_CHANGED);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to delete subscription");
        }
    }

    private void configureTable() {
        serviceColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getServiceName()));
        billingCycleColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getBillingCycle().name()));
        costColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(MoneyUtil.formatCurrency(aCell.getValue().getCost())));
        monthlyEquivalentColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(
                MoneyUtil.formatCurrency(aCell.getValue().getMonthlyEquivalentCost())
        ));
        nextRenewalColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getNextRenewalDate().toString()));
        statusColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getStatus().name()));
        subscriptionsTable.setItems(subscriptions);
    }

    private void refresh() {
        try {
            List<Subscription> loaded = subscriptionManager.getAllSubscriptions();
            subscriptions.setAll(loaded);
            applyClientFilters();
            statusLabel.setText("Loaded " + subscriptions.size() + " subscriptions");
        } catch (Exception ignored) {
            statusLabel.setText("Failed to load subscriptions");
        }
    }

    private void applyClientFilters() {
        String searchText = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        boolean activeOnly = activeOnlyCheckBox.isSelected();

        List<Subscription> filtered = new ArrayList<>();
        for (Subscription aSubscription : subscriptions) {
            if (activeOnly && aSubscription.getStatus() != SubscriptionStatus.ACTIVE) {
                continue;
            }
            if (!searchText.isBlank() && !aSubscription.getServiceName().toLowerCase().contains(searchText)) {
                continue;
            }
            filtered.add(aSubscription);
        }
        subscriptionsTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private SubscriptionInput showSubscriptionDialog(String aTitle, Subscription anExisting) {
        Dialog<SubscriptionInput> aDialog = new Dialog<>();
        aDialog.setTitle(aTitle);
        aDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane aGrid = new GridPane();
        aGrid.setHgap(10);
        aGrid.setVgap(10);

        TextField aServiceNameField = new TextField();
        ComboBox<BillingCycle> aBillingCycleField = new ComboBox<>(FXCollections.observableArrayList(BillingCycle.values()));
        TextField aCostField = new TextField();
        DatePicker aRenewalDatePicker = new DatePicker();
        ComboBox<SubscriptionStatus> aStatusField = new ComboBox<>(FXCollections.observableArrayList(SubscriptionStatus.values()));

        aGrid.addRow(0, new Label("Service name"), aServiceNameField);
        aGrid.addRow(1, new Label("Billing cycle"), aBillingCycleField);
        aGrid.addRow(2, new Label("Cost"), aCostField);
        aGrid.addRow(3, new Label("Next renewal"), aRenewalDatePicker);
        aGrid.addRow(4, new Label("Status"), aStatusField);

        if (anExisting != null) {
            aServiceNameField.setText(anExisting.getServiceName());
            aBillingCycleField.getSelectionModel().select(anExisting.getBillingCycle());
            aCostField.setText(anExisting.getCost().toPlainString());
            aRenewalDatePicker.setValue(anExisting.getNextRenewalDate());
            aStatusField.getSelectionModel().select(anExisting.getStatus());
        } else {
            aBillingCycleField.getSelectionModel().select(BillingCycle.MONTHLY);
            aRenewalDatePicker.setValue(LocalDate.now().plusMonths(1));
            aStatusField.getSelectionModel().select(SubscriptionStatus.ACTIVE);
        }

        aDialog.getDialogPane().setContent(aGrid);

        Node anOkButton = aDialog.getDialogPane().lookupButton(ButtonType.OK);
        anOkButton.addEventFilter(ActionEvent.ACTION, anActionEvent -> {
            List<String> validationErrors = new ArrayList<>();
            String aCurrentServiceName = aServiceNameField.getText() == null
                    ? "" : aServiceNameField.getText().trim();
            if (aCurrentServiceName.isBlank()) {
                validationErrors.add("Service name is required");
            }
            if (aBillingCycleField.getValue() == null) {
                validationErrors.add("Billing cycle is required");
            }
            BigDecimal aCurrentCost = parseMoney(aCostField.getText());
            if (aCurrentCost == null) {
                validationErrors.add("Cost is required");
            } else if (aCurrentCost.compareTo(BigDecimal.ZERO) < 0) {
                validationErrors.add("Cost must be 0 or greater");
            }
            if (aRenewalDatePicker.getValue() == null) {
                validationErrors.add("Next renewal date is required");
            }
            if (aStatusField.getValue() == null) {
                validationErrors.add("Status is required");
            }
            if (!validationErrors.isEmpty()) {
                anActionEvent.consume();
                Alert aValidationAlert = new Alert(Alert.AlertType.WARNING);
                aValidationAlert.setTitle("Validation Error");
                aValidationAlert.setHeaderText("Please correct the following:");
                aValidationAlert.setContentText(String.join("\n", validationErrors));
                aValidationAlert.showAndWait();
            }
        });

        aDialog.setResultConverter(aButton -> {
            if (aButton != ButtonType.OK) {
                return null;
            }
            String aServiceName = aServiceNameField.getText() == null ? "" : aServiceNameField.getText().trim();
            BillingCycle aBillingCycle = aBillingCycleField.getValue();
            BigDecimal aCost = parseMoney(aCostField.getText());
            LocalDate aNextRenewalDate = aRenewalDatePicker.getValue();
            SubscriptionStatus aStatus = aStatusField.getValue();
            return new SubscriptionInput(aServiceName, aBillingCycle, aCost, aNextRenewalDate, aStatus);
        });

        return aDialog.showAndWait().orElse(null);
    }

    private BigDecimal parseMoney(String aText) {
        if (aText == null) {
            return null;
        }
        String aTrimmed = aText.trim();
        if (aTrimmed.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(aTrimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static final class SubscriptionInput {
        private final String serviceName;
        private final BillingCycle billingCycle;
        private final BigDecimal cost;
        private final LocalDate nextRenewalDate;
        private final SubscriptionStatus status;

        private SubscriptionInput(
                String aServiceName,
                BillingCycle aBillingCycle,
                BigDecimal aCost,
                LocalDate aNextRenewalDate,
                SubscriptionStatus aStatus
        ) {
            serviceName = Objects.requireNonNull(aServiceName);
            billingCycle = Objects.requireNonNull(aBillingCycle);
            cost = Objects.requireNonNull(aCost);
            nextRenewalDate = Objects.requireNonNull(aNextRenewalDate);
            status = Objects.requireNonNull(aStatus);
        }
    }
}

