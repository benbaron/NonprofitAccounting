# Transaction Model Convergence Plan (`journal_*` → `txn*`)

## 1) Canonical target model recommendation

### Recommendation
Adopt `txn` (header) + `txn_split` (lines) as the **single source of truth** for accounting postings, and treat `journal_transaction` + `journal_entry` as a compatibility surface during migration.

### Why this is the right target
- `txn_split.amount_signed` naturally supports double-entry balancing checks (`SUM(amount_signed)=0` per `txn_id`) with less ambiguity than legacy debit/credit-side columns.
- Canonical model already connects to modern dimensions (`account.id`, `fund.id`, optional `activity_id`, `merchant_id`) and aligns with newer JPA domain entities.
- Legacy schema can be represented as a deterministic projection of canonical rows, while the inverse transform (legacy -> canonical) is lossy in edge cases.

### Target invariants
1. Every posted transaction has >= 2 splits.
2. Every posted transaction is balanced to currency precision.
3. Every split references valid account and fund.
4. Legacy IDs are retained as external references (not primary business keys).
5. Writes occur only to canonical tables after cutover; legacy is read-only via views.

---

## 2) Compatibility view strategy

### Principles
- Preserve backward compatibility for read paths and reports that still query `journal_transaction` / `journal_entry`.
- Prefer **views + INSTEAD OF triggers (if needed)** over dual-write logic.
- Keep a durable mapping table so IDs are stable during the transition.

### Proposed compatibility objects
- `legacy_txn_map(legacy_txn_id, canonical_txn_id, migrated_at, checksum)`
- `v_journal_transaction` (projection from `txn` + helper lookups)
- `v_journal_entry` (projection from `txn_split` joined to `txn`, `account`, `fund`)

### Mapping behavior
- For pre-existing legacy rows, preserve original IDs in `legacy_txn_map`.
- For net-new canonical rows created post-cutover, mint synthetic legacy IDs from sequence `legacy_txn_seq` only if legacy consumers still require numeric IDs.

---

## 3) Phased migration plan with rollback points

### Phase 0 — Hardening and observability
- Add missing constraints/indexes on canonical tables.
- Create drift detection queries and baseline reports.
- Define migration control table:
  - `migration_control(migration_name, phase, started_at, completed_at, status, notes)`.

**Rollback point 0:** Drop newly-added constraints/indexes if app performance/regressions appear.

### Phase 1 — Backfill mapping + canonical completeness
- Populate `legacy_txn_map` for all existing legacy transactions.
- Backfill any missing canonical rows from legacy where needed.
- Validate per-transaction balance and global totals.

**Rollback point 1:** Keep app on legacy read/write; truncate only newly backfilled canonical rows using migration marker.

### Phase 2 — Introduce compatibility views
- Create `v_journal_transaction` and `v_journal_entry` from canonical.
- Switch read-only report workloads to views in staging.
- Diff report totals old-table vs view.

**Rollback point 2:** Repoint reports to base legacy tables; keep views in place but unused.

### Phase 3 — Write path cutover (single-write canonical)
- Feature-flag write path to canonical only.
- Option A: freeze legacy base tables and expose compatibility views with same read contract.
- Option B: maintain temporary reverse-sync trigger (time-boxed) if absolutely required.

**Rollback point 3:** Toggle feature flag back to legacy writer and replay queued canonical txns through legacy adapter.

### Phase 4 — Legacy deprecation
- Lock legacy base tables (no direct DML).
- Keep compatibility views for one or two release cycles.
- Remove reverse-sync logic.

**Rollback point 4:** Re-enable legacy writer only if cutover SLOs breached, using retained mapping/checkpoint artifacts.

### Phase 5 — Legacy retirement
- Archive legacy tables.
- Keep views as stubs or remove after consumer sign-off.

**Rollback point 5:** Restore archived legacy tables from backup snapshot and mapping table.

---

## 4) Data quality checks (no net balance drift)

Run these checks before and after each phase gate:

1. **Per-txn balance:** `SUM(amount_signed)=0` for each canonical transaction.
2. **Min split count:** each canonical transaction has at least two splits.
3. **Legacy-vs-canonical header count parity** (for mapped set).
4. **Legacy-vs-canonical amount parity** by transaction and globally.
5. **Orphan checks** for all FKs (`txn_split`→`txn/account/fund`).
6. **Duplicate detection** (same txn/date/memo/hash and same splits hash).
7. **Checksum parity** using deterministic digest over sorted splits per transaction.

Gate rule: migration phase does not advance unless all blocking checks pass.

---

## 5) SQL DDL proposals (constraints + indexes)

> Notes:
> - SQL is written to be close to PostgreSQL/H2; adapt syntax by engine version.
> - Use `NOT VALID` then `VALIDATE CONSTRAINT` (PostgreSQL) for large online migrations.

## Deliverable A: Forward migration SQL

```sql
BEGIN;

-- 0) Operational metadata
CREATE TABLE IF NOT EXISTS migration_control (
  migration_name      VARCHAR(120) PRIMARY KEY,
  phase               VARCHAR(40) NOT NULL,
  started_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at        TIMESTAMP,
  status              VARCHAR(20) NOT NULL,
  notes               VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS legacy_txn_map (
  legacy_txn_id       BIGINT PRIMARY KEY,
  canonical_txn_id    BIGINT NOT NULL,
  migrated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  checksum            VARCHAR(128),
  CONSTRAINT fk_legacy_map_txn
    FOREIGN KEY (canonical_txn_id) REFERENCES txn(id) ON DELETE CASCADE,
  CONSTRAINT uq_legacy_map_canonical UNIQUE (canonical_txn_id)
);

-- 1) Canonical integrity constraints
ALTER TABLE txn_split
  ADD CONSTRAINT IF NOT EXISTS chk_txn_split_amount_nonzero
  CHECK (amount_signed <> 0);

ALTER TABLE txn_split
  ADD CONSTRAINT IF NOT EXISTS chk_txn_split_amount_scale
  CHECK (amount_signed = ROUND(amount_signed, 2));

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_amount_positive
  CHECK (amount > 0);

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS chk_fund_transfer_distinct_funds
  CHECK (from_fund_id <> to_fund_id);

ALTER TABLE fund_transfer
  ADD CONSTRAINT IF NOT EXISTS fk_fund_transfer_posted_txn
  FOREIGN KEY (posted_txn_id) REFERENCES txn(id) ON DELETE SET NULL;

-- 2) Performance indexes
CREATE INDEX IF NOT EXISTS ix_txn_date_id ON txn(txn_date, id);
CREATE INDEX IF NOT EXISTS ix_txn_bank_date ON txn(bank_account_id, txn_date);
CREATE INDEX IF NOT EXISTS ix_split_txn_amount ON txn_split(txn_id, amount_signed);
CREATE INDEX IF NOT EXISTS ix_split_account_fund ON txn_split(account_id, fund_id);
CREATE INDEX IF NOT EXISTS ix_split_fund_txn ON txn_split(fund_id, txn_id);
CREATE INDEX IF NOT EXISTS ix_fund_transfer_posted_txn ON fund_transfer(posted_txn_id);

-- 3) Backfill mapping from existing one-to-one ids where available
INSERT INTO legacy_txn_map(legacy_txn_id, canonical_txn_id, checksum)
SELECT jt.id, t.id, NULL
FROM journal_transaction jt
JOIN txn t ON t.id = jt.id
LEFT JOIN legacy_txn_map m ON m.legacy_txn_id = jt.id
WHERE m.legacy_txn_id IS NULL;

-- 4) Compatibility views
CREATE OR REPLACE VIEW v_journal_transaction AS
SELECT
  COALESCE(m.legacy_txn_id, t.id) AS id,
  EXTRACT(EPOCH FROM t.created_at) * 1000 AS booking_ts,
  CAST(t.txn_date AS VARCHAR(32)) AS date_text,
  t.memo AS memo,
  cp.display_name AS to_from,
  CAST(NULL AS VARCHAR(64)) AS check_number,
  CAST(NULL AS VARCHAR(64)) AS clear_bank,
  CAST(NULL AS VARCHAR(128)) AS bank_name,
  FALSE AS reconciled,
  CAST(NULL AS VARCHAR(512)) AS budget_tracking,
  f.name AS associated_fund_name
FROM txn t
LEFT JOIN legacy_txn_map m ON m.canonical_txn_id = t.id
LEFT JOIN counterparty cp ON cp.id = t.payee_id
LEFT JOIN (
  SELECT ts.txn_id, MIN(ts.fund_id) AS fund_id
  FROM txn_split ts
  GROUP BY ts.txn_id
) tf ON tf.txn_id = t.id
LEFT JOIN fund f ON f.id = tf.fund_id;

CREATE OR REPLACE VIEW v_journal_entry AS
SELECT
  ts.id AS id,
  COALESCE(m.legacy_txn_id, t.id) AS txn_id,
  ABS(ts.amount_signed) AS amount,
  a.account_number AS account_number,
  CASE WHEN ts.amount_signed < 0 THEN 'CREDIT' ELSE 'DEBIT' END AS account_side,
  a.name AS account_name,
  f.code AS fund_number
FROM txn_split ts
JOIN txn t ON t.id = ts.txn_id
JOIN account a ON a.id = ts.account_id
JOIN fund f ON f.id = ts.fund_id
LEFT JOIN legacy_txn_map m ON m.canonical_txn_id = t.id;

COMMIT;
```

## Deliverable B: Rollback SQL

```sql
BEGIN;

DROP VIEW IF EXISTS v_journal_entry;
DROP VIEW IF EXISTS v_journal_transaction;

DROP INDEX IF EXISTS ix_fund_transfer_posted_txn;
DROP INDEX IF EXISTS ix_split_fund_txn;
DROP INDEX IF EXISTS ix_split_account_fund;
DROP INDEX IF EXISTS ix_split_txn_amount;
DROP INDEX IF EXISTS ix_txn_bank_date;
DROP INDEX IF EXISTS ix_txn_date_id;

ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS fk_fund_transfer_posted_txn;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS chk_fund_transfer_distinct_funds;
ALTER TABLE fund_transfer DROP CONSTRAINT IF EXISTS chk_fund_transfer_amount_positive;
ALTER TABLE txn_split DROP CONSTRAINT IF EXISTS chk_txn_split_amount_scale;
ALTER TABLE txn_split DROP CONSTRAINT IF EXISTS chk_txn_split_amount_nonzero;

DROP TABLE IF EXISTS legacy_txn_map;
DROP TABLE IF EXISTS migration_control;

COMMIT;
```

## Deliverable C: Validation SQL

```sql
-- V1: canonical per-transaction out-of-balance rows (must return 0 rows)
SELECT ts.txn_id,
       ROUND(SUM(ts.amount_signed), 2) AS net_amount,
       COUNT(*) AS split_count
FROM txn_split ts
GROUP BY ts.txn_id
HAVING ROUND(SUM(ts.amount_signed), 2) <> 0;

-- V2: canonical transactions with fewer than 2 splits (must return 0 rows)
SELECT t.id
FROM txn t
LEFT JOIN txn_split ts ON ts.txn_id = t.id
GROUP BY t.id
HAVING COUNT(ts.id) < 2;

-- V3: orphan split references (must return 0 rows)
SELECT ts.id, ts.txn_id, ts.account_id, ts.fund_id
FROM txn_split ts
LEFT JOIN txn t ON t.id = ts.txn_id
LEFT JOIN account a ON a.id = ts.account_id
LEFT JOIN fund f ON f.id = ts.fund_id
WHERE t.id IS NULL OR a.id IS NULL OR f.id IS NULL;

-- V4: legacy vs canonical mapped txn net parity (must return 0 rows)
WITH legacy_net AS (
  SELECT je.txn_id AS legacy_txn_id,
         ROUND(SUM(CASE
              WHEN UPPER(COALESCE(je.account_side,'DEBIT')) = 'CREDIT' THEN -ABS(je.amount)
              ELSE ABS(je.amount)
         END), 2) AS legacy_net
  FROM journal_entry je
  GROUP BY je.txn_id
), canon_net AS (
  SELECT m.legacy_txn_id,
         ROUND(SUM(ts.amount_signed), 2) AS canon_net
  FROM legacy_txn_map m
  JOIN txn_split ts ON ts.txn_id = m.canonical_txn_id
  GROUP BY m.legacy_txn_id
)
SELECT l.legacy_txn_id, l.legacy_net, c.canon_net
FROM legacy_net l
JOIN canon_net c ON c.legacy_txn_id = l.legacy_txn_id
WHERE l.legacy_net <> c.canon_net;

-- V5: global parity over mapped population (must return exactly one row with delta=0)
WITH l AS (
  SELECT ROUND(SUM(CASE
      WHEN UPPER(COALESCE(account_side,'DEBIT'))='CREDIT' THEN -ABS(amount)
      ELSE ABS(amount)
    END), 2) AS total_legacy
  FROM journal_entry je
  WHERE EXISTS (SELECT 1 FROM legacy_txn_map m WHERE m.legacy_txn_id = je.txn_id)
), c AS (
  SELECT ROUND(SUM(ts.amount_signed), 2) AS total_canon
  FROM txn_split ts
  WHERE EXISTS (SELECT 1 FROM legacy_txn_map m WHERE m.canonical_txn_id = ts.txn_id)
)
SELECT l.total_legacy, c.total_canon, ROUND(l.total_legacy - c.total_canon, 2) AS delta
FROM l CROSS JOIN c;
```

## Deliverable D: Risk register

| ID | Risk | Impact | Likelihood | Mitigation | Owner | Trigger |
|---|---|---|---|---|---|---|
| R1 | Dual-write divergence during transition | Financial misstatement | High | Move to single-write canonical quickly; enable drift checks per deploy | Data Eng | Any V4/V5 delta != 0 |
| R2 | Legacy report breakage due to schema expectations | Operational outage for finance users | Medium | Provide compatibility views with identical column contract; stage diff tests | App Eng | Report mismatch vs baseline |
| R3 | ID mapping collisions / missing mappings | Broken drill-down links | Medium | Enforce PK + unique canonical mapping; populate before cutover | DBA | Null join from legacy id to map |
| R4 | Constraint rollout blocks writes on dirty historical data | Release delay | Medium | Pre-clean scripts; add constraints in deferred/online mode where possible | DBA | Constraint validation failures |
| R5 | Performance regression from new joins/views | Slow reporting | Medium | Add targeted indexes; monitor query plans and p95 latency | SRE/DBA | p95 query latency > SLO |
| R6 | Partial rollback loses newly-posted canonical records | Data loss risk | Low | Snapshot + WAL/binlog bookmark; reversible migration markers | DBA | Failed cutover requiring rollback |
| R7 | Currency rounding mismatch between models | Balance drift pennies | Medium | Standardize rounding policy at 2dp for operational checks and posting code | Finance Systems | Non-zero txn net after rounding |

---

## Additional implementation notes
- During cutover, enforce write control by revoking DML grants on legacy base tables for app roles.
- Keep a nightly reconciliation job using Deliverable C queries and alert on non-zero drift.
- Decommission sequence: adapters -> reverse-sync -> legacy tables, only after two clean close cycles.

## Execution status in repository
- Forward migration SQL has been materialized at `scripts/sql/txn_convergence_forward.sql`.
- Rollback SQL has been materialized at `scripts/sql/txn_convergence_rollback.sql`.
- Validation SQL has been materialized at `scripts/sql/txn_convergence_validation.sql`.
- Risk register has been materialized at `scripts/sql/txn_convergence_risk_register.md`.
- PowerShell runner for H2 execution has been materialized at `scripts/sql/run-txn-convergence-h2.ps1`.
