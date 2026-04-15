# Logical Relationship Hardening — Risk Register

| ID | Risk | Likelihood | Impact | Detection | Mitigation | Owner |
|---|---|---:|---:|---|---|---|
| LH-R1 | Existing orphan rows block `VALIDATE CONSTRAINT`. | High | High | Run `scripts/sql/logical_relationship_hardening_validation.sql` orphan inventory section before validation. | Clean and/or quarantine orphan rows before running `VALIDATE CONSTRAINT`. | DBA + Data Ops |
| LH-R2 | FK checks increase write latency on `fund_transfer` and config tables. | Medium | Medium | Track p95/p99 write latency and lock waits during rollout. | Create child-key indexes first, deploy in phases, run cutovers during low traffic windows. | Platform |
| LH-R3 | Producer/consumer ordering mismatch causes runtime FK violations. | Medium | High | Observe DB errors and app error-rate spikes after enabling constraints. | Enforce write ordering contract; use retries with bounded dead-letter queue. | App Team |
| LH-R4 | Partial rollout leaves constraints in `NOT VALID` indefinitely. | Medium | High | Query `pg_constraint.convalidated = false` as a release gate. | Add explicit migration checklist step: orphan cleanup + scheduled `VALIDATE CONSTRAINT`. | Release Manager |
| LH-R5 | Rollback removes safety constraints without compensating application checks. | Low | Medium | Post-rollback drift in orphan counts. | Keep rollback playbook with temporary app-level integrity guards and post-rollback audits. | On-call DBA |
| LH-R6 | P2 links (`fund.parent_id`, `txn_split.activity_id`, `txn_split.merchant_id`) remain unenforced too long. | Medium | Medium | Monthly drift report on dangling references/cycles. | Track as explicit backlog with SLA and deliver dedicated P2 migration after data cleanup. | Data Model Owner |
