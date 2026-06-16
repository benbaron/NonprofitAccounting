# Review of `alternate-ui-development-plan.md`

This review checks the alternate UI development plan for completeness and identifies additions that should be folded into the main plan prompts. The current plan is directionally complete: it covers alternate shell migration, old-panel functionality migration, company handling, database handling, import/export, SCLX, chart-of-accounts import/export, database repair, and explicitly drops Documents & Attachments from scope.

The main remaining issue is not missing high-level features. The main issue is that several prompts should be made stricter so that a GPT/Codex-style implementation agent does not invent APIs, skip build verification, make unsafe destructive operations, or implement UI-only behavior without service support.

## Summary verdict

The plan is complete enough to guide development, but it should be strengthened in these areas:

- Add a universal preflight prompt used before every implementation task.
- Require repository discovery before adding new service/model APIs.
- Require a compile/test baseline before and after each task.
- Require import/export/admin operations to have backup, dry-run/preview, validation, commit, result summary, and rollback/error behavior.
- Require company destroy/delete, database repair, and import commit workflows to be transaction-safe and strongly confirmed.
- Require sample/populate workflows to be explicit, repeatable, and never used as production fallback data.
- Require every workflow prompt to update the migration inventory and command availability model when it adds or retires functionality.
- Require exact inspection of existing SCLX and company/database classes rather than generic service names.

## Existing plan strengths

- Documents & Attachments are clearly dropped from scope.
- Company administration is now included: create, open/switch, close, destroy/delete, populate, and create sample company.
- Database administration is now included: open, import, export/backup, validate, repair/recover, and migration/update support.
- Import/export is now included: database, chart of accounts, SCLX, spreadsheet/XLSM/JSON where supported.
- The plan keeps accounting behavior in services rather than JavaFX panels.
- The plan correctly calls out no fake accounting data in production UI.
- The plan has good per-phase GPT prompts.

## Important repository-specific sources the prompts should name

The prompts should explicitly direct future agents to inspect existing concrete classes before inventing replacements.

For SCLX, the repository already contains a real importer/exporter area:

- `src/main/java/nonprofitbookkeeping/ui/panels/SclxImportPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/ImportSclxActionFX.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportService.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportOptions.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportResult.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportTarget.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/NonprofitBookkeepingSclxImportTarget.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxParser.java`
- `src/test/java/nonprofitbookkeeping/importer/sclx/SclxImportServiceTest.java`
- `src/test/java/nonprofitbookkeeping/importer/sclx/SclxImportExportRoundTripTest.java`
- `src/test/java/nonprofitbookkeeping/importer/sclx/RunScopedSclxExportServiceIntegrationTest.java`
- `src/test/java/nonprofitbookkeeping/importer/sclx/SclxImportExportServiceUnitTest.java`

For company handling, the repository already contains legacy UI flows:

- `src/main/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/CreateOrEditCompanyActionFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/CompanySelectionPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/OpenCompanyFileActionFX.java`
- `src/test/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFXTest.java`

For H2/database repair and open/migration work, the repository already contains:

- `src/main/java/nonprofitbookkeeping/tools/H2SchemaMigrator.java`
- `src/main/java/org/nonprofitbookkeeping/ui/DatabaseOpenService.java`
- `scripts/migrate_h2_schema.py`
- `src/test/java/nonprofitbookkeeping/tools/H2SchemaMigratorTest.java`

## Recommended addition 1 — Universal execution rules for every GPT prompt

Add this section near the top of the main plan, after Architecture principles.

```text
## Universal rules for GPT/Codex implementation prompts

Every implementation prompt in this plan should follow these rules unless the step explicitly says documentation-only:

1. Inspect existing classes, tests, and services before adding new APIs.
2. Prefer extending or adapting existing services over creating parallel services.
3. Do not invent model fields. If a needed field is missing, stop and propose the smallest model/service change first.
4. Keep JavaFX panels thin. UI code may bind controls and call services, but business rules belong in services.
5. Establish a baseline before editing: run or identify the appropriate compile/test command.
6. After editing, run the relevant compile/tests, or document why they could not be run.
7. Add or update tests for every service behavior change.
8. Update `doc/ui/alternate-ui-migration-inventory.md` when adding, replacing, retiring, or intentionally deferring a workflow.
9. Do not add Documents & Attachments functionality under this plan.
10. Do not introduce production-path demo data. Sample data belongs only in explicit sample-company/populate workflows.
11. For destructive actions, require confirmation, backup guidance, and safe failure behavior.
12. For import actions, require preview/validate/commit/result-summary behavior where technically possible.
```

## Recommended addition 2 — Phase 0.3 baseline build and test discovery

Add this after Phase 0.2.

```text
## 0.3 Establish build, test, and JavaFX execution baseline

### Checklist

- [ ] Identify the Maven/Gradle command used to compile the project.
- [ ] Identify the test command used for non-UI tests.
- [ ] Identify any JavaFX test bootstrap requirements.
- [ ] Identify whether UI tests are headless, skipped, or require a display.
- [ ] Record known failing tests before migration work begins.
- [ ] Add a short `doc/ui/alternate-ui-test-baseline.md` if no such note exists.

### GPT execution prompt

```text
Establish the build and test baseline for alternate UI migration work.

Inspect `pom.xml`, test source folders, CI files if present, JavaFX test setup, and existing test naming patterns. Determine the command to compile the project and the command to run relevant tests. Run the commands if possible.

Create `doc/ui/alternate-ui-test-baseline.md` documenting:
- compile command;
- test command;
- JavaFX/headless test notes;
- known failing tests, if any;
- which tests should be run for UI-only changes;
- which tests should be run for service/import/database/company changes.

Do not change production Java code in this step.
```
```

## Recommended addition 3 — Add an admin-operation safety model

The plan mentions backup/confirmation, but it should define a common safety contract for company deletion, database repair, database import, SCLX import, COA import, and destructive migrations.

Add this after Phase 1.5 or before Phase 2.4.

```text
## 1.6 Define admin operation safety contracts

### Checklist

- [ ] Create common DTOs for admin operation preview, validation messages, commit result, and failure summary.
- [ ] Distinguish warnings from blocking errors.
- [ ] Require confirmation text for destructive operations.
- [ ] Require backup recommendation or backup creation before destructive database/company operations.
- [ ] Require transaction/rollback behavior where database changes are committed.
- [ ] Require audit/result records where supported.
- [ ] Ensure async operations report progress and final status.

### Suggested DTO concepts

- `AdminOperationPreview`
- `AdminValidationMessage`
- `AdminCommitResult`
- `ImportPreview`
- `ImportCommitResult`
- `BackupResult`

### GPT execution prompt

```text
Define a shared safety/result contract for alternate UI administrative operations.

Inspect existing import, SCLX, H2 repair, database open, and company services before adding new types. Add common DTOs or interfaces only if no suitable equivalents exist.

The contract should support:
- preview/dry-run result;
- validation messages with severity: info, warning, error;
- destructive-operation confirmation requirements;
- backup recommendation or backup result;
- commit result with counts and output paths;
- rollback/failure summary;
- progress/status reporting for async UI operations.

Refactor no major workflows in this step unless needed for compilation. Add unit tests for the DTO/service contract behavior.
```
```

## Recommended addition 4 — Strengthen company administration prompt

Replace or augment the Company Administration prompt with stricter requirements:

```text
Additional requirements:
- Inspect `CreateOrEditCompanyPanelFX`, `CreateOrEditCompanyActionFX`, `CompanySelectionPanelFX`, `OpenCompanyFileActionFX`, company model/repository code, and any existing tests before designing new UI or services.
- Define exactly what `destroy/delete company` means: remove only a company row, remove all company-owned records, or delete a company file/database. Use the safest available interpretation and document it in the UI.
- Prevent deletion of the active company unless the workflow first closes/switches context safely.
- Require confirmation by typing the company name for destructive delete/destroy.
- Require or recommend database export/backup before destructive delete/destroy.
- Make Populate Company idempotent or detect already-populated state and explain what will happen.
- Make Create Sample Company deterministic enough for tests, with a named sample profile and no accidental use in production fallback paths.
- Add tests for duplicate company names, invalid required fields, delete active company, populate already-populated company, and sample company creation.
```

## Recommended addition 5 — Strengthen database administration prompt

Replace or augment the Database Administration prompt with stricter requirements:

```text
Additional requirements:
- Inspect `H2SchemaMigrator`, `DatabaseOpenService`, `scripts/migrate_h2_schema.py`, `H2SchemaMigratorTest`, and existing database open/repair call sites before creating new services.
- Distinguish Open Database, Import Database, Export/Backup Database, Validate Database, Repair Database, and Migrate Schema as separate operations.
- Never overwrite the active database file during repair without an explicit backup and confirmation step.
- Close or block active company/database context before operations that require exclusive file access.
- Show source path, target path, backup path, and result path in the UI before commit.
- Preserve recent database list only after successful open/import.
- Add tests for invalid path, unsupported extension, repair failure, export target exists, and open-after-repair behavior.
```

## Recommended addition 6 — Strengthen import/export prompt

Replace or augment the Import/Export prompt with stricter requirements:

```text
Additional requirements:
- Inspect existing import/export services and tests before adding new service classes.
- Define import modes: preview only, validate only, commit to active company, or create/import into new database/company.
- Define export modes: active company export, full database export/backup, chart of accounts export, SCLX export if supported.
- Every import must produce a result object with counts: created, updated, skipped, warnings, errors.
- Every import must show blocking errors before commit.
- COA import must define duplicate-code policy, account deactivation policy, and whether existing accounts with transaction history may be changed.
- SCLX import must use existing `SclxImportService`, `SclxImportOptions`, `SclxImportResult`, and target classes unless a review shows they are unsuitable.
- SCLX import/export should reuse existing SCLX tests and add UI/service boundary tests rather than duplicating parser logic in UI code.
- Database import/export should not be mixed with company-level import/export without clear labels.
```

## Recommended addition 7 — Add explicit `AppPanelId` and routing requirements

Several prompts say “add AppPanelId or route as custom admin panel.” That is flexible, but it may allow inconsistent navigation. Add a rule:

```text
When adding a first-class workflow, prefer a stable `AppPanelId` unless there is a clear reason to keep it as a transient custom pane. Update all of the following together:

- `AppPanelId`
- `WorkspaceRouter`
- `PanelHost.DefaultPanelFactory` or equivalent factory
- `NavigationPane`
- command center descriptors
- migration inventory
- smoke tests
```

## Recommended addition 8 — Add prompt completion checklist

Add this near the end of the document.

```text
## Completion checklist for every implementation prompt

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
```

## Prompt-specific gaps by section

### Phase 0

Good coverage. Add build/test baseline and universal rules.

### Phase 1

Good architectural direction. Add admin operation safety/result contracts. Also require that new context/service-provider code avoid circular dependencies with legacy `CurrentCompany`.

### Phase 2

Good coverage of dashboard, command state, search, database admin, company admin, and import/export. Needs more exact requirements for safe repair, exclusive file access, backup paths, destructive confirmation, import preview DTOs, and AppPanelId/routing consistency.

### Phase 3

Good migration coverage. Suggested improvements:

- Ledger prompt should explicitly say statement import belongs in Import/Export or Banking import review, not inside the register.
- Chart of Accounts prompt should explicitly state whether import changes are allowed against accounts with posted transactions.
- Reports prompt should include report export naming/location conventions and parameter validation.
- Settings prompt should state that database/company lifecycle destructive actions belong in admin panels, not settings.
- Donor prompt is fine, but should clarify whether donor data is imported/exported through general Import/Export in a future phase.
- Inventory prompt should clarify whether inventory and fixed assets are one model or two models before merging panels.
- Reconciliation prompt is good; add statement import file formats and import review ownership when discovered.

### Phase 4

Good SCA/nonprofit direction. Add “do not implement model changes directly in UI” to each prompt if not already present. Monthly close should include “backup/export database” and pending imports, which the plan already does.

### Phase 5

Good. Add tests for command routing after AppPanelId additions. Add tests that admin prompts do not route to generic `Template pending`.

### Phase 6

Good. Add requirement that adapter retirement also removes old direct command-center actions and updates navigation/search result targets.

## Recommended next action

Merge the recommended additions above into `doc/ui/alternate-ui-development-plan.md`, preferably as:

1. A new “Universal rules” section near the top.
2. A new Phase 0.3 build/test baseline step.
3. A new Phase 1.6 admin operation safety contract step.
4. Stronger language inside prompts 2.4, 2.5, and 2.6.
5. A final “Completion checklist for every implementation prompt.”
