CREATE DATABASE IF NOT EXISTS student_expense_tracker;

USE student_expense_tracker;

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS expense (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    expense_type VARCHAR(20) NOT NULL,
    recurrence_interval_days INT NULL,
    recurrence_end_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
    CONSTRAINT ck_expense_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_expense_type_valid CHECK (expense_type IN ('ONE_TIME', 'RECURRING'))
);

CREATE INDEX idx_expense_date ON expense(expense_date);
CREATE INDEX idx_expense_category ON expense(category_id);

CREATE TABLE IF NOT EXISTS subscription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(120) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    next_renewal_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT ck_subscription_cost_positive CHECK (cost >= 0),
    CONSTRAINT ck_billing_cycle_valid CHECK (billing_cycle IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY')),
    CONSTRAINT ck_subscription_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_subscription_next_renewal ON subscription(next_renewal_date);

CREATE TABLE IF NOT EXISTS budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    budget_month CHAR(7) NOT NULL,
    monthly_limit DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_category FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE RESTRICT,
    CONSTRAINT uq_budget_month_category UNIQUE (budget_month, category_id),
    CONSTRAINT ck_budget_limit_positive CHECK (monthly_limit >= 0),
    CONSTRAINT ck_budget_month_format CHECK (budget_month REGEXP '^[0-9]{4}-[0-9]{2}$')
);

CREATE INDEX idx_budget_month ON budget(budget_month);

