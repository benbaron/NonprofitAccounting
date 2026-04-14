# Fund Transfer Integrity Risk Register

| ID | Risk | Impact | Likelihood | Mitigation | Owner | Trigger |
|---|---|---|---|---|---|---|
| FT-R1 | Transfer status says `POSTED` but `posted_txn_id` is null | Missing audit trail and broken reports | Medium | Enforce `chk_fund_transfer_posted_link_by_status`; run validation SQL in deploy gate | DBA + App Eng | Validation check #2 returns rows |
| FT-R2 | `posted_txn_id` points to unbalanced `txn_split` set | Financial statement drift | Medium | Validation check #4 blocks close; require adjusting/reversal post before period close | Accounting Ops | Non-zero net on posted transfer txn |
| FT-R3 | Wrong funds used on posted lines relative to `from_fund_id` / `to_fund_id` | Misclassified restricted fund movement | Medium | Validation check #5; repair queue workflow with controller approval | Controller | Fund leg mismatch rows |
| FT-R4 | Concurrent posting creates duplicate transfer linkage to one txn | Double-counting or broken traceability | Low | Unique constraint `uq_fund_transfer_posted_txn`; serialize posting worker by transfer id | App Eng | Duplicate key violation / check #6 hit |
| FT-R5 | Historical dirty data blocks migration | Release delay | Medium | Run validation SQL first, stage repair queue, apply fixes in batches, then enforce constraints | DBA | Migration dry-run fails |
| FT-R6 | Status transitions bypass policy in application logic | Inconsistent lifecycle and stuck records | Medium | Use `fund_transfer_status_transition` as policy table and enforce via service/trigger guards; monitor POSTING age | App Eng | Transfers stuck in POSTING > 1 day |
| FT-R7 | Repair scripts over-correct historical rows | Audit concerns and unintended data mutation | Low | Two-phase repair (queue + human approval + controlled update scripts), backup before bulk updates | DBA + Finance | Large unexpected update count |
