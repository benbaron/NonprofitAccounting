# Current Posting and Journal Write Model

This document records the current design decision for finance-impacting writes.

## Write authority

For now, the legacy journal tables remain the write ledger:

- `journal_transaction`
- `journal_entry`

Active services should continue routing finance-impacting writes through `PostingFacade`, which preserves the existing `JournalRepository` write path and validation hook point.

## Canonical mirror

The canonical tables are a synchronized mirror/reference layer:

- `txn`
- `txn_split`

Facade writes through the legacy journal path are mirrored into these canonical tables by the existing repository/sync adapter path. The mirror is used for normalized references, read-model work, and future reporting/querying, but it is not yet the write authority.

## PostingReference

`PostingReference` carries both compatibility and canonical reference information:

- `journalTxnId` is the legacy journal transaction id and remains the compatibility id.
- `canonicalRef` is usually `txn:<id>` when the canonical mirror row exists.
- `canonicalTxnId()` parses a `txn:<id>` reference into a typed `Long` for domain tables that already have canonical transaction id columns.

If no canonical mirror is available, code must continue to operate from the legacy journal id.

## No PostingService decision

No decision has been made to replace `JournalRepository` with `org.nonprofitbookkeeping.service.PostingService`.

The current substitution is not `PostingService`; the current model is:

```text
legacy journal write path remains authoritative
canonical txn/txn_split mirror is populated and referenced where useful
```

## Migration posture

Safe incremental work at this stage:

1. Keep module-owned posting services writing through `PostingFacade`.
2. Preserve `journal_transaction` / `journal_entry` behavior.
3. Store `canonicalTxnId()` in domain records where a canonical transaction id column already exists.
4. Keep module-owned linkage tables unchanged until a separate link-model decision is made.
5. Do not remove `journal_*` tables or bypass `JournalRepository` without a later explicit design decision.
