# Project Overview: Nonprofit Bookkeeping Application

## 1. Core Purpose
The project is a desktop application designed for nonprofit bookkeeping. It aims to provide essential accounting functionalities, including managing a chart of accounts, recording journal entries, handling financial transactions, generating financial reports, managing inventory, and potentially other nonprofit-specific modules like fund accounting, donor/donation tracking, and budget management.

## 2. Technology Stack (Inferred)
*   **Language:** Java
*   **User Interface:** JavaFX (for the main application and newer UI components) with some existing Swing components (e.g., the original `BudgetPanel`).
*   **Reporting:**
    *   Historically used JXLS (with `.xlsx` templates) for some reports, driven by `ReportService.java`.
    *   Current initiative: Migrate to and expand reporting using JasperReports (with `.jrxml` templates), managed via an `AbstractReportGenerator` framework.
*   **Build Tool:** Apache Maven (`pom.xml`).
*   **Key Libraries:** Apache POI, Jackson, Lombok, SLF4J (with Logback/Log4j2), OpenJFX, JasperReports, Guava, OFX4J.

## 3. Key Functionalities & Modules
*   **Company Management:** Creating, opening, saving company files. `CurrentCompany` provides access to active company data.
*   **Chart of Accounts (COA):** Hierarchical account management.
*   **Journal Entries & Ledger:** Recording and storing financial transactions.
*   **Dashboard:** Summary view (key figures, recent transactions).
*   **Reporting:** Generation of financial reports (transitioning to JasperReports).
*   **Budgeting:** Creating and managing budgets (Swing panel, planned for JavaFX conversion).
*   **Inventory Management:** Tracking items and depreciation.
*   **Bank Reconciliation.**
*   **Account Activity Display:** Detailed transaction views for accounts.
*   **Plugin System:** Supports extending functionality.

## 4. UI Structure (Evolving)
*   Main application window (`NonprofitBookkeepingFX`) uses `MainApplicationView`.
*   `MainApplicationView` uses a `TabPane` for primary navigation (Dashboard, Journal, COA, Reports, Account Details).
*   Some functionalities use separate dialogs/stages.
*   Panels observe company open/close events by registering a
    `CurrentCompany.CompanyChangeListener` with
    `CurrentCompany.CompanyListener.addCompanyListener(...)`. Panels should
    unregister via `removeCompanyListener(...)` when destroyed so their UI
    updates correctly and listeners do not leak.

## 5. Code Documentation and Quality Tracking

### 5.1. Javadoc Documentation
As of a recent project-wide effort, a comprehensive pass was made to add Javadoc documentation to all Java source files across all packages in the project. This includes documentation for classes, methods, fields, enums, and constructors, aiming to improve code maintainability and understanding.

### 5.2. TODO List for Observed Issues
A `TODO.md` file has been added to the root directory of the project. This file catalogs observed code issues, potential bugs, and areas for improvement that were identified during the Javadoc documentation effort and other development activities. It serves as a supplementary tracking document for items not yet in a formal issue tracker or for quick notes on code quality aspects. Refer to `TODO.md` for specific details.

---

### Outstanding UI Issues (Observed)

The following user-facing problems have been noted in the current JavaFX UI implementation.  These items remain to be addressed in code:

1. **Select Company Tab** – now loads the full selection panel; it should close automatically once a company is chosen.
2. **Dashboard Totals** – summary totals should reflect live ledger data; the existing labels do not refresh with current values.
3. **Journal Transactions** – a created transaction doesn't show the Debit/Credit entries.
4. **Journal Filters** – filtering controls are not functional; the date filter should use a `DatePicker`.
5. **Edit Transaction UI** – row highlighting makes selection difficult; focus should remain on the active field only.
6. **Edit Transaction Layout** – the date chooser and memo should appear as columns within the entry table, not as separate controls.
7. **Debit/Credit Selection** – the side of an entry should default from the account’s natural side rather than an explicit user choice.
8. **Generate Report Action** – menu actions should invoke JasperReports generators but currently do nothing.
9. **Account Details Filtering** – fails to display journal entries even with valid ranges and should default to all entries when no range is set.
10. **Reconcile Panel** – selecting the menu option should open `ReconcilePanelFX` but presently does not.
11. **Save Company Dialog** – needs explicit `OK`, `No`, and `Cancel` buttons.
12. **Budget Editor Access** – opening the budget editor fails even when a company file is loaded.
13. **Settings Panel** – all values are placeholders; real getters/setters must be implemented.


## Current Major Task & Objectives: Enhancing Reporting with JasperReports

The primary focus is to **standardize and expand the application's reporting capabilities by fully adopting JasperReports and moving away from JXLS.**

### Specific Current Goals:

1.  **Establish a Robust JasperReports Framework:**
    *   Utilize and refine `AbstractReportGenerator.java` as the base.
    *   Ensure correct lifecycle: `.jrxml` loading/compilation, `JRDataSource` preparation (typically `JRBeanCollectionDataSource`), parameter setting, report filling, exporting (PDF, HTML, etc.).
    *   **Status (as of last work session):** `AbstractReportGenerator` refactored to have `generateAndExportReport` return `File`. Export helpers also return `File`. Concrete generators (`IncomeStatementJasperGenerator`, `CashFlowStatementJasperGenerator`, `BalanceResultReportGenerator`) updated for API compliance. `ReportService.generateJasperReport` dispatcher updated to use these `File` returns.

2.  **Implement Specific Financial Reports using JasperReports:**
    *   **Income Statement:**
        *   JRXML: `income_statement.jrxml` created (basic).
        *   Data Bean: `IncomeStatementRowBean.java` created.
        *   Data Prep: `ReportService.prepareIncomeStatementJasperData` created.
        *   Generator: `IncomeStatementJasperGenerator.java` created.
        *   **Status:** Backend largely done.
    *   **Cash Flow Statement:**
        *   JRXML: `cash_flow_statement.jrxml` created (basic).
        *   Data Bean: `CashFlowStatementRowBean.java` created.
        *   Data Prep: `ReportService.prepareCashFlowStatementJasperData` created.
        *   Generator: `CashFlowStatementJasperGenerator.java` created.
        *   **Status:** Backend largely done.
    *   **Trial Balance:**
        *   JRXML: `TrialBalanceReport.jrxml` (pre-existing).
        *   Data Bean: `TrialBalanceRowBean.java` created.
        *   Data Prep: `ReportService.prepareTrialBalanceJasperData` created.
        *   Generator: `TrialBalanceJasperGenerator.java` created.
        *   **Status:** Backend largely done. UI integration (dispatcher, panel trigger) was the next immediate step.
    *   **Balance Sheet:** (from `BalanceResultReport.jrxml` or new)
        *   JRXML: `BalanceResultReport.jrxml` (pre-existing). Generator `BalanceResultReportGenerator.java` made API compliant.
        *   **Status:** Needs full Jasper refactor for data logic (live balances) and potentially new JRXML/generator.
    *   **Chart of Accounts Report:**
        *   JRXML: `ChartOfAccountsReport.jrxml` (pre-existing).
        *   **Status:** Needs data bean, data prep, generator.
    *   **New Reports Requested:** Specific Account Detail (Printable), Inventory and Depreciation, Funds Report, Bank Reconciliation Report.
        *   **Status:** Require new JRXMLs, data beans, data prep, and generators.

3.  **Integrate JasperReport Generation into the UI:**
    *   Modify `SkeletonReportsPanelFX` to trigger JasperReports via `ReportService.generateJasperReport`.
    *   **Status:** Done for Income Statement and Cash Flow Statement. Trial Balance was next.

4.  **Data Accuracy:**
    *   Ensure report data uses live, calculated balances.
    *   **Status:** Addressed in new data prep methods. `BalanceResultReportGenerator` still needs this for its data.

### Branching Information (Last Understood State):
*   Initial Jasper setup (JRXMLs, framework review) was committed to `jasper-reporting-setup`.
*   Subsequent work (AbstractReportGenerator refactor, IS/CFS/TB backends) was intended for `new-reporting-branch`.
    *   *User Note: There was significant confusion regarding the actual branch the last commit landed on. The user observed it on `skeleton-ui-workflow-demo` according to their UI, but the agent's `submit()` call specified `new-reporting-branch`. This needs to be rectified/verified by the user in their Git environment.*

## Recent Work: General Journal Entry Panel

The latest code introduces a reimagined JavaFX workspace named
`JournalEntryWorkspaceFX` (still surfaced publicly through
`GeneralJournalEntryPanelFX`). It provides a unified experience for entering or
editing multi-line journal transactions. The new layout combines a line-entry
table, contextual metadata form, and a status footer that tracks debit, credit
and difference totals in real time. Save is prevented until the workspace is
balanced and any validation issue is highlighted inline. It is used for creating
and editing transactions within both `JournalPanelFX` and
`SkeletonJournalPanel`.

### Next Steps

* Improve validation and error handling when accounts are missing or amounts are
  invalid.
* Integrate the new panel fully into the edit workflow so existing journal
  transactions can be modified with the same interface.
* Resolve the Maven plugin resolution failure to enable unit test execution and
  verify functionality automatically.

---
