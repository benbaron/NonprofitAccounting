# Alternate UI Completion Plan (Parity with `nonprofitbookkeeping.ui`)

## Objective
Complete the alternate dashboard-first interface so it reaches full feature and data parity with the classic `nonprofitbookkeeping.ui` shell.

---

## Phase 0 — Baseline and Parity Inventory

### Goals
- Establish exact gap list between classic and alternate shells.
- Freeze acceptance criteria for “complete”.

### Tasks
1. Inventory all classic menu actions, toolbar actions, and panel entry points.
2. Inventory all `AppPanelId` routes and record-service routes.
3. Build a parity matrix (`classic feature` -> `alternate equivalent` -> `status`).
4. Identify all placeholder/template-only screens in alternate.

### GPT Prompt (Phase 0)
```text
You are working in NonprofitAccounting. Build a parity inventory between MainWindow (classic) and MainWindowAlternate.

Tasks:
1) Enumerate all user-visible actions from MainWindow menu/toolbars.
2) Enumerate all panel routes from AppPanelId and PanelHost.create(...).
3) Enumerate all alternate routes in MainWindowAlternate.
4) Produce a Markdown parity matrix with columns:
   - Feature
   - Classic location
   - Alternate location
   - Status (implemented / partial / missing)
   - Notes
5) Save output to docs/alternate-ui-parity-matrix.md

Constraints:
- Do not change runtime behavior in this phase.
- Only add docs and minimal helper comments if needed.

Validation:
- Run mvn test -q and include result.
```

---

## Phase 1 — Shared Routing Core

### Goals
- Prevent duplicated feature logic across shells.
- Centralize workspace routing decisions.

### Tasks
1. Introduce a shell-agnostic routing adapter (e.g., `WorkspaceRouter` / `AlternatePanelPresenter`).
2. Route by `AppPanelId` to either:
   - `PanelHost` real panel, or
   - alternate composed view wrapper around real panel.
3. Keep `MainWindow` behavior unchanged while moving reusable logic into shared services.

### GPT Prompt (Phase 1)
```text
Implement a shared routing abstraction for classic and alternate shells.

Deliverables:
1) New class(es) under org.nonprofitbookkeeping.ui.routing:
   - WorkspaceRouteDecision
   - WorkspaceRouter
2) Refactor MainWindowAlternate to use WorkspaceRouter for panel/open decisions.
3) Keep MainWindow behavior unchanged.

Requirements:
- No functional regressions.
- Preserve PanelHost as source of real panel instantiation.
- Keep alternate template screens only where explicitly required.

Validation:
- mvn test -q
- Provide list of changed files and rationale.
```

---

## Phase 2 — Data Context (DB + Company) Functional Wiring

### Goals
- Make alternate DB/company selectors perform real context switching.

### Tasks
1. Replace status-only behavior with real DB/company open flow.
2. Reuse or wrap existing classic actions (`OpenCompanyFileActionFX`, DB tooling).
3. Emit context-change event and refresh active panel data.
4. Persist recent DB/company list.

### GPT Prompt (Phase 2)
```text
Wire Open Database / Open Company in MainWindowAlternate to real application context switching.

Tasks:
1) Find existing classic open-company/open-db actions and extract reusable service methods.
2) Invoke those methods from alternate selector panes.
3) Add success/error feedback in alternate status area.
4) Refresh currently visible workspace content after context changes.
5) Persist and reload recent DB/company choices.

Constraints:
- Preserve classic behavior.
- Avoid duplicate business logic between shells.

Validation:
- mvn test -q
- Add tests for context switch service(s) where feasible.
```

---

## Phase 3 — Core Panel Real-Data Plumbing

### Goals
- Remove placeholder-only behavior for core accounting modules.

### Tasks
1. Chart of Accounts -> real panel data and actions.
2. General Journal / Ledger Register -> real data and entry operations.
3. Inventory -> real data views (Transfer Orders and related sections).
4. Funds, Schedules, Budget -> real data surfaces.
5. Settings sections (Custom Fields, Localization, Expenses, Serial Number, etc.) -> real persistence-backed controls.

### GPT Prompt (Phase 3)
```text
Replace alternate placeholder screens with real-data-backed implementations.

Scope:
- CHART_OF_ACCOUNTS
- LEDGER_REGISTER
- INVENTORY
- FUNDS
- SCHEDULES
- BUDGET_EDITOR
- SETTINGS subsections (Custom Fields, Localization, Expenses, Serial Number)

Requirements:
- Prefer embedding/reusing existing AppPanel implementations from PanelHost.
- If alternate layout differs, wrap real panel root in alternate chrome rather than duplicating logic.
- Ensure save/new/copy/paste hooks still work.

Validation:
- mvn test -q
- Add/update tests for services touched.
- Provide parity checklist updates.
```

---

## Phase 4 — Reports Parity (GL/TB/BS and Actions)

### Goals
- Full reports workspace parity with print/export/schedule workflows.

### Tasks
1. Integrate real reports workspace for General Ledger, Trial Balance, Balance Sheet.
2. Implement basis/date filter bar with report refresh behavior.
3. Wire print/export/schedule actions.
4. Validate report numbers against known fixtures.

### GPT Prompt (Phase 4)
```text
Implement full reports parity in alternate UI.

Tasks:
1) Replace AlternateReportsOverviewView placeholders with real report components.
2) Add basis/date filter controls bound to report queries.
3) Wire Print / Export / Schedule actions to existing report actions/services.
4) Add regression checks comparing output totals to fixture expectations.

Validation:
- mvn test -q
- Include report verification summary.
```

---

## Phase 5 — Global Command Surface + Shortcuts

### Goals
- Restore discoverability of classic menu functions in alternate mode.

### Tasks
1. Build alternate command palette/action drawer containing classic menu commands.
2. Introduce shell-agnostic shortcut handler interface.
3. Enable safe shortcut installation for alternate shell.

### GPT Prompt (Phase 5)
```text
Create a command surface in alternate mode for classic menu parity.

Deliverables:
1) Alternate command palette (or action drawer) with grouped commands:
   File, Edit, Run, Database, Reports, Fundraising, Help.
2) Refactor GlobalShortcuts to use a shell-agnostic command target interface.
3) Enable shortcut installation for alternate shell without MainWindow casts.

Validation:
- mvn test -q
- Manual command mapping table in docs/alternate-ui-parity-matrix.md
```

---

## Phase 6 — Record Services and Plugin Workflows

### Goals
- Maintain compatibility for record-service and plugin-based workflows.

### Tasks
1. Ensure `RecordServicePanelRegistry` categories/actions fully operable.
2. Expose plugin flows in alternate command/nav surfaces.
3. Keep proposed/workspace distinction visually clear.

### GPT Prompt (Phase 6)
```text
Complete record-service and plugin workflow parity for alternate UI.

Tasks:
1) Audit RecordServicePanelRegistry routes and bind all to alternate surfaces.
2) Ensure plugin actions (including SCA-related flows) are reachable in alternate mode.
3) Preserve fallback behavior for non-workspace bindings.
4) Add tests or harness checks for route availability.

Validation:
- mvn test -q
- Update parity docs with pass/fail per route.
```

---

## Phase 7 — UX Polish, Accessibility, and Performance

### Goals
- Production-level quality and usability.

### Tasks
1. Replace text glyph icons with consistent icon system.
2. Keyboard navigation and focus management pass.
3. Contrast and labels accessibility pass.
4. Measure startup/panel-switch/report latency and optimize.

### GPT Prompt (Phase 7)
```text
Perform quality hardening of alternate UI.

Tasks:
1) Replace glyph-based nav icons with consistent icon assets/nodes.
2) Add keyboard traversal and focus-visible cues.
3) Ensure accessible labels and contrast compliance.
4) Benchmark and optimize panel switch/report render latency.

Output:
- docs/alternate-ui-hardening-report.md with metrics before/after.

Validation:
- mvn test -q
```

---

## Phase 8 — Release Readiness & Cutover

### Goals
- Controlled transition with rollback safety.

### Tasks
1. Ensure parity matrix is green for all critical workflows.
2. Keep classic fallback switch available.
3. Add release notes and migration notes for users.
4. Conduct UAT script and sign-off.

### GPT Prompt (Phase 8)
```text
Prepare alternate UI for production cutover.

Tasks:
1) Finalize parity matrix and mark all critical workflows complete.
2) Confirm classic fallback remains available via npbk.ui.variant=classic.
3) Write release notes and user migration guidance.
4) Produce final go/no-go checklist.

Validation:
- mvn test -q
- Include final risk register and mitigation list.
```

---

## Definition of Done
Alternate UI is complete when:
1. Every critical classic workflow has an alternate equivalent.
2. Real data and persistence flows are operational across all panels.
3. Reports/actions/shortcuts/plugins are parity-complete.
4. Automated tests pass and parity matrix is fully green.
5. Classic fallback remains available for risk-managed rollout.

---

## Round 1 status (Phase 0 + Phase 1)

### Implemented this round
- Added a concrete parity matrix at `docs/alternate-ui-parity-matrix.md` that inventories classic vs alternate feature coverage, including status and priority.
- Added shared routing core classes:
  - `org.nonprofitbookkeeping.ui.routing.WorkspaceRouteDecision`
  - `org.nonprofitbookkeeping.ui.routing.WorkspaceRouter`
- Refactored `MainWindowAlternate.openPanel(...)` to delegate route decisions to `WorkspaceRouter` while preserving existing runtime behavior (dashboard/custom alternate panes/panel-host-backed routes).

### Remaining for Phase 2
- Wire alternate Open Database and Open Company flows to real classic context-switch logic.
- Persist and reload recent database/company choices.
- Refresh active workspace content after context switches.

### Known risks
- Route mapping currently hardcodes which `AppPanelId` values are alternate custom panes; future additions require updating `WorkspaceRouter` to avoid drift.
- Alternate settings and several alternate template screens still do not host real production panel content, so parity remains partial.
- Database/company selector actions in alternate remain status-only and may mislead users until functional wiring lands in Phase 2.

## Round 2 status

### Implemented
- Alternate shell Open Database and Open Company selectors now invoke real data-context switching through a shared `AlternateDataContextService` that uses existing core/database/company persistence flows.
- Alternate selectors now persist and reload recent database paths and company IDs.
- Alternate shell refreshes the active workspace route after DB/company context changes so current content rebinds to updated context.

### Remaining for Phase 3
- Replace placeholder alternate templates for Chart of Accounts, Journal, Inventory, Reports, and Settings subsections with real panel-backed data views.
- Expand alternate status/inspector parity and richer contextual feedback beyond simple status text.

### Known risks
- Company recents are currently stored by company ID and displayed opportunistically from current DB listings; IDs that no longer exist in a selected DB are shown as fallback labels.
- Active-route refresh currently re-runs route presentation and may not trigger deeper model-level refresh hooks for every legacy panel.

## Round 3 status

### Implemented
- Split alternate context responsibilities by extracting `AlternateDatabaseContextSwitcher` for DB init/schema/repair orchestration and `AlternateRecentsStore` for recent database/company persistence + parsing.
- Simplified `AlternateDataContextService` into a thin coordination layer that normalizes context paths, delegates DB switching, and delegates recents behavior.
- Kept alternate selector routes and panel refresh behavior unchanged, preserving existing `MainWindowAlternate` UX and panel-host-backed navigation behavior.

### Test additions
- Added `AlternateRecentsStoreTest` coverage for per-database recent-company scoping, invalid recent entry filtering, and recents de-duplication ordering.
- Expanded `AlternateDataContextServiceTest` with an explicit DB-context transition test validating active-path normalization plus recent DB persistence after `openDatabase(...)`.

### Residual risks
- `openCompany(...)` still relies on legacy static persistence calls (`CurrentCompany` / `PreferencesService`), which remain harder to isolate in pure unit tests.
- Route-level refresh in alternate still depends on shell re-open semantics and may not fully exercise deeper panel-model refresh hooks in every legacy panel.

## Next metaprompt (Phase 3 execution)

```text
You are working in the NonprofitAccounting repo.

Context:
- Phase 0/1/2 are complete, including Phase 2 hardening (service split for DB switching and recents persistence/parsing).
- Current focus is Phase 3: replacing remaining alternate placeholder routes with real panel-backed behavior while preserving classic-shell behavior.

Scope:
Implement a focused “Phase 3 core panel real-data plumbing pass”.

Goals:
1) Replace placeholder-only alternate routes with real panel-backed content for:
   - CHART_OF_ACCOUNTS
   - LEDGER_REGISTER
   - INVENTORY
   - SETTINGS subsections where feasible in this pass
2) Preserve PanelHost creation semantics and classic MainWindow behavior.
3) Keep alternate-specific layout/chrome where needed, but reuse production panel roots instead of duplicating business logic.
4) Add test coverage for routing decisions and panel-host delegation for touched routes.

Requirements:
- Do not regress existing alternate routes that are already panel-host-backed (Funds/Schedules/Budget/etc.).
- Do not introduce test-only hooks in production classes.
- Keep changes reviewable and backward compatible.
- If user-visible behavior changes, update docs/alternate-ui-parity-matrix.md accordingly.
- Append a brief “Round 4 status” section to docs/alternate-ui-completion-plan.md covering:
  - refactors/rewiring done,
  - tests added/updated,
  - residual risks.

Validation:
- Run: mvn test -q
- Report exact result.

Output required:
1) changed files list
2) test result
3) short code review (risks, technical debt, recommended next fixes)
4) offer to fix review issues and failing tests immediately
```


## Round 4 status

### Implemented
- Routed alternate `CHART_OF_ACCOUNTS`, `LEDGER_REGISTER`, and `INVENTORY` through shared `PanelHost` panels by updating `WorkspaceRouter` and `MainWindowAlternate` route handling.
- Kept alternate custom-pane behavior unchanged for `SETTINGS` and `REPORTS_WORKSPACE`, preserving current alternate-only surfaces there.

### Test additions
- Added `WorkspaceRouterTest` to verify panel-host routing for CHART_OF_ACCOUNTS/LEDGER_REGISTER/INVENTORY and continued alternate-custom routing for SETTINGS/REPORTS_WORKSPACE.

### Residual risks
- `REPORTS_WORKSPACE` remains alternate-placeholder-backed and still requires full parity wiring.
- `SETTINGS` remains alternate custom content and does not yet mirror classic Settings panel behavior.


## Round 5 status

### Implemented
- Routed alternate `REPORTS_WORKSPACE` through shared `PanelHost` by removing it from alternate-custom routing decisions.
- Simplified alternate route handling so only `SETTINGS` remains an alternate-custom pane route.

### Test additions
- Updated `WorkspaceRouterTest` expectations to assert `REPORTS_WORKSPACE` now routes to panel-host panels while `SETTINGS` remains alternate-custom.

### Residual risks
- Report action-surface parity (print/export/schedule shortcuts and command mappings) still depends on broader command-surface work outside route wiring.
- `SETTINGS` remains alternate custom content and does not yet mirror classic Settings panel behavior.


## Review update (2026-05-07)

### 1) Are changes so far correct?
- **Mostly yes**: routing and tests are consistent with current implementation. `WorkspaceRouter` sends only `SETTINGS` to alternate-custom panes and routes `REPORTS_WORKSPACE` to `PANEL_HOST`, which matches `WorkspaceRouterTest` and the current `MainWindowAlternate.openPanel(...)` flow.
- **Issue found (documentation drift)**: parity docs still described Reports as an alternate template in one row. This is now corrected in `docs/alternate-ui-parity-matrix.md`.

### 2) Is the current plan correct?
- **Partially**. The phase structure remains sound, but some items are now OBE and should be removed from immediate scope:
  - OBE: “replace alternate reports template route” (already done in Round 5).
  - OBE: “core route rewiring for CHART_OF_ACCOUNTS / LEDGER_REGISTER / INVENTORY / REPORTS_WORKSPACE” (already complete).
- **Needs repair/focus shift**:
  - Move remaining Reports work to **action parity**, not route parity (print/export/schedule + command mappings).
  - Keep **SETTINGS parity** as explicit unresolved P0 item (still alternate custom pane).
  - Keep command-surface parity (File/Edit/Run/Database/Reports/Fundraising/Help + toolbar equivalents) as primary functional gap.

### Updated near-term execution focus (replacement for stale Phase 3/4 references)
1. Implement shell command surface parity (menu/toolbar equivalents + shortcut target abstraction).
2. Complete Reports action parity (print/export/schedule and discoverability).
3. Implement Settings parity strategy (embed/wrap classic panel or complete persistence-backed alternate controls).
4. Verify RecordService + plugin workflows remain reachable from alternate surfaces.
