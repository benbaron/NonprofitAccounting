# Legacy `ensureSchema()` responsibility inventory

This inventory covers the next cleanup slice after moving the reporting/schedule,
operational-linkage, and banking/reconciliation schema groups toward Flyway
ownership.

The important rule for this slice is that the remaining legacy accounting tables
are still part of active application write paths. They should not be removed from
`Database.ensureSchema()` until a guard proves Flyway owns their table shape and a
separate posting-path decision confirms whether legacy or canonical tables are the
write authority.

## Current `ensureSchema()` responsibilities to split

`Database.ensureSchema()` still invokes these legacy-accounting responsibilities:

| Method | Current responsibility | Cleanup classification |
| --- | --- | --- |
| `ensureAccountAndLegacyJournalTables(...)` | Creates and patches `account`, `account_fund`, `journal_transaction`, `journal_entry`, `transaction_info`, `txn_supplemental_line`, and `legacy_txn_map`; also applies account defaults/indexes and supplemental-line constraints/indexes. | Mixed schema DDL plus data repair. Add a Flyway-ownership guard before removing duplicate DDL. Preserve data repairs until reviewed separately. |
| `backfillLegacyTxnMap(...)` | Inserts mappings for legacy `journal_transaction` rows that already have matching canonical `txn` rows. | Data backfill. Do not delete with schema DDL cleanup. |
| `ensureCompatibilityViews(...)` | Creates read-model summary tables and recreates `v_journal_transaction` / `v_journal_entry` compatibility views over canonical `txn` / `txn_split`. | Mixed schema/view creation. Handle in a compatibility-view slice, not the legacy table-shape slice. |
| `ensureRemainingLegacyTables(...)` | Creates miscellaneous legacy storage tables and patches `imported_asset_record.accumulated_depreciation`. | Separate inventory needed; not part of journal/account DDL removal. |

## Active legacy write paths found

The following code still writes or allocates identifiers against the legacy
journal tables:

| Area | Evidence | Implication |
| --- | --- | --- |
| `JournalRepository` | `writeTransaction(...)` upserts `journal_transaction`, deletes/reinserts `journal_entry`, and replaces `transaction_info`. | Legacy journal remains an active write authority for the main repository path. |
| Donation posting | `DonationPostingService` allocates the next id from `journal_transaction` before building an `AccountingTransaction`. | Donation posting still depends on legacy id allocation and downstream legacy journal persistence. |
| Depreciation reversal | `DepreciationRunProcessingService` allocates a new legacy transaction id, inserts a reversal `journal_transaction`, copies reversed `journal_entry` rows, and deletes legacy journal headers when undoing. | Depreciation workflows still directly mutate legacy journal tables. |
| SC ledger gateway | `JournalLedgerPersistenceGateway` allocates transaction ids from `journal_transaction`. | Scaledger persistence still coordinates with legacy journal identity. |
| Canonical bridge | `CanonicalJournalSyncAdapter` reads `journal_transaction` and writes `txn_split` as part of legacy-to-canonical synchronization. | The canonical bridge depends on legacy rows remaining readable during migration. |

## Recommended next PR sequence

1. Add a guard test proving Flyway alone owns the legacy journal/account column
   sets for `ACCOUNT`, `ACCOUNT_FUND`, `JOURNAL_TRANSACTION`, `JOURNAL_ENTRY`,
   `TRANSACTION_INFO`, `TXN_SUPPLEMENTAL_LINE`, and `LEGACY_TXN_MAP`.
2. Extend the guard to compare foreign keys and indexes for those tables before
   deleting Java FK/index DDL.
3. Remove only duplicate Java table/column/index/constraint DDL that the guard
   proves is Flyway-owned.
4. Keep `backfillLegacyTxnMap(...)`, account data repairs, and compatibility
   views until separately reviewed.
5. Revisit active legacy write paths before attempting to make `txn` /
   `txn_split` the sole write authority.

## Open questions for the removal slice

- Should `account` table-shape cleanup be grouped with legacy journal cleanup, or
  split into its own PR because account rows are referenced by both legacy and
  canonical posting paths?
- Should read-model summary tables in `ensureCompatibilityViews(...)` move to
  Flyway alongside compatibility views, or remain in a read-model maintenance
  slice?
- Should the legacy id allocation strategy switch to a dedicated sequence before
  any deeper canonical-write cutover?
