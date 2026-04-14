# Database ERD Table Guide

This guide explains what each table in `doc/database-schema-er.drawio` represents.

## Domain: Legacy Ledger

- **account**: Legacy chart-of-accounts record keyed by `account_number`; stores account identity/type and legacy attributes.
- **account_fund**: Legacy join table mapping accounts to fund identifiers.
- **journal_transaction**: Legacy transaction header (date/memo/payee-ish text/reconciliation markers).
- **journal_entry**: Legacy transaction lines (debit/credit style amounts) linked to `journal_transaction` and `account`.
- **transaction_info**: Key/value metadata attached to a legacy transaction.
- **txn_supplemental_line**: Supplemental sub-ledger details tied to legacy transactions/entries (e.g., due dates, references).
- **person**: Contact/person directory used by supplemental lines and grant tracking.
- **donor**: Donor directory for fundraising/grant contexts.
- **bank_statement**: Imported/recorded bank statement snapshots used for reconciliation checks.

## Domain: Canonical GL

- **chart_of_accounts**: Canonical CoA version/header (name, version, status, timestamps).
- **account**: Shared account table used by both legacy and canonical paths; canonical mode uses numeric identity and hierarchy links.
- **fund**: Fund master with fund type, optional parent fund, status, effective dates, and restrictions.
- **counterparty**: Payee/payor/vendor/customer-like party referenced by canonical transactions.
- **txn**: Canonical transaction header with date, memo, payee (`counterparty`), and bank account link.
- **txn_split**: Canonical transaction line/split (account + fund + signed amount) plus activity/merchant context.
- **activity**: Program/activity dimension for split tagging.
- **merchant**: Merchant dimension for split tagging.
- **schedule_kind**: Type catalog for required schedules/disclosures.
- **report_section**: Financial-statement/report bucket definitions.
- **account_alias**: Alternate names/codes for account lookup/import mapping.
- **fund_alias**: Alternate names/codes for fund lookup/import mapping.
- **account_report_section**: Mapping between accounts and report sections (ordering/sign policy).
- **account_schedule_requirement**: Per-account schedule requirements.
- **account_subtype_schedule_default**: Default schedule requirement by account subtype.
- **fund_transfer**: Inter-fund transfer records (from/to fund, amount, status, optional posted canonical txn).

## Domain: Operations / Platform Support

- **schema_migration_history**: Records applied schema upgrades.
- **company_profile**: Organization-level settings and metadata for the active company file.
- **undeposited_funds_item**: Operational queue of undeposited items/checks/transfers pending resolution.
- **document**: Generic binary/blob document storage keyed by name.
- **json_storage**: Generic JSON payload key/value store.
- **company_store**: Named persisted payloads associated with company-level features.
- **bank_id_record**: Bank account identity registry (bank/account identifiers and metadata).
- **ledger_record**: Operational ledger linkage table connecting ledger IDs to journal entries/bank IDs.
- **banking_transaction_record**: Imported bank transaction log linked to bank IDs, journal txns, and funds.
- **asset_record_detail**: Fixed-asset register details and optional source journal linkage.
- **inventory_asset_link**: Links inventory items to fixed-asset records and journal transactions.
- **depreciation_run**: Batch/run header for depreciation processing.
- **depreciation_record**: Per-asset depreciation entries tied to a depreciation run.
- **grant_record**: Grant tracking table linked to donor/person/fund/journal context.
- **sale_record**: Sales activity records (item, quantity, pricing/cost details).

## Workflow Examples (End-to-End Table Paths)

### 1) Record a donation (cash receipt)
**Source of Truth:** Mixed (Canonical-first; Legacy mirror where required)
1. Create or resolve the donor/contact (`donor`, optionally `person`).
2. Create canonical transaction header (`txn`) with payer linked through `counterparty` (if modeled as counterparty).
3. Add one or more splits (`txn_split`) such as debit Cash, credit Contribution Revenue, with optional `fund` and `activity` tags.
4. If legacy path is still active, mirror/bridge to legacy rows (`journal_transaction` + `journal_entry`) as needed by existing features.

### 2) Post an expense paid from bank
**Source of Truth:** Canonical
1. Resolve vendor/payee (`counterparty`, optionally `merchant`).
2. Insert transaction header (`txn`) with date/memo.
3. Insert expense and cash lines in `txn_split` with target `account`(s), optional `fund`, optional `activity`.
4. Reconciliation/import features may cross-reference `banking_transaction_record` and `bank_id_record`.

### 3) Import and reconcile bank activity
**Source of Truth:** Mixed (Operational reconciliation + ledger posting model in use)
1. Load imported bank lines into `banking_transaction_record` tied to `bank_id_record`.
2. Match candidates to accounting entries via `txn` (canonical) and/or `journal_transaction` (legacy).
3. Persist linkage/traceability in `ledger_record`; mark statement-level context in `bank_statement` where applicable.

### 4) Execute inter-fund transfer
**Source of Truth:** Canonical
1. Create transfer intent row in `fund_transfer` (`from_fund_id`, `to_fund_id`, amount, status).
2. Post balancing accounting transaction in canonical tables (`txn` + paired `txn_split` rows).
3. Backfill `fund_transfer.posted_txn_id` when posting succeeds.

### 5) Run depreciation batch
**Source of Truth:** Mixed (Operational assets + posting path in use)
1. Create batch header in `depreciation_run`.
2. For each asset in `asset_record_detail`, write a `depreciation_record` row.
3. Optionally generate/post journal impact through `txn`/`txn_split` or legacy `journal_transaction`/`journal_entry` depending on enabled path.

### 6) Track grant activity and allocations
**Source of Truth:** Mixed (Grant metadata + accounting path in use)
1. Store grant metadata and ownership in `grant_record` (linked to `donor`, `person`, `fund`).
2. Record grant-related postings in accounting lines (`txn_split` or legacy entries).
3. Use `transaction_info`/`txn_supplemental_line` where extra grant references or reporting keys are needed.

### 7) Manage undeposited funds to bank deposit
**Source of Truth:** Mixed (Operational queue with canonical posting target)
1. Accumulate pending receipts in `undeposited_funds_item`.
2. On deposit, create bank-facing posting (`txn` + `txn_split`), then mark item(s) resolved.
3. Tie resulting bank movement to reconciliation artifacts (`banking_transaction_record`, `bank_statement`) when imported.

### 8) Produce financial statement mappings
**Source of Truth:** Canonical config tables
1. Maintain statement buckets (`report_section`) and account mappings (`account_report_section`).
2. Configure schedule requirements (`schedule_kind`, `account_schedule_requirement`, `account_subtype_schedule_default`).
3. Use configured mappings during report generation over posted balances.

## Legacy ↔ Canonical Overlap: Gotchas and Migration Notes

- **Shared `account` table, dual semantics:** legacy expects natural-key/account-number behavior while canonical features rely on numeric IDs and hierarchy metadata. Migration scripts should preserve both lookup modes.
- **Parallel transaction models:** some workflows still use `journal_transaction`/`journal_entry` while newer flows use `txn`/`txn_split`; avoid assuming one is always authoritative.
- **Logical vs enforced relationships:** several cross-domain links are application-level only (dashed in ERD overlay); integrity checks may need app/service validation rather than DB constraints.
- **Alias tables are critical for import stability:** `account_alias` and `fund_alias` should be curated before large historical imports to reduce mapping drift.
- **Fund transfer posting state:** `fund_transfer` may exist before posting; downstream logic should handle unposted transfers gracefully.
- **Operational tables are not always accounting source-of-truth:** `banking_transaction_record`, `ledger_record`, and `undeposited_funds_item` are workflow/control artifacts and may lag finalized journal posting state.
- **Grant/sales/asset modules may reference either ledger path:** integration code should explicitly choose canonical vs legacy posting targets to avoid duplicate financial impact.

## GPT Prompt Pack for Schema Unification / Improvement

Specific modification name: **Schema Unification Prompt Pack (v1)**

Use the prompts below with GPT to drive focused design reviews. Each prompt names a concrete schema section and asks for actionable outputs.

**Required deliverables checklist for every prompt response**
- Provide target-state recommendation(s) and tradeoff rationale.
- Provide forward migration SQL outline (DDL/index/constraint changes).
- Provide rollback SQL outline.
- Provide validation SQL checks (pre/post migration and invariants).
- Provide risk register with mitigations and rollout sequencing.


### Prompt A — Section: Transaction Model Convergence (`journal_transaction`/`journal_entry` vs `txn`/`txn_split`)
> You are a database architect reviewing a nonprofit accounting schema with dual transaction models: legacy (`journal_transaction`, `journal_entry`) and canonical (`txn`, `txn_split`). Produce: (1) canonical target model recommendation, (2) compatibility view strategy, (3) phased migration plan with rollback points, (4) data quality checks to guarantee no net balance drift, and (5) SQL DDL proposals for constraints/indexes. Include risks and mitigation. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

### Prompt B — Section: Account/Fund Master Data Harmonization (`account`, `fund`, aliases)
> Evaluate master-data consistency for `account`, `fund`, `account_alias`, and `fund_alias`. Recommend naming/key standards, dedupe rules, alias governance, and import-time matching policies. Return a proposed constraint set, sample deterministic matching algorithm, and a backfill plan for ambiguous aliases. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

### Prompt C — Section: Operational Banking Reconciliation (`bank_id_record`, `banking_transaction_record`, `ledger_record`, `bank_statement`)
> Propose an improved reconciliation architecture using `bank_id_record`, `banking_transaction_record`, `ledger_record`, and `bank_statement`. Deliver a state machine for matching status, idempotent import strategy, and anomaly detection rules (duplicates, amount/date outliers, stale unmatched items). Include schema/index changes and retention guidance. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

Reference response: `doc/prompt-c-operational-banking-reconciliation.md`.

### Prompt D — Section: Fund Transfer Integrity (`fund_transfer` + posted transactions)
> Design strict integrity rules for inter-fund transfers using `fund_transfer` and linked posted entries in `txn`/`txn_split`. Specify required invariants (equal/opposite amounts, status transitions, posting atomicity), recommended constraints/triggers, and a repair script approach for historical mismatches. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

Reference response: `doc/prompt-d-fund-transfer-integrity.md`.

### Prompt E — Section: Reporting & Schedule Configuration (`report_section`, schedule tables)
> Review `report_section`, `schedule_kind`, `account_report_section`, `account_schedule_requirement`, and `account_subtype_schedule_default`. Recommend a normalized model for statement mapping and schedule requirements that supports versioning and auditability. Provide migration SQL outline and validation queries. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

### Prompt F — Section: Asset & Depreciation Subsystem (`asset_record_detail`, `depreciation_run`, `depreciation_record`, `inventory_asset_link`)
> Propose improvements to fixed-asset and depreciation tables for audit readiness. Include constraints for run immutability, asset lifecycle states, and linkage to posted accounting transactions. Provide a monthly close checklist and suggested indexes. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

### Prompt G — Section: Grant/Donor/Program Traceability (`grant_record`, `donor`, `person`, `activity`, `fund`)
> Recommend a grant traceability design that connects `grant_record` to donor/contact, restrictions, program activity, and financial postings. Provide a reference model for restricted-vs-unrestricted reporting and identify missing constraints/columns for compliance reporting. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

### Prompt H — Section: Logical Relationship Hardening (dashed overlay edges)
> Given a set of logical (non-FK) relationships across modules, prioritize which should become physical foreign keys vs remain logical. Return a decision matrix with criteria (write throughput, backfill cost, orphan risk, operational coupling), plus a phased enforcement plan using NOT VALID constraints or equivalent. Include explicit deliverables: forward migration SQL, rollback SQL, validation SQL, and a risk register.

## Relationship Legend (from draw.io)

- **Solid arrows**: Physical FK relationships enforced in schema DDL.
- **Dashed arrows** (Logical Relationships Overlay): Modeled/expected links used by application logic but not all enforced as DB FKs.
