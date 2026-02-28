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
- Accept the map as contract: treat `field_semantics`, `target_registry`, `trigger_registry`, `views`, `flows`, and `diff_summary` as implementation guidance.
- If there is a conflict between map entries and runtime code, report the conflict and propose a minimal migration patch.

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
     - For package-map entries, `panel_class` resolves to an existing Java class unless explicitly marked as proposed with migration notes.
   - If validation fails, create a **map-normalization step** first (registry fixes, duplicate cleanup) before runtime changes.
   - Emit a short validation report with pass/fail per rule, unresolved references, and ambiguous mappings.

3. **Scope lock**
   - If the request names specific nodes/flows, limit changes to those; otherwise prioritize `flows` and `diff_summary`.
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
     - unresolved map/code divergences,
     - proposed follow-up patches.
   - Offer to implement follow-ups immediately.


8. **Progress handoff + next prompt (mandatory)**
   - After each implemented version, include a concise **Progress Update** describing what changed since the previous iteration.
   - End with a **"What to do next"** prompt that the reviewer can copy/paste to continue implementation.
   - Use this template:

```md
Progress Update
- Completed: <implemented items>
- In progress: <active items>
- Deferred/blocked: <items + reason>
- Test status: <pass/fail/not run>

What to do next
- <single actionable next step>
- Suggested prompt: "<copy/paste next instruction for Codex>"
```

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
- Progress update + “what to do next” prompt for the next iteration.
