# Account/Fund Master Data Harmonization Proposal

## Scope evaluated
- `account`
- `fund`
- `account_alias`
- `fund_alias`

## Naming and key standards
1. **Canonical key retention**
   - Account natural key: `account.account_number`.
   - Fund natural key: `fund.code`.
2. **Normalization standard (for uniqueness + matching only)**
   - `norm_key = UPPER(TRIM(value))`, then remove spaces and hyphens.
   - Keep original display values unchanged for reporting and UI.
3. **Active-row uniqueness policy**
   - Only one active record per normalized account number/code and fund code/name.
   - Historical duplicates are permitted only if marked inactive.
4. **Date validity hygiene**
   - `effective_to >= effective_from` when both present.

## Dedupe rules
1. Dedupe candidate groups are formed by normalized keys.
2. Candidate master selection order:
   - has earliest `effective_from`
   - then lowest `id`
3. Survivors remain active; non-survivors are set inactive.
4. Existing aliases on non-survivors are retained but deactivated unless explicitly re-homed.

## Alias governance model
1. Aliases are **global and unique among active rows** within domain (`ACCOUNT` or `FUND`).
2. Aliases support lifecycle states via `is_active` + review queue status.
3. Every ambiguous alias collision is inserted into `alias_review_queue`.
4. Steward workflow:
   - OPEN -> IN_PROGRESS -> RESOLVED/REJECTED
   - resolution requires `resolver` and `resolution_note`.

## Import-time deterministic matching policy
For each incoming token and domain:

1. **Exact canonical key (case-insensitive)**
   - account: `account_number`
   - fund: `code`
2. **Exact active alias text (case-insensitive)**
3. **Normalized canonical key match**
4. **Normalized active alias match**
5. Decision:
   - exactly one candidate => auto-match
   - zero candidates => reject with reason `NO_MATCH`
   - multiple candidates => reject with reason `AMBIGUOUS_MATCH`; write to `alias_review_queue`

### Pseudocode
```text
function match(domain, token):
  t = token.trim()
  n = normalize(t)
  candidates = set()

  candidates += exactCanonical(domain, t)
  if size(candidates) == 1: return MATCH(candidates[0], "EXACT_CANONICAL")

  candidates += exactAlias(domain, t, active=true)
  if size(candidates) == 1: return MATCH(candidates[0], "EXACT_ALIAS")

  candidates += normalizedCanonical(domain, n, active=true)
  if size(candidates) == 1: return MATCH(candidates[0], "NORMALIZED_CANONICAL")

  candidates += normalizedAlias(domain, n, active=true)
  if size(candidates) == 1: return MATCH(candidates[0], "NORMALIZED_ALIAS")

  if size(candidates) == 0: return REJECT("NO_MATCH")
  enqueueAliasReview(domain, token, n, candidates, "AMBIGUOUS_IMPORT_MATCH")
  return REJECT("AMBIGUOUS_MATCH")
```

## Backfill plan for ambiguous aliases
1. Run validation SQL and capture duplicate normalized alias groups.
2. Populate `alias_review_queue` (migration does initial load for active collisions).
3. Steward resolution playbook per queue row:
   - Choose target account/fund.
   - Deactivate conflicting alias rows.
   - Optionally add canonical alias for chosen target.
   - Mark queue row RESOLVED with note and approver.
4. Re-run validation SQL until collision checks return 0 rows.
5. Enable strict import policy (hard reject on ambiguity).

## Deliverables
- Forward migration SQL: `scripts/sql/master_data_harmonization_forward.sql`
- Rollback SQL: `scripts/sql/master_data_harmonization_rollback.sql`
- Validation SQL: `scripts/sql/master_data_harmonization_validation.sql`
- Risk register: `scripts/sql/master_data_harmonization_risk_register.md`
