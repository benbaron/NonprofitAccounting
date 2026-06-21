-- Finance-oriented SCLX/import staging record tables.
--
-- This migration continues moving repository-owned staging tables into Flyway.
-- It captures nullable staging shapes for:
--   AssetRecordRepository
--   BudgetRecordRepository
--   BankStatementRecordRepository
--   BankingItemRecordRepository
--
-- Repository CREATE TABLE IF NOT EXISTS fallback remains in place for now.

CREATE TABLE IF NOT EXISTS imported_asset_record (
    asset_id VARCHAR(255) PRIMARY KEY,
    date_acquired DATE,
    description CLOB,
    item_count INT,
    approx_value_total DECIMAL(19,2),
    accumulated_depreciation DECIMAL(19,2),
    value_per_item DECIMAL(19,2),
    item_type VARCHAR(128),
    used_for VARCHAR(255),
    lot_paid_total DECIMAL(19,2),
    lot_item_count INT,
    guardian_legal_name VARCHAR(255),
    guardian_email VARCHAR(255),
    guardian_phone VARCHAR(64),
    guardianship_date_as_of DATE,
    guardianship_confirmed BOOLEAN,
    guardianship_confirmation_status VARCHAR(128),
    guardianship_notes CLOB,
    removal_approved_by VARCHAR(255),
    removal_approval_date DATE,
    removal_reason CLOB,
    removal_number_removed INT,
    removal_removed BOOLEAN,
    removal_type VARCHAR(128),
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_asset_date_acquired
ON imported_asset_record(date_acquired);

CREATE INDEX IF NOT EXISTS ix_imported_asset_item_type
ON imported_asset_record(item_type);

CREATE TABLE IF NOT EXISTS imported_budget (
    budget_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    fiscal_year INT,
    fund_id VARCHAR(255),
    active BOOLEAN,
    description CLOB,
    extensions_json CLOB
);

CREATE TABLE IF NOT EXISTS imported_budget_line (
    budget_id VARCHAR(255) NOT NULL,
    line_ordinal INT NOT NULL,
    event_name VARCHAR(255),
    budgeted_amount DECIMAL(19,2),
    revenue_category VARCHAR(255),
    expense_category VARCHAR(255),
    account_id VARCHAR(255),
    notes CLOB,
    extensions_json CLOB,
    PRIMARY KEY (budget_id, line_ordinal),
    CONSTRAINT fk_imported_budget_line_budget
      FOREIGN KEY (budget_id) REFERENCES imported_budget(budget_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_imported_budget_fiscal_year
ON imported_budget(fiscal_year);

CREATE INDEX IF NOT EXISTS ix_imported_budget_line_account
ON imported_budget_line(account_id);

CREATE TABLE IF NOT EXISTS imported_bank_statement (
    import_id VARCHAR(255) PRIMARY KEY,
    source_format VARCHAR(64),
    source_version VARCHAR(64),
    statement_kind VARCHAR(64),
    bank_id VARCHAR(255),
    account_id VARCHAR(255),
    account_type VARCHAR(64),
    currency VARCHAR(16),
    statement_start DATE,
    statement_end DATE,
    ledger_balance DECIMAL(19,2),
    ledger_balance_asof TIMESTAMP,
    available_balance DECIMAL(19,2),
    available_balance_asof TIMESTAMP,
    document_id VARCHAR(255),
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_bank_statement_account_period
ON imported_bank_statement(bank_id, account_id, statement_start, statement_end);

CREATE TABLE IF NOT EXISTS imported_banking_item (
    banking_item_id VARCHAR(255) PRIMARY KEY,
    kind VARCHAR(64),
    bank_account_id VARCHAR(255),
    transaction_id VARCHAR(255),
    line_ids CLOB,
    cleared_date DATE,
    amount DECIMAL(19,2),
    check_number VARCHAR(255),
    payee VARCHAR(255),
    deposit_date DATE,
    payer VARCHAR(255),
    deposit_id VARCHAR(255),
    memo CLOB,
    source VARCHAR(64),
    status VARCHAR(64),
    import_id VARCHAR(255),
    ofx_fit_id VARCHAR(255),
    ofx_reference_number VARCHAR(255),
    ofx_name VARCHAR(255),
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_banking_item_account_date
ON imported_banking_item(bank_account_id, cleared_date);

CREATE INDEX IF NOT EXISTS ix_imported_banking_item_import
ON imported_banking_item(import_id);

CREATE INDEX IF NOT EXISTS ix_imported_banking_item_status
ON imported_banking_item(status);
