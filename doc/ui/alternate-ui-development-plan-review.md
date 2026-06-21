# Review of `alternate-ui-development-plan.md`

This review checks the alternate UI development plan for completeness and records prompt-strengthening guidance. The main plan now uses standalone prompt blocks: each prompt tells the agent which plan files to read and includes the surrounding requirements directly inside the prompt.

This review file should also remain useful when copied into another agent session. Therefore, every prompt below is written as a standalone command.

## Summary verdict

The alternate UI plan is complete enough to guide development. The important prompt-discipline rules are:

- A copied prompt must not rely on unspoken surrounding bullets.
- Every copied prompt should tell the agent to read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and this review file.
- Every implementation prompt should include local checklist details, not just a short task title.
- Every implementation prompt should require inspection of existing repository classes/tests before adding new APIs.
- Every implementation prompt should require compile/test verification or a written reason why verification could not be run.
- Every destructive or import/export prompt should include safety, backup, preview, validation, commit, result-summary, and failure/rollback expectations.
- Every prompt should preserve the current scope rule: do not add Documents & Attachments functionality.

## Repository-specific source references

Future prompts should name concrete classes where practical.

### SCLX import/export sources

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

### Company handling sources

- `src/main/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/CreateOrEditCompanyActionFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/CompanySelectionPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/OpenCompanyFileActionFX.java`
- `src/test/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFXTest.java`

### H2/database repair and migration sources

- `src/main/java/nonprofitbookkeeping/tools/H2SchemaMigrator.java`
- `src/main/java/org/nonprofitbookkeeping/ui/DatabaseOpenService.java`
- `scripts/migrate_h2_schema.py`
- `src/test/java/nonprofitbookkeeping/tools/H2SchemaMigratorTest.java`

---

# Standalone prompts for improving the plan itself

## Prompt A — Audit all prompts for standalone copy/paste use

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: audit all prompt blocks in `doc/ui/alternate-ui-development-plan.md` and `doc/ui/alternate-ui-development-plan-review.md` for standalone copy/paste usefulness.

Requirements:
- Every prompt block must be understandable without relying on nearby prose.
- Every prompt must either tell the agent to read the plan/review files first or include enough local requirements to perform the task safely.
- Every implementation prompt must include expected source files/classes to inspect when those are known.
- Every implementation prompt must include test expectations or explain when the task is documentation-only.
- Every admin/import/export prompt must include safety expectations: confirmation, backup guidance, preview/validation before commit, result summary, and failure behavior.
- Every prompt must preserve the current scope rule: do not add Documents & Attachments functionality.
- Update the Markdown files only; do not change Java code.
```

## Prompt B — Add universal prompt preamble to any weak prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: find prompt blocks in `doc/ui/alternate-ui-development-plan.md` and `doc/ui/alternate-ui-development-plan-review.md` that do not begin with enough context for standalone use, and strengthen them.

Requirements:
- Each strengthened prompt should begin by telling the agent it is working in the NonprofitAccounting repository.
- Each strengthened prompt should instruct the agent to read `AGENTS.md`, `PLANS.md`, the plan, and the review before starting.
- Each strengthened prompt should state the exact task.
- Each strengthened prompt should include the key checklist items from its surrounding section.
- Each strengthened prompt should include relevant files/classes to inspect where known.
- Each strengthened prompt should include testing or documentation-only expectations.
- Do not change production Java code.
```

## Prompt C — Strengthen company administration prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: verify and strengthen the Company Administration prompt in `doc/ui/alternate-ui-development-plan.md` so it is standalone.

The prompt must require inspection of:
- `src/main/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/CreateOrEditCompanyActionFX.java`
- `src/main/java/nonprofitbookkeeping/ui/panels/CompanySelectionPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/OpenCompanyFileActionFX.java`
- `src/test/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFXTest.java`
- `CurrentCompany`, company model/repository code, `MainWindowAlternate`, and `AlternateDataContextService`.

The prompt must include requirements for:
- listing companies;
- opening/switching company;
- closing active company;
- creating company;
- defining exactly what destroy/delete company means;
- preventing unsafe active-company deletion;
- requiring strong confirmation for destructive delete/destroy;
- recommending or requiring backup before destructive operations;
- populating empty companies safely;
- creating deterministic sample companies only through explicit user action;
- adding tests for duplicate names, invalid required fields, delete active company, populate already-populated company, and sample creation.

Update only the Markdown prompt text.
```

## Prompt D — Strengthen database administration prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: verify and strengthen the Database Administration prompt in `doc/ui/alternate-ui-development-plan.md` so it is standalone.

The prompt must require inspection of:
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
- `src/main/java/org/nonprofitbookkeeping/ui/DatabaseOpenService.java`
- `src/main/java/nonprofitbookkeeping/tools/H2SchemaMigrator.java`
- `scripts/migrate_h2_schema.py`
- `src/test/java/nonprofitbookkeeping/tools/H2SchemaMigratorTest.java`
- existing database open/import/export/backup call sites.

The prompt must include requirements for:
- Open Database;
- Close Database;
- Import Database;
- Export/Backup Database;
- Validate Database where supported;
- Repair/Recover H2 Database;
- Migrate Schema where supported;
- clear source/target/backup/result paths;
- no overwrite of active database without backup and confirmation;
- blocking or closing active context when exclusive file access is required;
- recent database list updates only after successful open/import;
- async progress/status for long-running operations;
- tests for invalid path, unsupported extension, repair failure, export target exists, command availability, and open-after-repair.

Update only the Markdown prompt text.
```

## Prompt E — Strengthen import/export prompt

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: verify and strengthen the Import/Export prompt in `doc/ui/alternate-ui-development-plan.md` so it is standalone.

The prompt must require inspection of:
- `src/main/java/nonprofitbookkeeping/ui/panels/SclxImportPanelFX.java`
- `src/main/java/nonprofitbookkeeping/ui/actions/ImportSclxActionFX.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportService.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportOptions.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportResult.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxImportTarget.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/NonprofitBookkeepingSclxImportTarget.java`
- `src/main/java/nonprofitbookkeeping/importer/sclx/SclxParser.java`
- SCLX tests under `src/test/java/nonprofitbookkeeping/importer/sclx`;
- existing chart-of-accounts import/export, spreadsheet/XLSM import, JSON import/export, database import/export, and backup code.

The prompt must include requirements for:
- Database import/export;
- Chart of Accounts import/export;
- SCLX import;
- supported spreadsheet/XLSM/JSON formats where already supported;
- import modes: preview only, validate only, commit to active company, create/import into new database/company;
- export modes: active company export, full database export/backup, chart of accounts export, SCLX export if supported;
- counts for created, updated, skipped, warnings, and errors;
- blocking errors before commit;
- duplicate-code and transaction-history policy for COA import;
- reuse of existing SCLX services/tests instead of duplicating parser logic in UI code;
- clear labels distinguishing database-level import/export from company-level import/export.

Update only the Markdown prompt text.
```

## Prompt F — Add completion checklist to every copied implementation task

```text
You are working in the NonprofitAccounting repository. Before doing this task, read `AGENTS.md`, `PLANS.md`, `doc/ui/alternate-ui-development-plan.md`, and `doc/ui/alternate-ui-development-plan-review.md`.

Task: update the plan documents so every copied implementation prompt has an explicit completion checklist or points to the document-wide completion checklist.

The completion checklist must require:
- existing relevant classes/services/tests were inspected;
- no duplicate service/model was created when an existing one could be extended;
- UI code remains thin and service-backed;
- production-path demo data was not added;
- destructive/admin operations have confirmation and safe failure behavior;
- imports have preview/validation/result reporting where possible;
- relevant tests were added or updated;
- compile/tests were run or the reason they could not be run was documented;
- migration inventory was updated if workflow status changed;
- Documents & Attachments were not added.

Update only Markdown files.
```

---

# Prompt quality checklist

When reviewing any future prompt added to the plan, verify:

- [ ] It names the repository context.
- [ ] It says which plan/guidance files to read.
- [ ] It states the task in one sentence.
- [ ] It includes the local checklist or requirements.
- [ ] It names existing files/classes to inspect when known.
- [ ] It tells the agent not to invent duplicate services/models.
- [ ] It says whether Java code may be changed.
- [ ] It includes test/build expectations.
- [ ] It includes safety rules for destructive or import/export work.
- [ ] It preserves the no Documents & Attachments scope rule.

## Recommended future action

If the plan is revised again, do not shorten prompt blocks back into terse commands. The prompts are intentionally verbose so they can be copied into a new agent session without surrounding context.
