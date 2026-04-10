# Student Budget & Expense Tracking System

A desktop application for university students to track daily expenses, manage recurring subscriptions, set category-based monthly budgets and visualise spending patterns through interactive charts.

Built with Java 17, JavaFX 21 and MySQL 8+.

## Features

- **Expense Management**: Add, edit, delete expenses with date, category, description and type (one-time / recurring)
- **Subscription Tracking**: Track recurring subscriptions with automatic monthly-equivalent cost calculation across four billing cycles (weekly, monthly, quarterly, annually)
- **Budget Monitoring**: Set per-category monthly budgets with three-tier utilisation alerts (OK / WARNING / CRITICAL)
- **Analytics Dashboard**: Real-time Pie Chart (spend by category), Line Chart (30-day trend) and Bar Chart (top categories)
- **CSV Export**: Export visible expenses to a CSV file
- **Date Range Filtering**: Load expenses within a selected date range
- **Category Filtering and Search**: Filter by category and search by description keyword

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| GUI | JavaFX 21 |
| Database | MySQL 8+ |
| Connectivity | JDBC (mysql-connector-j) |
| Build | Maven |
| Testing | JUnit 5 |

## Project Structure

```
src/
├── main/
│   ├── java/com/studentexpensetracker/
│   │   ├── app/         MainApp, AppModule (DI container)
│   │   ├── model/       Expense, Subscription, Budget, Category, enums
│   │   ├── dao/         DAO interfaces
│   │   ├── dao/mysql/   MySQL DAO implementations
│   │   ├── db/          DatabaseHandler (Singleton), MigrationsRunner
│   │   ├── service/     Managers, AnalyticsService, CsvExportService, AppEventBus
│   │   ├── ui/          JavaFX controllers
│   │   └── util/        MoneyUtil, DateUtil
│   └── resources/
│       ├── db/          schema.sql, seed.sql
│       └── ui/          FXML views, styles.css
└── test/
    └── java/com/studentexpensetracker/
        ├── model/       BillingCycleTest, BaseTransactionTest
        ├── service/     BudgetManagerTest
        └── db/          DatabaseHandlerSmokeTest
```

## Prerequisites

- Java 17+
- Maven 3.x
- MySQL 8+ (running locally)

## Setup

1. **Clone the repository**
   ```
   git clone https://github.com/ItsM0rty/bedfordshire-student-expense-manager.git
   cd bedfordshire-student-expense-manager
   ```

2. **Configure the database**

   Create a MySQL database and update `src/main/resources/application.properties` with your credentials. Alternatively, set environment variables:
   ```
   DB_URL=jdbc:mysql://localhost:3306/student_expense_tracker
   DB_USERNAME=root
   DB_PASSWORD=yourpassword
   ```

   The application runs `schema.sql` and `seed.sql` automatically on startup, so no manual table creation is needed.

3. **Build and run**
   ```
   mvn clean javafx:run
   ```

4. **Run tests**
   ```
   mvn test
   ```

## Architecture

The application follows a strict four-layer architecture:

```
UI Layer (FXML + Controllers)
        |
Service Layer (Managers, AnalyticsService, AppEventBus)
        |
DAO Layer (Interfaces + MySQL Implementations)
        |
Database Layer (DatabaseHandler + MigrationsRunner)
```

No layer communicates directly with a non-adjacent layer.

## Author

Suyash Bhattarai (2528793)
University of Bedfordshire
CIS096-1: Principles of Programming and Data Structures
