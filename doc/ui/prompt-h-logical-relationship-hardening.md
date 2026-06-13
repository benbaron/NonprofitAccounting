# Prompt H — Logical Relationship Hardening (Dashed Overlay Edges)

## Scope and source of truth
This plan is based on dashed logical edges (`69..81`) in `doc/database-schema-er.drawio` (`Logical Relationships Overlay`).

## Execution artifacts (implemented)
- Preflight SQL: `scripts/sql/logical_relationship_hardening_preflight.sql`
- Forward migration SQL: `scripts/sql/logical_relationship_hardening_forward.sql`
- Rollback SQL: `scripts/sql/logical_relationship_hardening_rollback.sql`
- Validation SQL: `scripts/sql/logical_relationship_hardening_validation.sql`
- Risk register: `scripts/sql/logical_relationship_hardening_risk_register.md`

## Decision matrix (physical FK now vs remain logical)

| Priority | Relationship (dashed edge) | Write throughput impact | Backfill cost | Orphan risk | Operational coupling | Decision |
|---|---|---:|---:|---:|---:|---|
| P0 | `fund_transfer.from_fund_id -> fund.id` (74) | Low | Medium | High | Medium | Promote to physical FK now |
| P0 | `fund_transfer.to_fund_id -> fund.id` (75) | Low | Medium | High | Medium | Promote to physical FK now |
| P0 | `fund_transfer.posted_txn_id -> txn.id` (76) | Low | Medium | High | Medium | Promote to physical FK now |
| P1 | `account_alias.account_id -> account.id` (70) | Low | Low-Med | Medium-High | Low | Promote to physical FK now |
| P1 | `fund_alias.fund_id -> fund.id` (71) | Low | Low-Med | Medium-High | Low | Promote to physical FK now |
| P1 | `account_report_section.account_id -> account.id` (77) | Low | Low | Medium | Low | Promote to physical FK now |
| P1 | `account_report_section.report_section_id -> report_section.id` (78) | Low | Low | Medium | Low | Promote to physical FK now |
| P1 | `account_schedule_requirement.account_id -> account.id` (79) | Low | Low | Medium | Low | Promote to physical FK now |
| P1 | `account_schedule_requirement.schedule_kind_id -> schedule_kind.id` (80) | Low | Low | Medium | Low | Promote to physical FK now |
| P1 | `account_subtype_schedule_default.schedule_kind_id -> schedule_kind.id` (81) | Low | Low | Medium | Low | Promote to physical FK now |
| P2 | `fund.parent_id -> fund.id` (69) | Medium | High | Medium | High | Keep logical for now; enforce after hierarchy cleanup |
| P2 | `txn_split.activity_id -> activity.id` (72) | Medium | Medium | Medium | Medium-High | Keep logical for now; enforce after hot-path benchmarking |
| P2 | `txn_split.merchant_id -> merchant.id` (73) | Medium | Medium | Medium | Medium-High | Keep logical for now; enforce after hot-path benchmarking |

## Phased enforcement plan (PostgreSQL)
1. **Preflight**: Run `logical_relationship_hardening_preflight.sql` to detect missing columns, type mismatches, and existing FK drift.
2. **Phase 1**: Apply `logical_relationship_hardening_forward.sql` (`NOT VALID` constraints + supporting indexes).
3. **Phase 2**: Backfill/remediate orphan rows identified by validation script.
4. **Phase 3**: Run `logical_relationship_hardening_validation.sql` and execute `VALIDATE CONSTRAINT` during low-traffic windows.
5. **Phase 4 (P2)**: Implement separate migration pack for `fund.parent_id`, `txn_split.activity_id`, and `txn_split.merchant_id` after cleanup/performance tests.

## Code review notes (current state)
1. The migration now includes a **preflight drift check** to reduce runtime failure risk from schema/type mismatches.
2. The forward migration is idempotent for repeated deploy attempts via `pg_constraint` guards.
3. Constraint names were kept stable to align with existing migration naming, and drift visibility is provided in preflight output.
