# Alternate UI Parity Matrix

This matrix inventories current parity across the classic `MainWindow` shell and `MainWindowAlternate`.

| Feature | Classic location | Alternate location | Status | Notes | Priority |
|---|---|---|---|---|---|
| Dashboard landing view | `MainWindow.openPanel(DASHBOARD)` + `PanelHost.create(DASHBOARD)` | `MainWindowAlternate.openPanel(DASHBOARD)` custom dashboard canvas | implemented | Alternate dashboard canvas now initializes the same dashboard data/context services used by classic `DashboardPanel` (via shared data context + dashboard service bindings). | P0 |
| Chart of Accounts route | `NavigationPane` + `PanelHost.create(CHART_OF_ACCOUNTS)` | `MainWindowAlternate.openPanel(CHART_OF_ACCOUNTS)` -> `PanelHost.show(CHART_OF_ACCOUNTS)` | implemented | Alternate delegates to shared panel-host panel creation, so the same backing services used by classic routes are reused in alternate. | P0 |
| Ledger Register route | `NavigationPane` + `PanelHost.create(LEDGER_REGISTER)` | `MainWindowAlternate.openPanel(LEDGER_REGISTER)` -> `PanelHost.show(LEDGER_REGISTER)` | implemented | Alternate delegates to shared panel-host panel creation, so the same backing services used by classic routes are reused in alternate. | P0 |
| Inventory route | `NavigationPane` + `PanelHost.create(INVENTORY)` | `MainWindowAlternate.openPanel(INVENTORY)` -> `PanelHost.show(INVENTORY)` | implemented | Alternate delegates to shared panel-host panel creation, so the same backing services used by classic routes are reused in alternate. | P0 |
| Reports Workspace route | `NavigationPane` + `PanelHost.create(REPORTS_WORKSPACE)` | `MainWindowAlternate.openPanel(REPORTS_WORKSPACE)` -> `PanelHost.show(REPORTS_WORKSPACE)` | implemented | Alternate delegates to shared panel-host reports workspace panel, reusing classic reports services and report-action pipelines. | P0 |
| Funds panel route | `NavigationPane` + `PanelHost.create(FUNDS)` | `MainWindowAlternate.openPanel(FUNDS)` -> `PanelHost.show(FUNDS)` | implemented | Alternate delegates to shared `PanelHost` panel creation for Funds, reusing the same fund repository/service stack as classic. | P1 |
| Schedules panel route | `NavigationPane` + `PanelHost.create(SCHEDULES)` | `MainWindowAlternate.openPanel(SCHEDULES)` -> `PanelHost.show(SCHEDULES)` | implemented | Uses shared panel implementation in both shells, including the same schedule service/persistence wiring used by classic. | P1 |
| Budget Editor route | `NavigationPane` + `PanelHost.create(BUDGET_EDITOR)` | `MainWindowAlternate.openPanel(BUDGET_EDITOR)` -> `PanelHost.show(BUDGET_EDITOR)` | implemented | Routed through shared `PanelHost`, preserving the same budget services/data bindings as classic. | P1 |
| Budget vs Actual route | `NavigationPane` + `PanelHost.create(BUDGET_VS_ACTUAL)` | `MainWindowAlternate` import/tools nav button -> `PanelHost.show(BUDGET_VS_ACTUAL)` | implemented | Available in alternate under Import & Tools group, and still backed by the same budget-vs-actual reporting services as classic. | P1 |
| Assets Register route | `NavigationPane` + `PanelHost.create(ASSETS_REGISTER)` | `MainWindowAlternate` import/tools nav button -> `PanelHost.show(ASSETS_REGISTER)` | implemented | Shared panel host behavior preserved, including classic backing register/depreciation services. | P1 |
| Depreciation Runs route | `NavigationPane` + `PanelHost.create(DEPRECIATION_RUNS)` | `MainWindowAlternate` import/tools nav button -> `PanelHost.show(DEPRECIATION_RUNS)` | implemented | Shared panel host behavior preserved, including classic backing register/depreciation services. | P1 |
| Alternate command center | Classic `MainWindow` menu/toolbar command discovery | `MainWindowAlternate` Command Center pane (icon rail + left-nav action) | partial | Adds grouped File/Run/Reports/Help command entry points with direct report print/schedule/export flows and classic-banking command entries; these actions invoke the same underlying action/service classes used by classic, though it is still not a full menu/toolbar parity surface. | P0 |
| Settings route | `NavigationPane` + `PanelHost.create(SETTINGS)` (and classic menu Settings panel) | `MainWindowAlternate.openPanel(SETTINGS)` -> custom alternate settings pane | partial | Decision gate resolved in Round 11: adopt embed/wrap strategy around classic `SettingsPanelFX` behavior so alternate settings continue using the same preference/config services as classic without duplicating logic. | P0 |
| File menu command surface | `MainWindow.buildMenuBar()` File menu actions | No equivalent command menu in alternate shell | missing | Alternate currently relies on left nav and dedicated DB/company actions only; no unified menu yet to surface all classic file-action service paths. | P0 |
| Edit menu command surface | `MainWindow.buildMenuBar()` Edit menu actions | No equivalent command menu in alternate shell | missing | Missing command palette/action drawer in alternate, so many classic edit-service entry points are not yet reachable. | P1 |
| Run menu command surface | `MainWindow.buildMenuBar()` Run menu actions | Partial via alternate navigation buttons | partial | Only subset exposed by nav; implemented actions call the same run/action services as classic, but many run actions remain absent. | P0 |
| Banking command surface | Classic Run menu (`Reconcile Accounts`, `Undeposited Funds`, `Documents & Attachments`) | Command Center includes direct `Reconcile Accounts`, `Undeposited Funds`, and `Documents & Attachments` panel openings; account activity/transactions route to live ledger | partial | Core banking command paths are directly wired to shared classic panels/services (reconcile, undeposited funds, documents/attachments, ledger activity) and now emit explicit success/failure inspector feedback; deeper transaction-fetch/account-link workflows remain pending. | P0 |
| Database menu command surface | `MainWindow.buildMenuBar()` Database menu actions | `Open Database` action opens selector pane | partial | Alternate selector now opens and initializes DB context via the same shared data-context/database services as classic and persists recents. | P2 |
| Reports menu shortcuts | `MainWindow.buildMenuBar()` Reports menu items | `Reports` nav button to `PanelHost.show(REPORTS_WORKSPACE)` | partial | Workspace route uses shared reports workspace panel; Command Center exposes direct one-click report print actions (Income Statement, Balance Sheet, Trial Balance), Schedule opens direct scheduling dialog (persisted via alternate preferences), and Export runs existing Excel template report action—all backed by the same reporting services/actions used by classic—with explicit success/failure feedback. | P1 |
| Fundraising menu commands | `MainWindow.buildMenuBar()` Fundraising actions (`Donors`, `Donations`, `Grants`, `Funds`) | Command Center Fundraising group opens `DonorsPanelFX`, `DonationsPanelFX`, `GrantsPanelFX`, and `Funds` route | implemented | Alternate now provides direct fundraising workflow entry points across donor/donation/grants/funds actions, all using the same fundraising panels and service layer as classic. | P1 |
| Help menu | `MainWindow.buildMenuBar()` Help action -> `HelpPanelFX` | Command Center `Help Center` shortcut opens embedded `HelpPanelFX` | implemented | Alternate now provides a direct help panel entry point from Command Center, reusing the classic help panel content/service wiring. | P2 |
| Toolbar New/Save/Find/Journal | `MainWindow.buildToolBar()` | Command Center `Toolbar-style actions` (New/Save/Find/Journal) | partial | Alternate now exposes toolbar-like commands via Command Center with active-panel-aware enable/disable for New/Save, invoking the same command handlers as classic; it still does not mirror classic toolbar placement. | P1 |
| Record services registry navigation | `NavigationPane` Record Services tree with `RecordServicePanelRegistry` | Same shared `NavigationPane` in alternate shell | implemented | Registry items still open workspace route or placeholder inspector callback through the shared record-service registry and panel resolver used by classic. | P1 |
| Context inspector behavior | `MainWindow` right-side `InspectorPane` | `MainWindowAlternate.alternateStatus` text area | partial | Alternate only updates status label, not full inspector pane UX, though messages originate from the same underlying action/service outcomes as classic. | P2 |
| Open Company workflow | `MainWindow` File menu -> `OpenCompanyFileActionFX` path | `MainWindowAlternate.openCompanySelector()` custom selector | partial | Alternate selector now opens selected persisted company, updates context, refreshes workspace, and stores recents using the same company-open/data-context services as classic. | P2 |

## How to migrate a panel with the Legacy Panel Adapter (step-by-step)

Use this sequence for each panel route that should run inside the alternate shell while still using classic panel logic.

1. **Confirm route identity**
   - Find the `AppPanelId` route in `PanelHost` and `WorkspaceRouter`.
2. **Keep panel creation in shared host**
   - Ensure `PanelHost.show(<ID>)` can create/show the panel from classic services.
3. **Ensure alternate route delegates**
   - In `MainWindowAlternate.openPanel(...)`, keep that route in the panel-host-backed path.
4. **Enable save-on-dismiss**
   - Ensure `panelHost.saveActive()` is called on panel switch.
   - If panel is adapted through `LegacyPanelAdapter`, ensure `saveContext()` is called on leave.
5. **Wire context-gated nav**
   - Add parent/subpanel mapping in `AlternateNavigationModel` where needed.
6. **Parity verification**
   - Open DB -> open company -> open target panel -> perform one edit -> switch panel -> confirm save behavior and no context loss.

## Concrete per-panel migration instructions (all panel routes in this matrix)

### `DASHBOARD`
- Keep native in alternate shell (`MainWindowAlternate` custom dashboard area).
- Ensure data bindings use shared dashboard services (not duplicated logic).
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/DashboardPanelFX.java`

### `CHART_OF_ACCOUNTS`
- Keep as adapted/shared panel-host route.
- Required wiring:
  - `WorkspaceRouter` marks as panel-host-backed.
  - `PanelHost.show(CHART_OF_ACCOUNTS)` uses shared panel creation path.
  - `MainWindowAlternate.openPanel(CHART_OF_ACCOUNTS)` delegates to panel host.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/WorkspaceRouter.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`

### `LEDGER_REGISTER`
- Same pattern as COA (adapt/shared host).
- Confirm dirty-state/save behavior on panel switch via `panelHost.saveActive()`.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`

### `INVENTORY`
- Start adapted via shared host; evaluate native rebuild later.
- Add/verify subpanel mapping in `AlternateNavigationModel` if inventory subroutes are exposed.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/AlternateNavigationModel.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`

### `REPORTS_WORKSPACE`
- Adapt first through shared host for fastest parity.
- Keep reports actions in command center pointing to shared report actions/services.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/ReportsPanelFX.java`

### `FUNDS`
- Initially adapt through shared host; decide native later based on coupling.
- If funds workflows depend on account activity/report services, validate those service calls unchanged.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/AccountsActivityPanelFX.java`

### `SCHEDULES`
- Candidate for native-first in later phase, but keep route parity through host until replacement is complete.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`

### `BUDGET_EDITOR`
- Planned native-first target; until native parity is done, keep host route stable.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`

### `BUDGET_VS_ACTUAL`
- Keep as report-adjacent adapted route under Import/Tools or reports subpanel.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/AlternateNavigationModel.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`

### `ASSETS_REGISTER`
- Keep adapted route through shared host and ensure depreciation/report dependencies remain shared.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`

### `DEPRECIATION_RUNS`
- Keep adapted route through shared host and validate execution path parity with classic.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/PanelHost.java`

### `SETTINGS`
- Native wrapper/bridge strategy: alternate shell owns framing, classic settings behavior stays shared until native parity is complete.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/MainWindowAlternate.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/panels/SettingsPanelFX.java`

### `REPORT_LIBRARY` (deprecated alias)
- Do not migrate directly.
- Route alias to `REPORTS_WORKSPACE` in router/host compatibility paths.
- Files to verify:
  - `src/main/java/org/nonprofitbookkeeping/ui/AppPanelId.java`
  - `src/main/java/org/nonprofitbookkeeping/ui/WorkspaceRouter.java`

## Metaprompt template for migrating any one panel

> Migrate `<APP_PANEL_ID>` from classic shell usage to alternate-shell parity using the legacy adapter/host path first.  
> 1) Confirm `WorkspaceRouter` routes `<APP_PANEL_ID>` to panel-host-backed flow.  
> 2) Confirm `PanelHost.show(<APP_PANEL_ID>)` builds/uses the shared panel with classic services.  
> 3) In `MainWindowAlternate`, ensure nav exposes this panel in the correct context state and `openPanel(...)` delegates to `panelHost.show(...)`.  
> 4) Ensure save-on-dismiss works (`panelHost.saveActive()`, plus adapted `saveContext()` when applicable).  
> 5) Add/adjust focused test coverage for open DB/company -> open panel -> mutate -> switch panel -> verify persisted state/no loss.
