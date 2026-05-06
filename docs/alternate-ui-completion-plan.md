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
