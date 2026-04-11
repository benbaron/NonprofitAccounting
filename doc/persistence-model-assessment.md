# Persistence model and database design assessment (code-first)

## A. Executive summary

- **Confirmed:** this codebase currently runs two persistence models in parallel:
  1) a **new canonical JPA model** in `org.nonprofitbookkeeping.model` (`txn`, `txn_split`, `account.id`, `fund.id`, etc.), and
  2) a **legacy JDBC model** in `nonprofitbookkeeping.persistence` (`journal_transaction`, `journal_entry`, `account.account_number`, etc.).
  The bridge is `CanonicalJournalSyncAdapter`, which mirrors legacy journal writes into canonical tables. (Confirmed.)
- **Confirmed:** `Database.ensureSchema()` is the deployed schema source in practice; it creates/updates both legacy and canonical tables in one H2 schema, then runs backfills (`reconciled-backfill-v1`). (Confirmed.)
- **Confirmed drift:** JPA entity nullability/uniqueness constraints are stricter than deployed DDL for multiple tables (notably `account.chart_id`, `account.code`, `account.normal_balance`, several link-table FKs/unique constraints). This suggests legacy compatibility pressure over strict canonical enforcement. (Confirmed.)
- **Confirmed risk:** several relationships exist in Java mappings but are not physically enforced with foreign keys in deployed DDL (`fund.parent_id`, `account_alias.account_id`, `fund_alias.fund_id`, `fund_transfer.*`, etc.). Integrity depends on application behavior. (Confirmed.)
- **Inferred:** canonical model is the intended long-term design; legacy model remains for UI/import compatibility and staged migration.

## B. Java persistence inventory

### Canonical JPA entity model (`org.nonprofitbookkeeping.model`)

- `ChartOfAccounts` → `chart_of_accounts` (id PK, status enum, timestamps). (Confirmed.)
- `Account` → `account` (identity `id`; required `chart` many-to-one; `code`, `accountType`, `normalBalance`; optional self-parent and subtype/effective dates). (Confirmed.)
- `Fund` → `fund` (identity `id`; unique `code`; optional self-parent; fund type enum; active/effective/timestamps). (Confirmed.)
- `Txn` → `txn` (identity `id`; `txn_date`; optional `payee` to `counterparty`; optional `bank_account_id` to `account.id`). (Confirmed.)
- `TxnSplit` → `txn_split` (identity `id`; required txn/account/fund; optional activity/merchant; signed amount). (Confirmed.)
- Reference entities: `Counterparty`, `Activity`, `Merchant`, `ScheduleKind`, `ReportSection`. (Confirmed.)
- Join/association entities: `AccountAlias`, `FundAlias`, `AccountReportSection`, `AccountScheduleRequirement`, `AccountSubtypeScheduleDefault`, `FundTransfer`. (Confirmed.)
- Enums persisted as strings: `AccountType`, `AccountSubtype`, `NormalBalance`, `FundType`, `FundTransferStatus`, `ChartStatus`, `ReportType`, `SignPolicy`, `CounterpartyKind`. (Confirmed.)

### JPA mapping behaviors

- No `@OneToMany` collections are modeled from parent entities (navigation is mostly child→parent only). (Confirmed.)
- No `@Embeddable`, `@MappedSuperclass`, `@IdClass`, `@EmbeddedId`, or `AttributeConverter` classes are present in the active JPA model package. (Confirmed.)
- JPA bootstraps with `hibernate.hbm2ddl.auto=update` (both `persistence.xml` and runtime `Jpa` helper). (Confirmed.)

### Legacy JDBC persistence model (`nonprofitbookkeeping.persistence`)

- `JournalRepository` persists legacy journal aggregate (`journal_transaction`, `journal_entry`, `transaction_info`) and supplemental lines; then synchronizes into canonical `txn`/`txn_split`. (Confirmed.)
- `AccountRepository` persists legacy-style account keyed by `account_number` plus `account_fund` link rows. (Confirmed.)
- `CompanyProfileRepository`, `CompanyRepository`, `DocumentRepository`, `JsonStorageRepository`, `DonorRepository`, `PersonRepository`, `UndepositedFundsRepository`, `BankStatementRepository` persist non-JPA tables directly. (Confirmed.)
- Import staging repositories under `persistence/impex` create/write `imported_*` tables dynamically when used. (Confirmed.)

## C. Table-by-table database map (present deployed design)

> Source basis: `Database.ensureSchema()` + H2 metadata from a schema initialized via that code path.

### Core canonical accounting

- `chart_of_accounts`: PK `id`; columns `name`, `version`, `status`, `created_at`, `updated_at`.
- `account`: **dual-keyed** (`account_number` legacy PK + `id` unique identity); canonical columns (`chart_id`, `code`, `account_type`, `subtype`, `normal_balance`, `parent_id`, posting/active/effective/description) plus legacy columns (`account_code`, `increase_side`, `parent_account_id`, `currency`, etc.).
- `fund`: PK `id`; unique index on `code`; includes parent/effective/timestamps.
- `counterparty`: PK `id`; `display_name`, `kind`, contact fields, `is_active`.
- `txn`: PK `id`; `txn_date`; optional FK to `counterparty` and to `account.id`; memo/timestamps.
- `txn_split`: PK `id`; FKs to `txn`, `account.id`, `fund.id`; optional `activity_id`, `merchant_id` columns present.
- `activity`, `merchant`, `schedule_kind`, `report_section`: PK `id` with lookup data.
- `account_alias`, `fund_alias`, `account_report_section`, `account_schedule_requirement`, `account_subtype_schedule_default`, `fund_transfer`: created and populated by app workflows; constraints are lighter than JPA mappings.

### Legacy + compatibility + ops tables

- `journal_transaction`, `journal_entry`, `transaction_info`: legacy journal storage still active in write path.
- `txn_supplemental_line`: tied to legacy journal (`txn_id`→`journal_transaction.id`) and optionally `journal_entry.id` and `person.id`.
- `company_profile`, `company_store`, `document`, `json_storage`, `bank_statement`, `donor`, `person`, `undeposited_funds_item`, `schema_migration_history`, `account_fund`.
- Import staging tables (`imported_budget`, `imported_banking_item`, etc.) are created by impex repositories at runtime, not by `Database.ensureSchema()`.

### Confirmed vs inferred in DB map

- **Confirmed:** columns/PK/FK/indexes above come from generated DDL and metadata inspection.
- **Inferred:** imported staging tables may or may not exist in a particular deployment depending on whether import paths were executed.

## D. Relationship map

### Physically enforced (FK constraints present)

- `txn.payee_id -> counterparty.id`; `txn.bank_account_id -> account.id`.
- `txn_split.txn_id -> txn.id`; `txn_split.account_id -> account.id`; `txn_split.fund_id -> fund.id`.
- `account.chart_id -> chart_of_accounts.id`; `account.parent_id -> account.id`.
- Legacy chain: `journal_entry.txn_id -> journal_transaction.id`, `journal_entry.account_number -> account.account_number`, `transaction_info.txn_id -> journal_transaction.id`.
- Supplemental chain: `txn_supplemental_line.txn_id -> journal_transaction.id`, `entry_id -> journal_entry.id`, `counterparty_person_id -> person.id`.

### Java-defined but not DB-enforced (drift)

- `Fund.parent` (`fund.parent_id`) has JPA relation but no FK in schema initializer.
- `TxnSplit.activity` / `TxnSplit.merchant` columns exist, but no FK constraints to `activity`/`merchant`.
- `AccountAlias.account`, `FundAlias.fund`, `AccountReportSection.account/reportSection`, `AccountScheduleRequirement.account/scheduleKind`, `AccountSubtypeScheduleDefault.scheduleKind`, `FundTransfer.fromFund/toFund/postedTxn` are mapped in JPA but missing corresponding FK DDL in schema initializer.
- Several JPA unique constraints (`uq_account_code`, `uq_account_report`, `uq_asr`, `uq_subtype_sched`) are not created by schema initializer.

## E. Java-to-database mismatches

- `Account.chart` is `optional=false` in JPA, but `account.chart_id` is nullable in deployed schema; backfill sets null charts to default, but DB still permits null. (Confirmed drift.)
- `Account.code`, `accountType`, `normalBalance`, `is_posting`, `is_active` are non-null in JPA semantics; schema allows null on several of these columns. (Confirmed drift.)
- `Counterparty` JPA has `notes` LOB, but table `counterparty` has no `notes` column in schema initializer metadata. (Confirmed drift.)
- `Merchant` JPA has `notes` LOB, but table `merchant` has no `notes` column in schema initializer metadata. (Confirmed drift.)
- `Fund` JPA defines parent relation and indexes (`ix_fund_parent`, `ix_fund_active`), but schema initializer only creates unique `code` index and no parent FK/index. (Confirmed drift.)
- Legacy `account_fund.fund_id` is `VARCHAR(64)` and is not FK-linked to canonical `fund.id BIGINT`; semantic relationship enforced only by code conventions. (Confirmed drift.)
- Canonical/legacy transaction duplication (`txn*` vs `journal_*`) can diverge if sync logic fails or if one path is written independently. (Confirmed design risk.)

## F. Duplication / normalization findings

- **Major duplication:** same accounting event exists in `journal_transaction` + `journal_entry` and again in `txn` + `txn_split`.
- **Account duplication:** `account` mixes legacy identifier model (`account_number`, `account_code`, `increase_side`, `parent_account_id`) and canonical identifier model (`id`, `code`, `normal_balance`, `parent_id`).
- **Counterparty duplication:** `donor` and `person` overlap with canonical `counterparty`; one-way backfill inserts counterparties from donor/person but no hard unification.
- **Document-like duplication:** `document` and `json_storage` both act as key-value stores with large payloads.
- **Inferred legacy residue:** `account_fund` and string fund ids appear to reflect pre-canonical fund linkage and can conflict with canonical `txn_split.fund_id`.

## G. Integrity / indexing findings

### Integrity gaps

- Missing FK coverage for many JPA relationships (see section D).
- Missing unique constraints for alias/link tables allow duplicate semantic rows.
- Nullable columns where business logic suggests required values (`account.chart_id`, canonical account fields).
- Mixed identifier domains (`account_number` string PK + `id` bigint unique) complicate FK correctness and query plans.

### Indexing gaps

- `fund.parent_id` lacks index and FK; hierarchical queries will scan.
- Lookup-heavy alias tables (`account_alias.alias_text`, `fund_alias.alias_text`) depend on Hibernate update behavior; schema initializer does not create explicit indexes/uniques there.
- `fund_transfer` has no explicit indexes/FKs in schema initializer despite expected query/filter by date/status/fund.
- Duplicate/redundant indexes exist in places (`donor.external_id` has both constraint index and explicit unique index; `undeposited_funds_item` index on PK column is redundant).

## H. Refactoring roadmap

### Low-risk (stabilize integrity without major behavior change)

1. Add missing FK constraints where data is already mostly clean (alias/link tables, `fund.parent_id`, `txn_split.activity_id`, `txn_split.merchant_id`, `fund_transfer` refs).
2. Add missing unique constraints for semantic uniqueness (`account_id+alias_text+is_active` as needed, `account_id+report_section_id`, etc.).
3. Add indexes for known lookup predicates (`fund.parent_id`, alias text normalization helper columns if introduced).
4. Tighten nullability for canonical columns gradually with data backfill + validation.

### Medium-risk (reduce drift and dual-write risk)

5. Introduce explicit migration scripts/versioning beyond ad hoc `ensureSchema()` and one migration key.
6. Convert legacy-to-canonical sync from implicit side effect to audited migration pipeline with reconciliation checks.
7. Move read paths progressively to canonical tables (`txn`/`txn_split`) and keep legacy as compatibility view/source until cutover.

### Major redesign (end-state simplification)

8. Retire duplicate legacy journal tables or freeze them as read-only snapshots/views.
9. Split mixed `account` table into canonical form only (drop legacy columns after migration).
10. Consolidate `donor`/`person` into `counterparty` (with subtype tables if needed) and remove duplicate contact storage.

## I. Top 10 concrete next changes

1. Add FK: `fund.parent_id -> fund.id` (ON DELETE SET NULL) + index `fund_parent_idx`.
2. Add FK/indexes for `account_alias.account_id`, `fund_alias.fund_id`.
3. Add FKs for `account_report_section` and `account_schedule_requirement` to referenced tables.
4. Add FK for `account_subtype_schedule_default.schedule_kind_id`.
5. Add FKs for `fund_transfer.from_fund_id`, `to_fund_id`, `posted_txn_id`.
6. Add FKs `txn_split.activity_id -> activity.id`, `txn_split.merchant_id -> merchant.id`.
7. Backfill and enforce `account.chart_id` NOT NULL; then align with JPA `optional=false`.
8. Add DB columns missing from JPA entities (`counterparty.notes`, `merchant.notes`) or remove fields from entities if intentionally unused.
9. Add reconciliation check job/test ensuring every `journal_transaction.id` maps 1:1 to `txn.id` and split totals tie out after each write.
10. Plan a controlled deprecation of `account_fund` string `fund_id` in favor of bigint FK strategy.

---

## Primary evidence pointers

- JPA entity mappings and enums: `src/main/java/org/nonprofitbookkeeping/model/*`.
- JPA bootstrap/config: `src/main/resources/META-INF/persistence.xml`, `src/main/java/org/nonprofitbookkeeping/persistence/Jpa.java`.
- Deployed schema initializer and backfill logic: `src/main/java/nonprofitbookkeeping/core/Database.java`.
- Legacy JDBC repositories and dual-write bridge: `src/main/java/nonprofitbookkeeping/persistence/JournalRepository.java`, `src/main/java/nonprofitbookkeeping/persistence/CanonicalJournalSyncAdapter.java`, `src/main/java/nonprofitbookkeeping/persistence/AccountRepository.java`.
- Supplemental and import staging repositories: `src/main/java/nonprofitbookkeeping/persistence/supplemental/*`, `src/main/java/nonprofitbookkeeping/persistence/impex/*`.


See also: [Persistence Model UML](persistence-model-uml.md).
