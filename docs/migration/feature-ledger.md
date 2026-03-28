# Legacy-to-B Migration Ledger

This ledger inventories legacy features in `nonprofitbookkeeping.ui.NonprofitBookkeepingFX` and assigns a disposition for the B-shell migration.

Legend:
- **Keep**: Preserve functionality in merged B path.
- **Replace**: Preserve intent but move/refactor implementation in B.
- **Retire**: Remove in merged B path.

## Policy decisions applied

- Keep: plugin init (migrate into B), full DB workflows, full company flow, autosave semantics (refactor trigger points).
- Retire: SCA/Outlands import flows, low-usage reports.
- Fundraising: keep B panels as authoritative path.

---

## Startup / Shell orchestration

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| App bootstrap | JavaFX `start(Stage)` orchestration with shutdown hook and scene setup | Replace | Keep behavior, rehome orchestration into B startup split. |
| Logging bridge | SLF4J bridge init | Keep | Keep same logging behavior in B. |
| Plugin discovery | Explicit plugin initialization loop (`SCALedgerPlugin`, sample plugin) | Replace | Keep functionality, migrate ownership into B shell startup. |
| Theme bootstrap | `ThemeManager.applyTheme(scene)` at scene creation | Keep | Already aligned with typed theme preference. |

## File menu

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Company file actions | Open company / Close company / Save company | Keep | Part of full company flow retention. |
| COA import/export | Import COA (XLSX), Export COA (XLSX) | Keep | Keep unless superseded by B equivalent with parity. |
| Financial import | Import financial file (OFX/QFX) | Keep | Keep operational import path. |
| SCA import flow | Import SCLX, Import Outlands Ledger, Import SCA Ledger, Save Modified SCA Workbook | Retire | Explicitly retired by decision. |
| Statement export | Export account statement (OFX/QFX) | Keep | Keep financial export capability. |

## Edit menu

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Company editor | Create or Edit Company | Keep | Part of full company flow. |
| CoA editor | Edit Chart of Accounts | Keep | Keep core accounting maintenance path. |
| Journal editor | Edit Journal | Keep | Keep core accounting workflow. |

## Run menu

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Reports workspace entry | Reports Workspace launcher | Keep | Keep as B panel command if still used. |
| Accounting ops | Reconcile Accounts, Undeposited Funds, Sales & COGS | Keep | Keep operation workflows. |
| Ops panels | Documents & Attachments, Inventory & Depreciation | Keep | Keep operations workflow entries. |
| Reporting utility | Generate Excel Template Report | Keep | Keep unless replaced by B report pipeline equivalent. |

## Database menu (full workflow retained)

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| DB lifecycle | Open/Create H2 DB | Keep | Keep full prompt + initialization flow. |
| Legacy data migration | Import legacy `.npbk` archive | Keep | Keep migration path for historical data. |
| Script import/export | Import H2 script / Export H2 script | Keep | **Ported to B** in `org.nonprofitbookkeeping.ui.MainWindow` file menu. |
| SQL tooling | Run SQL query panel | Keep | **Ported to B** in `org.nonprofitbookkeeping.ui.MainWindow` file menu. |

## Reports menu

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Core report entries | Income Statement / Balance Sheet / Account Details | Replace | Keep high-use subset in B; retire low-usage entries after usage review. |

## Fundraising menu

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Fundraising launchers | Donors / Donations / Grants / Funds & Fund Accounting | Replace | Use B fundraising panels as authoritative route. Legacy menu ownership should be removed after parity. |

## Settings / Plugins / Help

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Settings panel | Show Settings + callback applying global settings and autosave reschedule | Keep | Preserve behavior with B lifecycle integration. |
| Plugins menu | Plugin info + plugin menu item injection | Replace | Keep behavior, migrate to B startup/menu host. |
| Help panel | Help menu launcher | Keep | Keep user help entrypoint. |

## Company lifecycle / autosave / settings behavior

| Area | Legacy feature | Disposition | Notes |
|---|---|---|---|
| Company open flow | Company selection refresh and panel handoff on DB/open/import actions | Keep | Full company flow retained. |
| Autosave semantics | Scheduling, cancellation, and shutdown-triggered save | Replace | Keep semantics, refactor trigger points to B lifecycle hooks. |
| Global settings apply | Locale, currency format, theme, title, report defaults | Keep | Keep behavior and centralize under B-owned coordinator. |

---

## Open migration questions

1. Define objective threshold/telemetry for "low-usage reports" retirement.
2. Confirm whether COA XLSX and Excel template report remain first-class or move behind plugin/tools.
3. Confirm final B menu location for SQL query tooling and DB script import/export commands.
