# AGENTS.md

Guidance for GPT/Codex-style agents working in this repository.

## Project overview

This repository contains a Java desktop bookkeeping application for nonprofit/SCA-style accounting. The codebase includes legacy Swing/JavaFX UI code, newer alternate JavaFX UI work, bookkeeping services, persistence, H2/JPA database support, import/export tooling, report generation support, and tests.

Current strategic UI work is centered on the alternate UI under:

- `src/main/java/org/nonprofitbookkeeping/ui`
- `src/main/java/org/nonprofitbookkeeping/ui/panels`

Legacy or older UI functionality that may need to be migrated is generally under:

- `src/main/java/nonprofitbookkeeping/ui`
- `src/main/java/nonprofitbookkeeping/ui/panels`

Before implementing alternate UI work, read `PLANS.md` in the repository root. It points to the current development plan and review notes.

## Build and test commands

This is a Maven project. The root `pom.xml` configures:

- Java release: 17
- JavaFX version: 21.0.6
- JUnit/Surefire test execution
- Headless JavaFX/TestFX-related system properties for tests
- Compiler exclusions for some legacy Jasper/JRXML code

Common commands:

```bash
mvn clean compile
```

```bash
mvn test
```

```bash
mvn clean package
```

Useful targeted test examples:

```bash
mvn -Dtest=H2SchemaMigratorTest test
```

```bash
mvn -Dtest=SclxImportServiceTest test
```

```bash
mvn -Dtest=SclxImportExportServiceUnitTest test
```

Notes:

- The default Surefire configuration excludes many UI tests, `*FXTest.java`, `*UITest.java`, and tests under `**/ui/**/*`.
- Do not assume UI tests run in the normal `mvn test` path. Inspect `pom.xml` before changing test configuration.
- The project uses `useModulePath=false` for tests.
- Some JavaFX tests may require headless setup or may be intentionally excluded.
- If a command cannot be run in the current environment, document the reason and the command that should be run locally.

## Code style guidelines

- Use Java 17-compatible code.
- Keep JavaFX panel classes thin. UI classes should bind controls, render state, and call services; business rules belong in services.
- Prefer service/query DTOs over direct database calls from JavaFX panels.
- Avoid creating parallel services if an existing service can be extended safely.
- Inspect existing classes and tests before adding new APIs.
- Preserve accounting correctness over UI convenience.
- Avoid static/global context for new alternate UI work. Prefer context-aware service providers.
- Do not add realistic sample/demo accounting data in production UI paths.
- Sample data belongs only in explicit sample-company or populate-company workflows.
- Use clear empty, loading, error, disabled, and unsupported states instead of fake values.
- Use CSS/style classes for JavaFX styling rather than adding more inline style strings where practical.
- Do not migrate or expand Documents & Attachments functionality under the current alternate UI plan.

## Testing instructions

For every code change:

1. Identify the relevant existing tests before editing.
2. Add or update tests for changed service behavior.
3. Prefer service-level tests for accounting, import/export, database, company, and reconciliation behavior.
4. Use UI tests only where UI behavior itself is the subject of the change.
5. Run the narrowest relevant test command first.
6. Run a broader Maven test command before finalizing when practical.
7. If tests fail for pre-existing reasons, document the failure and avoid masking it.

Important test areas for current work:

- SCLX import/export:
  - `src/test/java/nonprofitbookkeeping/importer/sclx/SclxImportServiceTest.java`
  - `src/test/java/nonprofitbookkeeping/importer/sclx/SclxImportExportRoundTripTest.java`
  - `src/test/java/nonprofitbookkeeping/importer/sclx/SclxImportExportServiceUnitTest.java`
  - `src/test/java/nonprofitbookkeeping/importer/sclx/RunScopedSclxExportServiceIntegrationTest.java`
- Database repair/migration:
  - `src/test/java/nonprofitbookkeeping/tools/H2SchemaMigratorTest.java`
- Company UI legacy behavior:
  - `src/test/java/nonprofitbookkeeping/ui/panels/CreateOrEditCompanyPanelFXTest.java`

When adding alternate UI migration work, also update or create migration guard tests where appropriate.

## Security considerations

This project handles bookkeeping data. Treat database files, company files, imports, exports, and backups as sensitive user data.

- Never commit real user financial data, company files, database files, exports, or backups.
- Do not log full sensitive data records, credentials, tax IDs, account numbers, or donor/private contact details.
- For database import/export/repair, show source and target paths clearly and avoid accidental overwrite.
- Require confirmation for destructive operations such as company destroy/delete, database repair overwrite, or import commit that changes existing data.
- Prefer backup/export before destructive operations.
- Import workflows should preview and validate before commit wherever technically possible.
- File importers must validate expected file types and handle malformed input safely.
- Avoid path traversal or unsafe file writes when exporting/importing.
- Do not add secrets, credentials, API keys, local absolute paths, or machine-specific configuration to committed files.

## Plans

Read `PLANS.md` before beginning substantial work. The active alternate UI plan and review are currently:

- `doc/ui/alternate-ui-development-plan.md`
- `doc/ui/alternate-ui-development-plan-review.md`
