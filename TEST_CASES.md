# Test Cases: COA Import/Export and State Management

## I. COA XLSX Export Functionality Tests

1.  **Test Case EX-01: Export COA from Active Company**
    *   **Setup:** Open an existing company file that has a defined Chart of Accounts.
    *   **Action:**
        1.  Navigate to "File" -> "Export COA (XLSX)".
        2.  Select a valid location and filename (e.g., `coa_export_test.xlsx`).
        3.  Save the file.
    *   **Expected Result:**
        1.  An information alert confirms successful export.
        2.  The XLSX file is created at the specified location.
        3.  The content of the workbook accurately represents the company's COA structure and data (manual inspection of the spreadsheet would be needed).

2.  **Test Case EX-02: Attempt Export with No Company Open**
    *   **Setup:** Ensure no company file is open.
    *   **Action:** Navigate to "File" menu.
    *   **Expected Result:** The "Export COA (XLSX)" menu item should be disabled.

3.  **Test Case EX-03: Attempt Export with Company Open but No COA**
    *   **Setup:** Open/create a company that explicitly has no Chart of Accounts (if this state is possible).
    *   **Action:**
        1.  Navigate to "File" -> "Export COA (XLSX)".
    *   **Expected Result:**
        1.  An alert (WARNING/ERROR) should appear, stating "No Chart of Accounts to export" or similar.
        2.  No file should be created.

4.  **Test Case EX-04: Export Empty COA**
    *   **Setup:** Open/create a company and ensure its COA is empty (no accounts).
    *   **Action:**
        1.  Navigate to "File" -> "Export COA (XLSX)".
        2.  Save the file.
    *   **Expected Result:**
        1.  Successful export message.
        2.  XLSX file is created, representing an empty COA (e.g., with only headers and no rows).

## II. COA XLSX Import Functionality Tests

1.  **Test Case IM-01: Import COA into Active Company (Overwrite/Replace)**
    *   **Setup:**
        1.  Have a valid COA XLSX file ready (e.g., `coa_export_test.xlsx` from EX-01, or a new well-formed one).
        2.  Open an existing company file (it may or may not have an existing COA).
    *   **Action:**
        1.  Navigate to "File" -> "Import COA (XLSX)".
        2.  Select the prepared COA XLSX file.
    *   **Expected Result:**
        1.  An information alert confirms successful import.
        2.  The application's current company data now reflects the imported COA. (Verify by checking COA display/editor).
        3.  If there was an existing COA, it is replaced by the imported one.

2.  **Test Case IM-02: Attempt Import with No Company Open**
    *   **Setup:** Ensure no company file is open.
    *   **Action:** Navigate to "File" menu.
    *   **Expected Result:** The "Import COA (XLSX)" menu item should be disabled.

3.  **Test Case IM-03: Import Malformed/Invalid XLSX File**
    *   **Setup:**
        1.  Create an XLSX file with invalid or missing data, save as `invalid_coa.xlsx`.
        2.  Open an existing company file.
    *   **Action:**
        1.  Navigate to "File" -> "Import COA (XLSX)".
        2.  Select `invalid_coa.xlsx`.
    *   **Expected Result:**
        1.  An ERROR alert appears, stating "Error importing Chart of Accounts" or similar, possibly with an error detail.
        2.  The existing COA (if any) in the company remains unchanged.

4.  **Test Case IM-04: Import Valid XLSX that is not a COA structure**
    *   **Setup:**
        1.  Create a valid XLSX file that does not conform to the COA structure. Save as `not_a_coa.xlsx`.
        2.  Open an existing company file.
    *   **Action:**
        1.  Navigate to "File" -> "Import COA (XLSX)".
        2.  Select `not_a_coa.xlsx`.
    *   **Expected Result:**
        1.  An ERROR alert appears, indicating a format mismatch or parsing error.
        2.  The existing COA (if any) remains unchanged.

## III. State Management and General UI Tests

1.  **Test Case SM-01: Menu Item States - No Company Open**
    *   **Setup:** Start the application. Ensure no company is open.
    *   **Action:** Observe the "File" menu.
    *   **Expected Result:**
        *   "Open Company File": Enabled
        *   "Import COA (XLSX)": Disabled
        *   "Export COA (XLSX)": Disabled
        *   "Close Company File": Disabled
        *   "Save Company File": Disabled
        *   Other relevant items like "Edit Chart of Accounts", "Edit Journal" should also be disabled.

2.  **Test Case SM-02: Menu Item States - Company Open**
    *   **Setup:** Open a company file.
    *   **Action:** Observe the "File" menu and "Edit" menu.
    *   **Expected Result:**
        *   "Open Company File": Disabled
        *   "Import COA (XLSX)": Enabled
        *   "Export COA (XLSX)": Enabled
        *   "Close Company File": Enabled
        *   "Save Company File": Enabled
        *   "Edit Chart of Accounts": Enabled
        *   "Edit Journal": Enabled

3.  **Test Case SM-03: Menu Item States - During Company Creation**
    *   **Setup:** Start creating a new company (e.g., "File" -> "Create or Edit Company", assuming this path exists and leads to a "creating" state).
    *   **Action:** Observe the "File" menu.
    *   **Expected Result:**
        *   "Open Company File": Disabled
        *   "Import COA (XLSX)": Disabled
        *   "Export COA (XLSX)": Disabled
        *   "Close Company File": Disabled
        *   "Save Company File": Disabled

4.  **Test Case SM-04: Regression Test for Old StateMachine Removal**
    *   **Setup:** Perform various standard operations: open company, close company, navigate different panels.
    *   **Action:** Observe overall application stability and menu item behavior.
    *   **Expected Result:** Application functions correctly. No unexpected errors or UI freezes. Menu items managed by `NonprofitBookkeepingFX.setState()` behave as defined. This implicitly tests that the removal of the old Swing `StateMachine` had no adverse effects.

## Files to examine for test data preparation/verification (examples):
- Any existing company files with COA.
- XLSX files created by the export function.
- Manually crafted XLSX files for import testing (valid, invalid, empty).
