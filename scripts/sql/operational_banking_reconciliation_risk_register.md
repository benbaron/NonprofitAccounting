# Prompt C Risk Register — Operational Banking Reconciliation

| Risk ID | Description | Impact | Mitigation |
|---|---|---|---|
| C-01 | Historical bank rows missing deterministic keys for fingerprint generation. | Medium | Backfill normalized description/external id with tiered fallback before enforcing uniqueness. |
| C-02 | Match status transitions enforced too early can block operations. | High | Stage rollout: additive schema first, service guards second, hard enforcement after cleanup. |
| C-03 | Duplicate detection false positives on recurring same-amount transactions. | Medium | Keep duplicate flags advisory initially; require reviewer confirmation for hard duplicate action. |
| C-04 | Reconciled status attached to open statements due to legacy flows. | Medium | Validation gate before close, plus reconciliation service guard updates. |
