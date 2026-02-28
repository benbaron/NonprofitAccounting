# Prompt Template: Execute Changes from the Merged UI Map

Use this prompt with Codex when you want it to **accept, parse, validate, and execute** work from the merged UI maps:

- `doc/ui/nonprofitbookkeeping-ui-canonical-merged-map.yaml` (canonical merged map)
- `doc/ui/nonprofitbookkeeping-ui-map.yaml`
- `doc/ui/nonprofitbookkeeping-ui-package-map.yaml`

---

## Copy/Paste Prompt

You are implementing UI work from our merged UI map files.

### Source of truth
Read and treat these files as authoritative:
1. `doc/ui/nonprofitbookkeeping-ui-canonical-merged-map.yaml`
2. `doc/ui/nonprofitbookkeeping-ui-map.yaml`
3. `doc/ui/nonprofitbookkeeping-ui-package-map.yaml`

### Your required behavior

### Canonical merged map identifier (mandatory)
Use this canonical merged map as the top-level entrypoint:
- **Name:** `nonprofitbookkeeping-ui-canonical-merged-map`
- **Path:** `doc/ui/nonprofitbookkeeping-ui-canonical-merged-map.yaml`

Always read the canonical map first, then load the namespace-specific map(s) it references under `namespace_maps`.

### UI namespace disambiguation (mandatory)
Because this repository contains two UI codepaths (`org.nonprofitbookkeeping.ui` and `nonprofitbookkeeping.ui`), you must disambiguate scope before planning changes.

Use this deterministic resolution order:
1. **Explicit user scope wins**
   - If the user says `org.nonprofitbookkeeping.ui` or `nonprofitbookkeeping.ui`, use that namespace only.
2. **Map file selection by namespace**
   - If the request references `doc/ui/nonprofitbookkeeping-ui-package-map.yaml` or `src/main/java/nonprofitbookkeeping/ui/*`, use namespace `nonprofitbookkeeping.ui`.
   - If the request references `doc/ui/nonprofitbookkeeping-ui-map.yaml` or `src/main/java/org/nonprofitbookkeeping/ui/*`, use namespace `org.nonprofitbookkeeping.ui`.
3. **panel_class majority fallback**
   - If scope is still ambiguous, inspect selected nodes and choose the namespace with the majority of matching `panel_class` prefixes.
4. **No silent cross-namespace edits**
   - Do not modify both namespaces unless the user explicitly asks for dual-namespace changes.

Before coding, output a one-line declaration:
- `Resolved UI namespace: <namespace> (reason: <rule used>)`

If both maps are loaded for analysis, keep separate indexes and labels:
- `map_index.org_nonprofitbookkeeping_ui`
- `map_index.nonprofitbookkeeping_ui`

### Implementation Plan (mandatory)
Before changing code, produce and follow this plan:

#### Current Status (mandatory)
- At the start of each iteration, add a `Current Status` block with:
  - resolved namespace,
  - map validation state (pass/fail + unresolved references count),
  - implementation progress (`not started` / `in progress` / `implemented` / `deferred`),
  - latest test status (`not run` / `pass` / `fail`).
- Update this block after every meaningful change set so reviewers can see iteration-to-iteration movement.
- If status changes from pass to fail, include a one-line reason and next corrective action.

Template:

```md
Current Status
- Resolved namespace: <namespace> (reason: <rule>)
- Map validation: <pass/fail>, unresolved refs: <count>
- Implementation progress: <not started|in progress|implemented|deferred>
- Latest tests: <not run|pass|fail>
- If failing: <one-line reason> | Next action: <one-line corrective step>
```

1. **Namespace resolution**
   - Resolve the single UI namespace using the existing deterministic rules.
   - Print: `Resolved UI namespace: <namespace> (reason: <rule>).`

2. **Contract parse + validation**
   - Parse the canonical merged map first, then the referenced namespace map.
   - Build indexes for: `id`, `target`, `trigger`, `panel_class`.
   - Validate:
     - `id` uniqueness per map,
     - target resolution (`id` or `target_registry`),
     - trigger resolution (`trigger_registry`),
     - self-target panes include `panel_class`.
   - If validation fails, create a **map-normalization step** first (registry fixes, duplicate cleanup) before runtime changes.

3. **Scope lock**
   - List selected map nodes and flows to implement.
   - Explicitly list excluded nodes to avoid silent cross-namespace edits.

4. **Implementation sequencing**
   - **Step A:** map consistency updates (if needed).
   - **Step B:** runtime wiring updates (menus/tabs/dialog routes).
   - **Step C:** focused tests for changed routing/actions.
   - **Step D:** docs/diff-summary updates.

5. **Verification gates**
   - Run map validation script:
     - `python scripts/validate_ui_maps.py`
   - Run project tests:
     - `mvn test`
   - For menu/tab routing changes, verify each new action maps to intended panel/action token.

6. **Traceability output**
   - Provide a table with:
     - `map_node_id`, `target`, `trigger`, `panel_class`, `code_file_changed`, `status`.

7. **Review + follow-up**
   - Include a code review section with:
     - regression risks,
     - unresolved map/code divergence,
     - proposed follow-up patches.
   - Offer to implement follow-ups immediately.

1. **Accept the map as contract**
   - Treat `field_semantics`, `target_registry`, `trigger_registry`, `views`, `flows`, and `diff_summary` as implementation guidance.
   - If there is conflict between map entries and runtime code, report the conflict and propose a minimal migration patch.

2. **Parse and validate first (before coding)**
   - Build an internal index of all `id`, `target`, `trigger`, and `panel_class` values.
   - Validate these rules:
     - Every `id` is unique per file.
     - Every `target` resolves to either:
       - another `id` in the same map, or
       - a key in `target_registry`.
     - Every `trigger` resolves to `trigger_registry`.
     - Every self-targeted pane (`target == id`) has `panel_class`.
     - For package map entries, `panel_class` must correspond to an existing Java class unless marked as proposed with migration notes.
   - Output a short validation report with:
     - pass/fail per rule,
     - unresolved references,
     - ambiguous mappings.

3. **Plan implementation from requested scope**
   - If I name specific nodes/flows, limit changes to those.
   - Otherwise, prioritize by `flows` and `diff_summary`.
   - Generate a stepwise plan: parsing fixes, runtime wiring, tests, docs.

4. **Act on the map**
   - For each selected map node:
     - Identify implementing class from `panel_class`.
     - Identify entry point/action wiring from `target` + `trigger`.
     - Implement or adjust JavaFX wiring (menus, tabs, side panes, dialogs, stages) to match the map.
   - Keep behavior changes minimal and traceable to specific map entries.
   - If map says “proposed”, gate changes behind clear migration notes or TODO markers where full implementation is out-of-scope.

5. **Bidirectional traceability (mandatory)**
   - In your final response, include a table with columns:
     - `map_node_id`
     - `target`
     - `trigger`
     - `panel_class`
     - `code_file_changed`
     - `status` (implemented / deferred / blocked)

6. **Testing and verification (mandatory)**
   - Run relevant project tests.
   - At minimum run:
     - `mvn test`
   - If UI wiring changed, include focused verification notes for menu actions/tab routing.

7. **Review and next actions**
   - Perform a code review section listing:
     - regressions risk,
     - unresolved map/code divergences,
     - follow-up patches.
   - Offer to implement the follow-up items immediately.

### Constraints
- No cross-namespace edits unless explicitly requested.
- Do not invent ids/targets/triggers when an existing one fits; prefer map consistency.
- If you must add new ids/targets/triggers, update the relevant registry and explain why.
- Keep naming consistent with existing map conventions.
- Prefer explicit over implicit wiring.
- Keep behavior changes minimal and attributable to specific map nodes.

### Deliverables
- Code changes implementing requested map scope.
- Any map updates needed for consistency.
- Test results.
- Traceability table.
- Review findings + offer to fix.
