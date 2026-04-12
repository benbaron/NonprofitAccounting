# Transaction Convergence Risk Register

| ID | Risk | Impact | Likelihood | Mitigation | Owner | Trigger |
|---|---|---|---|---|---|---|
| R1 | Dual-write divergence during transition | Financial misstatement | High | Single-write canonical + mandatory validation SQL gates | Data Eng | Validation delta != 0 |
| R2 | Legacy report breakage due to interface mismatch | Reporting outage | Medium | Compatibility views + staged report diff tests | App Eng | Report totals mismatch |
| R3 | Constraint rollout fails on historical dirty rows | Release delay | Medium | Pre-clean scripts, phased enablement in staging | DBA | DDL validation failures |
| R4 | Performance regressions on compatibility views | Slow close/reporting | Medium | Composite indexes + plan inspection + p95 monitors | DBA/SRE | p95 above SLO |
| R5 | Rollback misses post-cutover writes | Data loss risk | Low | Point-in-time backup bookmark + replay process | DBA | Rollback event |
