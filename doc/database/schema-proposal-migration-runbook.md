# Schema Proposal Migration Runbook Index

This runbook maps each schema proposal to concrete migration artifacts and an execution order.

## Rollout Order

1. Prompt H — Logical Relationship Hardening
2. Prompt B — Master Data Harmonization
3. Prompt D — Fund Transfer Integrity
4. Prompt E — Reporting & Schedule Configuration
5. Prompt F — Asset & Depreciation Audit Readiness
6. Prompt G — Grant/Donor/Program Traceability
7. Prompt A — Transaction Model Convergence
8. Prompt C — Operational Banking Reconciliation

## Artifact Map

| Prompt | Forward SQL | Rollback SQL | Validation SQL | Risk Register / Proposal |
|---|---|---|---|---|
| A | `scripts/sql/txn_convergence_forward.sql` | `scripts/sql/txn_convergence_rollback.sql` | `scripts/sql/txn_convergence_validation.sql` | `scripts/sql/txn_convergence_risk_register.md`, `doc/transaction-model-convergence-plan.md` |
| B | `scripts/sql/master_data_harmonization_forward.sql` | `scripts/sql/master_data_harmonization_rollback.sql` | `scripts/sql/master_data_harmonization_validation.sql` | `scripts/sql/master_data_harmonization_risk_register.md`, `scripts/sql/master_data_harmonization_proposal.md` |
| C | `scripts/sql/operational_banking_reconciliation_forward.sql` | `scripts/sql/operational_banking_reconciliation_rollback.sql` | `scripts/sql/operational_banking_reconciliation_validation.sql` | `scripts/sql/operational_banking_reconciliation_risk_register.md`, `doc/prompt-c-operational-banking-reconciliation.md` |
| D | `scripts/sql/fund_transfer_integrity_forward.sql` | `scripts/sql/fund_transfer_integrity_rollback.sql` | `scripts/sql/fund_transfer_integrity_validation.sql` | `scripts/sql/fund_transfer_integrity_risk_register.md`, `doc/prompt-d-fund-transfer-integrity.md` |
| E | `scripts/sql/reporting_schedule_configuration_forward.sql` | `scripts/sql/reporting_schedule_configuration_rollback.sql` | `scripts/sql/reporting_schedule_configuration_validation.sql` | `scripts/sql/reporting_schedule_configuration_risk_register.md`, `scripts/sql/reporting_schedule_configuration_proposal.md` |
| F | `scripts/sql/asset_depreciation_audit_readiness_forward.sql` | `scripts/sql/asset_depreciation_audit_readiness_rollback.sql` | `scripts/sql/asset_depreciation_audit_readiness_validation.sql` | `scripts/sql/asset_depreciation_audit_readiness_risk_register.md`, `scripts/sql/asset_depreciation_audit_readiness_proposal.md` |
| G | `scripts/sql/grant_traceability_forward.sql` | `scripts/sql/grant_traceability_rollback.sql` | `scripts/sql/grant_traceability_validation.sql` | `scripts/sql/grant_traceability_risk_register.md`, `doc/prompt-g-grant-donor-program-traceability.md` |
| H | `scripts/sql/logical_relationship_hardening_forward.sql` | `scripts/sql/logical_relationship_hardening_rollback.sql` | `scripts/sql/logical_relationship_hardening_validation.sql` | `scripts/sql/logical_relationship_hardening_risk_register.md`, `doc/prompt-h-logical-relationship-hardening.md` |

## Minimum Acceptance Gate Per Prompt

- Preflight/orphan checks are clean (or waived with explicit risk sign-off).
- Forward SQL applies successfully.
- Validation SQL returns expected invariants.
- Rollback SQL executes in dry-run environment.
