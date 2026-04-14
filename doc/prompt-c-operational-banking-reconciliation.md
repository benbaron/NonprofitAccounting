# Prompt C — Operational Banking Reconciliation Architecture

## 1) Target Architecture

### 1.1 Design goals
- Keep `banking_transaction_record` as the immutable ingest/event table for imported bank lines.
- Keep `ledger_record` as the match-link table that ties bank lines to accounting entries.
- Keep `bank_statement` as statement-period control totals and close metadata.
- Keep `bank_id_record` as the canonical bank account identity.
- Add deterministic matching lifecycle fields so reconciliation can be resumed, audited, and replayed safely.

### 1.2 Proposed responsibilities by table
- `bank_id_record`
  - Canonical account identity (`institution`, account fingerprint/masked values, account type, active status).
  - Owns per-account reconciliation policy defaults (date tolerance, amount tolerance).
- `banking_transaction_record`
  - One row per imported bank transaction event.
  - Stores import-batch metadata, source hash, normalized amount/date/description fields.
  - Stores machine-assigned matching status and anomaly flags.
- `ledger_record`
  - One row per *match decision* between a bank transaction and a ledger posting target.
  - Supports one-to-one and many-to-one matching through `match_group_id`.
  - Carries reviewer/user metadata for manual overrides.
- `bank_statement`
  - Statement period header (`period_start`, `period_end`, closing balance, imported_on).
  - Status for close workflow (`OPEN`, `IN_REVIEW`, `CLOSED`, `LOCKED`).
  - Retention anchor for archival windows.

## 2) Matching Status State Machine

Status column: `banking_transaction_record.match_status`.

```text
NEW
 ├─(dedupe rule hit)──────────────▶ DUPLICATE
 ├─(auto-match candidate found)───▶ AUTO_MATCHED
 ├─(no candidate)─────────────────▶ UNMATCHED

AUTO_MATCHED
 ├─(review approved)──────────────▶ MATCH_CONFIRMED
 └─(review rejected)──────────────▶ UNMATCHED

UNMATCHED
 ├─(manual match chosen)──────────▶ MATCH_CONFIRMED
 ├─(write-off/adjustment posted)──▶ ADJUSTED
 └─(aging threshold reached)──────▶ STALE_UNMATCHED

MATCH_CONFIRMED
 ├─(statement closed)─────────────▶ RECONCILED
 └─(reopen statement)─────────────▶ UNMATCHED

DUPLICATE ─(override allowed)─────▶ UNMATCHED
ADJUSTED  ─(reopen/void adjust)───▶ UNMATCHED
STALE_UNMATCHED ─(activity)───────▶ UNMATCHED
```

State transition controls:
- Transition whitelist enforced by trigger or service layer guard.
- `MATCH_CONFIRMED` requires at least one active `ledger_record` link.
- `RECONCILED` requires parent `bank_statement.status IN ('CLOSED','LOCKED')`.
- `RECONCILED` additionally requires `COALESCE(bank_statement.difference, 0) = 0`.


### 2.1 Allowed transition matrix (for trigger/service enforcement)

| From | To | Condition |
|---|---|---|
| `NEW` | `UNMATCHED` | No deterministic candidate found |
| `NEW` | `AUTO_MATCHED` | Rule engine confidence >= auto threshold |
| `NEW` | `DUPLICATE` | Hard dedupe key collision |
| `AUTO_MATCHED` | `MATCH_CONFIRMED` | Reviewer accepted or auto-approval policy enabled |
| `AUTO_MATCHED` | `UNMATCHED` | Reviewer rejected candidate |
| `UNMATCHED` | `MATCH_CONFIRMED` | Manual match created in `ledger_record` |
| `UNMATCHED` | `ADJUSTED` | Approved write-off/adjusting entry posted |
| `UNMATCHED` | `STALE_UNMATCHED` | Age exceeds stale threshold |
| `STALE_UNMATCHED` | `UNMATCHED` | User activity/retriage |
| `MATCH_CONFIRMED` | `RECONCILED` | Statement is closed and difference = 0 |
| `MATCH_CONFIRMED` | `UNMATCHED` | Statement reopened or link voided |
| `DUPLICATE` | `UNMATCHED` | Reviewer override with reason |
| `ADJUSTED` | `UNMATCHED` | Adjustment reversed/voided |

### 2.2 Matching confidence and deterministic ordering

When multiple candidates exist, choose in this strict order:
1. Exact external ID equality.
2. Exact amount and date with payee similarity >= 0.95.
3. Exact amount, date within ±2 days, memo similarity >= 0.90.
4. Same amount and unresolved ledger entry age <= 10 days.

If tie remains after step 4, do not auto-match; set `UNMATCHED` and require manual review.


## 3) Idempotent Import Strategy

1. **Batch envelope**
   - Add `import_batch_id` and `source_system` per ingest run.
2. **Deterministic fingerprint**
   - Compute `source_fingerprint = SHA256(bank_id_record_id + posted_date + amount + normalized_description + external_transaction_id)`.
3. **Idempotent upsert**
   - Unique index on `(bank_id_record_id, source_fingerprint)`.
   - Import uses `MERGE`/upsert; duplicate lines increment `duplicate_seen_count` and update `last_seen_batch_id`.
4. **Late data handling**
   - If amount/date changes for same external ID, create new row with `supersedes_banking_record_id` reference.
5. **Replay safety**
   - Re-running same file only updates ingest metadata, never creates additional economic events.

## 4) Anomaly Detection Rules

### 4.1 Duplicate detection
- Hard duplicate: same `bank_id_record_id + source_fingerprint`.
- Soft duplicate: same account, amount, absolute date difference <= 1 day, normalized memo similarity >= threshold.
- Action: set `anomaly_duplicate = TRUE`; hard duplicate defaults `match_status = DUPLICATE`.

### 4.2 Amount outliers
- Rule: absolute z-score > 3.5 by rolling 180-day window by `(bank_id_record_id, transaction_type)`.
- Fallback for low volume: amount > `p95 * 2` for account window.
- Action: `anomaly_amount_outlier = TRUE`, require reviewer before `MATCH_CONFIRMED`.

### 4.3 Date outliers
- Rule: posted date is older than statement period start - 45 days, or newer than period end + 7 days.
- Action: `anomaly_date_outlier = TRUE`.

### 4.4 Stale unmatched
- Rule: `match_status IN ('UNMATCHED','STALE_UNMATCHED')` and `transaction_date < CURRENT_DATE - 30`.
- Action: set/retain `STALE_UNMATCHED`, raise operations queue record.

## 5) Schema and Index Changes

- Add controlled enums via `CHECK` constraints for statement and match statuses.
- Add import and anomaly columns to `banking_transaction_record`.
- Add reconciliation period columns to `bank_statement`.
- Add `match_group_id`, `match_method`, and reviewer metadata to `ledger_record`.
- Add partial/covering indexes for open-item workflows and duplicate checks.

## 6) Retention Guidance

- **Hot data (0–24 months):** full detail retained in primary DB.
- **Warm archive (24–84 months):** keep full detail but move closed statements and reconciled transactions to archive schema/table.
- **Cold/audit (84+ months):** retain statement headers, match links, and hash/fingerprint proofs; optional purge of verbose memos based on policy.
- Never purge rows tied to open disputes, legal hold, or unresolved anomalies.

---


## 7) Rollout Sequencing (Recommended)

1. **Phase 0 — Data profiling**
   - Run validation SQL V1/V2 against current dataset; estimate cleanup volume.
2. **Phase 1 — Additive schema rollout**
   - Deploy new columns, indexes, and `CHECK` constraints (non-breaking additions).
3. **Phase 2 — Backfill**
   - Populate `source_fingerprint`, `match_status`, and `statement_id` from historical context.
4. **Phase 3 — Enforcement**
   - Enable transition checks in service/trigger logic and reject invalid status transitions.
5. **Phase 4 — Operations hardening**
   - Activate stale-unmatched queue SLA and archival jobs with legal-hold gates.


## 8) Implementation Notes for Current Stack (H2 + `Database` bootstrap)

- The repository initializes schema through Java DDL execution in `Database.ensureCoreTables/ensureOperationalLinkageTables`.
- For H2 compatibility, keep enum-like behavior via `CHECK` constraints as shown (avoid DB-specific enum types).
- If trigger-based transition enforcement is deferred, implement transition guards first in the reconciliation service layer and add DB checks incrementally.
- Apply additive columns first, then backfill, then constraints that can reject writes.
- Add migration history marker so deployment is idempotent and traceable in `schema_migration_history`.

### Suggested implementation task breakdown
1. Add columns + indexes with `IF NOT EXISTS` statements in `Database` migration block.
2. Backfill `match_status='UNMATCHED'` for historical rows lacking explicit state.
3. Add service-layer transition whitelist to block illegal status moves.
4. Add nightly anomaly job for stale unmatched and outlier flags.
5. Add reconciliation close guard: prevent close if `difference != 0` or unresolved high-severity anomalies.

## Deliverable A: Forward Migration SQL

```sql
-- 1) bank_statement controls
ALTER TABLE bank_statement
  ADD COLUMN IF NOT EXISTS bank_id_record_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS period_start DATE,
  ADD COLUMN IF NOT EXISTS period_end DATE,
  ADD COLUMN IF NOT EXISTS status VARCHAR(24) DEFAULT 'OPEN' NOT NULL,
  ADD COLUMN IF NOT EXISTS imported_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS retention_until DATE;

ALTER TABLE bank_statement
  ADD CONSTRAINT IF NOT EXISTS ck_bank_statement_status
  CHECK (status IN ('OPEN','IN_REVIEW','CLOSED','LOCKED'));

ALTER TABLE bank_statement
  ADD CONSTRAINT IF NOT EXISTS fk_bank_statement_bank_id
  FOREIGN KEY (bank_id_record_id)
  REFERENCES bank_id_record(bank_id_record_id)
  ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_bank_statement_bank_period
  ON bank_statement(bank_id_record_id, period_start, period_end);

-- 2) banking_transaction_record matching + idempotency + anomaly fields
ALTER TABLE banking_transaction_record
  ADD COLUMN IF NOT EXISTS statement_id BIGINT,
  ADD COLUMN IF NOT EXISTS import_batch_id VARCHAR(128),
  ADD COLUMN IF NOT EXISTS source_system VARCHAR(64),
  ADD COLUMN IF NOT EXISTS source_fingerprint VARCHAR(128),
  ADD COLUMN IF NOT EXISTS normalized_description VARCHAR(512),
  ADD COLUMN IF NOT EXISTS external_transaction_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS match_status VARCHAR(24) DEFAULT 'NEW' NOT NULL,
  ADD COLUMN IF NOT EXISTS matched_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS duplicate_seen_count INT DEFAULT 0 NOT NULL,
  ADD COLUMN IF NOT EXISTS last_seen_batch_id VARCHAR(128),
  ADD COLUMN IF NOT EXISTS anomaly_duplicate BOOLEAN DEFAULT FALSE NOT NULL,
  ADD COLUMN IF NOT EXISTS anomaly_amount_outlier BOOLEAN DEFAULT FALSE NOT NULL,
  ADD COLUMN IF NOT EXISTS anomaly_date_outlier BOOLEAN DEFAULT FALSE NOT NULL,
  ADD COLUMN IF NOT EXISTS anomaly_reason VARCHAR(512),
  ADD COLUMN IF NOT EXISTS supersedes_banking_record_id VARCHAR(255);

ALTER TABLE banking_transaction_record
  ADD CONSTRAINT IF NOT EXISTS ck_banking_match_status
  CHECK (match_status IN (
    'NEW','UNMATCHED','AUTO_MATCHED','MATCH_CONFIRMED',
    'RECONCILED','DUPLICATE','ADJUSTED','STALE_UNMATCHED'
  ));

ALTER TABLE banking_transaction_record
  ADD CONSTRAINT IF NOT EXISTS fk_banking_statement
  FOREIGN KEY (statement_id)
  REFERENCES bank_statement(id)
  ON DELETE SET NULL;

ALTER TABLE banking_transaction_record
  ADD CONSTRAINT IF NOT EXISTS fk_banking_supersedes
  FOREIGN KEY (supersedes_banking_record_id)
  REFERENCES banking_transaction_record(banking_record_id)
  ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_banking_idempotent_fingerprint
  ON banking_transaction_record(bank_id_record_id, source_fingerprint);

CREATE INDEX IF NOT EXISTS idx_banking_open_items
  ON banking_transaction_record(bank_id_record_id, match_status, transaction_date);

CREATE INDEX IF NOT EXISTS idx_banking_anomaly_queue
  ON banking_transaction_record(bank_id_record_id, anomaly_duplicate, anomaly_amount_outlier, anomaly_date_outlier);

-- 3) ledger_record match metadata
ALTER TABLE ledger_record
  ADD COLUMN IF NOT EXISTS banking_record_id VARCHAR(255),
  ADD COLUMN IF NOT EXISTS match_group_id VARCHAR(64),
  ADD COLUMN IF NOT EXISTS match_method VARCHAR(24),
  ADD COLUMN IF NOT EXISTS reviewer_user VARCHAR(128),
  ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP,
  ADD COLUMN IF NOT EXISTS link_status VARCHAR(24) DEFAULT 'ACTIVE' NOT NULL;

ALTER TABLE ledger_record
  ADD CONSTRAINT IF NOT EXISTS ck_ledger_match_method
  CHECK (match_method IN ('AUTO','MANUAL','RULE','IMPORT_REPLAY') OR match_method IS NULL);

ALTER TABLE ledger_record
  ADD CONSTRAINT IF NOT EXISTS ck_ledger_link_status
  CHECK (link_status IN ('ACTIVE','VOIDED','SUPERSEDED'));

ALTER TABLE ledger_record
  ADD CONSTRAINT IF NOT EXISTS fk_ledger_banking_record
  FOREIGN KEY (banking_record_id)
  REFERENCES banking_transaction_record(banking_record_id)
  ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_ledger_match_group
  ON ledger_record(match_group_id);

CREATE INDEX IF NOT EXISTS idx_ledger_banking_active
  ON ledger_record(banking_record_id, link_status);
```

## Deliverable B: Rollback SQL

```sql
DROP INDEX IF EXISTS idx_ledger_banking_active;
DROP INDEX IF EXISTS idx_ledger_match_group;
ALTER TABLE ledger_record DROP CONSTRAINT IF EXISTS fk_ledger_banking_record;
ALTER TABLE ledger_record DROP CONSTRAINT IF EXISTS ck_ledger_link_status;
ALTER TABLE ledger_record DROP CONSTRAINT IF EXISTS ck_ledger_match_method;
ALTER TABLE ledger_record DROP COLUMN IF EXISTS link_status;
ALTER TABLE ledger_record DROP COLUMN IF EXISTS reviewed_at;
ALTER TABLE ledger_record DROP COLUMN IF EXISTS reviewer_user;
ALTER TABLE ledger_record DROP COLUMN IF EXISTS match_method;
ALTER TABLE ledger_record DROP COLUMN IF EXISTS match_group_id;
ALTER TABLE ledger_record DROP COLUMN IF EXISTS banking_record_id;

DROP INDEX IF EXISTS idx_banking_anomaly_queue;
DROP INDEX IF EXISTS idx_banking_open_items;
DROP INDEX IF EXISTS uq_banking_idempotent_fingerprint;
ALTER TABLE banking_transaction_record DROP CONSTRAINT IF EXISTS fk_banking_supersedes;
ALTER TABLE banking_transaction_record DROP CONSTRAINT IF EXISTS fk_banking_statement;
ALTER TABLE banking_transaction_record DROP CONSTRAINT IF EXISTS ck_banking_match_status;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS supersedes_banking_record_id;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS anomaly_reason;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS anomaly_date_outlier;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS anomaly_amount_outlier;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS anomaly_duplicate;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS last_seen_batch_id;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS duplicate_seen_count;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS matched_at;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS match_status;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS external_transaction_id;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS normalized_description;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS source_fingerprint;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS source_system;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS import_batch_id;
ALTER TABLE banking_transaction_record DROP COLUMN IF EXISTS statement_id;

DROP INDEX IF EXISTS idx_bank_statement_bank_period;
ALTER TABLE bank_statement DROP CONSTRAINT IF EXISTS fk_bank_statement_bank_id;
ALTER TABLE bank_statement DROP CONSTRAINT IF EXISTS ck_bank_statement_status;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS retention_until;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS closed_at;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS imported_at;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS status;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS period_end;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS period_start;
ALTER TABLE bank_statement DROP COLUMN IF EXISTS bank_id_record_id;
```

## Deliverable C: Validation SQL

```sql
-- V1: verify uniqueness/idempotency effectiveness
SELECT bank_id_record_id, source_fingerprint, COUNT(*) AS cnt
FROM banking_transaction_record
WHERE source_fingerprint IS NOT NULL
GROUP BY bank_id_record_id, source_fingerprint
HAVING COUNT(*) > 1;

-- V2: invalid state values
SELECT banking_record_id, match_status
FROM banking_transaction_record
WHERE match_status NOT IN (
  'NEW','UNMATCHED','AUTO_MATCHED','MATCH_CONFIRMED',
  'RECONCILED','DUPLICATE','ADJUSTED','STALE_UNMATCHED'
);

-- V3: MATCH_CONFIRMED or RECONCILED rows missing active ledger links
SELECT b.banking_record_id, b.match_status
FROM banking_transaction_record b
LEFT JOIN ledger_record l
  ON l.banking_record_id = b.banking_record_id
 AND l.link_status = 'ACTIVE'
WHERE b.match_status IN ('MATCH_CONFIRMED','RECONCILED')
GROUP BY b.banking_record_id, b.match_status
HAVING COUNT(l.ledger_record_id) = 0;

-- V4: stale unmatched monitor
SELECT banking_record_id, bank_id_record_id, transaction_date, match_status
FROM banking_transaction_record
WHERE match_status IN ('UNMATCHED','STALE_UNMATCHED')
  AND transaction_date < CURRENT_DATE - 30;

-- V5: duplicate anomaly consistency check
SELECT banking_record_id
FROM banking_transaction_record
WHERE match_status = 'DUPLICATE'
  AND anomaly_duplicate = FALSE;

-- V6: statement close readiness (difference must be zero)
SELECT id, bank_name, statement_date, difference, status
FROM bank_statement
WHERE status IN ('CLOSED','LOCKED')
  AND COALESCE(difference, 0) <> 0;
```

## Deliverable D: Risk Register

| Risk | Likelihood | Impact | Mitigation | Rollout stage |
|---|---|---:|---|---|
| False-positive duplicate flags on recurring payments | Medium | High | Tune fingerprint normalization and soft-duplicate threshold by account type; allow reviewer override path | Pilot |
| Import replay generates superseded chains too aggressively | Low | Medium | Cap replay window by statement period and require external transaction ID match for supersede | Pilot |
| Operational load increase from new indexes | Medium | Medium | Add indexes in phased deployment and monitor write latency; drop non-critical index if p95 ingest latency regresses | Stage 1 |
| Historical data lacks fields needed for deterministic matching | High | Medium | Backfill normalized descriptions and synthetic fingerprints; mark low-confidence rows as `UNMATCHED` | Backfill |
| Reconciliation workflow confusion with new statuses | Medium | Medium | UI mapping table + tooltip docs + training with transition matrix | Stage 2 |
| Constraint enforcement blocks dirty legacy records | Medium | High | Deploy with data cleanup pre-checks; apply constraints after remediation | Stage 1 |
| Archive jobs purge data under legal hold | Low | High | Add legal-hold flag gate and monthly exception report before purge | Operations |
| Statement closure without anomaly review | Medium | High | Require zero unresolved high-severity anomalies before `CLOSED` transition | Stage 2 |
