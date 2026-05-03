# Unified Posting Façade (Phase 1)

## What changed

A new service-layer façade (`PostingFacade`) now provides a unified API for finance-impacting writes:

- `post(command)`
- `reverse(journalTxnId, reason)`
- `amend(oldJournalTxnId, newCommand, reason)` (implemented as reverse + post)

`DefaultPostingFacade` wraps existing `JournalRepository` behavior so persistence semantics remain backward-compatible.

## Validation hooks

`DefaultPostingFacade` calls explicit validators before posting:

- `PostingDatePolicyValidator`
- `AccountFundRestrictionValidator`
- `PostingLockValidator`

Current default implementations are no-op stubs and are intended to be wired to policy and period-lock services in a later phase.

## Stable reference strategy

The façade returns `PostingReference` with:

- `journalTxnId`
- `canonicalRef` (`journal_transaction:<id>`)

This reference maps directly to the canonical journal transaction row.

## Donations migration strategy

`DonationPostingService` now routes journal writes through the façade while preserving existing donation record/link behavior and edit policy semantics.

Migration strategy for other modules:

1. Build `AccountingTransaction` as before.
2. Wrap in `PostingCommand` with module/domain metadata.
3. Call façade `post/reverse/amend`.
4. Keep module-owned linkage tables unchanged until full canonical link consolidation.
