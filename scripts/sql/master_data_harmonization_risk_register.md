# Account/Fund Master Data Harmonization Risk Register

| ID | Risk | Impact | Likelihood | Mitigation | Owner | Trigger |
|---|---|---|---|---|---|---|
| M1 | Normalized uniqueness indexes fail because historical duplicates already exist. | Migration blocked in prod. | High | Run validation SQL first, triage into alias_review_queue, and temporarily mark stale rows inactive before enabling app-level writes. | DBA + Data Steward | DDL failure on `uq_*_norm_active` indexes. |
| M2 | Alias deactivation causes import misses for older files. | Transactions fail to auto-match and require manual mapping. | Medium | Keep deprecated aliases as inactive records and log import misses with suggested candidates for steward review. | App Eng | Import mismatch rate increases > baseline. |
| M3 | Over-aggressive normalization (`space`/`dash` removal) collapses legitimately distinct keys. | Incorrect postings to wrong account/fund. | Medium | Deterministic algorithm requires exact key match first, then alias match; ambiguous matches never auto-post and must enter review queue. | Data Governance | Candidate count > 1 for normalized token. |
| M4 | Alias review queue grows faster than staff can resolve. | Operational backlog and delayed close. | Medium | Set SLA by priority (high-volume aliases first), dashboard queue aging, and weekly steward triage. | Controller Office | >50 OPEN items older than 14 days. |
| M5 | Rollback removes governance table needed for audit trail. | Reduced traceability after rollback. | Low | Export queue snapshot before rollback and retain in release artifact repository. | Release Manager | Rollback executed after cutover. |
