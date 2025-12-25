package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class LedgerToDomainMapperTest
{
    private final LedgerToDomainMapper mapper = new LedgerToDomainMapper();

    @Test
    void mapsLedgerRowToTransactionWithMetadata()
    {
        LedgerRow row = new LedgerRow();
        row.setDate(LocalDate.of(2024, 1, 15));
        row.setCheckNumber("1095");
        row.setClearedBankTag("Jan");
        row.setToFrom("Ben Baron");
        row.setMemo("Seed money");
        row.setBudgetNotes("Event launch");
        row.setSheetRowNumber(11);

        LedgerSplit asset = new LedgerSplit();
        asset.setAmount(new BigDecimal("200.00"));
        asset.setAssetLiabilityAccount("Checking");
        asset.setCanonicalCategory("I.a Undep. & non-interest cash");
        asset.setFund("General Fund");
        row.addSplit(asset);

        LedgerSplit income = new LedgerSplit();
        income.setAmount(new BigDecimal("200.00"));
        income.setIncomeCategory("Event Income");
        income.setCanonicalCategory("03.b Adj Gross Event Income");
        income.setFund("General Fund");
        row.addSplit(income);

        AccountingTransaction transaction = this.mapper.mapRowToTransaction(row, "Ledger_Q1");

        assertEquals("2024-01-15", transaction.getDate());
        assertEquals("1095", transaction.getCheckNumber());
        assertEquals("Ben Baron", transaction.getToFrom());
        assertEquals("Seed money", transaction.getMemo());
        assertEquals("Jan", transaction.getClearBank());
        assertEquals("Event launch", transaction.getBudgetTracking());
        assertEquals("General Fund", transaction.getAssociatedFundName());
        assertEquals("Ledger_Q1", transaction.getInfo().get("ledgerSheetName"));
        assertEquals("11", transaction.getInfo().get("ledgerSheetRow"));
        assertNotNull(transaction.getEntries());
        assertEquals(2, transaction.getEntries().size());

        Iterator<AccountingEntry> iterator = transaction.getEntries().iterator();
        AccountingEntry assetEntry = iterator.next();
        AccountingEntry incomeEntry = iterator.next();

        assertEquals(AccountSide.DEBIT, assetEntry.getAccountSide());
        assertEquals(new BigDecimal("200.00"), assetEntry.getAmount());
        assertEquals("I.a Undep. & non-interest cash", assetEntry.getAccountNumber());
        assertEquals("General Fund", assetEntry.getFundNumber());

        assertEquals(AccountSide.CREDIT, incomeEntry.getAccountSide());
        assertEquals(new BigDecimal("200.00"), incomeEntry.getAmount());
        assertEquals("03.b Adj Gross Event Income", incomeEntry.getAccountNumber());
        assertEquals("General Fund", incomeEntry.getFundNumber());
    }

    @Test
    void determinesCreditForAssetDecrease()
    {
        LedgerRow row = new LedgerRow();
        row.setDate(LocalDate.of(2024, 2, 5));

        LedgerSplit asset = new LedgerSplit();
        asset.setAmount(new BigDecimal("-75.00"));
        asset.setAssetLiabilityAccount("Checking");
        asset.setCanonicalCategory("I.a Undep. & non-interest cash");
        row.addSplit(asset);

        LedgerSplit expense = new LedgerSplit();
        expense.setAmount(new BigDecimal("75.00"));
        expense.setExpenseCategory("Supplies");
        expense.setCanonicalCategory("19b General Supplies - Activity");
        row.addSplit(expense);

        AccountingTransaction transaction = this.mapper.mapRowToTransaction(row, "Ledger_Q1");
        assertEquals(2, transaction.getEntries().size());

        Iterator<AccountingEntry> iterator = transaction.getEntries().iterator();
        AccountingEntry assetEntry = iterator.next();
        AccountingEntry expenseEntry = iterator.next();

        assertEquals(AccountSide.CREDIT, assetEntry.getAccountSide());
        assertEquals(AccountSide.DEBIT, expenseEntry.getAccountSide());
    }
}
