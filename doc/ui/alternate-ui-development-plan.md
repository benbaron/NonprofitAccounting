# Alternate UI Development Plan

This document defines a development plan for completing the `org.nonprofitbookkeeping.ui` alternate JavaFX user interface. The alternate UI should become the long-term shell and workspace style for the application, while functionality that currently exists only in the older `nonprofitbookkeeping.ui.panels` interface should be migrated into native alternate-style panels over time.

The plan is organized as a checklist of executable steps. Each step includes a ready-to-use prompt for a GPT/Codex-style coding assistant. The prompts assume the assistant has repository access and can edit files.

## Goals

- Make `org.nonprofitbookkeeping.ui` the primary shell/workspace layer.
- Replace fake/demo data with real service-backed data or explicit empty states.
- Migrate old-interface-only functionality into native alternate-style panels.
- Keep legacy panels as temporary adapters only where needed.
- Provide a consistent panel lifecycle, command model, styling system, and context model.
- Preserve accounting correctness: double-entry balance, fund/restriction reporting, reconciliation integrity, audit trail, and report reproducibility.

## Non-goals

- Do not delete the older `nonprofitbookkeeping.ui.panels` implementation until all required functionality has a native alternate-style equivalent.
- Do not hide incomplete functionality behind realistic-looking demo data.
- Do not make UI-only changes that imply persistence when no model/service persistence exists.
- Do not add new accounting behavior directly inside JavaFX panels when it belongs in a service.

## Definitions

- **Alternate UI**: Code under `src/main/java/org/nonprofitbookkeeping/ui` and `src/main/java/org/nonprofitbookkeeping/ui/panels`.
- **Old UI / legacy panels**: Code under `src/main/java/nonprofitbookkeeping/ui/panels` and related `nonprofitbookkeeping.ui` Swing/JavaFX compatibility code.
- **Native alternate panel**: Implements `org.nonprofitbookkeeping.ui.AppPanel` and follows the alternate shell style/lifecycle.
- **Adapted legacy panel**: A wrapper that exposes an old panel inside the alternate shell.
- **Migration complete**: The alternate UI contains equivalent or better functionality, with service-backed persistence and tests where appropriate.

## Current high-level state

The alternate UI has a promising shell: `MainWindowAlternate`, `PanelHost`, `NavigationPane`, `WorkspaceRouter`, `AppPanel`, and `AppPanelId` create a more modern workspace-oriented frame. However, many panels are either placeholders, hardcoded demos, or adapters to older panels. The immediate development priority is to make this truthful, service-backed, and consistent.

## Architecture principles

1. The alternate shell owns navigation, command routing, panel hosting, and workspace layout.
2. Accounting behavior belongs in services, not JavaFX panels.
3. Panels may call services through a context-aware registry or injected service factory.
4. The active database/company context must have one source of truth.
5. Demo data is allowed only in tests or clearly marked sample fixtures, never in production UI paths.
6. Old panels may remain temporarily, but every adapter must have a migration target and exit criterion.
7. Every panel must expose honest command states: available, disabled with reason, or not implemented.
8. Reconciliation, journal deletion, fund movement, and depreciation must preserve auditability.

---

# Phase 0 — Inventory, classification, and guardrails

## 0.1 Create an alternate UI migration inventory

### Checklist

- [ ] Add a migration inventory document or table under `doc/ui/`.
- [ ] List each `AppPanelId`.
- [ ] Identify current implementation type: native, legacy adapter, placeholder, demo-only, or mixed.
- [ ] Identify equivalent old-interface functionality, if any.
- [ ] Identify migration target class name.
- [ ] Identify required services.
- [ ] Identify whether real persistence exists.
- [ ] Identify tests to add.

### Suggested inventory columns

| AppPanelId | Current class | Current status | Old UI source | Missing old functionality | Target native panel | Required services | Test status |
|---|---|---|---|---|---|---|---|
| DASHBOARD | DashboardPanelFX / MainWindowAlternate cards | Mixed / partly real, partly hardcoded | DashboardPanelFX old panel | Summary cards, account activity, filters | AlternateDashboardPanel | DashboardDataBridge, journal/fund services | Needed |
| LEDGER_REGISTER | LedgerRegisterPanel | Demo-only | JournalPanelFX, AccountsActivityPanelFX, GeneralJournalEntryPanelFX, JournalEntryWorkspaceFX | Real transaction list, entry detail, editor, import, document links | AlternateLedgerRegisterPanel | Journal service, transaction query, document service | Needed |
| CHART_OF_ACCOUNTS | ChartOfAccountsPanel | Read-only service-backed | AccountsPanelFX / CoaEditorPanelFX | Add/edit/delete/deactivate, validation | AlternateChartOfAccountsPanel | AccountLookupService, AccountMaintenanceService | Needed |
| FUNDS | FundsPanel adapter | Legacy adapter | FundsPanelFX | Native fund balances, transfers, reclassification | AlternateFundsPanel | Fund services, journal service | Needed |
| INVENTORY | InventoryPanel adapter | Legacy adapter | InventoryPanelFX | Native asset/inventory management | AlternateInventoryPanel | InventoryService, asset service | Needed |
| ASSETS_REGISTER | AssetsRegisterPanel | Demo-only | InventoryPanelFX, DepreciationRunsPanel | Real asset register | AlternateAssetsRegisterPanel | asset/depreciation services | Needed |
| DEPRECIATION_RUNS | DepreciationRunsPanel | Mostly service-backed | InventoryPanelFX depreciation flow | Improve workflow, connect to asset register | AlternateDepreciationRunsPanel | Depreciation services | Partial |
| BUDGET_EDITOR | BudgetEditorPanel / custom pane | Demo-only / mixed | Budget panels, GenerateReportPanelFX report context | Real budget model and persistence | AlternateBudgetEditorPanel | BudgetService | Needed |
| BUDGET_VS_ACTUAL | BudgetVsActualPanel | Demo-only | ReportsPanelFX / budget report writers | Real report data and drilldown | AlternateBudgetVsActualPanel | Budget/report services | Needed |
| REPORTS_WORKSPACE | ReportLibraryPanel adapter | Legacy adapter | ReportsPanelFX, GenerateReportPanelFX, report writers | Native catalog, generation, open history | AlternateReportsWorkspacePanel | ReportService, semantic report services | Needed |
| SCHEDULES | SchedulesPanel | Gating skeleton | Outstanding detail/schedule concepts | Real schedule grids | AlternateSchedulesPanel | Schedule services | Needed |
| SETTINGS | SettingsPanel / alternate settings pane | Placeholder | SettingsPanelFX | Company/app/report settings | AlternateSettingsPanel | Preferences/settings service | Needed |

### GPT execution prompt

```text
Inspect the repository and create or update `doc/ui/alternate-ui-migration-inventory.md`.

List every `AppPanelId` in `src/main/java/org/nonprofitbookkeeping/ui/AppPanelId.java`.
For each panel, identify the current class used by `PanelHost.DefaultPanelFactory` or `MainWindowAlternate`, whether it is native alternate UI, legacy adapter, placeholder, demo-only, or mixed, and the old-interface functionality that must be migrated from `src/main/java/nonprofitbookkeeping/ui/panels`.

Do not change Java code in this step. Produce only the inventory document.
```

## 0.2 Add a no-demo-data policy for production UI

### Checklist

- [ ] Search alternate UI for hardcoded business sample data.
- [ ] Replace production demo values with explicit empty states where no service exists.
- [ ] Add comments for any temporary placeholders.
- [ ] Ensure dashboard, budget, ledger, and assets do not display realistic fake values.

### Known problem areas

- Hardcoded dashboard cards in `MainWindowAlternate`.
- Hardcoded rows in `LedgerRegisterPanel`.
- Hardcoded rows in `BudgetEditorPanel`.
- Hardcoded report tree in `BudgetVsActualPanel`.
- Hardcoded rows in `AssetsRegisterPanel`.
- Fallback demo accounts in `SchedulesPanel`.

### GPT execution prompt

```text
Remove or clearly quarantine production-path demo data from the alternate UI.

Search `src/main/java/org/nonprofitbookkeeping/ui` for hardcoded realistic accounting values, sample payees, sample budget rows, sample asset rows, and fallback demo accounts. Replace them with explicit empty/error/loading states unless a real service-backed query already exists.

Do not remove unit-test fixtures. Do not add new accounting services yet. Where functionality is not implemented, display a clear message such as `No service-backed data source is wired for this panel yet.`

Update or add tests that verify demo rows are not inserted by default in production UI classes.
```

---

# Phase 1 — Context, services, lifecycle, and shell consistency

## 1.1 Introduce a single UI session context

### Problem

The alternate shell uses database/company context logic, while older code also uses `CurrentCompany` and static service registries. This risks multiple active-company sources of truth.

### Checklist

- [ ] Create `UiSessionContext` under `org.nonprofitbookkeeping.ui` or `org.nonprofitbookkeeping.ui.context`.
- [ ] Track active database path/base path.
- [ ] Track active company ID and display name.
- [ ] Expose observable properties for JavaFX binding.
- [ ] Expose context state: no database, database open/no company, company open.
- [ ] Provide methods to clear context when database/company closes.
- [ ] Make `MainWindowAlternate` read header/nav state from this context.
- [ ] Add a legacy bridge only where old panels need `CurrentCompany`.

### GPT execution prompt

```text
Create a `UiSessionContext` for the alternate JavaFX UI.

Requirements:
- Place it under `src/main/java/org/nonprofitbookkeeping/ui` or a suitable subpackage.
- It must track active database base path, active company id, active company display label, and state flags for database/company open.
- Use JavaFX properties where useful so the header and navigation can bind to context changes.
- Refactor `MainWindowAlternate` to use this context as its primary source for header subtitle and navigation enablement.
- Do not remove `CurrentCompany` yet. Add a small compatibility note or helper for legacy panels that still require it.
- Add tests for context state transitions.
```

## 1.2 Refactor `UiServiceRegistry` into a context-bound service provider

### Problem

`UiServiceRegistry` currently creates static services backed by a static `Jpa`. That is fragile when the user can open a different database.

### Checklist

- [ ] Create `UiServiceProvider` or `UiServices` bound to `UiSessionContext`.
- [ ] Defer JPA/service creation until a database context is open.
- [ ] Rebuild services when the database changes.
- [ ] Keep a transitional static registry only as a bridge, marked deprecated.
- [ ] Refactor native alternate panels to obtain services from the provider.
- [ ] Do not refactor legacy adapters in the same step unless required.

### GPT execution prompt

```text
Refactor alternate UI service wiring away from static singleton services.

Create a context-bound `UiServiceProvider` that obtains or constructs services from the active `UiSessionContext`. It should provide account lookup, fund lookup, fund balance, schedule eligibility, dashboard data, and other services currently exposed by `UiServiceRegistry`.

Update native alternate panels to use the provider. Keep `UiServiceRegistry` as a temporary compatibility facade if needed, but mark it deprecated and route it through the context-bound provider where practical.

Add tests for service provider behavior when no database is open, when a database opens, and when the database changes.
```

## 1.3 Strengthen the `AppPanel` lifecycle contract

### Problem

`AppPanel.onSave()` defaults to a no-op, while `PanelHost` may imply data was saved. This is dangerous.

### Checklist

- [ ] Add optional lifecycle interfaces or expand `AppPanel` carefully.
- [ ] Support dirty state.
- [ ] Support save result: saved, no changes, failed, unsupported.
- [ ] Support `onEnter(context)` and `onLeave()` if not too disruptive.
- [ ] Update `PanelHost` to report actual save outcomes.
- [ ] Update `MainWindowAlternate` messages so they do not claim save occurred unless it did.
- [ ] Convert placeholder panels to report `save unsupported`.

### Suggested model

```java
public interface DirtyAwarePanel
{
    boolean isDirty();
}

public interface SaveAwarePanel
{
    SaveResult save();
}

public record SaveResult(Status status, String message)
{
    public enum Status { SAVED, NO_CHANGES, UNSUPPORTED, FAILED }
}
```

### GPT execution prompt

```text
Improve the alternate UI panel lifecycle so save behavior is truthful.

Add a save result model for `AppPanel`/`PanelHost` without breaking existing panel implementations. `PanelHost.saveActive()` should return a result indicating saved, no changes, unsupported, or failed. Update `MainWindowAlternate` so panel switches and command center Save display accurate messages.

Ensure panels with no persistence do not silently pretend to save. Add tests around `PanelHost` save delegation and switching behavior.
```

## 1.4 Create a shared alternate panel scaffold

### Problem

Panels hand-build inconsistent headers, action bars, status labels, separators, and empty states.

### Checklist

- [ ] Create `AlternatePanelScaffold` or `PanelScaffold`.
- [ ] Support title, subtitle/help text, primary actions, secondary actions, filter area, content, status/footer.
- [ ] Support empty/loading/error nodes.
- [ ] Apply consistent padding/style classes.
- [ ] Refactor at least two simple native panels to use it.
- [ ] Add CSS classes rather than inline styles.

### GPT execution prompt

```text
Create a reusable alternate panel scaffold for `org.nonprofitbookkeeping.ui` panels.

The scaffold should provide common title/subtitle, action bar, optional filter bar, content area, and status/footer areas. Use CSS style classes rather than inline styles. Refactor `ChartOfAccountsPanel` and `LedgerRegisterPanel` to use the scaffold without changing their business behavior yet.

Add or update CSS as needed, and add a simple UI/unit test if the project has test infrastructure for JavaFX components.
```

## 1.5 Move alternate shell styling into CSS

### Checklist

- [ ] Identify inline styles in `MainWindowAlternate` and native panels.
- [ ] Create or update alternate UI CSS file.
- [ ] Add classes for shell, icon rail, cards, status labels, panel titles, action bars, workspaces.
- [ ] Preserve current appearance where possible.
- [ ] Add tooltips and accessible text for icon rail buttons.

### GPT execution prompt

```text
Move alternate UI shell styling out of Java inline strings and into CSS.

Refactor `MainWindowAlternate` icon rail, dashboard cards, workspace surface, panel titles, status labels, and action bars to use style classes. Add tooltips and accessible labels to the icon rail buttons.

Do not change functional behavior in this step. Keep the visual appearance close to the current alternate shell.
```

---

# Phase 2 — Shell truthfulness and navigation completion

## 2.1 Replace hardcoded dashboard cards with service-backed dashboard cards

### Checklist

- [ ] Define dashboard snapshot DTOs.
- [ ] Use real fund/account/journal/reconciliation/document services where available.
- [ ] Show empty states for missing data.
- [ ] Provide cards for cash, fund balances, restricted/unrestricted, unreconciled count, undeposited funds, recent transactions, missing documents.
- [ ] Add refresh and date range awareness.
- [ ] Remove literal accounting values from `MainWindowAlternate`.

### GPT execution prompt

```text
Replace the hardcoded dashboard cards in `MainWindowAlternate` with a native service-backed alternate dashboard.

Create `AlternateDashboardPanel` implementing `AppPanel` or a reusable Node used by the shell. It must not show hardcoded accounting values. Use existing services or bridge classes where available, especially fund balance and dashboard bridge code. For missing metrics, show honest empty cards such as `Not wired yet` rather than fake numbers.

Include cards for cash/bank balances, fund balances, restricted/unrestricted net assets if available, unreconciled transaction count if available, undeposited funds if available, recent transactions if available, and missing document count if available.

Wire the dashboard to the active date range and context where practical. Add tests that assert no known demo values remain.
```

## 2.2 Finish navigation and command state model

### Checklist

- [ ] Define command metadata: label, action, availability, unavailable reason, category.
- [ ] Replace ad hoc command button construction with command descriptors.
- [ ] Show disabled commands with tooltips/reasons.
- [ ] Mark placeholder commands explicitly.
- [ ] Ensure command center and left navigation agree.

### GPT execution prompt

```text
Create a command metadata model for the alternate UI command center.

Replace ad hoc command button setup in `MainWindowAlternate` with command descriptors that include label, category, action, availability state, and disabled reason. The command center should show disabled buttons for unavailable commands with a tooltip explaining why.

Commands that are not implemented must be explicitly marked as not implemented rather than failing silently or showing a generic placeholder.

Add tests for command availability when no database is open, when a database is open but no company is open, and when a company is open.
```

## 2.3 Implement real global search

### Checklist

- [ ] Create `GlobalSearchService`.
- [ ] Search accounts, journal transactions, funds, donors, documents, reports.
- [ ] Return typed results with display title, subtitle, target panel/action.
- [ ] Replace `executeSearchQuery()` placeholder.
- [ ] Support empty query and error states.
- [ ] Add tests for at least account/fund/document search.

### GPT execution prompt

```text
Implement real global search for the alternate UI.

Create a `GlobalSearchService` that can search available domains: accounts, transactions, funds, donors, documents, and reports. Use existing services where available; where a domain is not wired, return no results with no failure.

Refactor the search pane in `MainWindowAlternate` so it displays a result list. Each result should have type, title, subtitle, and an action target. Clicking or double-clicking a result should open the appropriate panel or show a details placeholder if drilldown is not yet implemented.

Remove the current `Query staged for shared command surface` placeholder behavior. Add tests for search result mapping.
```

---

# Phase 3 — Migrate core old-interface functionality into native alternate panels

This phase is the heart of the migration. The old interface contains important behavior that must not be lost. The goal is not to copy the old panels pixel-for-pixel; the goal is to implement the same or better workflow in the new alternate style.

## 3.1 Migrate Journal / Ledger functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.JournalPanelFX`
- `nonprofitbookkeeping.ui.panels.AccountsActivityPanelFX`
- `nonprofitbookkeeping.ui.panels.GeneralJournalEntryPanelFX`
- `nonprofitbookkeeping.ui.panels.JournalEntryWorkspaceFX`
- `nonprofitbookkeeping.ui.panels.DocumentsPanelFX`
- `nonprofitbookkeeping.ui.panels.LedgerReconcilePanelFX`

### Required migrated functionality

- Real transaction register.
- Transaction filters by date, account, memo, amount, fund, cleared/reconciled status.
- Correct account-entry amounts, not whole transaction totals for account activity.
- Master/detail transaction display with journal lines.
- New/edit transaction using the richer journal entry workspace concepts.
- Void/reverse/delete workflow with confirmation and audit-safe behavior.
- Document attachment visibility and access.
- Statement import/review queue hooks.
- Reconcile drilldown.

### New alternate style target

- `AlternateLedgerRegisterPanel`
- `AlternateJournalEntryPanel` or `AlternateJournalEntryWorkspace`
- `AlternateAccountActivityPanel` as a drilldown view or mode inside the register

### Checklist

- [ ] Create transaction query DTOs and service methods if missing.
- [ ] Create native register panel with scaffold.
- [ ] Add date range and search filters.
- [ ] Add master/detail split layout.
- [ ] Add double-click/open behavior.
- [ ] Add New/Edit actions.
- [ ] Add void/reverse/delete command with confirmation.
- [ ] Add account activity mode showing selected-account entry amount and running balance.
- [ ] Add tests for split transaction account-amount correctness.
- [ ] Replace `LedgerRegisterPanel` demo data.
- [ ] Update `PanelHost` factory to use native panel.

### GPT execution prompt

```text
Migrate the old Journal and Account Activity functionality into a native alternate ledger register.

Inspect these old-interface files:
- `src/main/java/nonprofitbookkeeping/ui/panels/JournalPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/AccountsActivityPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/JournalEntryWorkspaceFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/GeneralJournalEntryPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/DocumentsPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/LedgerReconcilePanelFX.java`

Create or refactor a native alternate `LedgerRegisterPanel` that implements `AppPanel` and uses the alternate scaffold. Remove hardcoded sample rows. Load real transactions through an appropriate service/query layer. Show transaction header rows and a detail area containing journal entries.

Important correctness requirement: when filtering or displaying activity for a selected account, show the amount of the entry for that account, not the total amount of the whole transaction. Add a test using a split transaction to prove this.

Add New/Edit/Open behavior using a native alternate-style journal entry workspace or a temporary adapter if necessary. Deletion must not silently remove posted accounting history; implement confirmation and prefer void/reverse semantics if supported.
```

## 3.2 Migrate Chart of Accounts editing

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.AccountsPanelFX`
- `nonprofitbookkeeping.ui.panels.CoaEditorPanelFX`
- Any account service/repository classes.

### Required migrated functionality

- Add account.
- Edit account.
- Deactivate account.
- Prevent unsafe delete.
- Validate code/number uniqueness.
- Type/subtype selection.
- Posting/header flag.
- Parent account.
- Opening balance policy.
- Fund associations if supported.

### Checklist

- [ ] Add account maintenance service if missing.
- [ ] Create native add/edit dialog or side inspector.
- [ ] Replace read-only `onNew()` alert.
- [ ] Add validation and persistence.
- [ ] Add deactivate rather than delete where appropriate.
- [ ] Add tests for duplicate account code, invalid type/subtype, and deactivate-with-history.

### GPT execution prompt

```text
Migrate Chart of Accounts editing into the alternate UI.

Inspect old account panels and services, especially `AccountsPanelFX` and `CoaEditorPanelFX`. Refactor `org.nonprofitbookkeeping.ui.ChartOfAccountsPanel` so it is no longer read-only. Add native alternate-style create/edit/deactivate account functionality using services rather than direct table mutation.

Requirements:
- Validate account code uniqueness.
- Support account name, type, subtype, active/posting flags, and parent/header relationship if the model supports it.
- Do not allow unsafe deletion of accounts with posted entries; prefer deactivate.
- Persist changes through a service/repository.
- Add tests for validation and persistence behavior.
```

## 3.3 Migrate Funds functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.FundsPanelFX`
- Fund services and fund balance services.

### Required migrated functionality

- Fund list.
- Fund balances from ledger/service, not manually editable display balances.
- Add/edit/deactivate fund.
- Associate funds with accounts.
- Fund transfer/reclassification workflow.
- Clear distinction between bank transfer and fund restriction reclassification.
- Drilldown from fund balance to transactions.

### Checklist

- [ ] Create native `AlternateFundsPanel`.
- [ ] Replace adapter in `PanelHost`.
- [ ] Use fund balance service for balances.
- [ ] Remove direct manual balance editing.
- [ ] Add add/edit/deactivate fund dialog.
- [ ] Add reclassification journal workflow.
- [ ] Add drilldown to ledger register filtered by fund.
- [ ] Add tests for transfer/reclassification journal entries.

### GPT execution prompt

```text
Migrate the old Funds panel into a native alternate-style funds workspace.

Inspect `FundsPanelFX` and related fund services. Create a native `AlternateFundsPanel` or refactor `org.nonprofitbookkeeping.ui.FundsPanel` so it no longer wraps the old panel. It should use the alternate scaffold and real services.

Requirements:
- Show fund list and ledger-derived balances.
- Do not allow editing a display balance directly.
- Add/edit/deactivate funds through services.
- Implement fund reclassification/transfer as an accounting transaction with date, memo, from fund, to fund, amount, and account selection.
- Clearly distinguish bank-account movement from fund restriction reclassification.
- Add drilldown to ledger transactions for a selected fund.
- Add tests for generated journal entries.
```

## 3.4 Migrate Reports functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.ReportsPanelFX`
- `nonprofitbookkeeping.ui.panels.GenerateReportPanelFX`
- Report writer/generator services.
- Semantic workbook report services.

### Required migrated functionality

- Report catalog.
- Report parameter selection: date range, fund, account, donor, format.
- Generate reports.
- Open generated report history.
- Semantic workbook reports.
- Text/CSV/PDF/XLSX where supported.
- Scheduled reports, if kept.

### Checklist

- [ ] Create native `AlternateReportsWorkspacePanel`.
- [ ] Define unified report catalog model.
- [ ] Merge old report types and semantic workbook report types.
- [ ] Pass selected parameters into generation services.
- [ ] Remove disconnected controls.
- [ ] Add generated reports table/history.
- [ ] Add open/export actions.
- [ ] Add tests for report selection and parameter propagation.

### GPT execution prompt

```text
Migrate reports into a native alternate reports workspace.

Inspect `ReportsPanelFX`, `GenerateReportPanelFX`, semantic report services, and report writer/generator classes. Replace `ReportLibraryPanel`'s legacy adapter with a native alternate `ReportsWorkspacePanel` using the shared scaffold.

Requirements:
- Build a unified report catalog that includes legacy financial reports and semantic workbook reports.
- Date range, account, fund, donor, and output format parameters must be passed into the selected generator where supported.
- The Generate action must use the currently selected report and parameters; do not ignore controls.
- Show generated report history and provide open/export actions.
- Keep unsupported formats disabled with explanation.
- Add tests that verify selected report type and date range reach the generation service.
```

## 3.5 Migrate Settings functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.SettingsPanelFX`
- Preferences/settings services.
- Theme/format utilities.

### Required migrated functionality

- Organization/company profile.
- Fiscal year settings.
- Currency/format settings.
- Default accounts.
- Report directory/default report period.
- Theme/preferences.
- Autosave/backup settings if supported.
- Validation and persistence.

### Checklist

- [ ] Replace placeholder `SettingsPanel`.
- [ ] Decide whether settings are app-level, database-level, or company-level.
- [ ] Use tabs/sections in alternate scaffold.
- [ ] Validate fiscal year start.
- [ ] Validate default accounts against COA.
- [ ] Persist through settings/preferences service.
- [ ] Add tests for save behavior with no database/company and with open company.

### GPT execution prompt

```text
Migrate old Settings functionality into a native alternate settings workspace.

Inspect `SettingsPanelFX` and the settings/preferences services. Replace the placeholder `org.nonprofitbookkeeping.ui.SettingsPanel` and/or the alternate settings pane in `MainWindowAlternate` with a real native settings panel.

Requirements:
- Separate app-level settings from company-level settings.
- Include organization profile, fiscal year start, currency/format, default accounts, report defaults, theme, autosave/backup options where supported.
- Validate fiscal year start and account selections.
- Persist settings through the appropriate service.
- Disable or explain company-level settings when no company is open.
- Add tests for validation and persistence.
```

## 3.6 Migrate Documents and Attachments functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.DocumentsPanelFX`
- `DocumentStorageService`
- Journal/transaction document links.

### Required migrated functionality

- Attach document to selected transaction without manual transaction ID typing.
- Show documents associated with transaction/donor/asset/report where applicable.
- Open document.
- Replace/delete attachment with confirmation.
- Document type/category.
- Missing-document dashboard alerts.

### Checklist

- [ ] Create native `AlternateDocumentsPanel` or document inspector component.
- [ ] Add transaction picker/search.
- [ ] Add drag/drop file attach.
- [ ] Show existing documents.
- [ ] Add open/remove actions.
- [ ] Integrate document badges into ledger register.
- [ ] Add tests for attach and list operations.

### GPT execution prompt

```text
Migrate Documents & Attachments into the alternate UI style.

Inspect `DocumentsPanelFX`, `DocumentStorageService`, and journal/document link models. Build a native alternate document panel or reusable document inspector. Do not require users to manually type transaction IDs; provide transaction search/selection or open documents from a selected transaction.

Requirements:
- Attach a file to a transaction.
- List documents for a transaction.
- Open, replace, or remove attachments with confirmation.
- Support document type/category if the model supports it.
- Integrate document indicators into the alternate ledger register.
- Add tests for attach/list behavior.
```

## 3.7 Migrate Donor functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.DonorsPanelFX`
- `DonorService`
- Donation-related journal fields in `JournalEntryWorkspaceFX`.

### Required migrated functionality

- Donor list.
- Add/edit/deactivate donor.
- Email/phone/address/contact info.
- Donation history.
- In-kind donations.
- Receipts/acknowledgments if supported.
- Donor drilldown from donation transactions.

### Checklist

- [ ] Add `DONORS` or appropriate `AppPanelId` if not already represented by record-service registry.
- [ ] Create native donor workspace or record-service panel.
- [ ] Replace direct old-panel command-center donor action.
- [ ] Add donation history panel.
- [ ] Add tests for create/edit/list donor.

### GPT execution prompt

```text
Migrate donor management into the alternate UI style.

Inspect `DonorsPanelFX`, `DonorService`, and donation-related fields in `JournalEntryWorkspaceFX`. Add a native alternate donor workspace or record-service panel. If needed, add an `AppPanelId` or route through the existing record service registry.

Requirements:
- List donors.
- Add/edit/deactivate donors.
- Show contact info and donation history.
- Link donation transactions to donor records.
- Replace the command-center direct use of `DonorsPanelFX` with the new native panel or a clearly marked temporary adapter.
- Add tests for donor CRUD/list behavior.
```

## 3.8 Migrate Inventory / Asset functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.InventoryPanelFX`
- `org.nonprofitbookkeeping.ui.AssetsRegisterPanel`
- `org.nonprofitbookkeeping.ui.DepreciationRunsPanel`
- Asset/depreciation services.

### Required migrated functionality

- Inventory/asset list.
- Add/edit/delete or deactivate asset.
- Cost/acquired date parsing without formatted currency parse bugs.
- Depreciation method, useful life, accumulated depreciation, net book value.
- Depreciation run integration.
- Disposal workflow.
- Document attachments.

### Checklist

- [ ] Create native asset register backed by services.
- [ ] Merge or coordinate `InventoryPanel`, `AssetsRegisterPanel`, and `DepreciationRunsPanel`.
- [ ] Replace demo asset rows.
- [ ] Add validation for dates and money.
- [ ] Add depreciation drilldown.
- [ ] Add disposal workflow if model supports it.
- [ ] Add tests for money parsing and depreciation linkage.

### GPT execution prompt

```text
Migrate inventory/assets into native alternate panels.

Inspect `InventoryPanelFX`, `AssetsRegisterPanel`, `DepreciationRunsPanel`, and related services. Replace the legacy `InventoryPanel` adapter and demo-only `AssetsRegisterPanel` with service-backed native alternate panels.

Requirements:
- Asset register must load real assets.
- Add/edit/deactivate asset through services.
- Use safe numeric/date editors; do not parse formatted currency display strings as raw BigDecimal input.
- Show depreciation fields and link to depreciation runs.
- Preserve or improve the existing depreciation run workflow.
- Add document attachment hooks for assets.
- Add tests for asset save validation and depreciation linkage.
```

## 3.9 Migrate Reconciliation and banking functionality

### Old-interface sources to inspect

- `nonprofitbookkeeping.ui.panels.ReconcilePanelFX`
- `nonprofitbookkeeping.ui.panels.LedgerReconcilePanelFX`
- `nonprofitbookkeeping.ui.panels.UndepositedFundsPanelFX`
- Account activity import logic.
- Reconciliation services.

### Required migrated functionality

- Reconcile selected bank/cash account.
- Statement date and ending balance.
- Clear/reconcile transactions with BooleanProperty-backed model.
- Difference calculation.
- Prevent accidental reconciliation with nonzero difference unless explicit adjustment workflow exists.
- Import statement review queue.
- Undeposited funds workflow.
- Documents/receipt linkage.

### Checklist

- [ ] Create native `AlternateReconciliationPanel`.
- [ ] Replace direct old reconcile panel creation in command center.
- [ ] Use real BooleanProperty checkboxes.
- [ ] Add beginning balance, cleared total, ending balance, difference.
- [ ] Add validation and confirmation.
- [ ] Add statement import review queue.
- [ ] Add tests for difference calculation and reconcile save behavior.

### GPT execution prompt

```text
Migrate reconciliation and banking workflows into native alternate panels.

Inspect old `ReconcilePanelFX`, `LedgerReconcilePanelFX`, `UndepositedFundsPanelFX`, account activity import logic, and reconciliation services. Build native alternate-style reconciliation and banking panels.

Requirements:
- Use a proper row model with JavaFX BooleanProperty for cleared/reconciled selection.
- Show beginning balance, statement ending balance, cleared total, and difference.
- Invalid ending balance must show validation, not silently become zero.
- Prevent reconciliation with nonzero difference unless an explicit adjustment workflow is implemented.
- Migrate statement import into a review queue before posting transactions.
- Add tests for difference calculation and save behavior.
```

---

# Phase 4 — Native nonprofit/SCA workflow improvements

## 4.1 Event accounting workspace

### Rationale

For an SCA branch/nonprofit, many transactions relate to events. The UI should allow event-level accounting without forcing the user to infer event activity from account/memo filters.

### Checklist

- [ ] Define event entity or reuse existing metadata model.
- [ ] Add event list/detail panel.
- [ ] Link journal entries, income, expenses, deposits, refunds, and documents to an event.
- [ ] Provide event profit/loss summary.
- [ ] Provide event close checklist.

### GPT execution prompt

```text
Add an alternate-style Event Accounting workspace.

Inspect existing transaction metadata models for event-like fields. If an event entity exists, use it; otherwise propose a minimal service/model addition before implementing UI. The workspace should list events, show event income/expense summary, linked journal transactions, documents, deposits/refunds, and a close checklist.

Do not invent accounting postings in the UI. Use services for persistence and report calculations.
```

## 4.2 Monthly close / exchequer checklist

### Checklist

- [ ] Add close period selection.
- [ ] Show reconciliation status.
- [ ] Show missing documents.
- [ ] Show unbalanced/unposted transactions.
- [ ] Show report generation status.
- [ ] Lock/close period if supported.

### GPT execution prompt

```text
Create a native alternate monthly close / exchequer checklist panel.

The panel should guide a branch exchequer through close steps: reconcile bank accounts, review undeposited funds, resolve missing documents, verify fund balances, generate required reports, and lock/close the period if supported by services.

Use service-backed checks where available and explicit `not wired yet` states where missing. Do not use fake completion statuses.
```

## 4.3 Donation receipt and in-kind workflow

### Checklist

- [ ] Add donation transaction query.
- [ ] Link donor to donation transactions.
- [ ] Mark receipt-required/receipt-sent.
- [ ] Generate annual donor summary where supported.
- [ ] Support in-kind donation notes and valuation fields if model supports it.

### GPT execution prompt

```text
Add native alternate donor receipt workflow.

Use donor and journal services to identify donation transactions. Provide a donor detail view showing donation history, receipt status, and annual totals. Add receipt-required and receipt-sent workflow where the model supports it. Include in-kind donation fields only if they exist in the model or add a small model/service proposal first.
```

---

# Phase 5 — Testing and quality gates

## 5.1 Add UI smoke tests for every alternate panel

### Checklist

- [ ] Instantiate each `AppPanel` without database open where possible.
- [ ] Verify no production demo data appears.
- [ ] Verify no panel throws during construction.
- [ ] Verify commands report unsupported/disabled honestly.

### GPT execution prompt

```text
Add smoke tests for all alternate UI panels.

The tests should instantiate every `AppPanelId` through `PanelHost.DefaultPanelFactory` or an accessible test factory. Verify construction does not throw, root nodes are non-null, titles are non-blank, and panels with no data source do not insert realistic hardcoded accounting sample data.

If JavaFX test bootstrap is needed, add or reuse the existing test support.
```

## 5.2 Add accounting correctness tests for migrated workflows

### Checklist

- [ ] Split transaction account activity amount test.
- [ ] Fund reclassification journal test.
- [ ] Reconciliation difference calculation test.
- [ ] Budget actual query test.
- [ ] Report parameter propagation test.
- [ ] Delete/void/reverse audit behavior test.

### GPT execution prompt

```text
Add accounting correctness tests for alternate UI migration services.

Focus on service/query behavior rather than JavaFX rendering where possible. Include tests for:
- selected-account activity amount in a split transaction;
- fund reclassification journal entries;
- reconciliation difference calculation;
- report parameter propagation;
- budget vs actual calculation;
- void/reverse behavior instead of unsafe deletion.

Use fixtures/builders rather than production demo data.
```

## 5.3 Add migration completion checks

### Checklist

- [ ] Test that no `AppPanelId` routes to `Template pending` without an inventory entry.
- [ ] Test that all legacy adapters are listed in the migration inventory.
- [ ] Test that command center disabled commands have reasons.
- [ ] Add CI task if appropriate.

### GPT execution prompt

```text
Add migration guard tests for the alternate UI.

Tests should verify:
- every `AppPanelId` has a documented migration status;
- every legacy adapter is listed in `doc/ui/alternate-ui-migration-inventory.md`;
- command center unavailable commands have non-empty disabled reasons;
- no route shows a generic `Template pending` unless the inventory marks it as intentionally pending.
```

---

# Phase 6 — Retirement of old UI adapters

## 6.1 Adapter retirement plan

### Checklist

For each adapted legacy panel:

- [ ] Identify replacement native alternate panel.
- [ ] Verify feature parity or intentional improvement.
- [ ] Verify persistence behavior.
- [ ] Verify tests cover migrated behavior.
- [ ] Change `PanelHost.DefaultPanelFactory` to native panel.
- [ ] Remove direct command-center calls to old panel.
- [ ] Keep old panel only if used by another application mode.
- [ ] Update migration inventory.

### GPT execution prompt

```text
Retire one legacy adapter from the alternate UI.

Choose a single adapter listed in `PanelHost.DefaultPanelFactory` or `MainWindowAlternate`, confirm its native replacement exists and has tests, then switch the alternate shell to the native panel. Remove direct command-center construction of the old panel for that workflow.

Update `doc/ui/alternate-ui-migration-inventory.md` to mark the adapter retired. Do not delete the old panel unless no other mode uses it.
```

## 6.2 Final old-interface functionality audit

### Checklist

- [ ] Search old panels for user-visible actions.
- [ ] Confirm each action exists in alternate UI or is intentionally retired.
- [ ] Search old panels for service calls.
- [ ] Confirm each service-backed workflow exists in alternate UI.
- [ ] Search old panels for report generation paths.
- [ ] Confirm reports are reachable in alternate UI.
- [ ] Search old panels for import/export functionality.
- [ ] Confirm import/export is reachable in alternate UI.

### GPT execution prompt

```text
Perform a final old-interface functionality audit.

Search `src/main/java/nonprofitbookkeeping/ui/panels` for buttons, menu actions, service calls, report generation, import/export, file chooser use, document attachment use, reconciliation actions, donor actions, fund actions, and journal editing actions.

Produce a checklist showing whether each user-visible capability exists in the alternate UI. If missing, create TODO entries in `doc/ui/alternate-ui-migration-inventory.md`. Do not change Java code in this step unless the only change is documentation.
```

---

# Suggested implementation order

1. Create migration inventory.
2. Remove or label demo data.
3. Add `UiSessionContext`.
4. Refactor service registry into context-bound provider.
5. Strengthen `AppPanel` lifecycle/save results.
6. Add shared panel scaffold and CSS.
7. Replace dashboard fake cards.
8. Migrate ledger/register and journal entry workflow.
9. Migrate chart of accounts editing.
10. Migrate funds.
11. Migrate reports.
12. Migrate settings.
13. Migrate documents.
14. Migrate donors.
15. Migrate inventory/assets/depreciation into one coherent asset workflow.
16. Migrate reconciliation/banking/import review.
17. Add nonprofit/SCA workflows: event accounting, monthly close, donation receipts.
18. Add migration guard tests.
19. Retire adapters one at a time.
20. Run final old-interface functionality audit.

---

# Acceptance criteria for the alternate UI becoming primary

The alternate UI can be considered primary when:

- [ ] No primary workflow displays realistic hardcoded accounting data.
- [ ] Opening database and company uses a single authoritative context.
- [ ] Core panels use a consistent scaffold and command model.
- [ ] Dashboard is service-backed.
- [ ] Ledger register is service-backed and supports journal entry detail.
- [ ] Chart of accounts supports real maintenance.
- [ ] Funds workflow is native and ledger-derived.
- [ ] Reports workspace honors selected report parameters.
- [ ] Settings persist and validate.
- [ ] Documents are attachable without manual transaction ID entry.
- [ ] Reconciliation has correct checkbox state, difference calculation, and validation.
- [ ] Inventory/assets/depreciation workflows are coherent and service-backed.
- [ ] Old-interface-only functionality has been migrated or explicitly retired.
- [ ] Legacy adapters are documented and temporary.
- [ ] Smoke and correctness tests pass.

---

# Notes for future GPT/Codex sessions

- Work in small commits. Prefer one panel/workflow per change.
- Before editing a panel, inspect both the alternate panel and the old panel that owns the mature behavior.
- Do not migrate by copy/paste alone. Preserve behavior, but restyle into the alternate scaffold.
- Favor services and DTOs over direct database calls from JavaFX panels.
- Avoid static/global context for new work.
- Avoid fake accounting data in production UI code.
- Add tests for accounting behavior before polishing the UI.
- When a workflow is incomplete, show an explicit disabled or empty state rather than a realistic placeholder.
