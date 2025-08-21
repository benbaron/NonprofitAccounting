-- Initial schema for the application database.
-- Executed by Flyway on first run to create all core tables.
CREATE TABLE donors (
    donorId VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    totalDonations DECIMAL(19,2),
    lastDonationDate TIMESTAMP,
    donationAmount DECIMAL(19,2),
    donationType VARCHAR(255),
    donationDate TIMESTAMP
);

CREATE TABLE inventory_items (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    acquired VARCHAR(255),
    cost DECIMAL(19,2),
    accDep DECIMAL(19,2),
    netValue DECIMAL(19,2),
    lifeYears INT,
    depreciationRate DECIMAL(19,2),
    depreciationMethod VARCHAR(255)
);

CREATE TABLE sale_records (
    id VARCHAR(255) PRIMARY KEY,
    date VARCHAR(255),
    item VARCHAR(255),
    qty INT,
    price DECIMAL(19,2),
    cost DECIMAL(19,2)
);

CREATE TABLE accounting_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bookingDateTimestamp BIGINT,
    date VARCHAR(255),
    memo VARCHAR(255),
    toFrom VARCHAR(255),
    checkNumber VARCHAR(255),
    clearBank VARCHAR(255),
    budgetTracking VARCHAR(255),
    associatedFundName VARCHAR(255)
);

CREATE TABLE accounting_entries (
    id INT PRIMARY KEY AUTO_INCREMENT,
    amount DECIMAL(19,2),
    accountSide VARCHAR(255),
    accountNumber VARCHAR(255),
    fundNumber VARCHAR(255),
    accountName VARCHAR(255),
    supplementalRecordId VARCHAR(255),
    transaction_id INT,
    freeze BOOLEAN
);

CREATE TABLE supplemental_records (
    id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT,
    recordKey VARCHAR(255),
    recordValue VARCHAR(255),
    amount DOUBLE,
    assetAccount VARCHAR(255),
    incomeAccount VARCHAR(255),
    expenseAccount VARCHAR(255),
    fundName VARCHAR(255),
    sequenceNumber INT,
    ledger_entry_id BIGINT
);

CREATE TABLE ledger_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entryDate VARCHAR(255),
    checkNumber VARCHAR(255),
    cleared VARCHAR(255),
    toFrom VARCHAR(255),
    memoString VARCHAR(255),
    budgetTracking VARCHAR(255)
);

CREATE TABLE report_configurations (
    configurationId VARCHAR(255) PRIMARY KEY,
    userGivenName VARCHAR(255),
    reportType VARCHAR(255),
    dateSelectionMode INT,
    relativeDateRange VARCHAR(255),
    specificStartDate DATE,
    specificEndDate DATE,
    outputFormat VARCHAR(255)
);
