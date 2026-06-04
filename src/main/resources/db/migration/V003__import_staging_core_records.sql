-- Core SCLX/import staging record tables.
--
-- These table shapes mirror the current repository-owned DDL in:
--   OrganizationRecordRepository
--   FundRecordRepository
--   EventRecordRepository
--   DocumentRecordRepository
--
-- The repositories still retain CREATE TABLE IF NOT EXISTS fallback during the
-- Flyway transition. Once all staging tables are migration-owned and validated,
-- AbstractRepository self-DDL can be removed or guarded for test-only use.

CREATE TABLE IF NOT EXISTS imported_organization_record (
    organization_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(512) NOT NULL,
    parent_organization VARCHAR(255),
    base_currency VARCHAR(32),
    fiscal_year_start DATE,
    fiscal_year_end DATE,
    extensions_json CLOB
);

CREATE TABLE IF NOT EXISTS imported_fund_record (
    fund_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    restricted BOOLEAN NOT NULL,
    description CLOB,
    extensions_json CLOB
);

CREATE TABLE IF NOT EXISTS imported_event_record (
    event_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    hosting_organization_id VARCHAR(255),
    extensions_json CLOB
);

CREATE TABLE IF NOT EXISTS imported_document_record (
    document_id VARCHAR(255) PRIMARY KEY,
    document_type VARCHAR(128),
    reference_number VARCHAR(255),
    document_date DATE,
    file_name VARCHAR(512),
    notes CLOB,
    extensions_json CLOB
);

CREATE INDEX IF NOT EXISTS ix_imported_organization_parent
ON imported_organization_record(parent_organization);

CREATE INDEX IF NOT EXISTS ix_imported_event_host_org
ON imported_event_record(hosting_organization_id);

CREATE INDEX IF NOT EXISTS ix_imported_document_date
ON imported_document_record(document_date);
