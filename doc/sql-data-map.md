# SQL Data Map

This document summarizes the H2 database schema created by `nonprofitbookkeeping.core.Database#ensureSchema` and the primary repositories that access each table.

## Schema overview

| Table | Purpose | Primary Key | Related Tables | Main Repository/Service |
| --- | --- | --- | --- | --- |
| `company_profile` | Single-row company profile/configuration | `id` | — | `CompanyProfileRepository` |
| `account` | Chart of accounts master data | `account_number` | `account_fund`, `journal_entry` | `AccountRepository` |
| `account_fund` | Many-to-many mapping between accounts and funds | `(account_number, fund_id)` | `account` | `AccountRepository` |
| `journal_transaction` | Journal transaction headers | `id` | `journal_entry`, `transaction_info` | `JournalRepository` |
| `journal_entry` | Journal transaction lines | `id` (auto) | `journal_transaction`, `account` | `JournalRepository` |
| `transaction_info` | Key/value metadata for transactions | `(txn_id, k)` | `journal_transaction` | `JournalRepository` |
| `donor` | Donor contacts | `id` (auto), `external_id` (unique) | — | `DonorRepository` |
| `document` | JSON-like documents (legacy file replacements) | `name` | — | `DocumentRepository` |
| `json_storage` | Generic JSON storage by key | `storage_key` | — | `JsonStorageRepository` |
| `company_store` | Serialized company snapshots (binary payload) | `id` (identity) | — | `CompanyRepository` |

## Table details

### `company_profile`
- **Columns**: `id`, `name`, `address`, `phone`, `email`, `fiscal_year_start`, `base_currency`, `starting_balance_date`, `chart_of_accounts_type`, `admin_username`, `admin_password`, `default_bank_account`, `enable_fund_accounting`, `enable_inventory`, `enable_multi_currency`, `legal_structure`, `tax_id`, `company_file_dir`, `company_file_name`
- **Notes**: `id` is always `1` via `CompanyProfileRepository` upsert.

### `account`
- **Columns**: `account_number`, `name`, `account_code`, `account_type`, `increase_side`, `parent_account_id`, `currency`, `opening_balance`
- **Notes**: Referenced by `account_fund.account_number` and `journal_entry.account_number`.

### `account_fund`
- **Columns**: `account_number`, `fund_id`
- **Constraints**: composite PK; FK to `account(account_number)` with `ON DELETE CASCADE`.

### `journal_transaction`
- **Columns**: `id`, `booking_ts`, `date_text`, `memo`, `to_from`, `check_number`, `clear_bank`, `budget_tracking`, `associated_fund_name`
- **Notes**: Parent for `journal_entry` and `transaction_info`.

### `journal_entry`
- **Columns**: `id`, `txn_id`, `amount`, `account_number`, `account_side`, `account_name`, `fund_number`
- **Constraints**: FK `txn_id -> journal_transaction(id)` with `ON DELETE CASCADE`; FK `account_number -> account(account_number)`.

### `transaction_info`
- **Columns**: `txn_id`, `k`, `v`
- **Constraints**: composite PK; FK `txn_id -> journal_transaction(id)` with `ON DELETE CASCADE`.

### `donor`
- **Columns**: `id`, `external_id`, `name`, `email`, `phone`
- **Constraints**: `external_id` unique (indexed).

### `document`
- **Columns**: `name`, `content`
- **Notes**: Stores JSON payloads that used to be file-based (e.g., budgets/sales).

### `json_storage`
- **Columns**: `storage_key`, `payload`
- **Notes**: Shared key/value JSON storage.

### `company_store`
- **Columns**: `id`, `name`, `payload`, `updated_at`
- **Notes**: `payload` stores serialized `Company` aggregates.

## Repository/table access map

- `AccountRepository`
  - `account`
  - `account_fund`
- `JournalRepository`
  - `journal_transaction`
  - `journal_entry`
  - `transaction_info`
- `CompanyProfileRepository`
  - `company_profile`
- `DonorRepository`
  - `donor`
- `DocumentRepository`
  - `document`
- `JsonStorageRepository`
  - `json_storage`
- `CompanyRepository`
  - `company_store`

## Key relationships

- `account (1) -> account_fund (many)` via `account_fund.account_number`.
- `journal_transaction (1) -> journal_entry (many)` via `journal_entry.txn_id`.
- `journal_transaction (1) -> transaction_info (many)` via `transaction_info.txn_id`.
- `account (1) -> journal_entry (many)` via `journal_entry.account_number`.
