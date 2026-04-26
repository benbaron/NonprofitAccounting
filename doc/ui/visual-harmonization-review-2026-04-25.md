# Visual Harmonization Review (2026-04-25)

## Scope reviewed
- JavaFX theme infrastructure (`ThemeManager`, `light.css`, `dark.css`).
- Main shell/tab composition (`MainApplicationView`, `NonprofitBookkeepingFX`).
- Representative UI panels with strong visual customization (`JournalEntryWorkspaceFX`, `DashboardPanelFX`, `AccountsActivityPanelFX`, skeleton panels).

## High-impact harmonization opportunities

### 1) Consolidate visual tokens into a shared design system stylesheet
**Finding:** Theme stylesheets currently define only a small set of root variables and very few component rules. Many visual decisions are embedded inline in Java code.

Examples:
- Theme files are minimal and do not define spacing, typography, card treatments, button hierarchy, table density, or semantic status colors.
- Panel-level styles hardcode colors and borders in Java (`setStyle(...)`) and bypass theme adaptation.

**Recommendation:**
- Add a shared base stylesheet (for both themes) with tokens such as:
  - spacing scale (`4/8/12/16`)
  - semantic colors (`success`, `warning`, `error`, `muted`, `surface`)
  - typography scale (`heading`, `subheading`, `body`, `caption`)
  - reusable component classes (`.card`, `.section-title`, `.status-badge`, `.toolbar`, `.panel-padding`)
- Keep theme files focused on token values; keep component structure classes theme-agnostic.

### 2) Replace hardcoded inline colors with semantic style classes
**Finding:** The UI currently uses many hardcoded values that do not map cleanly to dark mode.

Examples:
- `JournalEntryWorkspaceFX` hardcodes white surfaces, gray badge defaults, red validation text, and state colors.
- `DashboardPanelFX` uses hardcoded light-gray borders/backgrounds for banners and filters.
- `AccountsActivityPanelFX` hardcodes segmented borders with literal light gray values.

**Recommendation:**
- Introduce semantic style classes and assign classes in code (e.g., `getStyleClass().add("status-badge-error")`) rather than injecting style strings.
- Move all `-fx-background-color`, `-fx-border-color`, and `-fx-text-fill` literals into CSS variables/classes.

### 3) Standardize layout rhythm (padding, gaps, section spacing)
**Finding:** Spacing values vary significantly (5, 6, 8, 10, 12, 15, 16, 20) across panels, creating an inconsistent rhythm.

**Recommendation:**
- Define a canonical spacing scale and apply it consistently:
  - Page padding: 12 or 16
  - Section spacing: 8
  - Grid gaps: 8 (vertical), 12 (horizontal)
  - Action bars: 8–10 top margin
- Add helper constants or utility wrappers for container construction to reduce drift.

### 4) Unify table presentation defaults
**Finding:** Numeric alignment is correctly right-aligned in places, but table density, header treatment, row striping, and empty-state styling are not centrally controlled.

**Recommendation:**
- Add global table rules in CSS:
  - header weight, row height, selection contrast, hover state
  - right-aligned numeric column class (`.numeric-col`) reused across panels
  - empty-table placeholder style for consistency

### 5) Align shell-level hierarchy and navigation emphasis
**Finding:** The tab-heavy shell is functional but does not visibly communicate hierarchy between data-entry workflows, review dashboards, and reporting tabs.

**Recommendation:**
- Introduce visual grouping through tab styling and/or grouped navigation affordances:
  - operational tabs vs reporting tabs
  - stronger active-tab indicator
  - subtle surface differences between “workspace editors” and “read-only reports”

### 6) Improve state signaling consistency (draft/valid/error)
**Finding:** Status badges and validation feedback are implemented in individual panels with local style strings.

**Recommendation:**
- Define a shared state language:
  - `state-neutral`, `state-valid`, `state-warning`, `state-error`
  - associated iconography and color tokens
- Apply these classes consistently to badges, inline messages, and form highlights.

## Practical rollout plan

### Phase 1 (fast, low risk)
1. Add base `ui-system.css` with spacing/typography/status/card classes.
2. Expand `light.css` and `dark.css` to theme semantic tokens.
3. Refactor the most visible panel (`JournalEntryWorkspaceFX`) to class-based styling only.

### Phase 2 (broad consistency)
1. Refactor `DashboardPanelFX`, `AccountsActivityPanelFX`, and skeleton panels to shared classes.
2. Normalize container spacing in all frequently used panels.
3. Apply shared table classes in dashboard, journal, account details, reports.

### Phase 3 (polish)
1. Add subtle motion/focus states (consistent keyboard and hover cues).
2. Introduce icon set and semantic icon usage.
3. Validate contrast and accessibility for both themes.

## Review checklist for future UI changes
- No hardcoded hex colors in Java panel code.
- No `setStyle(...)` for reusable visuals.
- New panels use shared spacing and typography classes.
- Dark/light parity verified for all new controls.
- Table columns use shared numeric/text alignment classes.
