# H2 Adoption Review Findings

## 1. Modernize Menus and Save/Close Actions
* **Issue:** Main menu labels and action classes still imply a file-based workflow, which conflicts with the H2-centric persistence model.
* **Key Locations:**
  * `src/main/java/nonprofitbookkeeping/ui/NonprofitBookkeepingFX.java` (lines 265-305)
  * `src/main/java/nonprofitbookkeeping/ui/actions/SaveCompanyFileAction.java` (lines 21-55)
  * `src/main/java/nonprofitbookkeeping/ui/actions/CloseCompanyFileAction.java` (lines 17-69)
* **Recommended Steps:**
  1. Rename the File menu to focus on database operations and remove obsolete import/export entries.
  2. Gate menu enablement on both `Database.isInitialized()` and `CurrentCompany.isOpen()`.
  3. Update the save/close actions and prompts to drop the "file" terminology while continuing to call `CurrentCompany.persist()` and `close()`.

## 2. Guide Users When the Database or Company Is Unavailable
* **Issue:** Company actions remain usable even if the database is not initialized, and the UI offers little guidance when no company is open.
* **Key Locations:**
  * `src/main/java/nonprofitbookkeeping/ui/NonprofitBookkeepingFX.java` (lines 563-708)
  * `src/main/java/nonprofitbookkeeping/ui/MainApplicationView.java` (lines 73-219)
* **Recommended Steps:**
  1. Disable company actions until the database is ready and alert users if they attempt to open a company prematurely.
  2. Automatically launch the company selector once the database starts.
  3. Replace the main tab content with `CompanySelectionPanelFX` when no company is active and restore the tabs when one is opened.

## 3. Offer Demo Seeding During Company Creation
* **Issue:** New companies store profile metadata only; they lack default accounts and transactions for H2 demos.
* **Key Locations:**
  * `src/main/java/nonprofitbookkeeping/ui/actions/CreateOrEditCompanyActionFX.java` (lines 63-88)
  * `src/main/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFX.java` (lines 35-289)
* **Recommended Steps:**
  1. Add a `DemoCompanySeeder` service that loads standard accounts and sample ledger entries.
  2. Extend the creation wizard with an option to populate demo data and return the choice through its callback.
  3. Seed the defaults before persisting new companies via `CreateOrEditCompanyActionFX`.

## 4. Upgrade the Company Selection Experience
* **Issue:** The selection panel lacks shortcuts for demo companies and does not automatically refresh after changes.
* **Key Locations:** `src/main/java/nonprofitbookkeeping/ui/panels/CompanySelectionPanelFX.java` (lines 48-221)
* **Recommended Steps:**
  1. Provide a "Create Demo Company" action that inserts a seeded record and refreshes the list.
  2. Trigger `reloadCompanyList()` after any create/import/delete action so the UI stays in sync.
  3. Surface additional metadata (e.g., last updated) in list cells to reinforce the database-backed workflow.

## 5. Align Settings and Backups with H2 Storage
* **Issue:** Settings and backup panels still assume filesystem directories and `.npbk` archives.
* **Key Locations:**
  * `src/main/java/nonprofitbookkeeping/ui/panels/SettingsPanelFX.java` (lines 51-116)
  * `src/main/java/nonprofitbookkeeping/ui/panels/SettingsPanelFX.java` (lines 280-343)
* **Recommended Steps:**
  1. Use `SettingsService` and database identifiers instead of filesystem paths.
  2. Replace legacy backup/restore with H2-native `SCRIPT TO` / `RUNSCRIPT FROM` workflows.
  3. Update labels and dialogs to reflect database-driven operations.

## Testing
* Not run (review-only assessment).
