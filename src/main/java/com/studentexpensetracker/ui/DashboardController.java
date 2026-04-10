package com.studentexpensetracker.ui;

import com.studentexpensetracker.app.AppModule;
import com.studentexpensetracker.model.Category;
import com.studentexpensetracker.service.AnalyticsService;
import com.studentexpensetracker.service.AppEventBus;
import com.studentexpensetracker.util.MoneyUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public final class DashboardController {
    @FXML
    private Label monthSpendLabel;

    @FXML
    private Label upcomingRenewalsLabel;

    @FXML
    private Label budgetAlertsLabel;

    @FXML
    private PieChart spendByCategoryChart;

    @FXML
    private LineChart<String, Number> spendTrendChart;

    @FXML
    private BarChart<String, Number> topCategoriesChart;

    private final AnalyticsService analyticsService = AppModule.getInstance().getAnalyticsService();

    @FXML
    private void initialize() {
        AppEventBus.getInstance().subscribe(anEvent -> {
            if (anEvent == AppEventBus.AppEvent.EXPENSES_CHANGED
                    || anEvent == AppEventBus.AppEvent.SUBSCRIPTIONS_CHANGED
                    || anEvent == AppEventBus.AppEvent.BUDGETS_CHANGED) {
                refreshDashboard();
            }
        });
        refreshDashboard();
    }

    private void refreshDashboard() {
        Platform.runLater(() -> {
            try {
                YearMonth aMonth = YearMonth.now();
                BigDecimal aMonthSpend = analyticsService.getMonthSpend(aMonth);
                monthSpendLabel.setText(MoneyUtil.formatCurrency(aMonthSpend));

                int upcomingRenewals = analyticsService.countUpcomingRenewals(30);
                upcomingRenewalsLabel.setText(String.valueOf(upcomingRenewals));

                int budgetAlerts = analyticsService.countBudgetAlerts(aMonth);
                budgetAlertsLabel.setText(String.valueOf(budgetAlerts));

                loadSpendByCategoryChart(aMonth);
                loadSpendTrendChart();
                loadTopCategoriesChart(aMonth);
            } catch (Exception ignored) {
            }
        });
    }

    private void loadSpendByCategoryChart(YearMonth aMonth) throws Exception {
        Map<Category, BigDecimal> spendByCategory = analyticsService.getSpendByCategory(aMonth);
        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        for (Map.Entry<Category, BigDecimal> entry : spendByCategory.entrySet()) {
            chartData.add(new PieChart.Data(entry.getKey().getName(), entry.getValue().doubleValue()));
        }
        spendByCategoryChart.setData(chartData);
    }

    private void loadSpendTrendChart() throws Exception {
        spendTrendChart.getData().clear();
        XYChart.Series<String, Number> aSeries = new XYChart.Series<>();
        for (AnalyticsService.DailySpend dailySpend : analyticsService.getSpendLastDays(30)) {
            aSeries.getData().add(new XYChart.Data<>(
                    dailySpend.getDate().toString().substring(5),
                    dailySpend.getAmount().doubleValue()
            ));
        }
        spendTrendChart.getData().add(aSeries);
    }

    private void loadTopCategoriesChart(YearMonth aMonth) throws Exception {
        topCategoriesChart.getData().clear();
        XYChart.Series<String, Number> aSeries = new XYChart.Series<>();
        for (AnalyticsService.CategorySpend aSpend : analyticsService.getTopCategorySpends(aMonth, 6)) {
            aSeries.getData().add(new XYChart.Data<>(aSpend.getCategory().getName(), aSpend.getAmount().doubleValue()));
        }
        topCategoriesChart.getData().add(aSeries);
    }
}

