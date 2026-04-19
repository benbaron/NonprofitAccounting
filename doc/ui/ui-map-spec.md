# Dual UI maps for pane selection (`org.nonprofitbookkeeping.ui` + `nonprofitbookkeeping.ui`)

Use this document to mix-and-match panes from both UI stacks.

## Map A — Present state of `org.nonprofitbookkeeping.ui` (SCA/Jakarta shell)

### Shell layout and navigation model
- **Layout**: top chrome (menu + toolbar), left navigation tree, center panel host, right inspector pane.
- **Routing key**: `AppPanelId` enum values are the stable route IDs.
- **Current navigation groupings**:
  - Operations: Ledger Register, Transaction Editor, Schedules, Budget Editor, Budget vs Actual, Asset Register, Depreciation Runs
  - Outputs: Reports Library
  - Reference: Chart of Accounts, Funds
  - System: Settings

### Route map (current implementation status)
| Route ID | Current class | Present-state quality | Notes |
|---|---|---|---|
| `DASHBOARD` | `DashboardPanel` | **Hybrid / data-backed** | Shows fund balances + embeds imported `DashboardPanelFX` workspace. |
| `LEDGER_REGISTER` | `LedgerRegisterPanel` | **Read-only** | Register-only surface; no transaction editor route. |
| `SCHEDULES` | `SchedulesPanel` | **Partially wired** | Account-driven tab gating works; schedule tab content still placeholder text. |
| `BUDGET_EDITOR` | `BudgetEditorPanel` | **Skeleton** | Header/actions only; TODO center content. |
| `BUDGET_VS_ACTUAL` | `BudgetVsActualPanel` | **Skeleton** | Header/actions only; TODO report content. |
| `ASSETS_REGISTER` | `AssetsRegisterPanel` | **Skeleton** | Header/actions only; TODO register content. |
| `DEPRECIATION_RUNS` | `DepreciationRunsPanel` | **Skeleton** | Header/actions only; TODO wizard/preview content. |
| `REPORT_LIBRARY` | `ReportLibraryPanel` | **Skeleton+layout** | Report list + placeholder parameters/preview panes. |
| `CHART_OF_ACCOUNTS` | `ChartOfAccountsPanel` | **Read-only wired** | Async load of active accounts; create flow still placeholder. |
| `FUNDS` | `FundsPanel` | **Read-only wired** | Async load of active funds; create flow still placeholder. |
| `SETTINGS` | `SettingsPanel` | **Skeleton** | Minimal shell with TODO center content. |

### Shell-level caveats
- Many top menu actions are intentionally “not wired yet” placeholders.
- Inspector is present and reusable, but several contexts are still placeholder content.

---

## Map B — Present state of `nonprofitbookkeeping.ui` (existing app workspace)

### Shell layout and navigation model
- **Layout**: top menu bar + center content region.
- **Center mode**:
  - Company-selection pane when no company is open.
  - Main workspace as a tabbed `TabPane` when company is open.
- **Routing key**: `MainApplicationView.PanelType` enum values.

### Route map (tab/workspace state)
| PanelType | Current tab title | Present-state quality | Notes |
|---|---|---|---|
| `DASHBOARD` | Dashboard | **Skeleton** | Uses `SkeletonDashboardPanel`. |
| `JOURNAL` | Journal | **Mostly functional skeleton** | Uses `SkeletonJournalPanel`; real journal workflows exist but still scaffold-style. |
| `COA` | Chart of Accounts | **Functional editor** | Uses `CoaEditorPanelFX`; updates company COA and supports editing flows. |
| `COA_TABLE` | Chart of Accounts Table | **Functional/reference** | Uses `ChartOfAccountsTablePanelFX`. |
| `REPORTS` | Reports | **Skeleton** | Uses `SkeletonReportsPanel`. |
| `INCOME_STATEMENT` | Income Statement | **Functional report panel** | Uses `IncomeStatementPanelFX`. |
| `BALANCE_SHEET` | Balance Sheet | **Functional report panel** | Uses `BalanceSheetPanelFX`. |
| `ACCOUNT_DETAILS` | Account Details | **Functional report/detail panel** | Uses `AccountTransactionDetailsPanelFX`; defaults configurable. |

### Companion panes/components often used by menu actions
| Component | Present-state quality | Notes |
|---|---|---|
| `DashboardPanelFX` | Functional workspace | Already embedded in Map A dashboard (hybrid pattern). |
| `JournalPanelFX` / `GeneralJournalEntryPanelFX` / `JournalEntryWorkspaceFX` | Functional building blocks | Useful candidates to replace Map A ledger/editor skeletons. |
| `ReportsPanelFX` / `GenerateReportPanelFX` | Functional building blocks | Good candidates to replace Map A report library placeholders. |
| `FundsPanelFX` | Functional panel | Candidate to replace/augment Map A funds panel. |
| `SettingsPanelFX` | Functional panel | Candidate to replace Map A settings skeleton. |
| `CoaEditorPanelFX` | Functional editor | Candidate if Map A requires full COA editing (not read-only). |

---

## How to send me your mix/match request (copy/paste template)

```yaml
ui_mix_map:
  shell_choice:
    top_shell: "org.nonprofitbookkeeping.ui | nonprofitbookkeeping.ui | hybrid"
    nav_style: "left-tree | tabbed | hybrid"
    inspector: "required | optional | none"

  panel_selection:
    - target_route: "LEDGER_REGISTER"
      source: "nonprofitbookkeeping.ui"
      source_component: "JournalPanelFX"
      keep_route_id: true
      notes: "Replace entire center pane"

      source: "nonprofitbookkeeping.ui"
      source_component: "JournalEntryWorkspaceFX"
      keep_route_id: true
      notes: "Open from ledger double-click + New"

  flow_priority:
    - id: "daily-cash-receipt"
      must_work_first: true
      steps:
        - "Open ledger"
        - "Create txn"
        - "Post/validate"
        - "Return + see txn"

  inspector_behavior:
    - context: "ledger-row-select"
      show: ["txn-summary", "drcr-preview", "audit-fields"]

  acceptance:
    - "No placeholder alerts in selected flows"
    - "Selected route opens within 250ms on warm navigation"
```

If you fill this out, I can implement the migration directly in route order and keep each chosen panel isolated enough for incremental rollout.
