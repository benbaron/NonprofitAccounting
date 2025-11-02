# NonprofitAccounting Import Bundle (scaledger package)

This bundle lets your NonprofitAccounting / SCALedger codebase ingest the Excel ledger
(`Ledger_Q1` / `Ledger_Q2` / `Ledger_Q3` / `Ledger_Q4` sheets) and turn them into
Java objects you can persist in H2.

## Package / Namespace

All classes are under:

```text
nonprofitbookkeeping.ui.actions.scaledger
```

This matches your requested package location so you can drop these files
directly into that package in your codebase.

## Contents

- `src/main/java/nonprofitbookkeeping/ui/actions/scaledger/`
  - `LedgerSplit.java`
    - One accounting leg (amount + category + fund).
  - `LedgerRow.java`
    - One row from your ledger sheet, including metadata like Date / Check # / Memo
      plus up to 4 LedgerSplit legs.
  - `LedgerQuarter.java`
    - A collection of LedgerRow objects representing one sheet (e.g. "Ledger_Q4").
  - `ChartTranslationMap.java`
    - Loads a JSON dictionary that maps the literal dropdown text in your Excel ledger
      to your canonical chart-of-accounts strings (e.g. "Checking" -> "I.a Undep. & non-interest cash").
    - Punctuation / dash style is preserved exactly.
  - `CellUtil.java`
    - Safe helpers for reading Apache POI `Row`/`Cell` values into LocalDate, BigDecimal, etc.
  - `LedgerSheetImporter.java`
    - Opens an `.xlsx` / `.xlsm` workbook with Apache POI, reads one `Ledger_Q#` sheet,
      and converts each row into a `LedgerRow` plus its `LedgerSplit`s.
    - Each split also gets a `canonicalCategory` via `ChartTranslationMap`.
  - `ImporterCli.java`
    - Example `main()` you can run locally. It:
      1. Loads the chart map JSON
      2. Imports a sheet
      3. Prints the parsed data as pretty JSON.
    - Replace the "print JSON" section with calls into your actual
      persistence layer (AccountingTransaction / AccountingEntry, etc.).

- `chart-map.json`
  - The translation dictionary. Keys are EXACTLY what appears in the ledger dropdowns
    under "Asset/Liability Account", "Income Category", "Expense Category",
    and funds like "General Fund".
  - Values are the canonical chart/fund strings you want stored in H2.
  - You can add/modify entries here without touching code.

## How to integrate into NonprofitAccounting

1. **Copy the code**
   - Copy the entire `nonprofitbookkeeping/ui/actions/scaledger` directory into
     your existing source tree under the SAME package path.
   - Copy `chart-map.json` somewhere reachable at runtime (for now, next to your jar
     or in a known config directory).

2. **Add Maven/Gradle deps**
   You will need (use versions compatible with your repo):
   - Apache POI (`poi-ooxml`) for reading `.xlsm`
   - Jackson (`jackson-databind`, `jackson-datatype-jsr310`) for JSON + LocalDate

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

3. **Run the importer in your environment**
   - Compile so `ImporterCli` is on your classpath.
   - Then run:
     ```bash
     java nonprofitbookkeeping.ui.actions.scaledger.ImporterCli chart-map.json "CG Ledger 2024 Q4 v3.xlsm" Ledger_Q4
     ```
     Where:
     - `chart-map.json` is the translation map in this bundle.
     - `"CG Ledger 2024 Q4 v3.xlsm"` is your ledger workbook.
     - `Ledger_Q4` is the sheet/tab name to ingest.

   - This prints a pretty JSON dump of the parsed quarter to stdout.
     That JSON structure looks like:
     ```json
     {
       "sheetName": "Ledger_Q4",
       "rows": [
         {
           "date": "2025-08-17",
           "checkNumber": "1095",
           "clearedBankTag": "Aug",
           "toFrom": "Ben Baron",
           "memo": "Enduro Seed Money",
           "budgetNotes": "seed money $200",
           "sheetRowNumber": 11,
           "splits": [
             {
               "amount": 200.00,
               "assetLiabilityAccount": "Checking",
               "incomeCategory": null,
               "expenseCategory": "Asset Movement",
               "fund": "General Fund",
               "canonicalCategory": "I.a Undep. & non-interest cash"
             }
           ]
         }
       ]
     }
     ```

4. **Persist into your H2 / JPA model**
   - In your codebase you likely have or will have:
     - `AccountingTransaction` (header: date, memo, payee, checkNumber, clearedBankTag)
     - `AccountingEntry` (one per split: amount, canonicalCategory, fund)
     - a repository / service layer that saves those to H2 with JPA.
   - Create a mapper that:
     - loops each `LedgerRow`
     - creates one `AccountingTransaction`
     - loops that row's `splits` and creates `AccountingEntry` children
       using `split.getCanonicalCategory()` for the account name
       and `split.getFund()` for fund.
   - Save.

## Rules / expectations

- DO NOT normalize punctuation in account/category names.
  If Excel says `21b Occupancy - Activity Rel`, keep that exact spacing and dash style.
  The `chart-map.json` is where you guarantee canonical spelling.

- Funds (like "General Fund") are treated the same way:
  keep the literal string.

- You don't have to import the running balance columns,
  "Quarterly Net Change" block, or the Summary sheet.
  Those are derived views, not source-of-truth.
