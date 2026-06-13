# Unified Posting Façade (Phase 1)

## What changed

A service-layer façade (`PostingFacade`) provides a unified API for finance-impacting writes:

- `post(command)`
- `reverse(journalTxnId, reason)`
- `amend(oldJournalTxnId, newCommand, reason)` (implemented as reverse + post)

`DefaultPostingFacade` wraps existing `JournalRepository` behavior so persistence semantics remain backward-compatible.

## Current write model

The legacy journal tables remain the write ledger for now:

- `journal_transaction`
- `journal_entry`

The normalized `txn` / `txn_split` tables are a synchronized mirror/reference layer produced by the existing repository/sync adapter path. They are not currently the write authority.

No decision has been made to replace `JournalRepository` with `org.nonprofitbookkeeping.service.PostingService`.

## Validation hooks

`DefaultPostingFacade` calls explicit validators before posting:

- `PostingDatePolicyValidator`
- `AccountFundRestrictionValidator`
- `PostingLockValidator`

Current default implementations are no-op stubs and are intended to be wired to policy and period-lock services in a later phase.

## Stable reference strategy

The façade returns `PostingReference` with:

- `journalTxnId`: the legacy journal transaction id and compatibility id
- `canonicalRef`: usually `txn:<id>` when the canonical mirror row exists
- `canonicalTxnId()`: a typed `Long` parsed from `txn:<id>`, or `null` when no canonical mirror reference is available

Domain records may store `canonicalTxnId()` where they already have a canonical transaction id column, while keeping legacy journal ids and module-owned linkage tables unchanged.

## Donations migration strategy

`DonationPostingService` routes journal writes through the façade while preserving existing donation record/link behavior and edit policy semantics.

Migration strategy for other modules:

1. Build `AccountingTransaction` as before.
2. Wrap in `PostingCommand` with module/domain metadata.
3. Call façade `post/reverse/amend`.
4. Preserve `journal_transaction` / `journal_entry` behavior.
5. Store `canonicalTxnId()` in domain records only where a canonical transaction id column already exists.
6. Keep module-owned linkage tables unchanged until a separate link-model decision is made.
