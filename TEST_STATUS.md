# Test Status (2025-10-15)

## Command
- `mvn test` (fails) — see `/tmp/mvn-test.log` excerpt in chunk `ac169e`.

## Observations
- JavaFX/TestFX UI suites such as `NewTransactionPanelFXTest` fail early with `java.lang.UnsupportedOperationException: Unable to open DISPLAY`, which indicates the headless CI container lacks the native display libraries required by TestFX. 【fa6adc†L8-L26】【ac169e†L1-L19】
- Several persistence-oriented tests (`CompanyPersistenceTest`, `SalesServiceTest`) fail while initializing the H2 schema because the database rejects the `IDENTITY` column definition currently used in the migration. 【fa6adc†L4-L6】【fa6adc†L28-L30】
- `JacksonDataStorerZipTest` also fails because the generated archive omits `chart_of_accounts.json`, so the regression suite cannot round-trip a company file using the new persistence implementation. 【fa6adc†L1-L4】
- Additional report/panel UI tests are likewise blocked by the missing DISPLAY dependency, so the majority of the suite aborts before exercising application logic. 【fa6adc†L10-L27】

## Next Steps
1. Provide a headless-friendly JavaFX/TestFX configuration (Monocle or similar) or mark the UI suites @Disabled in CI so functional tests can complete.
2. Revisit the recent H2 schema changes to use column types supported by the bundled driver (e.g., replace `IDENTITY` with `GENERATED ALWAYS AS IDENTITY`).
3. Ensure `JacksonDataStorer` writes both company and chart-of-accounts documents into the zipped payload to restore backwards-compatible exports.
