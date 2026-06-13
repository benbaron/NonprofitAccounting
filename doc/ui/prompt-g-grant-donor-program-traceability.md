# Prompt G — Grant/Donor/Program Traceability (`grant_record`, `donor`, `person`, `activity`, `fund`)

## 1) Recommended Traceability Design

### 1.1 Target relationship model
- Keep `grant_record` as the grant award/control header.
- Keep donor/contact dimensions split for compatibility (`donor`, `person`) but add canonical `counterparty_id` on `grant_record` for convergence with canonical transactions.
- Add `activity_id` on `grant_record` so each grant can be tied to a primary program/activity reporting axis.
- Keep `fund_id` mandatory in policy (nullable in schema for backfill compatibility) to anchor restricted/unrestricted reporting.
- Add `canonical_txn_id` alongside existing `journal_txn_id` for dual-ledger traceability.
- Add a new bridge table `grant_posting_link` for one-to-many linkage from grant award to specific postings (`txn_split` or `journal_entry`).

### 1.2 Why this model
- Supports **both** legacy and canonical posting paths without forcing an immediate cutover.
- Preserves donor/contact data while introducing a path to `counterparty` normalization.
- Produces auditable grant-level reporting using posting-level joins rather than free-text memo heuristics.

## 2) Restricted-vs-Unrestricted Reference Reporting Model

Use `grant_record.restriction_class` plus linked fund/activity/postings:
- `RESTRICTED`: donor/grantor-imposed restrictions.
- `UNRESTRICTED`: no donor-imposed restrictions.
- `BOARD_DESIGNATED`: internally designated funds that should remain distinguishable from donor-restricted classes.

Reference output is exposed via `v_grant_restriction_reporting` and includes:
- award amount,
- recognized amount,
- deferred amount,
- unrecognized balance,
- donor/contact identity,
- fund + activity dimensions,
- compliance due-date fields.

## 3) Missing Constraints / Columns (Compliance Focus)

Added columns for compliance and controls:
- lifecycle dates: `award_date`, `period_start`, `period_end`, `closeout_date`.
- restrictions/compliance dimensions: `restriction_class`, `restriction_release_rule`, `compliance_status`, `reporting_frequency`, `next_report_due`.
- traceability and dedupe: `grant_reference_number` (unique), `canonical_txn_id`, `activity_id`, `counterparty_id`, `contact_person_id`.

Added constraints and integrity controls:
- domain checks for `restriction_class` and `compliance_status`.
- date-range check (`period_start <= period_end`).
- non-negative award amount check.
- contact-presence check (at least one of `donor_id`, `person_id`, `counterparty_id`, or legacy `grantor` text) to preserve compatibility for service-owned rows.
- physical FKs from `grant_record` to donor/person/fund/activity/txn/journal.
- posting-link XOR check in `grant_posting_link` (exactly one of canonical split or legacy entry).

## 4) Deliverables
- Forward migration SQL: `scripts/sql/grant_traceability_forward.sql`
- Rollback SQL: `scripts/sql/grant_traceability_rollback.sql`
- Validation SQL: `scripts/sql/grant_traceability_validation.sql`
- Risk register: `scripts/sql/grant_traceability_risk_register.md`
