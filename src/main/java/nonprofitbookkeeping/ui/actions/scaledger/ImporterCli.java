package nonprofitbookkeeping.ui.actions.scaledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Path;

/**
 * Example CLI entry point.
 *
 * Usage:
 *   java nonprofitbookkeeping.ui.actions.scaledger.ImporterCli chart-map.json "CG Ledger 2024 Q4 v3.xlsm" Ledger_Q4
 *
 * This will:
 *   1. Load the chart-of-accounts translation map from chart-map.json
 *   2. Read the given sheet from the workbook
 *   3. Print a JSON dump of the parsed LedgerQuarter
 *
 * Integration point for NonprofitAccounting:
 *   - Instead of just printing JSON, you would:
 *     a) For each LedgerRow, create an AccountingTransaction entity
 *        in your H2 layer (date, memo, checkNumber, clearedBankTag, etc.)
 *     b) For each LedgerSplit in that row, create AccountingEntry entities
 *        with amount, canonicalCategory, fund, etc.
 *     c) Persist via your existing repository / service layer.
 */
public class ImporterCli
{
    public static void main(String[] args) throws Exception
    {
        if (args.length < 3)
        {
            System.err.println("Usage: ImporterCli <chartMap.json> <workbook.xlsm> <sheetName>");
            System.exit(1);
        }

        Path chartMapPath = Path.of(args[0]);
        Path workbookPath = Path.of(args[1]);
        String sheetName = args[2];

        ChartTranslationMap translation =
            ChartTranslationMap.fromJsonFile(chartMapPath);

        LedgerSheetImporter importer = new LedgerSheetImporter();
        LedgerQuarter quarter =
            importer.importQuarter(workbookPath, sheetName, translation);

        // Dump result to stdout as pretty JSON so Codex / you can inspect.
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        String jsonOut = mapper.writeValueAsString(quarter);
        System.out.println(jsonOut);
    }
}
