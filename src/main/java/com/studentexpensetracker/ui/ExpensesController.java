package com.studentexpensetracker.ui;

import com.studentexpensetracker.app.AppModule;
import com.studentexpensetracker.model.Category;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.ExpenseType;
import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.service.AnalyticsService;
import com.studentexpensetracker.service.BudgetManager;
import com.studentexpensetracker.service.CategoryManager;
import com.studentexpensetracker.service.ExpenseManager;
import com.studentexpensetracker.service.AppEventBus;
import com.studentexpensetracker.service.CsvExportService;
import com.studentexpensetracker.util.DateUtil;
import com.studentexpensetracker.util.MoneyUtil;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ExpensesController {
    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<CategorySelection> categoryComboBox;

    @FXML
    private TextField searchTextField;

    @FXML
    private TableView<Expense> expensesTable;

    @FXML
    private TableColumn<Expense, String> dateColumn;

    @FXML
    private TableColumn<Expense, String> categoryColumn;

    @FXML
    private TableColumn<Expense, String> typeColumn;

    @FXML
    private TableColumn<Expense, String> descriptionColumn;

    @FXML
    private TableColumn<Expense, String> amountColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label totalLabel;

    private final ExpenseManager expenseManager = AppModule.getInstance().getExpenseManager();
    private final CategoryManager categoryManager = AppModule.getInstance().getCategoryManager();
    private final BudgetManager budgetManager = AppModule.getInstance().getBudgetManager();
    private final AnalyticsService analyticsService = AppModule.getInstance().getAnalyticsService();
    private final CsvExportService csvExportService = AppModule.getInstance().getCsvExportService();

    private final ObservableList<Expense> expenses = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configureTable();
        configureFilters();
        loadCategories();
        loadDefaultDates();
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    @FXML
    private void onAddExpense() {
        ExpenseInput input = showExpenseDialog("Add expense", null);
        if (input == null) {
            return;
        }
        try {
            Expense aNewExpense = new Expense(
                    0,
                    input.amount,
                    input.expenseDate,
                    input.category,
                    input.description,
                    input.expenseType
            );
            expenseManager.addExpense(aNewExpense);
            refresh();
            statusLabel.setText("Expense added");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.EXPENSES_CHANGED);
            checkBudgetAndAlert(input.category, input.amount);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to add expense");
        }
    }

    @FXML
    private void onEditSelected() {
        Expense aSelected = expensesTable.getSelectionModel().getSelectedItem();
        if (aSelected == null) {
            statusLabel.setText("Select an expense to edit");
            return;
        }

        ExpenseInput input = showExpenseDialog("Edit expense", aSelected);
        if (input == null) {
            return;
        }
        try {
            Expense anUpdatedExpense = new Expense(
                    aSelected.getId(),
                    input.amount,
                    input.expenseDate,
                    input.category,
                    input.description,
                    input.expenseType
            );
            expenseManager.updateExpense(anUpdatedExpense);
            refresh();
            statusLabel.setText("Expense updated");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.EXPENSES_CHANGED);
            checkBudgetAndAlert(input.category, input.amount);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to update expense");
        }
    }

    @FXML
    private void onDeleteSelected() {
        Expense aSelected = expensesTable.getSelectionModel().getSelectedItem();
        if (aSelected == null) {
            statusLabel.setText("Select an expense to delete");
            return;
        }
        try {
            expenseManager.deleteExpense(aSelected.getId());
            refresh();
            statusLabel.setText("Expense deleted");
            AppEventBus.getInstance().publish(AppEventBus.AppEvent.EXPENSES_CHANGED);
        } catch (Exception ignored) {
            statusLabel.setText("Failed to delete expense");
        }
    }

    @FXML
    private void onExportCsv() {
        List<Expense> aVisibleExpenses = expensesTable.getItems();
        if (aVisibleExpenses == null || aVisibleExpenses.isEmpty()) {
            statusLabel.setText("No expenses to export");
            return;
        }

        FileChooser aFileChooser = new FileChooser();
        aFileChooser.setTitle("Export expenses to CSV");
        aFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        aFileChooser.setInitialFileName("expenses-export.csv");

        Window aWindow = expensesTable.getScene() == null ? null : expensesTable.getScene().getWindow();
        java.io.File aFile = aFileChooser.showSaveDialog(aWindow);
        if (aFile == null) {
            return;
        }

        try {
            Path aPath = aFile.toPath();
            csvExportService.exportExpenses(aPath, aVisibleExpenses);
            statusLabel.setText("Exported to " + aPath.getFileName());
        } catch (Exception ignored) {
            statusLabel.setText("Failed to export CSV");
        }
    }

    private void configureTable() {
        dateColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getExpenseDate().toString()));
        categoryColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getCategory().getName()));
        typeColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getExpenseType().getDisplayName()));
        descriptionColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(aCell.getValue().getDescription()));
        amountColumn.setCellValueFactory(aCell -> new ReadOnlyStringWrapper(MoneyUtil.formatCurrency(aCell.getValue().getAmount())));
        expensesTable.setItems(expenses);
    }

    private void configureFilters() {
        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategorySelection anObject) {
                return anObject == null ? "" : anObject.displayName;
            }

            @Override
            public CategorySelection fromString(String aString) {
                return null;
            }
        });

        searchTextField.textProperty().addListener((anObs, anOldValue, aNewValue) -> applyClientFilters());
        categoryComboBox.valueProperty().addListener((anObs, anOldValue, aNewValue) -> applyClientFilters());
    }

    private void loadDefaultDates() {
        LocalDate aToday = LocalDate.now();
        fromDatePicker.setValue(aToday.minusDays(30));
        toDatePicker.setValue(aToday);
    }

    private void loadCategories() {
        List<CategorySelection> filterSelections = new ArrayList<>();
        filterSelections.add(CategorySelection.all());
        try {
            List<Category> categories = categoryManager.getAllCategories();
            for (Category aCategory : categories) {
                filterSelections.add(CategorySelection.of(aCategory));
            }
        } catch (Exception ignored) {
        }
        categoryComboBox.setItems(FXCollections.observableArrayList(filterSelections));
        categoryComboBox.getSelectionModel().selectFirst();
    }

    private List<CategorySelection> getDialogCategorySelections() {
        List<CategorySelection> dialogSelections = new ArrayList<>();
        try {
            List<Category> categories = categoryManager.getAllCategories();
            for (Category aCategory : categories) {
                dialogSelections.add(CategorySelection.of(aCategory));
            }
        } catch (Exception ignored) {
        }
        return dialogSelections;
    }

    private void refresh() {
        LocalDate aFrom = DateUtil.safeDate(fromDatePicker.getValue(), LocalDate.now().minusDays(30));
        LocalDate aTo = DateUtil.safeDate(toDatePicker.getValue(), LocalDate.now());

        try {
            List<Expense> loadedExpenses = expenseManager.getExpensesByDateRange(aFrom, aTo);
            expenses.setAll(loadedExpenses);
            applyClientFilters();
            updateTotalLabel(expenses);
            statusLabel.setText("Loaded " + expenses.size() + " expenses");
        } catch (Exception ignored) {
            statusLabel.setText("Failed to load expenses");
        }
    }

    private void applyClientFilters() {
        String searchText = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        CategorySelection selection = categoryComboBox.getValue();
        Category selectedCategory = selection == null ? null : selection.category;

        List<Expense> filtered = new ArrayList<>();
        for (Expense anExpense : expenses) {
            if (selectedCategory != null && anExpense.getCategory().getId() != selectedCategory.getId()) {
                continue;
            }
            if (!searchText.isBlank() && !anExpense.getDescription().toLowerCase().contains(searchText)) {
                continue;
            }
            filtered.add(anExpense);
        }
        expensesTable.setItems(FXCollections.observableArrayList(filtered));
        updateTotalLabel(filtered);
    }

    private void updateTotalLabel(List<Expense> anExpenses) {
        BigDecimal total = BigDecimal.ZERO;
        for (Expense anExpense : anExpenses) {
            total = total.add(anExpense.getAmount());
        }
        totalLabel.setText(MoneyUtil.formatCurrency(total));
    }

    private ExpenseInput showExpenseDialog(String aTitle, Expense anExisting) {
        Dialog<ExpenseInput> aDialog = new Dialog<>();
        aDialog.setTitle(aTitle);
        aDialog.setHeaderText(anExisting == null ? "Add a new expense" : "Edit expense details");
        aDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        aDialog.getDialogPane().setPrefWidth(420);

        GridPane aGrid = new GridPane();
        aGrid.setHgap(14);
        aGrid.setVgap(14);
        aGrid.setPadding(new Insets(10, 0, 0, 0));

        ColumnConstraints aLabelCol = new ColumnConstraints();
        aLabelCol.setMinWidth(90);
        ColumnConstraints aFieldCol = new ColumnConstraints();
        aFieldCol.setHgrow(Priority.ALWAYS);
        aGrid.getColumnConstraints().addAll(aLabelCol, aFieldCol);

        TextField anAmountField = new TextField();
        anAmountField.setPromptText("e.g. 25.50");
        DatePicker aDatePicker = new DatePicker();
        aDatePicker.setPromptText("Select date");
        aDatePicker.setMaxWidth(Double.MAX_VALUE);
        ComboBox<CategorySelection> aCategoryField = new ComboBox<>();
        aCategoryField.setMaxWidth(Double.MAX_VALUE);
        aCategoryField.setPromptText("Select a category");
        TextField aDescriptionField = new TextField();
        aDescriptionField.setPromptText("e.g. Groceries from Tesco");
        CheckBox aRecurringCheckBox = new CheckBox("This is a recurring expense");

        List<CategorySelection> dialogCategories = getDialogCategorySelections();
        aCategoryField.setItems(FXCollections.observableArrayList(dialogCategories));
        aCategoryField.setConverter(categoryComboBox.getConverter());

        aGrid.addRow(0, new Label("Amount (£)"), anAmountField);
        aGrid.addRow(1, new Label("Date"), aDatePicker);
        aGrid.addRow(2, new Label("Category"), aCategoryField);
        aGrid.addRow(3, new Label("Description"), aDescriptionField);
        aGrid.add(aRecurringCheckBox, 0, 4, 2, 1);

        if (anExisting != null) {
            anAmountField.setText(anExisting.getAmount().toPlainString());
            aDatePicker.setValue(anExisting.getExpenseDate());
            for (CategorySelection aSelection : aCategoryField.getItems()) {
                if (aSelection.category != null && aSelection.category.getId() == anExisting.getCategory().getId()) {
                    aCategoryField.getSelectionModel().select(aSelection);
                    break;
                }
            }
            aDescriptionField.setText(anExisting.getDescription());
            aRecurringCheckBox.setSelected(anExisting.getExpenseType() == ExpenseType.RECURRING);
        } else {
            aDatePicker.setValue(LocalDate.now());
            if (!aCategoryField.getItems().isEmpty()) {
                aCategoryField.getSelectionModel().selectFirst();
            }
        }

        aDialog.getDialogPane().setContent(aGrid);

        Node anOkButton = aDialog.getDialogPane().lookupButton(ButtonType.OK);
        anOkButton.addEventFilter(ActionEvent.ACTION, anActionEvent -> {
            List<String> validationErrors = new ArrayList<>();
            BigDecimal anAmount = parseMoney(anAmountField.getText());
            if (anAmount == null) {
                validationErrors.add("Amount is required");
            } else if (anAmount.compareTo(BigDecimal.ZERO) <= 0) {
                validationErrors.add("Amount must be greater than 0");
            }
            if (aDatePicker.getValue() == null) {
                validationErrors.add("Date is required");
            }
            CategorySelection aCurrentSelection = aCategoryField.getValue();
            if (aCurrentSelection == null || aCurrentSelection.category == null) {
                validationErrors.add("Category is required");
            }
            String aCurrentDescription = aDescriptionField.getText() == null
                    ? "" : aDescriptionField.getText().trim();
            if (aCurrentDescription.isBlank()) {
                validationErrors.add("Description is required");
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
            BigDecimal anAmount = parseMoney(anAmountField.getText());
            LocalDate aDate = aDatePicker.getValue();
            CategorySelection aSelection = aCategoryField.getValue();
            String aDescription = aDescriptionField.getText() == null ? "" : aDescriptionField.getText().trim();
            ExpenseType aType = aRecurringCheckBox.isSelected() ? ExpenseType.RECURRING : ExpenseType.ONE_TIME;
            return new ExpenseInput(anAmount, aDate, aSelection.category, aDescription, aType);
        });

        return aDialog.showAndWait().orElse(null);
    }

    private void checkBudgetAndAlert(Category aCategory, BigDecimal aNewAmount) {
        try {
            YearMonth aMonth = YearMonth.now();
            Optional<Budget> aBudgetOpt = budgetManager.getBudgetForMonth(aCategory.getId(), aMonth);
            if (aBudgetOpt.isEmpty()) {
                return;
            }
            Budget aBudget = aBudgetOpt.get();
            Map<Category, BigDecimal> spendByCategory = analyticsService.getSpendByCategory(aMonth);
            BigDecimal aCurrentSpend = spendByCategory.getOrDefault(aCategory, BigDecimal.ZERO);
            BigDecimal aTotalAfter = aCurrentSpend;

            BigDecimal aUtilisation = budgetManager.calculateUtilisation(aTotalAfter, aBudget.getMonthlyLimit());
            BudgetManager.BudgetAlertLevel aLevel = budgetManager.classifyUtilisation(aUtilisation);

            if (aLevel == BudgetManager.BudgetAlertLevel.CRITICAL) {
                Alert anAlert = new Alert(Alert.AlertType.WARNING);
                anAlert.setTitle("Budget Exceeded");
                anAlert.setHeaderText("You've exceeded your " + aCategory.getName() + " budget!");
                anAlert.setContentText(
                        "Budget limit: " + MoneyUtil.formatCurrency(aBudget.getMonthlyLimit())
                                + "\nTotal spent: " + MoneyUtil.formatCurrency(aTotalAfter)
                                + "\nOver by: " + MoneyUtil.formatCurrency(aTotalAfter.subtract(aBudget.getMonthlyLimit()))
                );
                anAlert.showAndWait();
            } else if (aLevel == BudgetManager.BudgetAlertLevel.WARNING) {
                Alert anAlert = new Alert(Alert.AlertType.INFORMATION);
                anAlert.setTitle("Budget Warning");
                anAlert.setHeaderText("Approaching your " + aCategory.getName() + " budget limit");
                anAlert.setContentText(
                        "Budget limit: " + MoneyUtil.formatCurrency(aBudget.getMonthlyLimit())
                                + "\nTotal spent: " + MoneyUtil.formatCurrency(aTotalAfter)
                                + "\nRemaining: " + MoneyUtil.formatCurrency(aBudget.getMonthlyLimit().subtract(aTotalAfter))
                );
                anAlert.showAndWait();
            }
        } catch (Exception ignored) {
        }
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

    private static final class ExpenseInput {
        private final BigDecimal amount;
        private final LocalDate expenseDate;
        private final Category category;
        private final String description;
        private final ExpenseType expenseType;

        private ExpenseInput(
                BigDecimal anAmount,
                LocalDate anExpenseDate,
                Category aCategory,
                String aDescription,
                ExpenseType anExpenseType
        ) {
            amount = Objects.requireNonNull(anAmount);
            expenseDate = Objects.requireNonNull(anExpenseDate);
            category = Objects.requireNonNull(aCategory);
            description = Objects.requireNonNull(aDescription);
            expenseType = Objects.requireNonNull(anExpenseType);
        }
    }

    private static final class CategorySelection {
        private final String displayName;
        private final Category category;

        private CategorySelection(String aDisplayName, Category aCategory) {
            displayName = Objects.requireNonNull(aDisplayName);
            category = aCategory;
        }

        public static CategorySelection all() {
            return new CategorySelection("All", null);
        }

        public static CategorySelection of(Category aCategory) {
            return new CategorySelection(aCategory.getName(), aCategory);
        }

        @Override
        public boolean equals(Object anObject) {
            if (this == anObject) {
                return true;
            }
            if (!(anObject instanceof CategorySelection that)) {
                return false;
            }
            if (category == null || that.category == null) {
                return category == null && that.category == null && displayName.equals(that.displayName);
            }
            return category.getId() == that.category.getId();
        }

        @Override
        public int hashCode() {
            return category == null ? displayName.hashCode() : Long.hashCode(category.getId());
        }
    }
}

