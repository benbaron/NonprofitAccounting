# Proposed SQL Schema for Nonprofit Bookkeeping

This schema is intended for migrating the application's JSON-based persistence (as described in `PROJECT_OVERVIEW.md`) to a relational database.  The goal is compatibility with an embedded Java database (e.g. H2) that can reside in the user's `.m2` directory or another local path.

## Tables

### company
- `company_id` INTEGER PRIMARY KEY AUTO_INCREMENT
- `name` VARCHAR(255)
- `legal_structure` VARCHAR(64)
- `tax_id` VARCHAR(64)
- `address` VARCHAR(255)
- `phone` VARCHAR(64)
- `email` VARCHAR(128)
- `fiscal_year_start` VARCHAR(16)
- `base_currency` VARCHAR(8)
- `starting_balance_date` VARCHAR(16)
- `chart_of_accounts_type` VARCHAR(64)
- `admin_username` VARCHAR(64)
- `admin_password` VARCHAR(64)
- `default_bank_account` VARCHAR(64)
- `enable_fund_accounting` BOOLEAN
- `enable_inventory` BOOLEAN
- `enable_multi_currency` BOOLEAN

### account
- `account_id` VARCHAR(64) PRIMARY KEY
- `company_id` INTEGER REFERENCES company(company_id)
- `account_code` VARCHAR(64)
- `name` VARCHAR(255)
- `type` VARCHAR(32)
- `increase_side` VARCHAR(8)
- `parent_account_id` VARCHAR(64) REFERENCES account(account_id)
- `currency` VARCHAR(8)
- `opening_balance` DECIMAL(15,2)

### fund
- `fund_id` VARCHAR(64) PRIMARY KEY
- `company_id` INTEGER REFERENCES company(company_id)
- `name` VARCHAR(255)
- `balance` DECIMAL(15,2)

### account_fund
Mapping table for many‑to‑many relationship between accounts and funds.
- `account_id` VARCHAR(64) REFERENCES account(account_id)
- `fund_id` VARCHAR(64) REFERENCES fund(fund_id)
- PRIMARY KEY (`account_id`, `fund_id`)

### transaction
- `transaction_id` INTEGER PRIMARY KEY AUTO_INCREMENT
- `company_id` INTEGER REFERENCES company(company_id)
- `booking_timestamp` BIGINT
- `date` VARCHAR(16)
- `memo` VARCHAR(255)

### entry
- `entry_id` INTEGER PRIMARY KEY AUTO_INCREMENT
- `transaction_id` INTEGER REFERENCES transaction(transaction_id)
- `account_id` VARCHAR(64) REFERENCES account(account_id)
- `amount` DECIMAL(15,2)
- `account_side` VARCHAR(8)
- `account_name` VARCHAR(255)

### budget
- `budget_id` VARCHAR(64) PRIMARY KEY
- `company_id` INTEGER REFERENCES company(company_id)
- `budget_name` VARCHAR(255)
- `fiscal_year` INTEGER
- `description` VARCHAR(512)
- `currency` VARCHAR(8)
- `applicable_fund_id` VARCHAR(64) REFERENCES fund(fund_id)

### budget_line
- `line_id` INTEGER PRIMARY KEY AUTO_INCREMENT
- `budget_id` VARCHAR(64) REFERENCES budget(budget_id)
- `account_id` VARCHAR(64) REFERENCES account(account_id)
- `account_name` VARCHAR(255)
- `total_amount` DECIMAL(15,2)
- `periodicity` VARCHAR(16)
- `fund_id` VARCHAR(64) REFERENCES fund(fund_id)

### budget_line_period_amount
Stores periodic amounts for a budget line when `periodicity` is not ANNUAL.
- `line_id` INTEGER REFERENCES budget_line(line_id)
- `period_index` INTEGER
- `amount` DECIMAL(15,2)
- PRIMARY KEY (`line_id`, `period_index`)

### donor
- `donor_id` VARCHAR(64) PRIMARY KEY
- `company_id` INTEGER REFERENCES company(company_id)
- `name` VARCHAR(255)
- `total_donations` DECIMAL(15,2)
- `last_donation_date` DATE

### donation
Tracks individual donations from donors.
- `donation_id` INTEGER PRIMARY KEY AUTO_INCREMENT
- `donor_id` VARCHAR(64) REFERENCES donor(donor_id)
- `amount` DECIMAL(15,2)
- `donation_type` VARCHAR(64)
- `donation_date` DATE

### inventory_item
- `item_id` VARCHAR(64) PRIMARY KEY
- `company_id` INTEGER REFERENCES company(company_id)
- `name` VARCHAR(255)
- `acquired` DATE
- `cost` DECIMAL(15,2)
- `accum_depreciation` DECIMAL(15,2)
- `net_value` DECIMAL(15,2)
- `life_years` INTEGER
- `depreciation_rate` DECIMAL(8,4)
- `depreciation_method` VARCHAR(64)

### document_attachment
- `document_id` VARCHAR(64) PRIMARY KEY
- `transaction_id` VARCHAR(64)
- `file_path` VARCHAR(255)
- `original_name` VARCHAR(255)
- `upload_time` BIGINT


## Diagram
The file `uml/ER_DIAGRAM.puml` contains a PlantUML entity relationship diagram for these tables.

## Notes
- All monetary values use `DECIMAL(15,2)` for simplicity.  Adjust precision as needed.
- Enum fields (such as account type, side, and periodicity) are stored as strings for readability.
- Foreign keys reference the primary keys of related tables, enabling joins.
- H2 in embedded mode can store the DB file anywhere, including the Maven `.m2` directory if desired.
