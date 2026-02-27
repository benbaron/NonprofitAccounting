# Amendment: Map-Driven UI Implementation Plan (Codex)

Add the following section to the execution prompt used for UI-map work.

## Implementation Plan (mandatory)
Before changing code, produce and follow this plan:

1. **Namespace resolution**
   - Resolve the single UI namespace using the existing deterministic rules.
   - Print: `Resolved UI namespace: <namespace> (reason: <rule>)`.

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

## Enforcement notes
- No cross-namespace edits unless explicitly requested.
- Prefer existing ids/targets/triggers; if adding new ones, update registries in the same patch.
- Keep behavior changes minimal and attributable to specific map nodes.
