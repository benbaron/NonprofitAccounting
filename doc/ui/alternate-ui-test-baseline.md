# Alternate UI build and test baseline

Established: 2026-06-17

## Scope inspected

This baseline was established before alternate UI migration implementation by inspecting:

- Root Maven configuration in `pom.xml`.
- Test sources under `src/test/java` and resources under `src/test/resources`.
- CI workflow `.github/workflows/maven-tests.yml`.
- JavaFX/TestFX setup and test naming patterns.
- Current alternate UI plan and review notes.

No production Java code was changed for this step.

## Compile command

Use the standard Maven compile command from the repository root:

```bash
mvn clean compile
```

Baseline result on 2026-06-17: **passed**.

Notes:

- The project compiles with Maven's compiler plugin using Java release 17.
- Some legacy Jasper/JRXML-related Java sources are excluded from the active compiler path by `pom.xml`.
- The local run used the available toolchain in this container; CI config installs Temurin JDK 21 while Maven still compiles with release 17.

## Primary non-UI test command

Use the standard Maven test command from the repository root:

```bash
mvn test
```

Baseline result on 2026-06-17: **passed**.

Observed result:

- Default Surefire run: `Tests run: 266, Failures: 0, Errors: 0, Skipped: 0`.
- The additional `skeleton-panel-reset-tests` Surefire execution completed without changing the build result.
- The configured console-launcher execution was skipped because `skip.console.launcher` defaults to `true`.
- Overall Maven result: `BUILD SUCCESS`.

CI currently runs:

```bash
mvn test -q
```

## JavaFX and headless notes

The Maven Surefire configuration includes JavaFX/TestFX-related headless properties even though most UI tests are excluded from the default test set:

- `testfx.toolkit=glass`
- `testfx.robot=glass`
- `testfx.headless=true`
- `glass.platform=Monocle`
- `monocle.platform=Headless`
- `prism.order=sw`
- `prism.text=t2k`
- `prism.es2=false`
- `prism.allowhidpi=false`
- `java.awt.headless=false`

The test dependencies include TestFX and Monocle. This lets selected JavaFX-adjacent tests run in headless environments, but it does not mean all UI tests are part of the normal Maven baseline.

## UI-test behavior and naming patterns

Default `mvn test` intentionally excludes broad UI coverage by Surefire patterns:

- `**/ui/**/*`
- `**/*FXTest.java`
- `**/*UITest.java`
- `**/ui/SimpleUITest.java`
- `**/core/ApplicationContextImplTest.java`

Important implications for alternate UI migration work:

- Do not assume JavaFX panel tests run in the default CI path.
- UI tests are still present under packages such as `src/test/java/nonprofitbookkeeping/ui`, `src/test/java/nonprofitbookkeeping/ui/panels`, and `src/test/java/org/nonprofitbookkeeping/ui`.
- Some non-UI or guard tests under UI-adjacent names may run when they do not match the excluded path/name combinations.
- Use UI tests only when UI behavior is the subject of the change; prefer service-level tests for business behavior.

Common naming patterns observed in `src/test/java` include:

- `*Test.java` for regular JUnit tests.
- `*FXTest.java` for JavaFX panel tests, excluded by default.
- `*UITest.java` for UI tests, excluded by default.
- `*SmokeTest.java`, `*E2ETest.java`, and `*ValidationTest.java` for targeted smoke, end-to-end, and schema validation checks.

## Known failing tests at baseline

No failing tests were observed in the default baseline commands run on 2026-06-17:

- `mvn clean compile` passed.
- `mvn test` passed with 266 default tests and zero failures/errors/skips.

Log output during tests included expected warning/error messages from negative-path tests, including malformed import-file handling and route attempts against intentionally empty test databases. Those messages did not fail the build.

## Relevant targeted test commands for alternate UI migration work

Run the narrowest relevant test first, then run the broader Maven baseline when practical.

### Import/export and SCLX

```bash
mvn -Dtest=SclxImportServiceTest test
```

```bash
mvn -Dtest=SclxImportExportServiceUnitTest test
```

```bash
mvn -Dtest=SclxImportExportRoundTripTest test
```

```bash
mvn -Dtest=RunScopedSclxExportServiceIntegrationTest test
```

Also consider `FileImportServiceTest`, `FileExportServiceTest`, and import/export guard tests when changing supported file formats or import/export workflows.

### Database repair, migration, and schema compatibility

```bash
mvn -Dtest=H2SchemaMigratorTest test
```

Additional database/schema coverage exists under `src/test/java/nonprofitbookkeeping/schema` and includes compatibility, Flyway baseline, backfill, and lifecycle validation tests. Use these when database administration, repair/recovery, Flyway migration, or schema ownership changes are involved.

### Company administration and legacy company UI behavior

```bash
mvn -Dtest=CreateOrEditCompanyPanelFXTest test
```

This is a JavaFX-style legacy company panel test and is excluded from default `mvn test` by naming/path patterns. Expect to run it explicitly only when working on company UI migration or legacy behavior equivalence. Company model and persistence behavior also has non-UI coverage in `CompanyTest` and `CompanyPersistenceTest`.

### Alternate UI guard and shell/context tests

Useful existing tests for alternate UI migration guardrails include:

```bash
mvn -Dtest=AlternateUiNoProductionDemoDataTest test
```

```bash
mvn -Dtest=UiSessionContextTest test
```

```bash
mvn -Dtest=AppPanelContractTest,PanelHostLifecycleTest,PhaseOneWorkflowStateTest test
```

```bash
mvn -Dtest=MapDrivenRoutingTest test
```

## Guidance for future alternate UI migration changes

- Keep JavaFX panels thin: bind controls, render state, and call services; keep accounting and persistence rules in services.
- Prefer service-level tests for accounting, import/export, database, company, reconciliation, posting, and migration behavior.
- Add or update UI tests only when the UI behavior itself is the change being verified.
- Preserve the default no-demo-production-data guard by running `AlternateUiNoProductionDemoDataTest` when changing alternate UI panels.
- If touching SCLX import/export, reuse existing SCLX services and run the SCLX tests listed above.
- If touching database repair/migration/open workflows, run `H2SchemaMigratorTest` and the relevant schema/Flyway validation tests.
- If touching company administration workflows, verify duplicate/invalid/destructive-company behavior through service tests where possible and run explicit company UI tests when UI behavior is changed.
- After targeted tests pass, run `mvn test` before finalizing when practical.
