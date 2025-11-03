package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LedgerImportServiceTest
{
    @Test
    void importsRowsAndDelegatesToGateway() throws IOException
    {
        LedgerQuarter quarter = new LedgerQuarter("Ledger_Q2");
        LedgerRow row = new LedgerRow();
        row.setDate(LocalDate.of(2024, 3, 10));
        row.setCheckNumber("1101");
        row.setClearedBankTag("Mar");
        row.setToFrom("Acme Supplies");
        row.setMemo("Art supplies");
        row.setBudgetNotes("Workshops");
        row.setSheetRowNumber(25);

        LedgerSplit asset = new LedgerSplit();
        asset.setAmount(new BigDecimal("-150.00"));
        asset.setAssetLiabilityAccount("Checking");
        asset.setCanonicalCategory("I.a Undep. & non-interest cash");
        asset.setFund("General Fund");
        row.addSplit(asset);

        LedgerSplit expense = new LedgerSplit();
        expense.setAmount(new BigDecimal("150.00"));
        expense.setExpenseCategory("Supplies");
        expense.setCanonicalCategory("19b General Supplies - Activity");
        expense.setFund("General Fund");
        row.addSplit(expense);

        quarter.addRow(row);
        quarter.getRows().add(new LedgerRow()); // blank row should be skipped

        LedgerSheetImporter stubImporter = new LedgerSheetImporter()
        {
            @Override
            public LedgerQuarter importQuarter(Path workbookPath, String sheetName, ChartTranslationMap translation)
            {
                return quarter;
            }
        };

        RecordingGateway gateway = new RecordingGateway();
        LedgerImportService service = new LedgerImportService(stubImporter, new LedgerToDomainMapper(), gateway);

        Path chartMap = Files.createTempFile("chart-map", ".json");
        Files.writeString(chartMap, "{\n  \"Checking\": \"I.a Undep. & non-interest cash\"\n}");

        List<AccountingTransaction> saved = service.importAndPersist(chartMap, Path.of("workbook.xlsm"), "Ledger_Q2");

        assertEquals(1, gateway.saved.size());
        assertEquals(1, saved.size());

        AccountingTransaction transaction = saved.get(0);
        assertNotNull(transaction.getEntries());
        assertEquals(2, transaction.getEntries().size());
        assertEquals("Ledger_Q2", transaction.getInfo().get("ledgerSheetName"));
        assertEquals("25", transaction.getInfo().get("ledgerSheetRow"));
    }

    private static class RecordingGateway implements LedgerPersistenceGateway
    {
        private final List<AccountingTransaction> saved = new ArrayList<>();
        private int nextId = 1;

        @Override
        public AccountingTransaction saveTransactionWithEntries(AccountingTransaction transaction)
        {
            transaction.setId(nextId++);
            saved.add(transaction);
            return transaction;
        }
    }
}
