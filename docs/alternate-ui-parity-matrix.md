# Alternate UI Parity Matrix

This matrix inventories current parity across the classic `MainWindow` shell and `MainWindowAlternate`, organized by feature category.

---

## Core Routes

| Feature | Status | Classic Implementation | Alternate Implementation | Notes |
|---|---|---|---|---|
| Dashboard landing view | ✅ Implemented | `MainWindow.openPanel(DASHBOARD)` + `PanelHost.create(DASHBOARD)` | Custom dashboard canvas | Alternate dashboard canvas fully replaces classic view |
| Chart of Accounts route | ✅ Implemented | `NavigationPane` + `PanelHost.create(CHART_OF_ACCOUNTS)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Alternate delegates to shared panel host |
| Ledger Register route | ✅ Implemented | `NavigationPane` + `PanelHost.create(LEDGER_REGISTER)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Alternate delegates to shared panel host |
| Inventory route | ✅ Implemented | `NavigationPane` + `PanelHost.create(INVENTORY)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Alternate delegates to shared panel host |
| Reports Workspace route | ✅ Implemented | `NavigationPane` + `PanelHost.create(REPORTS_WORKSPACE)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Alternate delegates to shared panel host |
| Funds panel route | ✅ Implemented | `NavigationPane` + `PanelHost.create(FUNDS)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Alternate delegates to shared panel host |
| Schedules panel route | ✅ Implemented | `NavigationPane` + `PanelHost.create(SCHEDULES)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Uses shared panel implementation |
| Budget Editor route | ✅ Implemented | `NavigationPane` + `PanelHost.create(BUDGET_EDITOR)` | `MainWindowAlternate.openPanel()` → `PanelHost.show()` | Routed through shared panel host |
| Budget vs Actual route | ✅ Implemented | `NavigationPane` + `PanelHost.create(BUDGET_VS_ACTUAL)` | Import/tools nav button → `PanelHost.show()` | Available in all shells |
| Assets Register route | ✅ Implemented | `NavigationPane` + `PanelHost.create(ASSETS_REGISTER)` | Import/tools nav button → `PanelHost.show()` | Shared panel host implementation |
| Depreciation Runs route | ✅ Implemented | `NavigationPane` + `PanelHost.create(DEPRECIATION_RUNS)` | Import/tools nav button → `PanelHost.show()` | Shared panel host implementation |
| Record services registry navigation | ✅ Implemented | `NavigationPane` Record Services tree | Same shared `NavigationPane` | Registry items still operational in alternate |

---

## Menu & Command Surfaces

| Feature | Status | Classic Implementation | Alternate Implementation | Notes |
|---|---|---|---|---|
| File menu | ❌ Missing | `MainWindow.buildMenuBar()` File menu actions | No equivalent command menu | Alternate relies on left nav and dedicated DB/company selectors |
| Edit menu | ❌ Missing | `MainWindow.buildMenuBar()` Edit menu actions | No equivalent command menu | Missing command palette/action drawer in alternate |
| Run menu | ⚠️ Partial | `MainWindow.buildMenuBar()` Run menu actions | Partial via navigation buttons | Only subset exposed; implemented actions call same run/action logic |
| Banking commands | ✅ Implemented | Run menu: `Reconcile Accounts`, `Undeposited Funds`, `Documents & Attachments` | Command Center with direct shortcuts | Banking operations fully accessible |
| Database menu | ⚠️ Partial | `MainWindow.buildMenuBar()` Database menu actions | `Open Database` action opens selector pane | Selector initializes DB context via alternate shell |
| Reports menu | ⚠️ Partial | `MainWindow.buildMenuBar()` Reports menu items | `Reports` nav button → `PanelHost.show(REPORTS_WORKSPACE)` | Workspace route uses shared reports panel |
| Fundraising menu | ✅ Implemented | Fundraising actions: `Donors`, `Donations`, `Grants`, `Funds` | Command Center Fundraising group with direct panel shortcuts | All fundraising features accessible |
| Help menu | ✅ Implemented | `MainWindow.buildMenuBar()` Help action → `HelpPanelFX` | Command Center `Help Center` shortcut | Direct help panel access in alternate |
| Toolbar actions | ⚠️ Partial | New/Save/Find/Journal toolbar | Command Center `Toolbar-style actions` group | Alternate exposes toolbar commands via Command Center |

---

## Command Center & Navigation

| Feature | Status | Implementation | Notes |
|---|---|---|---|
| Alternate command center | ⚠️ Partial | Icon rail + left-nav action pane | Adds grouped File/Run/Reports/Help/Fundraising command discovery |
| Settings route | ⚠️ Partial | Custom alternate settings pane | Decision pending on full settings parity |
| Open Company workflow | ⚠️ Partial | Custom selector pane | Selector now opens and initializes company context |
| Context inspector behavior | ⚠️ Partial | Status text area (limited) | Alternate only updates status label, not full inspector UX |

---

## Legend

- **✅ Implemented** — Feature fully available in alternate shell with equivalent functionality
- **⚠️ Partial** — Feature partially available; some aspects implemented, others pending or degraded
- **❌ Missing** — Feature not yet available in alternate shell

---

## Summary

| Category | Implemented | Partial | Missing |
|---|---|---|---|
| Core Routes | 12 | 0 | 0 |
| Menu & Commands | 4 | 4 | 2 |
| Command Center & Navigation | 0 | 4 | 0 |
| **Total** | **16** | **8** | **2** |
