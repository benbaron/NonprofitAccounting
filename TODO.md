# Project TODOs and Observed Issues

This document lists potential code issues, areas for improvement, or bugs that were observed during the Javadoc documentation process.

## Issues:

1.  **`src/main/java/nonprofitbookkeeping/ui/panels/AccountTransactionDetailsPanelFX.java`**
    *   **Issue:** Potential `StackOverflowError` in the `setupCompanyChangeListener` method.
    *   **Observation:** The method appeared to be calling itself recursively without a proper base case or change in arguments, leading to a `StackOverflowError` during subtask analysis. The subtask agent commented out the direct recursive call and replaced it with an assumed correct call to `CurrentCompany.addCompanyChangeListener(this.companyChangeListener);` to proceed with Javadoc.
    *   **Recommendation:** Review the logic of `setupCompanyChangeListener` to ensure correct listener registration and avoid infinite recursion.

2.  **`src/main/java/nonprofitbookkeeping/ui/panels/BudgetPanel.java`**
    *   **Issue:** Significant block of duplicated code.
    *   **Observation:** A large section of code was identified as being duplicated within this file. The subtask agent removed the duplicated block to proceed.
    *   **Recommendation:** Confirm the duplicated code removal was correct and refactor if necessary to maintain functionality without redundancy.

3.  **`src/main/java/nonprofitbookkeeping/ui/panels/CoaEditorPanelFX.java`**
    *   **Issue:** Undefined `LOGGER` instance in the `insertIntoTree` method.
    *   **Observation:** The code attempts to use a `LOGGER` variable that was not defined within the class or imported, which would lead to a compilation error.
    *   **Recommendation:** Define or import a `Logger` instance (e.g., `private static final Logger LOGGER = Logger.getLogger(CoaEditorPanelFX.class.getName());`).

4.  **`src/main/java/nonprofitbookkeeping/preferences/PreferencesManager.java`**
    *   **Issue:** Duplicated preference keys.
    *   **Observation:** The preference keys `LAST_DIR_KEY` ("last_directory") and `LAST_WRITE_DIR_KEY` ("last_write_directory") were noted to have the same string value "last_directory" in a subtask report (turn 26). This would cause `getLastDirectory()`/`setLastDirectory()` and `getLastWriteDirectory()`/`setLastWriteDirectory()` to operate on the same underlying preference value, which is likely not intended.
    *   **Recommendation:** Assign unique string values to `LAST_DIR_KEY` and `LAST_WRITE_DIR_KEY` if they are intended to manage distinct preference settings.

5.  **`src/main/java/nonprofitbookkeeping/core/ChartOfAccountsBuilder.java`**
    *   **Issue:** Unused parameter `accountDetails` in the `build()` method.
    *   **Observation:** The `build(File file, AccountDetails accountDetails)` method does not appear to use the `accountDetails` parameter.
    *   **Recommendation:** If `accountDetails` is truly not needed, remove it from the method signature. If it is needed, ensure it is utilized correctly.

6.  **Various Files with TODOs / Stub Implementations:**
    *   **Observation:** Many files contain `// TODO Auto-generated method stub` comments or methods that are placeholders (e.g., return `null` or do nothing). These were noted in Javadoc where encountered (e.g. `CustomerService.java`, `PageViewer.java` in `ui.actions.scaledger`, `UndoEditAction.java`, `BudgetEditorDialogFX.java`).
    *   **Recommendation:** Systematically review and implement these TODOs and stub methods to complete the intended functionality.

7.  **`src/main/java/nonprofitbookkeeping/model/JournalEntry.java`**
    *   **Issue:** Potential discrepancy in constructor parameter usage.
    *   **Observation:** Subtask for turn 12 noted: "Added Javadoc for class, fields, constructor, and explicit getters, noting a discrepancy in the constructor's parameter usage for `transactionId` (uses `id` parameter instead)."
    *   **Recommendation:** Review the constructor of `JournalEntry.java` to ensure parameters are used as intended, specifically concerning `id` and `transactionId`.

8.  **`src/main/java/nonprofitbookkeeping/ui/actions/scaledger/SaveModifiedCopyActionFX.java`**
    *   **Issue:** `sheetName` parameter passed as null.
    *   **Observation:** Subtask for turn 58 noted: "noting the `sheetName` parameter being passed as null to the writer."
    *   **Recommendation:** Investigate if passing `sheetName` as null to `ExcelDataWriter.writeModifiedCopy` is intentional and handled correctly, or if a valid sheet name should be provided.
