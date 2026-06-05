-- Operational SCLX/import staging record tables.
--
-- This migration continues the transition of repository-owned staging tables
-- into Flyway. It mirrors the current repository table shapes for:
--   ReportingPeriodRecordRepository
--   SupplyRecordRepository
--   OtherAssetItemRecordRepository
--   OutstandingItemRecordRepository
--
-- Repository CREATE TABLE IF NOT EXISTS fallback remains in place for now.

CREATE TABLE IF NOT EXISTS imported_reporting_period_record (
    period_key VARCHAR(768) PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    label VARCHAR(255),
    fiscal_year INT,
    period_type VARCHAR(64),
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_reporting_period_dates
ON imported_reporting_period_record(start_date, end_date);

CREATE TABLE IF NOT EXISTS imported_supply_record (
    supply_id VARCHAR(255) PRIMARY KEY,
    item_number VARCHAR(255),
    date_acquired DATE,
    description CLOB,
    count_value INT,
    approx_value_total DECIMAL(19,2),
    value_per_item DECIMAL(19,2),
    guardian_legal_name VARCHAR(255),
    guardian_email VARCHAR(255),
    guardian_phone VARCHAR(64),
    guardianship_date_as_of DATE,
    guardianship_last_confirmed DATE,
    guardianship_returned BOOLEAN,
    guardianship_notes CLOB,
    removal_approved_by VARCHAR(255),
    removal_reason CLOB,
    removal_number_removed INT,
    removal_removed BOOLEAN,
    removal_type VARCHAR(128),
    additional_notes CLOB,
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_supply_date_acquired
ON imported_supply_record(date_acquired);

CREATE TABLE IF NOT EXISTS imported_other_asset_item_record (
    other_asset_item_id VARCHAR(255) PRIMARY KEY,
    ledger_transaction_id VARCHAR(255),
    ledger_line_id VARCHAR(255),
    workbook_sheet_key VARCHAR(255),
    workbook_row_index INT,
    paid_to VARCHAR(255),
    year_value INT,
    reason CLOB,
    type_value VARCHAR(255),
    type_code VARCHAR(64),
    event_budget_label VARCHAR(255),
    amount_as_of_prior_year_end DECIMAL(19,2),
    paid_returned_on_ledger_row_index INT,
    settlement_transaction_id VARCHAR(255),
    settlement_line_id VARCHAR(255),
    status VARCHAR(64),
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_other_asset_ledger_ref
ON imported_other_asset_item_record(ledger_transaction_id, ledger_line_id);

CREATE INDEX IF NOT EXISTS ix_imported_other_asset_status
ON imported_other_asset_item_record(status);

CREATE TABLE IF NOT EXISTS imported_outstanding_item_record (
    outstanding_item_id VARCHAR(255) PRIMARY KEY,
    kind VARCHAR(64),
    ledger_transaction_id VARCHAR(255),
    ledger_line_id VARCHAR(255),
    workbook_sheet_key VARCHAR(255),
    workbook_row_index INT,
    date_sent_or_received DATE,
    incoming_check_or_transfer_date DATE,
    transfer_id_or_check_number VARCHAR(255),
    date_shows_on_statement DATE,
    person_or_business_name VARCHAR(255),
    details_notes CLOB,
    from_to_card_merchant VARCHAR(255),
    account_for_payment_or_deposit VARCHAR(255),
    amount DECIMAL(19,2),
    date_reversed DATE,
    reversal_reason_and_approval CLOB,
    reversal_transaction_id VARCHAR(255),
    reversal_line_id VARCHAR(255),
    status VARCHAR(64),
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_outstanding_ledger_ref
ON imported_outstanding_item_record(ledger_transaction_id, ledger_line_id);

CREATE INDEX IF NOT EXISTS ix_imported_outstanding_status
ON imported_outstanding_item_record(status);

CREATE INDEX IF NOT EXISTS ix_imported_outstanding_statement_date
ON imported_outstanding_item_record(date_shows_on_statement);
