# Alternate UI Unified Migration Plan

This document merges and supersedes:
- `docs/panel-adaptation-plan.md`
- `docs/alternate-ui-parity-matrix.md`

Its single goal is to migrate **all panel routes** to alternate-shell parity using:
## How to migrate a panel with the Legacy Panel Adapter (step-by-step)

1. **Confirm route identity**
   - Locate `AppPanelId` usage in `WorkspaceRouter` and `PanelHost`.
2. **Preserve shared panel creation**
   - Ensure `PanelHost.show(<ID>)` builds/uses the classic shared panel/service path.
3. **Delegate from alternate shell**
   - Ensure `MainWindowAlternate.openPanel(<ID>)` reaches panel-host-backed flow.
4. **Guarantee save-on-dismiss**
   - On route switch, call `panelHost.saveActive()`.
   - If adapted via `LegacyPanelAdapter`, call `AdaptedPanel.saveContext()`.
5. **Wire context-gated navigation**
   - Update `AlternateNavigationModel` parent/subpanel mapping for discoverability.
6. **Verify parity workflow**
   - Open DB -> open company -> open panel -> mutate -> switch panel -> confirm no data/context loss.

---

## Universal metaprompt (required for every phase/substage)
> Read `docs/alternate-ui-unified-migration-plan.md` first.  
> Step 1: explicitly implement the concrete class and panel changes listed for the target phase/substage.  
> Step 2: explicitly review the implementation for completeness against the end-state goals:  
> (a) all panels migrated, (b) deterministic save-on-dismiss, (c) context-correct header/nav, (d) reduced legacy coupling where native replacement is planned.  
> Step 3: run focused tests for DB/company open, panel switch/save, and panel-specific parity behavior.

---

## End-state goals
- All `AppPanelId` routes are parity-complete in alternate shell.
- Navigation is context-aware (DB/company) and parent/subpanel accurate.
- Header always reflects active panel + open company state.
- Save-on-dismiss behavior is deterministic for adapted and native panels.

---

## Full panel inventory and migration target

| AppPanelId | Strategy | Phase | Primary classes to change/verify |
|---|---|---:|---|
| `DASHBOARD` | Native shell panel | 1 | `MainWindowAlternate`, `DashboardPanelFX` |
| `SETTINGS` | Native wrapper/bridge (then native parity) | 1/3 | `MainWindowAlternate`, `SettingsPanelFX` |
| `CHART_OF_ACCOUNTS` | Adapt legacy first | 1 | `WorkspaceRouter`, `PanelHost`, `MainWindowAlternate` |
| `LEDGER_REGISTER` | Adapt legacy first | 1 | `PanelHost`, `MainWindowAlternate` |
| `REPORTS_WORKSPACE` | Adapt legacy first | 2 | `PanelHost`, `MainWindowAlternate`, `ReportsPanelFX` |
| `FUNDS` | Adapt first, evaluate native later | 2 | `PanelHost`, `AccountsActivityPanelFX`, `MainWindowAlternate` |
| `INVENTORY` | Adapt first, evaluate native later | 2 | `PanelHost`, `AlternateNavigationModel`, `MainWindowAlternate` |
| `ASSETS_REGISTER` | Adapt under reports branch | 2 | `PanelHost`, `AlternateNavigationModel` |
| `BUDGET_VS_ACTUAL` | Adapt under reports branch | 2 | `PanelHost`, `AlternateNavigationModel` |
| `DEPRECIATION_RUNS` | Adapt under reports branch | 2 | `PanelHost`, `AlternateNavigationModel` |
| `BUDGET_EDITOR` | Native-first target | 3 | `MainWindowAlternate`, `JournalEntryWorkspaceFX` (if partial), budget panels |
| `SCHEDULES` | Native-first target | 3 | `MainWindowAlternate`, schedules panel classes |
| `REPORT_LIBRARY` (deprecated) | Alias only | n/a | `AppPanelId`, `WorkspaceRouter` (route to `REPORTS_WORKSPACE`) |

---

## Phase 1 — Foundation + Core workflow
### Scope
- Native shell: Dashboard + Settings framing.
- Adapted routes: COA + Ledger Register.

### Concrete class changes
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - finalize DB/company gated nav in `rebuildNavigationButtons()`
  - ensure `openPanel(...)` always performs save-on-dismiss
  - ensure header labels always reflect active context
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateDataContextService.java`
  - centralize DB/company transition side effects and active-context getters
- `src/main/java/org/nonprofitbookkeeping/ui/LegacyPanelAdapter.java`
  - keep `saveContext()` hook usable for adapted routes
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateNavigationModel.java`
  - verify mappings for dashboard, COA, ledger branches
- `src/main/java/org/nonprofitbookkeeping/ui/WorkspaceRouter.java`
  - ensure COA/ledger are panel-host-backed in alternate

### Phase 1 metaprompt
> Read `docs/alternate-ui-unified-migration-plan.md` first.  
> Step 1: implement all Phase 1 concrete class changes above, and explicitly migrate `CHART_OF_ACCOUNTS` + `LEDGER_REGISTER` using the adapter/host steps in this document.  
> Step 2: review implementation against end-state goals and list remaining gaps for reports/funds/inventory/native replacements.  
> Step 3: run tests for DB open -> company open -> COA/ledger open -> mutate -> switch -> save verified.

---

## Phase 2 — Operational coverage for remaining adapted routes
### Scope
- Reports workspace + funds/inventory + report-adjacent tools.

### Concrete class changes
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - expose/report parent/subpanel choices for reports/funds/inventory
  - show in-page warnings for unsaved context where needed
- `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - standardize lifecycle: `show`, `saveActive`, optional dirty-state query
- `src/main/java/org/nonprofitbookkeeping/ui/WorkspaceRouter.java`
  - refine decisions for reports-family and funds/inventory branches
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateNavigationModel.java`
  - include report-adjacent subpanels (`ASSETS_REGISTER`, `BUDGET_VS_ACTUAL`, `DEPRECIATION_RUNS`)
- Panel classes to verify behavior parity:
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/ReportsPanelFX.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/SkeletonReportsPanel.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/AccountsActivityPanelFX.java`

### Phase 2 metaprompt
> Read `docs/alternate-ui-unified-migration-plan.md` first.  
> Step 1: implement all Phase 2 class changes and migrate `REPORTS_WORKSPACE`, `FUNDS`, `INVENTORY`, `ASSETS_REGISTER`, `BUDGET_VS_ACTUAL`, and `DEPRECIATION_RUNS` using the adapter/host steps.  
> Step 2: review implementation against end-state goals and identify remaining native-first replacements for Phase 3.  
> Step 3: run tests for parent/subpanel rendering + save-on-dismiss across reports/funds/inventory/tool routes.

---

## Phase 3 — Native-first replacements and coupling reduction
### Scope
- Native-first: budget editor, schedules, advanced UX polish.
- Keep adaptation only where cost-effective and low-frequency.

### Concrete class changes
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - finalize command/nav consistency after native replacements
- `src/main/java/org/nonprofitbookkeeping/ui/panels/SettingsPanelFX.java`
  - complete parity for settings behavior in alternate framing
- `src/main/java/org/nonprofitbookkeeping/ui/panels/JournalEntryWorkspaceFX.java` (if used for budget-native transition)
- `src/main/java/org/nonprofitbookkeeping/ui/LegacyPanelAdapter.java`
  - reduce usage footprint where native panels replace adapted routes
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateRecentsStore.java`
  - verify recents behavior remains stable after route replacements
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateDataContextService.java`
  - verify context transitions remain deterministic

### Phase 3 metaprompt
> Read `docs/alternate-ui-unified-migration-plan.md` first.  
> Step 1: implement all Phase 3 concrete class changes and complete native-first migration for `BUDGET_EDITOR` and `SCHEDULES`, while keeping deprecated alias routing stable (`REPORT_LIBRARY` -> `REPORTS_WORKSPACE`).  
> Step 2: review implementation against end-state goals and list any final blockers to declaring parity complete.  
> Step 3: run regression tests for navigation continuity, context correctness, and no data loss on panel transitions.

---

## Per-panel execution checklist (run for each AppPanelId)
1. Route mapped in `WorkspaceRouter`.
2. Panel creation path validated in `PanelHost`.
3. Alternate route exposed in `MainWindowAlternate` nav for correct context state.
4. Save-on-dismiss verified (`panelHost.saveActive()`, adapter `saveContext()` where applicable).
5. Header + company context labels validated after navigation.
6. Focused test case exists and passes.
