# Prompt G Risk Register — Grant/Donor/Program Traceability

| Risk ID | Risk | Likelihood | Impact | Mitigation | Rollout Phase |
|---|---|---:|---:|---|---|
| G-01 | Historical `grant_record` rows have missing donor/contact references and will violate new contact-presence check. | High | High | Run preflight cleanup to backfill `counterparty_id` or `person_id` before enabling strict checks in production. | Phase 0-1 |
| G-02 | Dual posting models (`journal_entry` and `txn_split`) can result in duplicate linkage for the same economic event. | Medium | High | Enforce `grant_posting_link` XOR target check and reconciliation queries to detect duplicate recognition totals. | Phase 1-2 |
| G-03 | Existing integrations write `date_awarded_text` only; new date columns may remain null and degrade compliance reporting. | High | Medium | Add parser/backfill job to populate `award_date`, `period_start`, and `period_end`; keep legacy text column for compatibility. | Phase 1-2 |
| G-04 | New FKs may fail on dirty data (orphan fund/activity references). | Medium | High | Run validation SQL V1 and remediate orphan rows before promoting migration to production. | Phase 0 |
| G-05 | Restriction classification defaults to `RESTRICTED`; misclassification could misstate unrestricted reporting. | Medium | High | Require classification review queue and sign-off for grants created before model rollout. | Phase 2 |
| G-06 | `grant_reference_number` unique index can fail if duplicate external grant IDs already exist. | Medium | Medium | Populate canonical unique value set with deterministic suffixing for duplicates before creating unique index. | Phase 1 |
| G-07 | Reporting consumers may depend on direct table access and not adopt `v_grant_restriction_reporting`. | Medium | Medium | Publish compatibility mapping and run parallel report checks for one close cycle before cutover. | Phase 2-3 |
| G-08 | Additional indexes increase write cost on grant maintenance workflows. | Low | Medium | Measure insert/update latency post-deploy; drop low-value indexes after 30-day observation if needed. | Phase 3 |
| G-09 | Compliance statuses may drift from policy if updated ad-hoc without workflow control. | Medium | Medium | Add service-layer state transition guard and audit log for compliance status changes. | Phase 3 |
| G-10 | Rollback complexity if downstream reporting already depends on new view/table. | Low | High | Use feature flag and staged adoption; keep rollback scripts ready and only enable consumers after validation pass. | Phase 3-4 |

## Recommended sequencing
1. **Phase 0 (Profile & cleanup):** Run `grant_traceability_validation.sql` V1-V3 and remediate data quality defects.
2. **Phase 1 (Additive schema):** Deploy `grant_traceability_forward.sql` with constraints and indexes.
3. **Phase 2 (Backfill & reconcile):** Populate new columns and create `grant_posting_link` entries from historical postings.
4. **Phase 3 (Operational hardening):** Cut over compliance dashboards to `v_grant_restriction_reporting`; enforce workflow guards.
5. **Phase 4 (Steady-state):** Monitor performance, validate month-end totals, and retire temporary backfill scripts.
