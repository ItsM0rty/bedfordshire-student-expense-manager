package com.studentexpensetracker.app;

import com.studentexpensetracker.dao.BudgetDao;
import com.studentexpensetracker.dao.CategoryDao;
import com.studentexpensetracker.dao.ExpenseDao;
import com.studentexpensetracker.dao.SubscriptionDao;
import com.studentexpensetracker.dao.mysql.MySqlBudgetDao;
import com.studentexpensetracker.dao.mysql.MySqlCategoryDao;
import com.studentexpensetracker.dao.mysql.MySqlExpenseDao;
import com.studentexpensetracker.dao.mysql.MySqlSubscriptionDao;
import com.studentexpensetracker.db.DatabaseHandler;
import com.studentexpensetracker.service.AnalyticsService;
import com.studentexpensetracker.service.BudgetManager;
import com.studentexpensetracker.service.CategoryManager;
import com.studentexpensetracker.service.CsvExportService;
import com.studentexpensetracker.service.ExpenseManager;
import com.studentexpensetracker.service.SubscriptionManager;

public final class AppModule {
    private static final AppModule instance = new AppModule();

    private final DatabaseHandler databaseHandler;
    private final ExpenseManager expenseManager;
    private final SubscriptionManager subscriptionManager;
    private final CategoryManager categoryManager;
    private final BudgetManager budgetManager;
    private final AnalyticsService analyticsService;
    private final CsvExportService csvExportService;

    private AppModule() {
        databaseHandler = DatabaseHandler.getInstance();

        ExpenseDao anExpenseDao = new MySqlExpenseDao();
        SubscriptionDao aSubscriptionDao = new MySqlSubscriptionDao();
        CategoryDao aCategoryDao = new MySqlCategoryDao();
        BudgetDao aBudgetDao = new MySqlBudgetDao();

        expenseManager = new ExpenseManager(databaseHandler, anExpenseDao);
        subscriptionManager = new SubscriptionManager(databaseHandler, aSubscriptionDao);
        categoryManager = new CategoryManager(databaseHandler, aCategoryDao);
        budgetManager = new BudgetManager(databaseHandler, aBudgetDao);
        analyticsService = new AnalyticsService(expenseManager, subscriptionManager, budgetManager);
        csvExportService = new CsvExportService();
    }

    public static AppModule getInstance() {
        return instance;
    }

    public ExpenseManager getExpenseManager() {
        return expenseManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public CategoryManager getCategoryManager() {
        return categoryManager;
    }

    public BudgetManager getBudgetManager() {
        return budgetManager;
    }

    public AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    public CsvExportService getCsvExportService() {
        return csvExportService;
    }
}

