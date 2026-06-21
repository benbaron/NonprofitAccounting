# Alternate UI Development Plan

This document defines the development plan for completing the `org.nonprofitbookkeeping.ui` alternate JavaFX user interface. The alternate UI should become the long-term shell and workspace style for the application. Functionality that exists only in the older `nonprofitbookkeeping.ui.panels` interface should be migrated into native alternate-style panels over time.

Every prompt in this document is intended to be copy/paste friendly. Each prompt either explicitly tells the agent to read this document and the review file first, or includes the nearby requirements directly inside the prompt.

## Required references for all work

Before doing substantial work, read:

- `AGENTS.md`
- `PLANS.md`
- `doc/ui/alternate-ui-development-plan.md`
- `doc/ui/alternate-ui-development-plan-review.md`

## Goals

- Make `org.nonprofitbookkeeping.ui` the primary shell/workspace layer.
- Replace fake/demo data with real service-backed data or explicit empty states.
- Migrate old-interface-only functionality into native alternate-style panels.
- Keep legacy panels as temporary adapters only where needed.
- Provide a consistent panel lifecycle, command model, styling system, and context model.
- Add first-class company/database administration: create company, delete/destroy company, populate company, create sample company, open/switch company, and close company.
- Add first-class import/export/repair administration: database import/export, chart of accounts import/export, SCLX import, and database repair/recovery.
- Preserve accounting correctness: double-entry balance, fund/restriction reporting, reconciliation integrity, audit trail, and report reproducibility.

## Explicitly dropped from this plan

The alternate UI migration plan no longer includes a Documents & Attachments workflow. Do not migrate or expand document attachment features as part of this plan. Existing document-related legacy code may remain in the repository, but the alternate UI should not treat it as a required primary workflow.

## Non-goals

- Do not delete the older `nonprofitbookkeeping.ui.panels` implementation until all required functionality has a native alternate-style equivalent.
- Do not hide incomplete functionality behind realistic-looking demo data.
- Do not make UI-only changes that imply persistence when no model/service persistence exists.
- Do not add new accounting behavior directly inside JavaFX panels when it belongs in a service.
- Do not add or migrate Documents & Attachments functionality as part of this plan.

## Definitions

- **Alternate UI**: Code under `src/main/java/org/nonprofitbookkeeping/ui` and `src/main/java/org/nonprofitbookkeeping/ui/panels`.
- **Old UI / legacy panels**: Code under `src/main/java/nonprofitbookkeeping/ui/panels` and related `nonprofitbookkeeping.ui` Swing/JavaFX compatibility code.
- **Native alternate panel**: Implements `org.nonprofitbookkeeping.ui.AppPanel` and follows the alternate shell style/lifecycle.
- **Adapted legacy panel**: A wrapper that exposes an old panel inside the alternate shell.
- **Company administration**: UI and services for creating, opening, switching, closing, deleting/destroying, populating, and sample-seeding companies.
- **Database administration**: UI and services for opening, importing, exporting, backing up, repairing, migrating, and validating database files.
- **Migration complete**: The alternate UI contains equivalent or better functionality, with service-backed persistence and tests where appropriate.

## Architecture principles

1. The alternate shell owns navigation, command routing, panel hosting, and workspace layout.
2. Accounting behavior belongs in services, not JavaFX panels.
3. Panels may call services through a context-aware registry or injected service factory.
4. The active database/company context must have one source of truth.
5. Demo data is allowed only in tests or clearly marked sample fixtures, never in production UI paths.
6. Old panels may remain temporarily, but every adapter must have a migration target and exit criterion.
7. Every panel must expose honest command states: available, disabled with reason, or not implemented.
8. Reconciliation, journal deletion, fund movement, imports, exports, company destruction, database repair, and depreciation must preserve auditability and protect user data.
9. Destructive operations, including company deletion/destroy and database repair overwrite, require confirmation and backup guidance.
10. Import workflows must use preview/validation before committing changes to a company database.

## Universal rules for every implementation prompt

Every implementation prompt below must be treated as if it includes these rules:

- Inspect existing classes, tests, and services before adding new APIs.
- Prefer extending/adapting existing services over creating parallel services.
- Do not invent model fields. If a needed field is missing, propose the smallest model/service change first.
- Keep JavaFX panels thin. UI code may bind controls and call services, but business rules belong in services.
- Establish a compile/test baseline before editing when practical.
- After editing, run relevant compile/tests, or document why they could not be run.
- Add/update tests for every service behavior change.
- Update `doc/ui/alternate-ui-migration-inventory.md` when adding, replacing, retiring, or intentionally deferring a workflow.
- Do not add Documents & Attachments functionality under this plan.
- Do not introduce production-path demo data.
- For destructive actions, require confirmation, backup guidance, and safe failure behavior.
- For import actions, require preview/validate/commit/result-summary behavior where technically possible.

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
- [ ] Include company/database/import/export/repair workflows even if no `AppPanelId` exists yet.
- [ ] Explicitly mark Documents & Attachments as dropped/not in scope.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create or update `doc/ui/alternate-ui-migration-inventory.md`.

Requirements:
- List every `AppPanelId` in `src/main/java/org/nonprofitbookkeeping/ui/AppPanelId.java`.
- Also list required workflows that may not yet have an `AppPanelId`: company administration, database administration, import/export, SCLX import, chart-of-accounts import/export, and database repair.
- For each row, identify current class, current status, old UI source, missing old functionality, target native panel, required services, and test status.
- Classify status as native, legacy adapter, placeholder, demo-only, mixed, missing, dropped, or not applicable.
- Mark Documents & Attachments as dropped/not in scope for this plan.
- Do not change Java code in this step. Produce only the inventory document.
```

## 0.2 Add a no-demo-data policy for production UI

### Checklist

- [ ] Search alternate UI for hardcoded business sample data.
- [ ] Replace production demo values with explicit empty states where no service exists.
- [ ] Add comments for any temporary placeholders.
- [ ] Ensure dashboard, budget, ledger, and assets do not display realistic fake values.
- [ ] Allow sample data only through an explicit Create Sample Company / Populate Sample Company workflow.

### Known problem areas

- Hardcoded dashboard cards in `MainWindowAlternate`.
- Hardcoded rows in `LedgerRegisterPanel`.
- Hardcoded rows in `BudgetEditorPanel`.
- Hardcoded report tree in `BudgetVsActualPanel`.
- Hardcoded rows in `AssetsRegisterPanel`.
- Fallback demo accounts in `SchedulesPanel`.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: remove or quarantine production-path demo data from the alternate UI.

Requirements:
- Search `src/main/java/org/nonprofitbookkeeping/ui` for hardcoded realistic accounting values, sample payees, sample budget rows, sample asset rows, and fallback demo accounts.
- Pay special attention to `MainWindowAlternate`, `LedgerRegisterPanel`, `BudgetEditorPanel`, `BudgetVsActualPanel`, `AssetsRegisterPanel`, and `SchedulesPanel`.
- Replace production-path demo data with explicit empty/loading/error states unless a real service-backed query already exists.
- Do not remove unit-test fixtures.
- Do not remove sample-company seed data if it is only reachable through an explicit Create Sample Company or Populate Sample Company workflow.
- Do not add Documents & Attachments functionality.
- Add or update tests that verify demo rows are not inserted by default in production UI classes.
- Run the relevant Maven compile/test command, or document why it could not be run.
```

## 0.3 Establish build, test, and JavaFX execution baseline

### Checklist

- [ ] Identify the Maven compile command.
- [ ] Identify the non-UI test command.
- [ ] Identify JavaFX/headless test behavior.
- [ ] Record known failing tests before migration work begins.
- [ ] Add `doc/ui/alternate-ui-test-baseline.md` if no such note exists.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: establish the build and test baseline for alternate UI migration work.

Requirements:
- Inspect `pom.xml`, test source folders, CI files if present, JavaFX test setup, and existing test naming patterns.
- Determine the command to compile the project and the command to run relevant tests.
- Run the commands if possible.
- Create `doc/ui/alternate-ui-test-baseline.md` documenting compile command, test command, JavaFX/headless notes, known failing tests, UI-test notes, and service/import/database/company test guidance.
- Do not change production Java code in this step.
```

---

# Phase 1 — Context, services, lifecycle, and shell consistency

## 1.1 Introduce a single UI session context

### Checklist

- [ ] Create `UiSessionContext` under `org.nonprofitbookkeeping.ui` or a suitable subpackage.
- [ ] Track active database path/base path.
- [ ] Track active company ID and display name.
- [ ] Track whether current company is newly created, sample, populated, or production-like if supported.
- [ ] Expose JavaFX observable properties for binding.
- [ ] Expose context state: no database, database open/no company, company open.
- [ ] Provide clear/close methods for database and company changes.
- [ ] Make `MainWindowAlternate` read header/nav state from this context.
- [ ] Add a legacy bridge only where old panels need `CurrentCompany`.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a single `UiSessionContext` for the alternate JavaFX UI.

Requirements:
- Inspect `MainWindowAlternate`, `AlternateDataContextService`, `CurrentCompany`, `PanelHost`, `WorkspaceRouter`, and `UiServiceRegistry` before editing.
- Create `UiSessionContext` under `src/main/java/org/nonprofitbookkeeping/ui` or a suitable subpackage.
- Track active database base path, active company id, active company display label, and state flags for database/company open.
- Expose optional company state metadata such as sample/populated/newly-created only if this exists or can be represented safely.
- Use JavaFX properties where useful so the header and navigation can bind to context changes.
- Refactor `MainWindowAlternate` to use this context as the primary source for header subtitle and navigation enablement.
- Do not remove `CurrentCompany` yet; add a compatibility bridge only where legacy panels require it.
- Avoid circular dependencies between the new context and legacy globals.
- Add tests for context state transitions.
```

## 1.2 Refactor `UiServiceRegistry` into a context-bound service provider

### Checklist

- [ ] Create `UiServiceProvider` or `UiServices` bound to `UiSessionContext`.
- [ ] Defer JPA/service creation until a database context is open.
- [ ] Rebuild services when the database changes.
- [ ] Provide company, database, and import/export service access.
- [ ] Keep transitional static registry only as a deprecated bridge if needed.
- [ ] Refactor native alternate panels to obtain services from the provider.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: refactor alternate UI service wiring away from static singleton services.

Requirements:
- Inspect `UiServiceRegistry`, JPA bootstrap classes, account/fund/schedule/dashboard services, and any database/company context services before editing.
- Create a context-bound `UiServiceProvider` or `UiServices` that obtains services from the active `UiSessionContext`.
- The provider should expose account lookup, fund lookup, fund balance, schedule eligibility, dashboard data, company administration, database administration, and import/export services where existing services are available.
- Defer service creation until a database is open.
- Rebuild or invalidate services when the active database changes.
- Keep `UiServiceRegistry` only as a temporary compatibility facade if needed, mark it deprecated, and route it through the context-bound provider where practical.
- Do not refactor legacy adapters unless required for compilation.
- Add tests for no-database, database-open, company-open, and database-change behavior.
```

## 1.3 Strengthen the `AppPanel` lifecycle contract

### Checklist

- [ ] Support dirty state.
- [ ] Support save result: saved, no changes, failed, unsupported.
- [ ] Support `onEnter(context)` and `onLeave()` if practical.
- [ ] Update `PanelHost` to report actual save outcomes.
- [ ] Update `MainWindowAlternate` messages so they do not claim save occurred unless it did.
- [ ] Convert placeholder panels to report unsupported save.
- [ ] Allow destructive/admin panels to block/warn on navigation during long-running work.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: improve the alternate UI panel lifecycle so save behavior is truthful.

Requirements:
- Inspect `AppPanel`, `PanelHost`, `MainWindowAlternate`, and all native alternate panels before editing.
- Add a save result model without breaking existing panel implementations. A save result should distinguish saved, no changes, unsupported, and failed.
- Add dirty-state support through an optional interface or backward-compatible default behavior.
- Update `PanelHost.saveActive()` to return the real save result.
- Update panel-switch and command-center Save messages so they do not imply save occurred unless it did.
- Ensure panels with no persistence report unsupported/no changes rather than pretending to save.
- Ensure long-running or destructive admin operations such as import/export/repair can block or warn on navigation.
- Add tests around `PanelHost` save delegation, switching behavior, unsupported save behavior, and failed save behavior.
```

## 1.4 Create a shared alternate panel scaffold

### Checklist

- [ ] Create `AlternatePanelScaffold` or `PanelScaffold`.
- [ ] Support title, subtitle/help, primary actions, secondary actions, filter area, content, status/footer.
- [ ] Support empty/loading/error nodes.
- [ ] Support warning/destructive-operation banners.
- [ ] Apply consistent padding/style classes.
- [ ] Refactor at least two simple native panels to use it.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a reusable alternate panel scaffold.

Requirements:
- Inspect `MainWindowAlternate`, `ChartOfAccountsPanel`, `LedgerRegisterPanel`, `SettingsPanel`, and any existing CSS resources before editing.
- Create `AlternatePanelScaffold` or `PanelScaffold` under the alternate UI package.
- Support common title, subtitle/help text, primary actions, secondary actions, optional filter bar, content area, status/footer, empty state, loading state, error state, and warning/destructive-operation banner.
- Use CSS style classes rather than inline styles where practical.
- Refactor `ChartOfAccountsPanel` and `LedgerRegisterPanel` to use the scaffold without changing business behavior.
- Add/update CSS and tests where practical.
```

## 1.5 Move alternate shell styling into CSS

### Checklist

- [ ] Identify inline styles in `MainWindowAlternate` and native panels.
- [ ] Create or update alternate UI CSS file.
- [ ] Add classes for shell, icon rail, cards, status labels, panel titles, action bars, workspaces, admin warnings.
- [ ] Add tooltips and accessible text for icon rail buttons.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: move alternate UI shell styling out of Java inline strings and into CSS.

Requirements:
- Inspect `MainWindowAlternate`, native alternate panels, existing CSS resources, and JavaFX application startup code.
- Refactor icon rail, dashboard cards, workspace surface, panel titles, status labels, warning banners, and action bars to use style classes.
- Preserve the current appearance as much as practical.
- Add tooltips and accessible labels to icon rail buttons.
- Do not change functional behavior in this step.
- Run the relevant compile/test command or document why it could not be run.
```

## 1.6 Define admin operation safety contracts

### Checklist

- [ ] Define common preview, validation, commit, backup, failure, and progress result concepts.
- [ ] Distinguish info/warning/error validation messages.
- [ ] Require confirmation text for destructive operations.
- [ ] Require backup recommendation/result before destructive database/company operations.
- [ ] Require transaction/rollback behavior where database changes are committed.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: define a shared safety/result contract for alternate UI administrative operations.

Requirements:
- Inspect existing import, SCLX, H2 repair, database open, and company services before adding new types.
- Add common DTOs or interfaces only if no suitable equivalents exist.
- The contract should support preview/dry-run result, validation messages with severity, destructive-operation confirmation requirements, backup recommendation/result, commit result with counts/output paths, rollback/failure summary, and async progress/status.
- Do not refactor major workflows in this step unless needed for compilation.
- Add unit tests for the DTO/service contract behavior.
```

---

# Phase 2 — Shell truthfulness, navigation, company, and database administration

## 2.1 Replace hardcoded dashboard cards with service-backed dashboard cards

### Checklist

- [ ] Define dashboard snapshot DTOs.
- [ ] Use real fund/account/journal/reconciliation services where available.
- [ ] Show empty states for missing data.
- [ ] Provide cards for cash, fund balances, restricted/unrestricted, unreconciled count, undeposited funds, recent transactions, company status, database status, and pending imports.
- [ ] Add refresh and date range awareness.
- [ ] Remove literal accounting values from `MainWindowAlternate`.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: replace hardcoded dashboard cards with a native service-backed alternate dashboard.

Requirements:
- Inspect `MainWindowAlternate`, existing dashboard bridge/data classes, fund/account/journal/reconciliation services, and any old `DashboardPanelFX` behavior before editing.
- Create `AlternateDashboardPanel` implementing `AppPanel` or a reusable dashboard node used by the shell.
- Do not show hardcoded accounting values.
- Show honest empty/not-wired cards where a metric has no service-backed source.
- Include cards for cash/bank balances, fund balances, restricted/unrestricted net assets if available, unreconciled transaction count if available, undeposited funds if available, recent transactions if available, pending import count if available, active company status, and active database status.
- Wire the dashboard to active date range and UI context where practical.
- Add tests that assert known demo values are absent and that empty states render when no data source exists.
```

## 2.2 Finish navigation and command state model

### Checklist

- [ ] Define command metadata: label, action, availability, unavailable reason, category.
- [ ] Replace ad hoc command buttons with descriptors.
- [ ] Show disabled commands with tooltips/reasons.
- [ ] Add Database & Company group.
- [ ] Add Import/Export group.
- [ ] Ensure command center and left navigation agree.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a command metadata model for the alternate UI command center and navigation.

Requirements:
- Inspect `MainWindowAlternate`, `NavigationPane`, `WorkspaceRouter`, `PanelHost`, and `AppPanelId` before editing.
- Replace ad hoc command button setup with command descriptors containing label, category, action, availability state, and disabled reason.
- Commands that are not implemented must be explicitly marked not implemented rather than failing silently.
- Add command groups for Database & Company and Import/Export.
- Include commands for open database, import database, export database, repair database, create company, destroy/delete company, populate company, create sample company, import chart of accounts, export chart of accounts, and import SCLX.
- Prefer stable `AppPanelId` values for first-class workflows. Update `AppPanelId`, `WorkspaceRouter`, `PanelHost`, `NavigationPane`, command descriptors, migration inventory, and smoke tests together when adding a new first-class route.
- Add tests for command availability when no database is open, database open/no company, and company open.
```

## 2.3 Implement real global search

### Checklist

- [ ] Create `GlobalSearchService`.
- [ ] Search accounts, journal transactions, funds, donors, reports, companies, import/export histories where available.
- [ ] Return typed results with display title, subtitle, and target action.
- [ ] Replace placeholder query behavior.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: implement real global search for the alternate UI.

Requirements:
- Inspect `MainWindowAlternate`, available account/journal/fund/donor/report/company services, and navigation/routing classes before editing.
- Create `GlobalSearchService` only if no suitable service exists.
- Search available domains: accounts, transactions, funds, donors, reports, companies, and import/export histories where available.
- Missing domains should return no results without causing failure.
- Replace the current placeholder `executeSearchQuery()` behavior with a result list.
- Each result should have type, title, subtitle, and target action or panel route.
- Double-click/click should open the appropriate panel or show an honest details placeholder if drilldown is not implemented.
- Add tests for result mapping and no-database behavior.
```

## 2.4 Add native Database Administration workspace

### Required functionality

- Open database.
- Close database.
- Import database.
- Export/backup database.
- Validate database.
- Repair/recover H2 database.
- Run migration/update schema if supported.
- Show recent databases.
- Show active database path/status.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a native alternate Database Administration workspace.

Existing sources to inspect before editing:
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
- `src/main/java/org/nonprofitbookkeeping/ui/DatabaseOpenService.java`
- `src/main/java/nonprofitbookkeeping/tools/H2SchemaMigrator.java`
- `scripts/migrate_h2_schema.py`
- `src/test/java/nonprofitbookkeeping/tools/H2SchemaMigratorTest.java`
- any existing database open/import/export/backup call sites.

Requirements:
- Add `DATABASE_ADMIN` as a stable `AppPanelId` unless there is a clear reason not to.
- Update `AppPanelId`, `WorkspaceRouter`, `PanelHost`, `NavigationPane`, command descriptors, migration inventory, and smoke tests together.
- Create `AlternateDatabaseAdminPanel` or equivalent native `AppPanel`.
- Support Open Database, Close Database, Import Database, Export/Backup Database, Validate Database if supported, Repair/Recover H2 Database, and Migrate Schema if supported.
- Distinguish these operations clearly in the UI.
- Never overwrite the active database during repair without explicit backup and confirmation.
- Close or block active company/database context before operations requiring exclusive file access.
- Show source path, target path, backup path, and result path before commit where applicable.
- Preserve recent database list only after successful open/import.
- Use async execution and progress/status reporting for long-running operations.
- Add tests for invalid path, unsupported extension, repair failure, export target exists, command availability, and open-after-repair behavior.
```

## 2.5 Add native Company Administration workspace

### Required functionality

- List companies in the active database.
- Open/switch company.
- Create company.
- Destroy/delete company with strong confirmation.
- Populate company with starter chart/settings.
- Create sample company.
- Close active company.
- Show recent companies and company status.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a native alternate Company Administration workspace.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/CreateOrEditCompanyActionFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/CompanySelectionPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/OpenCompanyFileActionFX.java`
- `src/test/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFXTest.java`
- `CurrentCompany`, company model/repository code, `MainWindowAlternate`, and `AlternateDataContextService`.

Requirements:
- Add `COMPANY_ADMIN` as a stable `AppPanelId` unless there is a clear reason not to.
- Update `AppPanelId`, `WorkspaceRouter`, `PanelHost`, `NavigationPane`, command descriptors, migration inventory, and smoke tests together.
- Create `AlternateCompanyAdminPanel` or equivalent native `AppPanel`.
- List companies in the active database.
- Open/switch company and update `UiSessionContext`.
- Close the active company safely.
- Create a new company with required organization/fiscal settings.
- Define exactly what destroy/delete means in this project: remove one company row, remove all company-owned records, or delete a company file/database. Use the safest available interpretation and document it in the UI.
- Prevent deleting the active company unless the workflow first closes/switches context safely.
- Require strong confirmation for destructive delete/destroy, preferably typing the company name.
- Require/recommend database export/backup before destructive delete/destroy.
- Populate an empty company with starter chart/settings; make it idempotent or detect already-populated state and explain what will happen.
- Create a deterministic sample company only through explicit user action.
- Add tests for duplicate company names, invalid required fields, create/open/close/delete/populate/sample workflows, delete active company, and populate already-populated company.
```

## 2.6 Add native Import/Export workspace

### Required functionality

- Database import.
- Database export/backup.
- Chart of accounts import/export.
- SCLX import.
- Existing spreadsheet/XLSM/JSON import/export if supported and in scope.
- Validation preview before commit.
- Result summary.

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a native alternate Import/Export workspace.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/SclxImportPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/ImportSclxActionFX.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportService.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportOptions.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportResult.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportTarget.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/NonprofitBookkeepingSclxImportTarget.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxParser.java`
- SCLX tests under `src/test/java/nonprofitbookkeeping/importer/sclx`.
- Existing chart-of-accounts import/export, spreadsheet/XLSM import, JSON import/export, database import/export, and backup code.

Requirements:
- Add `IMPORT_EXPORT` as a stable `AppPanelId` unless there is a clear reason not to.
- Update `AppPanelId`, `WorkspaceRouter`, `PanelHost`, `NavigationPane`, command descriptors, migration inventory, and smoke tests together.
- Create `AlternateImportExportPanel` or equivalent native `AppPanel`.
- Provide clearly separated sections for Database import/export, Chart of Accounts import/export, SCLX import, and any existing supported spreadsheet/database formats.
- Define import modes: preview only, validate only, commit to active company, or create/import into a new database/company.
- Define export modes: active company export, full database export/backup, chart of accounts export, SCLX export if supported.
- Every import must produce a result with counts: created, updated, skipped, warnings, errors.
- Every import must show blocking errors before commit.
- COA import must define duplicate-code policy, account deactivation policy, and whether existing accounts with transaction history may be changed.
- SCLX import must use existing `SclxImportService`, `SclxImportOptions`, `SclxImportResult`, and target classes unless a review shows they are unsuitable.
- Reuse existing SCLX tests and add UI/service boundary tests rather than duplicating parser logic in UI code.
- Database import/export must not be mixed with company-level import/export without clear labels.
```

---

# Phase 3 — Migrate core old-interface functionality into native alternate panels

Documents & Attachments are intentionally not included in this phase.

## 3.1 Migrate Journal / Ledger functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate old Journal and Account Activity functionality into a native alternate ledger register.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/JournalPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/AccountsActivityPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/JournalEntryWorkspaceFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/GeneralJournalEntryPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/LedgerReconcilePanelFX.java`
- Existing journal/ledger transaction services and tests.

Requirements:
- Create or refactor a native alternate ledger register implementing `AppPanel` and using the alternate scaffold.
- Remove hardcoded sample rows.
- Load real transactions through a service/query layer.
- Support filters by date, account, memo, amount, fund, cleared status, and reconciled status where services support them.
- Show transaction header rows and detail journal-entry lines.
- Important correctness rule: when filtering/displaying activity for a selected account, show the amount of the entry for that account, not the total amount of the whole transaction.
- Add New/Edit/Open behavior using a native alternate journal entry workspace or a clearly marked temporary adapter.
- Do not silently delete posted accounting history. Use confirmation and prefer void/reverse semantics if supported.
- Add import review queue entry points only; keep actual import mechanics in Import/Export or banking import review.
- Do not migrate Documents & Attachments.
- Add a split-transaction test proving selected-account amount correctness.
```

## 3.2 Migrate Chart of Accounts editing and import/export

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate Chart of Accounts editing and chart import/export into the alternate UI.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/AccountsPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/CoaEditorPanelFX.java`
- Existing account model, account services/repositories, COA seed/populate code, and COA import/export code.

Requirements:
- Refactor `org.nonprofitbookkeeping.ui.ChartOfAccountsPanel` so it is no longer read-only.
- Add create/edit/deactivate account functionality through services, not direct table mutation.
- Validate account code uniqueness.
- Support account name, type, subtype, active/posting flags, and parent/header relationship if supported.
- Do not allow unsafe deletion of accounts with posted entries; prefer deactivate.
- Define which import changes are allowed for accounts with posted transaction history.
- Provide Import Chart of Accounts and Export Chart of Accounts actions directly or routed to Import/Export workspace.
- Provide Populate Starter Chart for empty companies if service support exists.
- Add tests for duplicate account code, invalid type/subtype, deactivate-with-history, import validation, export behavior, and starter chart population.
```

## 3.3 Migrate Funds functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate the old Funds panel into a native alternate-style funds workspace.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/FundsPanelFX.java`
- Existing fund services, fund balance services, journal services, and tests.

Requirements:
- Create `AlternateFundsPanel` or refactor `org.nonprofitbookkeeping.ui.FundsPanel` so it no longer wraps the old panel.
- Use the alternate scaffold.
- Show fund list and ledger-derived balances.
- Do not allow direct editing of display balances.
- Add/edit/deactivate funds through services.
- Implement fund reclassification/transfer as an accounting transaction with date, memo, from fund, to fund, amount, and account selection.
- Clearly distinguish bank-account movement from fund restriction reclassification.
- Add drilldown to ledger transactions for a selected fund.
- Add tests for generated journal entries and fund balance display.
```

## 3.4 Migrate Reports functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate reports into a native alternate reports workspace.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/ReportsPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/GenerateReportPanelFX.java`
- Existing report writer/generator services.
- Existing semantic workbook report services.

Requirements:
- Replace `ReportLibraryPanel` legacy adapter with a native `ReportsWorkspacePanel` using the shared scaffold.
- Build a unified report catalog that includes legacy financial reports and semantic workbook reports.
- Support report parameter selection: date range, fund, account, donor, output format, and report-specific options where supported.
- The Generate action must use the selected report and parameters; do not ignore controls.
- Show generated report history and provide open/export actions.
- Define report export naming/location conventions.
- Validate required parameters before running a report.
- Keep unsupported formats disabled with explanation.
- Add tests verifying selected report type, date range, output format, and parameters reach the generation service.
```

## 3.5 Migrate Settings functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate old Settings functionality into a native alternate settings workspace.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/SettingsPanelFX.java`
- Existing preferences/settings services.
- Theme/format utilities and account lookup services.

Requirements:
- Replace placeholder `org.nonprofitbookkeeping.ui.SettingsPanel` and/or the alternate settings pane in `MainWindowAlternate` with a real native settings panel.
- Separate app-level settings from database-level and company-level settings.
- Include organization profile, fiscal year start, currency/format, default accounts, report defaults, theme, autosave/backup options where supported.
- Validate fiscal year start and account selections.
- Persist settings through the appropriate service.
- Disable or explain company-level settings when no company is open.
- Link to Database Administration and Company Administration for lifecycle/destructive actions instead of duplicating those workflows inside Settings.
- Add tests for validation and persistence.
```

## 3.6 Migrate Donor functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate donor management into the alternate UI style.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/DonorsPanelFX.java`
- Existing `DonorService` and donor model/repository code.
- Donation-related fields in `JournalEntryWorkspaceFX`.

Requirements:
- Add a native donor workspace or record-service panel.
- Add `DONORS` as an `AppPanelId` if donor management is a first-class route.
- List donors.
- Add/edit/deactivate donors.
- Show contact information and donation history.
- Link donation transactions to donor records.
- Clarify whether donor import/export belongs in a future Import/Export extension; do not implement it unless service support already exists.
- Replace command-center direct use of `DonorsPanelFX` with the native panel or a clearly marked temporary adapter.
- Add tests for donor create/edit/list/deactivate behavior.
```

## 3.7 Migrate Inventory / Asset functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate inventory/assets into native alternate panels.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/InventoryPanelFX.java`
- `src/main/java/org/nonprofitbookkeeping/ui/AssetsRegisterPanel.java`
- `src/main/java/org/nonprofitbookkeeping/ui/DepreciationRunsPanel.java`
- Existing asset, inventory, and depreciation services/tests.

Requirements:
- Determine whether inventory and fixed assets are one model or two models before merging panels.
- Replace the legacy `InventoryPanel` adapter and demo-only `AssetsRegisterPanel` with service-backed native alternate panels.
- Asset register must load real assets.
- Add/edit/deactivate asset through services.
- Use safe numeric/date editors; do not parse formatted currency display strings as raw BigDecimal input.
- Show depreciation method, useful life, accumulated depreciation, net book value, and depreciation run links where supported.
- Preserve or improve existing depreciation run workflow.
- Add disposal workflow if model/service support exists.
- Add tests for asset save validation, money parsing, and depreciation linkage.
```

## 3.8 Migrate Reconciliation and banking functionality

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: migrate reconciliation and banking workflows into native alternate panels.

Existing sources to inspect before editing:
- `src/main/java/nonprofitbookkeeping/ui/panels/ReconcilePanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/LedgerReconcilePanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/UndepositedFundsPanelFX.java`
- Account activity import logic.
- Existing reconciliation services/tests.

Requirements:
- Create `AlternateReconciliationPanel` or equivalent native panel.
- Replace direct old reconcile panel creation in command center.
- Use a row model with JavaFX `BooleanProperty` for cleared/reconciled selection.
- Show beginning balance, statement ending balance, cleared total, and difference.
- Invalid ending balance must show validation, not silently become zero.
- Prevent reconciliation with nonzero difference unless an explicit adjustment workflow is implemented.
- Migrate statement import into a review queue before posting transactions.
- Route import/export mechanics through Import/Export workspace or a dedicated banking import panel.
- Discover supported statement import file formats before implementing UI.
- Add tests for difference calculation, checkbox binding, validation, and save behavior.
```

---

# Phase 4 — Native nonprofit/SCA workflow improvements

## 4.1 Event accounting workspace

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: add an alternate-style Event Accounting workspace.

Requirements:
- Inspect existing transaction metadata models and services for event-like fields before adding model changes.
- If an event entity exists, use it.
- If no event model exists, propose the smallest service/model addition before implementing UI.
- The workspace should list events, show event income/expense summary, linked journal transactions, deposits/refunds, and event close checklist.
- Do not invent accounting postings in UI code.
- Use services for persistence and report calculations.
- Do not add Documents & Attachments functionality.
- Add tests for event summary calculations and event-to-transaction linking where service support exists.
```

## 4.2 Monthly close / exchequer checklist

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: create a native alternate monthly close / exchequer checklist panel.

Requirements:
- Inspect existing reconciliation, report, fund balance, database export/backup, period close, and transaction validation services before editing.
- The panel should guide a branch exchequer through close steps: reconcile bank accounts, review undeposited funds, resolve pending imports, verify fund balances, generate required reports, export/backup the database, and lock/close the period if supported.
- Use service-backed checks where available.
- Show explicit not-wired states where checks are missing.
- Do not use fake completion statuses.
- Do not implement accounting rules directly in JavaFX panels.
- Add tests for checklist state calculation where service support exists.
```

## 4.3 Donation receipt and in-kind workflow

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: add native alternate donor receipt workflow.

Requirements:
- Inspect donor services, journal services, donation metadata, receipt-related fields, and existing donor tests before editing.
- Use donor and journal services to identify donation transactions.
- Provide a donor detail view showing donation history, receipt status, and annual totals.
- Add receipt-required and receipt-sent workflow where the model supports it.
- Include in-kind donation fields only if they exist in the model or after proposing a small model/service addition first.
- Do not implement donation accounting rules directly in UI code.
- Add tests for donation query, receipt status updates, and annual donor summary calculations where services support them.
```

---

# Phase 5 — Testing and quality gates

## 5.1 Add UI smoke tests for every alternate panel

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: add smoke tests for all alternate UI panels.

Requirements:
- Inspect `pom.xml`, JavaFX test setup, existing UI tests, `PanelHost`, `AppPanelId`, and `PanelHost.DefaultPanelFactory` before editing.
- Instantiate every `AppPanelId` through `PanelHost.DefaultPanelFactory` or an accessible test factory.
- Verify construction does not throw.
- Verify root nodes are non-null.
- Verify titles are non-blank.
- Verify panels with no data source do not insert realistic hardcoded accounting sample data.
- Test database/company/import/export/admin panels in no-database, database-open/no-company, and company-open states where practical.
- Respect existing Surefire exclusions and headless JavaFX configuration.
```

## 5.2 Add accounting and administration correctness tests

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: add accounting and administration correctness tests for alternate UI migration services.

Requirements:
- Focus on service/query behavior rather than JavaFX rendering where possible.
- Include tests for selected-account activity amount in a split transaction.
- Include tests for fund reclassification journal entries.
- Include tests for reconciliation difference calculation.
- Include tests for report parameter propagation.
- Include tests for budget vs actual calculation.
- Include tests for void/reverse behavior instead of unsafe deletion.
- Include tests for company create/open/close/delete/populate/create-sample workflows.
- Include tests for database import/export/repair command boundaries.
- Include tests for chart-of-accounts import/export validation.
- Include tests for SCLX import validation using existing SCLX services/tests where practical.
- Use fixtures/builders rather than production demo data.
```

## 5.3 Add migration completion checks

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: add migration guard tests for the alternate UI.

Requirements:
- Verify every `AppPanelId` has a documented migration status in `doc/ui/alternate-ui-migration-inventory.md`.
- Verify every legacy adapter is listed in the migration inventory.
- Verify unavailable command-center commands have non-empty disabled reasons.
- Verify no route shows a generic `Template pending` unless the inventory marks it intentionally pending.
- Verify Documents & Attachments are not treated as required alternate UI workflows.
- Verify company administration, database administration, import/export, chart-of-accounts import/export, SCLX import, and database repair are documented required workflows.
- Verify first-class workflow additions update `AppPanelId`, `WorkspaceRouter`, `PanelHost`, `NavigationPane`, command descriptors, migration inventory, and smoke tests together where applicable.
```

---

# Phase 6 — Retirement of old UI adapters

## 6.1 Adapter retirement plan

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: retire one legacy adapter from the alternate UI.

Requirements:
- Choose one adapter listed in `PanelHost.DefaultPanelFactory`, `MainWindowAlternate`, command-center code, or navigation code.
- Confirm a native replacement exists and has tests.
- Verify feature parity or document intentional improvement.
- Verify persistence behavior is service-backed.
- Switch the alternate shell to the native panel.
- Remove direct command-center actions that construct the old panel for that workflow.
- Update navigation/search result targets if they pointed to the adapter.
- Keep the old panel if another application mode still uses it.
- Update `doc/ui/alternate-ui-migration-inventory.md` to mark the adapter retired.
- Run relevant tests or document why they could not be run.
```

## 6.2 Final old-interface functionality audit

### Standalone GPT execution prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: perform a final old-interface functionality audit.

Requirements:
- Search `src/main/java/nonprofitbookkeeping/ui/panels`, `src/main/java/nonprofitbookkeeping/ui`, and `src/main/java/org/nonprofitbookkeeping/ui`.
- Look for buttons, menu actions, toolbar actions, service calls, report generation, import/export, file chooser use, database open/export/import/repair, company create/open/switch/populate/sample/delete, reconciliation actions, donor actions, fund actions, journal editing actions, and direct legacy panel construction.
- Produce a checklist showing whether each user-visible capability exists in the alternate UI.
- If missing, create TODO entries in `doc/ui/alternate-ui-migration-inventory.md`.
- Documents & Attachments are intentionally out of scope; list them only in an out-of-scope note if discovered.
- Do not change Java code in this step unless the only change is documentation.
```

---

# Suggested implementation order

1. Create migration inventory.
2. Establish build/test baseline.
3. Remove or label demo data.
4. Add `UiSessionContext`.
5. Refactor service registry into context-bound provider.
6. Strengthen `AppPanel` lifecycle/save results.
7. Add shared panel scaffold and CSS.
8. Define admin operation safety contracts.
9. Add company administration workspace.
10. Add database administration workspace.
11. Add import/export workspace, including database, chart of accounts, and SCLX.
12. Replace dashboard fake cards.
13. Migrate ledger/register and journal entry workflow.
14. Migrate chart of accounts editing plus COA import/export hooks.
15. Migrate funds.
16. Migrate reports.
17. Migrate settings.
18. Migrate donors.
19. Migrate inventory/assets/depreciation into one coherent asset workflow.
20. Migrate reconciliation/banking/import review.
21. Add nonprofit/SCA workflows: event accounting, monthly close, donation receipts.
22. Add migration guard tests.
23. Retire adapters one at a time.
24. Run final old-interface functionality audit.

---

# Acceptance criteria for the alternate UI becoming primary

- [ ] No primary workflow displays realistic hardcoded accounting data.
- [ ] Opening database and company uses a single authoritative context.
- [ ] Core panels use a consistent scaffold and command model.
- [ ] Dashboard is service-backed.
- [ ] Company administration supports create, open/switch, close, destroy/delete, populate, and create sample company.
- [ ] Database administration supports open, import, export/backup, validate where supported, and repair/recovery.
- [ ] Import/Export workspace supports database import/export, chart of accounts import/export, and SCLX import where service support exists.
- [ ] Ledger register is service-backed and supports journal entry detail.
- [ ] Chart of accounts supports real maintenance.
- [ ] Funds workflow is native and ledger-derived.
- [ ] Reports workspace honors selected report parameters.
- [ ] Settings persist and validate.
- [ ] Reconciliation has correct checkbox state, difference calculation, and validation.
- [ ] Inventory/assets/depreciation workflows are coherent and service-backed.
- [ ] Old-interface-only functionality has been migrated or explicitly retired.
- [ ] Documents & Attachments are explicitly out of scope and not required for alternate UI completion.
- [ ] Legacy adapters are documented and temporary.
- [ ] Smoke and correctness tests pass.

---

# Completion checklist for every implementation prompt

Before considering a prompt complete, verify:

- [ ] Existing relevant classes/services/tests were inspected.
- [ ] No duplicate service/model was created when an existing one could be extended.
- [ ] UI code remains thin and service-backed.
- [ ] Production-path demo data was not added.
- [ ] Destructive/admin operations have confirmation and safe failure behavior.
- [ ] Imports have preview/validation/result reporting where possible.
- [ ] Relevant tests were added or updated.
- [ ] Compile/tests were run or the reason they could not be run was documented.
- [ ] Migration inventory was updated if workflow status changed.
- [ ] Documents & Attachments were not added.
