# Design Goal Assessment (A/B Shell Merge)

Date: 2026-03-29

Scope compared:
- **A** = `nonprofitbookkeeping.ui.NonprofitBookkeepingFX`
- **B** = `org.nonprofitbookkeeping.ui.FxMain` + `org.nonprofitbookkeeping.ui.MainApp` + `org.nonprofitbookkeeping.ui.MainWindow`

Evaluation granularity: function blocks (smaller than whole methods when possible).

## Stated design goals checked

`MainApp` documents the shell design goals as:
1. office-like top menu + toolbar,
2. left navigation tree,
3. center workspace with panels,
4. right-side inspector panel.

Current implementation in `MainWindow` satisfies those shell goals via top chrome composition and a 3-way split (`NavigationPane`, `PanelHost`, `InspectorPane`).

## A/B decisions by function block (current state)

| Function block | A/B pick | Current evidence |
|---|---|---|
| Entry-point launcher split | **B** | `FxMain.main` is a minimal launcher into `MainApp`; startup shell wiring remains in `MainApp.start`. |
| Scene + shell bootstrap | **B** | `MainApp.start` constructs `MainWindow`, scene, shortcuts, plugin initialization call, and startup settings load. |
| Startup orchestration depth (runtime concerns) | **A** (still richer), but converging | Legacy A still includes stage decoration/logging bridge/runtime property setup and explicit plugin list bootstrapping before menu build. |
| Plugin bootstrap ownership | **B (adopted)** | `MainWindow.initializePlugins` now owns plugin bootstrap into the B menu host when stage/menu are available. |
| Shell composition (menu/toolbar/nav/workspace/inspector) | **B** | B explicitly composes menu+toolbar top chrome and a left/center/right split-pane workspace shell. |
| Database workflow surface (open/create/import/export/query) | **B** | B `Database` menu includes wizard/open/create/select + import legacy archive + H2 script import/export + SQL query. |
| Fundraising navigation model | **B** | B routes fundraising through panel-based entries (`Donors`, `Grants`, `Funds`) in its shell menu. |
| Company lifecycle UX retention | **A semantics retained, hosted in B** | Legacy A still contains mature open/close/save/create/edit company flow logic; merge intent keeps full flow while moving shell ownership to B. |
| Settings/theme pipeline | **A semantics retained, bridged in B** | A has centralized `ensureSettingsLoaded` + `applyGlobalSettings`; B currently applies startup settings through `SettingsStartupCoordinator`. |
| Autosave/shutdown behavior | **A semantics retained, refactor target** | A contains explicit autosave lifecycle events (`STARTUP`, `COMPANY_OPENED`, `SETTINGS_SAVED`, `SHUTDOWN`) and scheduling logic that remains the behavior baseline. |

## Determination: are we meeting stated design goals?

### Yes for shell-level UX goals
The documented shell goals in `MainApp` are met in B today (top chrome, left nav, center panel host, right inspector).

### Partially for migration/system goals
The migration ledger goals are **partially met**:
- **Met/aligned:** B owns shell composition, plugin bootstrap entry, and full DB workflow menu surface.
- **In progress:** full company lifecycle parity and autosave trigger refactor are still split between A legacy behavior and B shell ownership.
- **Decisioned but not fully closed:** low-usage report retirement and remaining legacy feature decommissioning are tracked, but not all parity/retirement checks are closed.

## Short conclusion
Use **B as the authoritative shell** and continue porting retained A runtime semantics (company flow, settings lifecycle, autosave semantics) until A is no longer operationally required.

## Code review findings (current)

1. **Startup responsibilities are still split across A and B**
   - B owns shell construction and plugin bootstrap entry.
   - A still owns richer runtime initialization (stage decoration, runtime flags, explicit plugin list loop, autosave lifecycle implementation).
   - Risk: duplicated startup contracts while both shells remain runnable.

2. **Company lifecycle parity is not yet fully centralized in B**
   - B exposes company menu affordances (`Company Wizard`, `Add Company`, `Close Company`) and state labels.
   - A still contains the mature open/close/save/create/edit flow implementation and associated state gating logic.
   - Risk: behavior drift if both paths evolve independently.

3. **Autosave trigger refactor remains incomplete**
   - A has explicit lifecycle-triggered autosave scheduling/cancellation semantics.
   - B does not yet own equivalent lifecycle trigger orchestration end-to-end.
   - Risk: inconsistent persistence timing after migration cutover.

### Offer to fix
If you want, I can implement the next pass in this order:
1. move autosave lifecycle trigger ownership into B,
2. wire full company open/close/save/create/edit parity into B command handlers,
3. remove duplicate startup runtime concerns from A once parity tests pass.
