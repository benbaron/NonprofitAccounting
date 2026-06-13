# Prompt D — Fund Transfer Integrity (`fund_transfer` + posted transactions)

## Required invariants
- Transfer amounts are positive and source/target funds are distinct.
- `fund_transfer.status` must be in: `DRAFT`, `APPROVED`, `POSTING`, `POSTED`, `FAILED`, `VOIDED`.
- `posted_txn_id` must be present **only** when status is `POSTED`.
- One posted canonical transaction can back at most one transfer.
- For posted transfers, linked `txn`/`txn_split` rows must net to zero and carry equal/opposite fund legs:
  - from-fund net = `-fund_transfer.amount`
  - to-fund net = `+fund_transfer.amount`
- Posting must be atomic at service level: write `txn`, `txn_split`, and `fund_transfer.posted_txn_id/status='POSTED'` in one DB transaction.

## Recommended constraints and trigger strategy
- Enforce domain and status/link checks with DB `CHECK` constraints.
- Enforce uniqueness of `posted_txn_id`.
- Maintain a status transition policy table (`fund_transfer_status_transition`) used by service guard or DB trigger.
- For engines that support SQL triggers, enforce transition legality and emit integrity events.
- In H2 environments, keep trigger semantics in service layer and run validation SQL as a release/close gate.

## Historical mismatch repair approach
1. Run validation SQL to enumerate all mismatches.
2. Insert unresolved mismatches into `fund_transfer_repair_queue`.
3. Review each queue item and choose action:
   - reclassify status/link,
   - re-post transfer with corrected splits,
   - post compensating reversal and replacement transfer.
4. Mark queue item resolved with approver + note.
5. Re-run validation SQL until clean.

## Deliverables
- Forward migration SQL: `scripts/sql/fund_transfer_integrity_forward.sql`
- Rollback SQL: `scripts/sql/fund_transfer_integrity_rollback.sql`
- Validation SQL: `scripts/sql/fund_transfer_integrity_validation.sql`
- Risk register: `scripts/sql/fund_transfer_integrity_risk_register.md`
