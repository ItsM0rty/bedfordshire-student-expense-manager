package com.studentexpensetracker.ui;

import com.studentexpensetracker.app.AppModule;
import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.model.Category;
import com.studentexpensetracker.service.AnalyticsService;
import com.studentexpensetracker.service.AppEventBus;
import com.studentexpensetracker.service.BudgetManager;
import com.studentexpensetracker.service.CategoryManager;
import com.studentexpensetracker.util.DateUtil;
import com.studentexpensetracker.util.MoneyUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BudgetsController {
    @FXML
    private ComboBox<YearMonth> monthComboBox;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private TextField limitTextField;

    @FXML
    private TableView<BudgetRow> budgetsTable;

    @FXML
    private TableColumn<BudgetRow, String> categoryColumn;

    @FXML
    private TableColumn<BudgetRow, String> limitColumn;

    @FXML
    private TableColumn<BudgetRow, String> spentColumn;

    @FXML
    private TableColumn<BudgetRow, String> utilisationColumn;

    @FXML
    private TableColumn<BudgetRow, String> alertColumn;

    @FXML
    private Label statusLabel;

    private final CategoryManager categoryManager = AppModule.getInstance().getCategoryManager();
    private final BudgetManager budgetManager = AppModule.getInstance().getBudgetManager();
    private final AnalyticsService analyticsService = AppModule.getInstance().getAnalyticsService();

    private final ObservableList<BudgetRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureControls();
        configureTable();
        loadMonths();
        loadCategories();
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onSaveBudget() {
        YearMonth aMonth = monthComboBox.getValue();
        Category aCategory = categoryComboBox.getValue();
        BigDecimal aLimit = parseMoney(limitTextField.getText());

        List<String> validationErrors = new ArrayList<>();
        if (aMonth == null) {
            validationErrors.add("Month is required");
        }
        if (aCategory == null) {
            validationErrors.add("Category is required");
        }
        if (aLimit == null) {
            validationErrors.add("Limit is required");
        } else if (aLimit.compareTo(BigDecimal.ZERO) < 0) {
            validationErrors.add("Limit must be 0 or greater");
        }
        if (!validationErrors.isEmpty()) {
            Alert aValidationAlert = new Alert(Alert.AlertType.WARNING);
            aValidationAlert.setTitle("Validation Error");
            aValidationAlert.setHeaderText("Please correct the following:");
            aValidationAlert.setContentText(String.join("\n", validationErrors));
            aValidationAlert.showAndWait();
            statusLabel.setText("Enter month, category, and valid limit");
            return;
        }

        try {
            Budget aBudget = new Budget(0, aCategory, aMonth, aLimit);
            budgetManager.setMonthlyBudget(aBudget);
            refresh();
            statusLabel.setText("Budget saved");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.BUDGETS_CHANGED);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to save budget");
        }
    }

    private void configureControls() {
        monthComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(YearMonth anObject) {
                return anObject == null ? "" : DateUtil.formatYearMonth(anObject);
            }

            @Override
            public YearMonth fromString(String aString) {
                return null;
            }
        });
        monthComboBox.valueProperty().addListener((anObs, anOldValue, aNewValue) -> refresh());

        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category anObject) {
                return anObject == null ? "" : anObject.getName();
            }

            @Override
            public Category fromString(String aString) {
                return null;
            }
        });
    }

    private void configureTable() {
        categoryColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().categoryName));
        limitColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(MoneyUtil.formatCurrency(aCell.getValue().limit)));
        spentColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(MoneyUtil.formatCurrency(aCell.getValue().spent)));
        utilisationColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().utilisationText));
        alertColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().alertLevel));
        budgetsTable.setItems(rows);
    }

    private void loadMonths() {
        monthComboBox.setItems(FXCollections.observableArrayList(DateUtil.recentMonths(12)));
        monthComboBox.getSelectionModel().selectFirst();
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryManager.getAllCategories();
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            categoryComboBox.getSelectionModel().selectFirst();
        } catch (Exception ignored) {
        }
    }

    private void refresh() {
        YearMonth aMonth = monthComboBox.getValue();
        if (aMonth == null) {
            return;
        }
        try {
            Map<Long, Budget> budgetByCategoryId = new HashMap<>();
            for (Budget aBudget : budgetManager.getBudgetsForMonth(aMonth)) {
                budgetByCategoryId.put(aBudget.getCategory().getId(), aBudget);
            }

            Map<Category, BigDecimal> spendByCategory = analyticsService.getSpendByCategory(aMonth);
            Map<Long, BigDecimal> spendByCategoryId = new HashMap<>();
            for (Map.Entry<Category, BigDecimal> entry : spendByCategory.entrySet()) {
                spendByCategoryId.put(entry.getKey().getId(), entry.getValue());
            }

            List<Category> categories = categoryComboBox.getItems();
            rows.clear();
            for (Category aCategory : categories) {
                BigDecimal aSpent = spendByCategoryId.getOrDefault(aCategory.getId(), BigDecimal.ZERO);
                Budget aBudget = budgetByCategoryId.get(aCategory.getId());
                BigDecimal aLimit = aBudget == null ? BigDecimal.ZERO : aBudget.getMonthlyLimit();
                BigDecimal aUtilisation = budgetManager.calculateUtilisation(aSpent, aLimit);
                BudgetManager.BudgetAlertLevel aLevel = budgetManager.classifyUtilisation(aUtilisation);
                String aUtilisationText = formatPercent(aUtilisation);
                rows.add(new BudgetRow(aCategory.getName(), aLimit, aSpent, aUtilisationText, aLevel.name()));
            }
            statusLabel.setText("Loaded budgets for " + DateUtil.formatYearMonth(aMonth));
        } catch (Exception ignored) {
            statusLabel.setText("Failed to load budgets");
        }
    }

    private String formatPercent(BigDecimal aRatio) {
        if (aRatio == null) {
            return "0%";
        }
        BigDecimal aPercent = aRatio.multiply(new BigDecimal("100"));
        return aPercent.setScale(0, RoundingMode.HALF_UP) + "%";
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

    public static final class BudgetRow {
        private final String categoryName;
        private final BigDecimal limit;
        private final BigDecimal spent;
        private final String utilisationText;
        private final String alertLevel;

        public BudgetRow(
                String aCategoryName,
                BigDecimal aLimit,
                BigDecimal aSpent,
                String aUtilisationText,
                String anAlertLevel
        ) {
            categoryName = aCategoryName;
            limit = aLimit;
            spent = aSpent;
            utilisationText = aUtilisationText;
            alertLevel = anAlertLevel;
        }
    }
}

