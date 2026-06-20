package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlternateFundsServiceTest
{
    @Test
    void fundRowsDisplayLedgerDerivedBalancesInsteadOfEditableFundBalances() throws Exception
    {
        Company company = companyWithAccount();
        addTransaction(company, "2026-06-01", "Opening", "General", AccountSide.DEBIT, new BigDecimal("125.50"));
        addTransaction(company, "2026-06-02", "Expense", "General", AccountSide.CREDIT, new BigDecimal("25.25"));
        AlternateFundsService service = new AlternateFundsService(() -> company, () -> true, () -> {});

        List<AlternateFundsService.FundWorkspaceRow> rows = service.fundRows();

        assertEquals(1, rows.size());
        assertEquals("General", rows.get(0).name());
        assertEquals(0, new BigDecimal("100.25").compareTo(rows.get(0).ledgerBalance()));
        assertTrue(rows.get(0).active());
    }

    @Test
    void recordRestrictionReclassificationCreatesBalancedJournalEntryWithoutBankMovement() throws Exception
    {
        Company company = companyWithAccount();
        AtomicInteger persistCount = new AtomicInteger();
        AlternateFundsService service = new AlternateFundsService(() -> company, () -> true, persistCount::incrementAndGet);

        AccountingTransaction tx = service.recordRestrictionReclassification(LocalDate.of(2026, 6, 20),
            "Board-approved release", "Restricted", "General", new BigDecimal("75.00"),
            company.getChartOfAccounts().getAccount("3000"));

        assertEquals("2026-06-20", tx.getDate());
        assertEquals("Board-approved release", tx.getMemo());
        assertEquals("NO_BANK_ACCOUNT_MOVEMENT", tx.getInfo().get("movement_type"));
        assertEquals(2, tx.getEntries().size());
        assertTrue(tx.getEntries().stream().anyMatch(e -> e.getAccountSide() == AccountSide.CREDIT
            && "Restricted".equals(e.getFundNumber()) && new BigDecimal("75.00").compareTo(e.getAmount()) == 0));
        assertTrue(tx.getEntries().stream().anyMatch(e -> e.getAccountSide() == AccountSide.DEBIT
            && "General".equals(e.getFundNumber()) && new BigDecimal("75.00").compareTo(e.getAmount()) == 0));
        assertEquals(1, company.getLedger().getJournal().getJournalTransactions().size());
        assertEquals(1, persistCount.get());
    }

    @Test
    void transactionsForFundDrillsDownToSelectedFundEntries() throws Exception
    {
        Company company = companyWithAccount();
        addTransaction(company, "2026-06-01", "General only", "General", AccountSide.DEBIT, new BigDecimal("10.00"));
        addTransaction(company, "2026-06-02", "Restricted only", "Restricted", AccountSide.DEBIT, new BigDecimal("20.00"));
        AlternateFundsService service = new AlternateFundsService(() -> company, () -> true, () -> {});

        var rows = service.transactionsForFund("Restricted");

        assertEquals(1, rows.size());
        assertEquals("Restricted only", rows.get(0).transaction().getMemo());
    }

    @Test
    void editFundRenamesLedgerReferencesWithoutEditingDisplayBalances() throws Exception
    {
        Company company = companyWithAccount();
        addTransaction(company, "2026-06-01", "Opening", "Old Name", AccountSide.DEBIT, new BigDecimal("42.00"));
        AlternateFundsService service = new AlternateFundsService(() -> company, () -> true, () -> {});

        service.editFund("Old Name", "New Name");

        List<AlternateFundsService.FundWorkspaceRow> rows = service.fundRows();
        assertEquals(1, rows.stream().filter(row -> "New Name".equals(row.name())).count());
        assertEquals(0, new BigDecimal("42.00").compareTo(rows.stream()
            .filter(row -> "New Name".equals(row.name()))
            .findFirst()
            .orElseThrow()
            .ledgerBalance()));
        assertEquals(0, rows.stream().filter(row -> "Old Name".equals(row.name())).count());
        assertTrue(company.getLedger().getJournal().getJournalTransactions().stream()
            .flatMap(tx -> tx.getEntries() == null ? java.util.stream.Stream.empty() : tx.getEntries().stream())
            .allMatch(entry -> !"Old Name".equals(entry.getFundNumber())));
    }

    private static Company companyWithAccount()
    {
        Company company = new Company();
        ChartOfAccounts chart = new ChartOfAccounts();
        chart.addAccount(new Account("3000", "Net Assets", AccountType.EQUITY, BigDecimal.ZERO));
        company.setChartOfAccounts(chart);
        return company;
    }

    private static void addTransaction(Company company, String date, String memo, String fund, AccountSide side, BigDecimal amount)
    {
        AccountingEntry entry = new AccountingEntry(amount, "3000", side, "Net Assets");
        entry.setFundNumber(fund);
        AccountingTransaction tx = new AccountingTransaction();
        tx.setDate(date);
        tx.setMemo(memo);
        tx.setBookingDateTimestamp(System.nanoTime());
        tx.setEntries(new LinkedHashSet<>(List.of(entry)));
        company.getLedger().getJournal().addTransaction(tx);
    }
}
