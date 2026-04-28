# Journal-First Source-of-Truth Proposal

## Purpose

This proposal defines a general pattern for ensuring all operational sub-ledgers and workflow panels (Donations, Grants, Funds, Budget, Bank Reconciliation, Inventory/Depreciation, etc.) use the **Journal** as the canonical accounting source of truth.

The intent is to avoid drift between modules, remove duplicate accounting logic, and make reporting/audit behavior deterministic.

---

## Current Pain Pattern

Across accounting systems, the common failure mode is:

1. A supplemental panel keeps its own totals/status fields.
2. A second flow writes journal entries separately.
3. Edits/delete/reversal in one place are not mirrored in the other.
4. Reports disagree by panel.

The fix is architectural: make the Journal the only accounting write target and treat supplemental records as metadata + workflow state.

---

## Core Principles

### 1) Canonical financial truth = Journal entries

- Only Journal transactions affect balances, trial balance, and financial statements.
- No panel may maintain independent balance-affecting totals as source data.

### 2) Sub-ledgers are intent + context

- Supplemental modules store:
  - workflow metadata (status, approvals, references),
  - domain attributes (donor, grant code, bank statement line id, etc.),
  - links to journal transaction IDs (or reversal IDs).

### 3) All accounting writes are posted through a common posting pipeline

- Central Posting Service validates and posts balanced transactions.
- Panels send typed posting commands instead of writing Journal directly.

### 4) Reversals are first-class

- Corrections should be done by reversing and reposting, not destructive mutation.
- Preserve audit history for all finance-impacting changes.

### 5) Idempotency and link integrity

- Every workflow write should carry an idempotency key.
- Re-running actions (import/reconcile) must not duplicate Journal entries.

---

## Target Architecture

## A. Shared components

### Posting API (application layer)

Introduce a posting façade:

- `post(command)` -> returns `JournalTxnRef`
- `reverse(journalTxnId, reason)` -> returns `JournalTxnRef`
- `amend(oldJournalTxnId, newCommand)` -> internally reverse + post

Each `command` type is domain-specific but maps to balanced journal lines.

### Link table strategy

Each supplemental domain gets a mapping table:

- `domain_record_id`
- `journal_txn_id`
- `link_role` (ORIGINAL, REVERSAL, ADJUSTMENT)
- `created_at`, `created_by`

This keeps domain modules explainable and traceable.

### Validation rules

- Posting date policy.
- Fund/account restrictions.
- Reconciliation lock periods.
- Grant/fund eligibility constraints.

All enforced in one place.

---

## B. Module-by-module behavior

## 1) Donations

- Creating a donation posts journal entries (e.g., cash/bank debit, contribution revenue credit).
- Donation record stores donor metadata + `journal_txn_id`.
- Editing amount/accounting dimensions => reverse original + post adjusted transaction.
- Deleting after posting => soft-delete donation + reversal entry.

### Donations panel specification (Phase 1 implementation contract)

#### Data contract

- Panel row model = `DonationRecord`:
  - `donation_id`
  - `donor_external_id`
  - `donation_date`
  - `amount`
  - `memo`
  - `cash_account_number`
  - `revenue_account_number`
  - `fund_number`
  - `journal_txn_id`
- Persistence source = `donation_record` table.
- Link/audit source = `donation_journal_link` (`ORIGINAL`, `REVERSAL`, `ADJUSTMENT`).

#### Panel operations

- **Refresh**
  - Read rows from `DonationRecordRepository.listAll()`.
- **Add donation**
  - Build `DonationRecord` from dialog/form input.
  - Post via `DonationPostingService.postDonation(record)`.
  - Persist link role `ORIGINAL`.
- **Edit donation**
  - Reuse same `donation_id`.
  - Post via `DonationPostingService.postDonation(record)` and apply configured edit policy:
    - `UPDATE_IN_PLACE` => update existing linked txn in place.
    - `REVERSE_AND_REPOST` => create `REVERSAL` + `ADJUSTMENT` links and update `journal_txn_id` to the adjusted txn.
- **Trace from panel to journal**
  - Use `journal_txn_id` column on each row.
- **Trace from journal to panel**
  - Lookup by `DonationRecordRepository.findByJournalTxnId(...)`.

#### Settings dependency

- Panel/accounting behavior consumes `SettingsModel.donationEditPostingPolicy`.
- UI must expose this preference as a user-selectable setting:
  - `UPDATE_IN_PLACE`
  - `REVERSE_AND_REPOST`

#### Validation/UX requirements

- Amount required and `> 0`.
- Cash and revenue account numbers required.
- Posting failures are non-destructive to existing saved rows.
- Successful posts show resulting `journal_txn_id` in the grid.

## 2) Grants

- Grant awards, drawdowns, and recognition events post through Posting API.
- Grant panel maintains compliance/status metadata and references to posted txns.
- Budget vs actual by grant derives actuals from journal entries tagged to grant/fund dimensions.

### Grants panel specification (Phase 1 implementation contract)

#### Data/link contract

- Primary record source = `grant_record`.
- Posted-link requirement for finance-impacting states:
  - `journal_txn_id` must be populated for posted grant events.
- Optional granular link source = `grant_posting_link` for one-to-many posting traceability.

#### Panel operations

- **Create award/drawdown/recognition event**
  - Build a posting command and call Posting API.
  - Persist resulting `journal_txn_id` to `grant_record`.
  - Persist line-level links in `grant_posting_link` when available.
- **Edit posted grant financial fields**
  - Reverse existing linked posting and post adjusted replacement.
  - Update effective `journal_txn_id`; preserve prior links as historical roles.
- **Non-financial edits**
  - Update compliance/workflow metadata only; no posting mutation.

#### Traceability expectations

- `grant_record -> journal_transaction` via `journal_txn_id`.
- `journal_transaction/journal_entry -> grant_record` via `grant_posting_link` or
  `transaction_info.domain_record_id`.

## 3) Funds / Fund Accounting

- Fund transfers produce paired journal lines with fund dimensions.
- Fund balances are computed from Journal, not from mutable “fund balance” fields.
- Fund panel can cache read models, but recomputation source remains Journal.

### Funds panel specification (Phase 1 implementation contract)

#### Data/link contract

- Operational transfer/workflow state remains in fund transfer domain tables.
- Every posted transfer must carry a journal reference (or canonical posted txn reference mapped to journal lineage).

#### Panel operations

- **Create fund transfer**
  - Build transfer posting command with equal/opposite fund-dimension lines.
  - Post through Posting API.
  - Persist posted transaction reference in transfer row.
- **Edit posted transfer amount/fund dimensions**
  - Reverse prior posting; post adjusted transfer; update active link.
- **Status-only workflow changes (approve/review)**
  - No posting write unless financial dimensions changed.

#### Traceability expectations

- Transfer row -> posted transaction id -> journal lines.
- Journal lines include fund dimensions to support deterministic fund balance recomputation.

## 4) Budget + Budget vs Actual

- Budget lines remain planning data.
- Actuals are selected from Journal-based aggregation pipelines (account/fund/date dimensions).
- Keep pluggable sources only as explicit mode options; default = Journal.

## 5) Ledger register / journal workspaces

- Ledger views are direct Journal projections with filters.
- Any “quick edit” posts via amend flow (reverse + repost) to preserve auditability.

## 6) Bank Reconciliation

- Reconciliation never edits amounts of posted entries.
- Reconciliation writes status metadata:
  - statement ID
  - cleared date
  - cleared balance checkpoints
  - matched/unmatched links
- Optional adjustment entries (bank fees, interest, corrections) must be posted via Posting API.
- Locked reconciled periods disallow destructive edits; require dated adjusting entries.

### Bank Reconciliation panel specification (Phase 1 implementation contract)

#### Data/link contract

- `banking_transaction_record.journal_txn_id` is the required linkage for matched posted items.
- Reconciliation status metadata remains operational (`match_status`, `matched_at`, statement identifiers).

#### Panel operations

- **Import/match flow**
  - For ledger-backed items, attach/retain `journal_txn_id`.
  - Mark match status without mutating posted amounts.
- **Post reconciliation adjustment**
  - Create adjustment command (fees/interest/corrections), post via Posting API.
  - Store resulting `journal_txn_id` on created/updated banking record.
- **Void/undo match**
  - Update workflow status links only; do not destructive-edit prior posted entries.

#### Traceability expectations

- Bank item -> `journal_txn_id` -> journal transaction.
- Journal transaction -> matched banking rows via `banking_transaction_record`.

## 7) Inventory / Depreciation / other operational modules

- Operational event creates accounting command(s) through Posting API.
- Module persists operational metadata and references to resulting txns.

### Inventory / Depreciation panel specification (Phase 1 implementation contract)

#### Data/link contract

- Inventory/asset events persist operational metadata in module tables.
- Posted events must persist journal linkage:
  - acquisition/disposal links in asset detail records
  - depreciation links in `depreciation_record.posted_journal_txn_id` (and reversal id when applicable)

#### Panel operations

- **Acquisition/disposal**
  - Post through Posting API and persist journal reference on asset records.
- **Depreciation run post**
  - Post run entries via Posting API/journal service.
  - Persist posted journal txn ids per generated depreciation row.
- **Depreciation reversal/void**
  - Create reversal postings and persist reversal journal ids.
  - Keep immutable history for original and reversal links.

#### Traceability expectations

- Asset/depreciation row -> posted journal ids -> journal entries.
- Journal entries -> originating asset/run ids via linkage columns and posting metadata.

---

## C. Read-model strategy

To keep UI fast while Journal stays canonical:

- Build denormalized read models/materialized views per panel.
- Rebuild incrementally on Journal append events.
- If read model gets stale/corrupt, it can be rebuilt from Journal + metadata links.

---

## D. Migration strategy

### Phase 1: Link-first adoption

- Keep existing panel storage.
- For each new/edited record, also post via Posting API and store `journal_txn_id`.

### Phase 2: Backfill existing data

- Reconstruct missing links by deterministic matching/migration scripts.
- Flag ambiguous cases for manual review.

### Phase 3: Enforce constraints

- Block finance-impacting operations that do not go through Posting API.
- Add DB constraints requiring journal links for posted states.

### Phase 4: Remove duplicate accounting fields

- Decommission shadow amount/balance columns from supplemental tables.

---

## E. Controls and audit

- Immutable posting log (who/when/why).
- Reversal reason required.
- Role-based permissions for post/reverse/period-lock actions.
- Reconciliation lock controls for closed statements/periods.
- Traceability query support:
  - domain record -> journal entries
  - journal entry -> originating domain module/record

---

## F. Engineering checklist for each panel

When onboarding a panel to Journal-first:

1. Identify all balance-impacting actions.
2. Define posting command schema.
3. Implement command-to-journal mapper with validation.
4. Store link metadata (`journal_txn_id`, role).
5. Replace direct balance math with Journal-derived queries.
6. Implement reversal/amend flows.
7. Add idempotency keys for import/batch operations.
8. Add regression tests for:
   - create/edit/delete
   - reverse/repost
   - report consistency
   - reconciliation lock behavior

---

## Expected outcomes

- One accounting truth path across all modules.
- Consistent reports (GL, budget vs actual, fund statements, reconciliations).
- Better auditability and safer correction workflows.
- Lower maintenance cost from removing duplicated accounting logic.
