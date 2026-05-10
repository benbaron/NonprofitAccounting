# Alternate Shell Panel Adaptation Plan

## Goal
Migrate the alternate shell to a stable, role-oriented navigation and panel experience while minimizing rewrite risk.

## Principles
- Prefer adapting existing panels first for high-value workflows.
- Rebuild natively only where legacy panel behavior is tightly coupled or UX no longer fits.
- Keep save-on-leave behavior consistent across adapted and native panels.

## Phase 1 (Foundation + Core Workflow)
- **Native:** Dashboard, Settings shell/state controls.
- **Adapt legacy:** Chart of Accounts, Journal.
- **Deliverables:**
  - stable DB/company open flow
  - context-sensitive navigation
  - save-on-dismiss behavior validated

### Phase 1 concrete class changes
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - finalize DB/company gate logic in `rebuildNavigationButtons()`
  - ensure `openPanel(...)` always triggers save-on-dismiss for both adapted and panel-host panels
  - ensure header labels are updated from true context (`CurrentCompany` + active panel)
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateDataContextService.java`
  - add explicit getters for active DB/company context used by nav/header
  - centralize context-transition side effects (recents, last-used state)
- `src/main/java/org/nonprofitbookkeeping/ui/LegacyPanelAdapter.java`
  - add optional hooks for `onEnter`/`onLeave` if needed by adapted panels
- `src/main/java/org/nonprofitbookkeeping/ui/AlternateNavigationModel.java`
  - add explicit parent->subpanel mapping for initial workflow panels

### Phase 1 metaprompt
> Read and follow `docs/panel-adaptation-plan.md` first, then implement Phase 1 of the alternate shell migration. Keep Dashboard/Settings native and adapt COA/Journal. Update `MainWindowAlternate`, `AlternateDataContextService`, `LegacyPanelAdapter`, and `AlternateNavigationModel` so navigation is context-gated by DB/company, save-on-dismiss is deterministic, and header/company labels always reflect active context. Add/adjust focused tests for DB/company open and panel switch behavior.

## Phase 2 (Operational Coverage)
- **Adapt legacy:** Reports workspace.
- **Evaluate adapt vs native:** Funds and Inventory based on interaction complexity.
- **Deliverables:**
  - main daily accounting workflow parity
  - navigation subpanel structure per parent panel

### Phase 2 concrete class changes
- `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - expand parent/subpanel presentation for Reports/Funds/Inventory paths
  - support in-page warning surfaces when unsaved state exists
- `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - standardize panel lifecycle calls (`show`, `saveActive`, optional dirty-state query)
- `src/main/java/org/nonprofitbookkeeping/ui/WorkspaceRouter.java`
  - refine route decisions for reports-family panels and inventory/funds branches
- Candidate adapted panels (legacy-backed):
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/ReportsPanelFX.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/SkeletonReportsPanel.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/AccountsActivityPanelFX.java` (if funds flow depends on it)

### Phase 2 metaprompt
> Read and follow `docs/panel-adaptation-plan.md` first, then implement Phase 2 operational coverage. Adapt Reports first, then evaluate Funds/Inventory adapt-vs-native based on coupling and UX fit. Update `MainWindowAlternate`, `PanelHost`, and `WorkspaceRouter` for deterministic lifecycle and routing. Add tests for parent/subpanel nav rendering and save-on-dismiss behavior when switching across reports/funds/inventory panels.

## Phase 3 (Specialized Panels + UX Polish)
- **Native-first candidates:** Budget editor, schedules, advanced tools.
- **Optional legacy adaptation:** niche/low-frequency panels where adaptation is cheaper than rebuild.
- **Deliverables:**
  - consistent alternate-shell design language
  - reduced legacy coupling in user-facing flows

### Phase 3 concrete class changes
- Native-first panel targets:
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/JournalEntryWorkspaceFX.java` (if partial replacement required)
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/SettingsPanelFX.java` (extract shared settings sections)
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java` (final nav/command-center cleanup)
- Remove or narrow adaptation wrappers where native replacements are complete:
  - `src/main/java/org/nonprofitbookkeeping/ui/LegacyPanelAdapter.java`
- Stabilize recents/context:
  - `src/main/java/org/nonprofitbookkeeping/ui/AlternateRecentsStore.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/AlternateDataContextService.java`

### Phase 3 metaprompt
> Read and follow `docs/panel-adaptation-plan.md` first, then implement Phase 3 specialized migration and polish. Build native Budget/Schedules/advanced workflows where adaptation cost is high, and shrink legacy adapter usage to only low-frequency panels. Update `MainWindowAlternate`, relevant panel classes, and context/recents services for consistency. Add regression tests for navigation continuity, header context correctness, and no-data-loss transitions.

## Decision Criteria (per panel)
- User frequency and business criticality.
- Legacy panel API coupling risk.
- Data loss risk when navigating away.
- UX mismatch between legacy panel and alternate shell.
- Testability and maintenance cost.

## Exit Criteria
- No blocker workflows require the old shell.
- Navigation state and panel-save behavior are deterministic.
- Target panel set has automated test coverage for open/switch/save flows.

## Substage checklist template (apply in every phase)
1. **Inventory:** list target panels and classify `adapt` vs `native`.
2. **Lifecycle:** verify enter/leave/save hooks and dirty-state handling.
3. **Navigation:** verify parent/subpanel choices for each context state.
4. **Context:** verify DB/company open state reflected in header + available actions.
5. **Tests:** add/adjust focused tests (open DB/company, switch panel, save-on-dismiss).
6. **Telemetry/Debug:** add concise debug logs only where needed for migration support.
