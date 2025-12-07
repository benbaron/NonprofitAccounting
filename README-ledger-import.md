Perfect. Below are the “developer handoff” instructions you can drop into Codex (or into a PR description / README for the NonprofitAccounting repo). They explain how to wire the new ledger importer classes into the existing NonprofitAccounting codebase so that Excel bank journal rows become persisted accounting transactions in H2.

I’ll assume:

* Your main app / domain layer is `nonprofitaccounting` (or similar).
* You have (or will have) persistent entities like `AccountingTransaction` and `AccountingEntry`.
* You’re using H2 via JPA/Hibernate or equivalent, with repositories/services already in the project or planned.

If some naming differs, Codex can adjust accordingly.

---

## Goal

Connect the new importer in
`nonprofitbookkeeping.ui.actions.scaledger`
to the core NonprofitAccounting data model so that:

1. We can read the ledger workbook (`.xlsx` / `.xlsm`) the exchequer keeps.
2. We parse a given sheet (`Ledger_Q1`, `Ledger_Q2`, etc.) into Java objects.
3. We convert those Java objects into proper persisted accounting transactions and ledger entries in the H2 database.

In plain terms: “Import from Excel → produce `AccountingTransaction` with multiple `AccountingEntry` splits → save.”

---

## 1. Classes provided in `nonprofitbookkeeping.ui.actions.scaledger`

These are already in the bundle:

### Parsing / model classes (in-memory, not JPA)

* `LedgerQuarter`

  * Represents one sheet (“Ledger_Q1”, etc.)
  * Holds a `List<LedgerRow>`

* `LedgerRow`

  * Represents one row in the sheet.
  * Fields include:

    * `LocalDate date`
    * `String checkNumber`
    * `String clearedBankTag` (your “Clear Bank” column)
    * `String toFrom`
    * `String memo`
    * `String budgetNotes`
    * `Integer sheetRowNumber` (original Excel row index)
    * `List<LedgerSplit> splits`

* `LedgerSplit`

  * Represents one accounting leg (a debit or credit slice of that row).
  * Fields include:

    * `BigDecimal amount`
    * `String assetLiabilityAccount`
    * `String incomeCategory`
    * `String expenseCategory`
    * `String fund`
    * `String canonicalCategory`

      * This is the translated/normalized chart-of-accounts label.
      * For example `"Checking"` → `"I.a Undep. & non-interest cash"`
      * We *do not* change punctuation or dash style. We preserve your literal strings.

### Support classes

* `ChartTranslationMap`

  * Loads `chart-map.json`
  * Maps literal dropdown text from Excel → canonical chart/fund values used internally.
  * This is where “Checking” becomes `"I.a Undep. & non-interest cash"`, `"General Supplies - AR"` becomes `"19b General Supplies - Activity"`, etc.
  * Add new mappings here when you add new categories to the spreadsheet. You don’t have to recompile code.

* `CellUtil`

  * Safe helpers for reading Apache POI cells (dates, numeric amounts, strings).

* `LedgerSheetImporter`

  * Opens a workbook, locates a sheet, scans headers, and converts each data row into a `LedgerRow` with up to 4 `LedgerSplit`s.
  * Applies the translation map so that each `LedgerSplit` has `canonicalCategory` filled.

* `ImporterCli`

  * Example `main()` that shows how to call the importer.
  * You can run this standalone for debugging, or call the same logic from your app’s UI or service layer.

---

## 2. Add these classes into the project

1. Copy the entire directory structure from the bundle:
   `src/main/java/nonprofitbookkeeping/ui/actions/scaledger/`
   into the same relative package path in the NonprofitAccounting repo.

   This must preserve the package declaration:

   ```java
   package nonprofitbookkeeping.ui.actions.scaledger;
   ```

   Do **not** rename that package unless you update all imports.

2. Copy `chart-map.json` into a known runtime location.
   Options:

   * Easiest: put it next to the application `.jar` and refer to it by absolute/relative file path.
   * Nicer long-term: put it in your classpath as a resource, e.g. `src/main/resources/chart-map.json`, and load with `ClassLoader.getResourceAsStream`.
     (If you do that, update `ChartTranslationMap.fromJsonFile(...)` or add a second `fromClasspathResource(...)` helper to read from the classpath.)

3. Add Apache POI and Jackson if they’re not already in your build:

   * `poi-ooxml`
   * `jackson-databind`
   * `jackson-datatype-jsr310`

   Example Maven snippet:

   ```xml
   <dependencies>
     <dependency>
       <groupId>org.apache.poi</groupId>
       <artifactId>poi-ooxml</artifactId>
       <version>5.2.5</version>
     </dependency>

     <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
       <version>2.17.2</version>
     </dependency>

     <dependency>
       <groupId>com.fasterxml.jackson.datatype</groupId>
       <artifactId>jackson-datatype-jsr310</artifactId>
       <version>2.17.2</version>
     </dependency>
   </dependencies>
   ```

   Codex: make sure these versions are aligned with whatever is already in the repo. If there are conflicts, prefer the repo’s versions and update imports accordingly.

---

## 3. Add persistence-side domain objects if they don’t exist yet

Codex, check if the repo already has something like this. If not, create them in the existing NonprofitAccounting domain module (not in `ui.actions.scaledger`):

### `AccountingTransaction`

```java
package nonprofitbookkeeping.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccountingTransaction {
    private Long id; // @Id if JPA
    private LocalDate date;
    private String checkNumber;
    private String clearedBankTag;
    private String payeeOrPayer;   // maps from LedgerRow.toFrom
    private String memo;          // LedgerRow.memo
    private String budgetNotes;   // LedgerRow.budgetNotes

    private final List<AccountingEntry> entries = new ArrayList<>();

    public void addEntry(AccountingEntry e) {
        if (e != null) {
            entries.add(e);
        }
    }

    // getters/setters...
}
```

### `AccountingEntry`

```java
package nonprofitbookkeeping.domain;

import java.math.BigDecimal;

public class AccountingEntry {
    private Long id; // @Id if JPA
    private BigDecimal amount;
    private String canonicalCategory; // LedgerSplit.canonicalCategory
    private String fund;              // LedgerSplit.fund

    // Optional: backref to parent transaction if you’re doing @OneToMany
    private AccountingTransaction parent;

    // getters/setters...
}
```

If you’re using JPA/Hibernate:

* Add the appropriate `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy=...)` annotations.
* Add a bidirectional `@OneToMany(mappedBy="parent", cascade=CascadeType.ALL)` on `AccountingTransaction.entries`.
* Add a `@ManyToOne` on `AccountingEntry.parent`.

Codex should generate those annotations consistent with the existing persistence style in the repo (H2-backed).

---

## 4. Create a mapper to go from `LedgerQuarter` → persistent objects

In a new class, e.g.:

```java
package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.domain.AccountingTransaction;
import nonprofitbookkeeping.domain.AccountingEntry;

public class LedgerToDomainMapper {

    public AccountingTransaction mapRowToTransaction(LedgerRow row) {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setDate(row.getDate());
        txn.setCheckNumber(row.getCheckNumber());
        txn.setClearedBankTag(row.getClearedBankTag());
        txn.setPayeeOrPayer(row.getToFrom());
        txn.setMemo(row.getMemo());
        txn.setBudgetNotes(row.getBudgetNotes());

        // For each split in this row, create an AccountingEntry
        for (LedgerSplit split : row.getSplits()) {
            AccountingEntry entry = new AccountingEntry();
            entry.setAmount(split.getAmount());
            entry.setCanonicalCategory(split.getCanonicalCategory());
            entry.setFund(split.getFund());

            // Wire parent if we're using bidirectional:
            entry.setParent(txn);
            txn.addEntry(entry);
        }

        return txn;
    }
}
```

Notes for Codex:

* Do not alter punctuation in `canonicalCategory`. These are regulatory / audit strings.
* `fund` is currently often `"General Fund"`. Treat this literally. Do not normalize.
* If a row had multiple splits, you’ll end up with multiple `AccountingEntry` children under one transaction. That’s correct. That’s the journal-style multiple-leg posting of a single check/deposit.
* If a row is essentially blank (`row.isEffectivelyBlank()`), skip it.

---

## 5. Add a service that drives the full import

In `nonprofitbookkeeping.ui.actions.scaledger`, create something like `LedgerImportService`:

```java
package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.domain.AccountingTransaction;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LedgerImportService {

    private final LedgerSheetImporter sheetImporter;
    private final LedgerToDomainMapper mapper;
    private final LedgerPersistenceGateway persistence;

    /**
     * @param persistence an adapter that knows how to save
     *                    AccountingTransaction (+ child entries)
     *                    into H2/JPA.
     */
    public LedgerImportService(LedgerPersistenceGateway persistence) {
        this.sheetImporter = new LedgerSheetImporter();
        this.mapper = new LedgerToDomainMapper();
        this.persistence = persistence;
    }

    /**
     * Import all rows from one Excel sheet and persist them.
     *
     * @param chartMapFile path to chart-map.json
     * @param workbookFile path to .xlsx/.xlsm
     * @param sheetName    "Ledger_Q1", etc.
     */
    public List<AccountingTransaction> importAndPersist(
            Path chartMapFile,
            Path workbookFile,
            String sheetName
    ) throws Exception {

        ChartTranslationMap translation = ChartTranslationMap.fromJsonFile(chartMapFile);

        LedgerQuarter quarter =
                sheetImporter.importQuarter(workbookFile, sheetName, translation);

        List<AccountingTransaction> saved = new ArrayList<>();

        for (LedgerRow row : quarter.getRows()) {
            if (row.isEffectivelyBlank()) {
                continue;
            }
            AccountingTransaction txn = mapper.mapRowToTransaction(row);
            AccountingTransaction persisted = persistence.saveTransactionWithEntries(txn);
            saved.add(persisted);
        }

        return saved;
    }
}
```

Now Codex needs to provide `LedgerPersistenceGateway` as a boundary between UI and DB:

---

## 6. Add a persistence gateway to isolate database details

This keeps `ui.actions.scaledger` from knowing about repositories directly. Codex should create an interface, and then provide a JPA-backed implementation somewhere in the service layer.

### Interface (in `ui.actions.scaledger`)

```java
package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.domain.AccountingTransaction;

public interface LedgerPersistenceGateway {
    AccountingTransaction saveTransactionWithEntries(AccountingTransaction txn);
}
```

### Implementation (in your service layer, wherever persistence lives)

For example, if you already have a Spring-ish or JPA-ish service, Codex can generate:

```java
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.domain.AccountingTransaction;
import nonprofitbookkeeping.ui.actions.scaledger.LedgerPersistenceGateway;

public class LedgerPersistenceGatewayJpa implements LedgerPersistenceGateway {

    private final AccountingTransactionRepository transactionRepo;

    public LedgerPersistenceGatewayJpa(AccountingTransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    @Override
    public AccountingTransaction saveTransactionWithEntries(AccountingTransaction txn) {
        // transactionRepo.save(txn) should cascade entries if you annotated it.
        return transactionRepo.save(txn);
    }
}
```

Codex:

* If you already have `AccountingTransactionRepository` (Spring Data JPA style), reuse it.
* If not, generate it:

  ```java
  public interface AccountingTransactionRepository {
      AccountingTransaction save(AccountingTransaction t);
      // plus whatever find/query methods you need
  }
  ```

  and back it with your existing persistence setup.

---

## 7. Hook this into the UI

We’re still under the `nonprofitbookkeeping.ui.actions.scaledger` namespace, so we can expose an “Import Ledger…” action in the UI menu (File → Import Excel Ledger…).

High-level flow for that action:

1. Prompt user for:

   * Path to `chart-map.json` (or assume default)
   * Path to ledger workbook (`.xlsm`)
   * Sheet name (`Ledger_Q1`, `Ledger_Q2`, etc.)

2. Call:

   ```java
   LedgerImportService svc = new LedgerImportService(persistenceGatewayImpl);
   svc.importAndPersist(chartMapPath, workbookPath, sheetName);
   ```

3. After import, refresh whatever on-screen register / general ledger view you already have in NonprofitAccounting.

Codex:
If the existing UI command framework (like `Action` subclasses, or ribbon actions) lives in this package already (there are other classes under `nonprofitbookkeeping.ui.actions.scaledger`), add a new action class such as `ImportLedgerAction` that wraps the call above and handles dialogs, errors, etc.

---

## 8. Data integrity rules Codex must preserve

1. **Do not “fix” punctuation.**
   `LedgerSplit.canonicalCategory` must come directly from `ChartTranslationMap` values.
   Do not normalize hyphens, spacing, capitalization, etc.
   This is legally/audit important.

2. **Fund values are literal too.**
   For example `"General Fund"`.
   Save them exactly. Treat them as a dimension on the entry.

3. A single `LedgerRow` often has multiple `LedgerSplit` legs.
   They all belong to the **same** `AccountingTransaction`.

4. Rows that are just “0.00 see free form tab” are informational.
   `LedgerRow.isEffectivelyBlank()` returns true on those.
   Skip persisting those.

5. The “clearedBankTag” field (like `"Aug"`, `"Sep"`) tells you bank reconciliation status / month.
   Store it on the transaction so you can later run a “bank rec by month” report.

6. The “budgetNotes” is narrative / audit trail text.
   Persist it on the transaction header.
   Do not drop it; it just doesn’t affect the math.

7. The importer already splits up complex rows where an event settlement has multiple categories (e.g. part Event Income, part Asset Movement).
   Codex should preserve every split as a distinct `AccountingEntry` line item.

---

## 9. Final: how Codex should integrate

Codex should:

1. Add the provided classes under `nonprofitbookkeeping.ui.actions.scaledger` unchanged.
2. Add `LedgerToDomainMapper`, `LedgerImportService`, and `LedgerPersistenceGateway` as described.
3. Ensure there are (or generate) `AccountingTransaction`, `AccountingEntry`, and a persistence layer (`AccountingTransactionRepository` or equivalent JPA repository).
4. Add a UI action (menu/ribbon/etc.) that calls `LedgerImportService.importAndPersist(...)`.
5. Verify that saving a transaction cascades its entries into H2.
6. Confirm that all imported category strings and fund labels are stored exactly as given (no normalization).

That’s it. After this, NonprofitAccounting can import any quarterly ledger sheet the exchequer gives you and store it as first-class accounting data in your H2-backed system.
