Issues:

A. ~~Journal Panel:~~
   - The panel now reloads automatically when a company is opened so entries
     appear right away.
   - The single date text field was replaced with start and end
     {@code DatePicker}s to filter by a range.

B. Open Budget Editor:
1. ~~Gives error "The current company has not been saved to a file yet". This is incorrect.~~
   - Resolved by keeping the company's file reference in sync when loading and falling back to the global current file.
2. Further what is the plan for implementation of a correct budget editor.
   - **Plan:** Continue migrating the Swing based `BudgetPanel` to the newer JavaFX `BudgetPanelFX`.  Persist budgets using `BudgetService` in the company's directory and support editing budget lines through `BudgetLineDialogFX`.
3. **Progress:** `BudgetPanelFX` now launches from the main menu and can save budgets through `BudgetService`. Work continues on editing lines and refining the UI.

C. ~~Save company panel needs to give a choice of NO. Currently it only says Yes, which means it is irrelevant.~~
   - Resolved by offering **Yes**, **No**, and **Cancel** when closing a company file.

D. Reports Panel
1. ~~Income Statement fails (generates invalid xlsx)~~
   - Fixed by replacing the plain text templates with real XLSX files that JXLS
     can process.
2. ~~Balance Sheet fails (generates invalid xlsx)~~
   - Same fix as above; generates a proper workbook now.
3. ~~Trial Balance fails (generates invalid xlsx)~~
4. ~~Cash Flow Statement fails (generates invalid xlsx)~~

E. ~~Account Details panel fails (Account chooser says No accounts in COA, which is wrong).~~
   - Fixed by refreshing the account selector whenever the company changes.

F. Inventory panel. The inventory item is not correctly saved to file store
   - Added JSON persistence to `InventoryService` and panel now saves items to `inventory.json`.

G. ~~Funds panel. The Fund item is not correctly saved to file store.~~
   - Implemented JSON persistence in `FundAccountingService` and updated
     `FundsPanelFX` to load and save `funds.json` in the company directory.

H. ~~Reconciliation panel is entirely a dummy panel. Does not work at all.~~
   - Implemented an in-memory `ReconciliationService` that loads ledger
     transactions for cash accounts and allows marking them as cleared.

I. ~~Show Reports panel. All the report types fail to generate.~~
   - Generate Report dialog now calls `ReportService.generateJasperReport` for
     Income Statement, Balance Sheet, Cash Flow, and Trial Balance reports.
     Unsupported types warn the user.

J. ~~Show Accounts panel. Does not show any accounts.~~
   - Fixed `AccountsPanelFX` so it properly populates the table when a
     company is open. Rows are no longer cleared immediately after
     loading and the buttons disable when no company is loaded.

K. ~~Reports > Account Activity > Reconcile does nothing~~
   - The Reconcile button in the Account Activity panel now opens the
     Ledger Reconcile panel with the selected account pre-populated.

L. ~~Reports > Account Activity > Import Statement does nothing~~
   - Import Statement now opens a file chooser, parses OFX/QIF files with
     `FileImportService`, and adds the transactions to the ledger.

M. ~~Reports > Generate (any). None of the reports types do anything.~~
   - Added a general **Generate Reports...** menu item that launches
     `GenerateReportsAction`. This action now prompts for the report type,
     date range, and output format then invokes `ReportService.generateJasperReport`
     to create the chosen report.

N. Reports > Managed Saved Reports states "Company context not set for managing reports"
   - Fixed by checking `CurrentCompany.isOpen()` and falling back to the global
     company file reference when opening the Manage Saved Reports dialog.
     The menu now opens the dialog as long as a company is loaded and saved,
     otherwise it prompts the user to load or save a company first.

O. ~~Panels > Donors is only sample data. Donor data is not saved.~~
   - Added `DonorContact` model and updated `DonorService` to persist `donors.json` in each company directory.
   - `DonorsPanelFX` now loads and saves donor contacts automatically when changes are made.

P. ~~Panels > Grants has no way to add or save data.~~
   - Implemented JSON persistence in `GrantsService` and updated
     `GrantsPanelFX` to add/edit/delete grants. Grant data is now
     stored as `grants.json` inside the `.npbk` company zip file.

Q. ~~Panels > Sales & COG does nothing. Data is not saved~~
   - Added `SalesService` with JSON persistence and updated
     `SalesAndCOGPanelFX` to load and save `sales.json` in the
     company directory.

R. ~~Settings > Company Info > Organization Name is duplicate,
1. Fiscal Year start is not used (not real data - it needs to be saved as a variable)
2. Default Currency is not used.
3. Users are not used.
4. Accounting > Default income and Expense accounts should refer to the Chart of accounts and be saved.
5. Auto-Number vouchers is irrelevant.~~
   - Introduced `SettingsService` that persists `settings.json` per company directory.
   - `SettingsPanelFX` now loads and saves organization info, fiscal year start, currency,
     users, and default accounts.
   - Default account selectors populate from the current chart of accounts and the
     unused auto-number option was removed.

S. ~~UI preferences: Implement actual theme selection.~~
   - Settings now persist the chosen theme and applying it updates all scenes via `ThemeManager`.



