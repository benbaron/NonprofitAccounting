# Database Architecture and File Persistence Manual

## 1. Overview
This application combines an embedded H2 relational database with JSON-based file storage. Hibernate/JPA manages the entity mappings while a small service layer wraps common persistence operations. User-facing data such as company profiles and chart of accounts can also be imported and exported as JSON or spreadsheet files.

## 2. Embedded H2 Database
The `DatabaseManager` bootstraps a file-backed H2 database stored in `./data/nonprofit` and exposes `EntityManager` instances to the rest of the application (see `src/main/java/nonprofitbookkeeping/persistence/DatabaseManager.java`, lines 40–57). Configuration for the persistence unit `nonprofitPU` resides in `META-INF/persistence.xml` and specifies the H2 connection properties and dialect (see `src/main/resources/META-INF/persistence.xml`, lines 1–10).

### Service Layer
`DatabaseService` provides a facade over repository classes so callers can save and load domain models without dealing with `EntityManager` directly (see `src/main/java/nonprofitbookkeeping/persistence/DatabaseService.java`, lines 17–72). It can reconstruct a `Company` with its ledger transactions and offers convenience methods for creating, finding, or deleting companies (lines 73–118).

### Backup and Restore
`DatabaseBackupService` issues `SCRIPT` and `RUNSCRIPT` commands through a JDBC connection to create or restore SQL dump files, enabling offline backups of the H2 store (see `src/main/java/nonprofitbookkeeping/persistence/DatabaseBackupService.java`, lines 1–32).

## 3. Company Files (`.npbk`)
Persistent company data can also be stored outside the database as a zipped file with the extension `.npbk`. `JacksonDataStorer` handles reading and writing these archives. Each `.npbk` file contains a single entry named `company_data.json` which is (de)serialized via Jackson (see `src/main/java/nonprofitbookkeeping/core/JacksonDataStorer.java`, lines 35–153).

## 4. Chart of Accounts Files
The `ChartOfAccountsIOService` reads and writes the chart of accounts either as JSON or Excel workbooks. The JSON format is a simple document with a `_schemaVersion` field and a `rootAccounts` array, produced using a preconfigured Jackson `ObjectMapper` (see `src/main/java/nonprofitbookkeeping/service/ChartOfAccountsIOService.java`, lines 31–67). Export and import helpers convert between the in-memory model and either `.json` or `.xlsx` files for interoperability with external accounting tools (lines 69–119).

## 5. Summary of Input/Output Artifacts

- **`./data/nonprofit`**  
  Format: H2 database files  
  Purpose: Primary relational store managed by Hibernate  

- **`*.npbk`**  
  Format: Zip containing `company_data.json`  
  Purpose: Portable company snapshots saved or loaded by the application  

- **`chart_of_accounts.json`**  
  Format: JSON  
  Purpose: Exported or imported chart of accounts structure  

- **`chart_of_accounts.xlsx`**  
  Format: Excel workbook  
  Purpose: Alternative spreadsheet representation of the chart of accounts  

These components together allow the application to persist data locally, exchange information with other systems, and maintain backups for recovery scenarios.
