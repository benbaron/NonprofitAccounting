# Persistence Model UML (code-first)

This diagram translates the persistence assessment into a UML-style view.

```mermaid
classDiagram
  direction LR

  class ChartOfAccounts {
    +Long id
    +String name
    +String version
    +ChartStatus status
    +Instant createdAt
    +Instant updatedAt
  }

  class Account {
    +Long id
    +String accountNumber [legacy PK]
    +Long chartId
    +String code
    +String name
    +AccountType accountType
    +AccountSubtype subtype
    +NormalBalance normalBalance
    +Long parentId
    +BigDecimal openingBalance
    +boolean isPosting
    +boolean isActive
  }

  class Fund {
    +Long id
    +String code
    +String name
    +FundType fundType
    +Long parentId
    +boolean isActive
  }

  class Counterparty {
    +Long id
    +String displayName
    +CounterpartyKind kind
    +String email
    +String phone
    +boolean isActive
  }

  class Txn {
    +Long id
    +LocalDate txnDate
    +Long payeeId
    +Long bankAccountId
    +String memo
  }

  class TxnSplit {
    +Long id
    +Long txnId
    +Long accountId
    +Long fundId
    +Long activityId
    +Long merchantId
    +BigDecimal amountSigned
    +boolean nmrFlag
  }

  class Activity {
    +Long id
    +String code
    +String name
    +boolean isActive
  }

  class Merchant {
    +Long id
    +String name
    +boolean isActive
  }

  class FundTransfer {
    +Long id
    +LocalDate transferDate
    +Long fromFundId
    +Long toFundId
    +BigDecimal amount
    +FundTransferStatus status
    +Long postedTxnId
  }

  class AccountAlias {
    +Long id
    +Long accountId
    +String aliasText
    +String source
    +boolean isActive
  }

  class FundAlias {
    +Long id
    +Long fundId
    +String aliasText
    +String source
    +boolean isActive
  }

  class JournalTransaction {
    +Integer id
    +String dateText
    +String memo
    +String toFrom
    +String clearBank
    +boolean reconciled
  }

  class JournalEntry {
    +Long id
    +Integer txnId
    +String accountNumber
    +BigDecimal amount
    +String accountSide
    +String fundNumber
  }

  class TransactionInfo {
    +Integer txnId
    +String key
    +String value
  }

  class TxnSupplementalLine {
    +Long id
    +Integer txnId
    +Long entryId
    +String lineKind
    +Long counterpartyPersonId
    +BigDecimal amount
  }

  class Person {
    +Long id
    +String name
    +String email
    +String phone
  }

  class Donor {
    +Long id
    +String externalId
    +String name
    +String email
    +String phone
  }

  %% Canonical enforced relationships
  ChartOfAccounts "1" <-- "0..*" Account : fk_account_chart
  Account "1" <-- "0..*" Txn : fk_txn_bank_account
  Counterparty "1" <-- "0..*" Txn : fk_txn_payee
  Txn "1" <-- "0..*" TxnSplit : fk_split_txn
  Account "1" <-- "0..*" TxnSplit : fk_split_account
  Fund "1" <-- "0..*" TxnSplit : fk_split_fund
  Account "1" <-- "0..*" Account : fk_account_parent

  %% Legacy enforced relationships
  JournalTransaction "1" <-- "0..*" JournalEntry : fk_journal_entry_txn
  Account "1" <-- "0..*" JournalEntry : fk_journal_entry_account_number
  JournalTransaction "1" <-- "0..*" TransactionInfo : fk_transaction_info_txn
  JournalTransaction "1" <-- "0..*" TxnSupplementalLine : fk_txn_supplemental_txn
  JournalEntry "1" <-- "0..*" TxnSupplementalLine : fk_txn_supplemental_entry
  Person "1" <-- "0..*" TxnSupplementalLine : fk_txn_supplemental_person

  %% Java-mapped but weak/missing DB constraints (dashed semantics)
  Fund "1" .. "0..*" Fund : parent (mapped; FK missing in DDL)
  Account "1" .. "0..*" AccountAlias : mapped; FK missing in DDL
  Fund "1" .. "0..*" FundAlias : mapped; FK missing in DDL
  Activity "1" .. "0..*" TxnSplit : mapped; FK missing in DDL
  Merchant "1" .. "0..*" TxnSplit : mapped; FK missing in DDL
  Fund "1" .. "0..*" FundTransfer : from/to mapped; FK missing in DDL
  Txn "1" .. "0..*" FundTransfer : postedTxn mapped; FK missing in DDL

  %% Cross-model synchronization
  JournalTransaction ..> Txn : CanonicalJournalSyncAdapter upsert
  JournalEntry ..> TxnSplit : CanonicalJournalSyncAdapter replace

```

## Notes
- Solid links represent relationships confirmed as physical FK constraints in the deployed schema path.
- Dotted links represent mappings present in Java entities but not enforced as FKs by `Database.ensureSchema()`.
- The model intentionally includes both canonical (`txn*`) and legacy (`journal_*`) storage because both are active.


Draw.io source: [persistence-model.drawio](persistence-model.drawio)
