package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlternateJournalAuditServiceTest
{
    @Test
    void reverseAddsAuditTransactionAndDoesNotDeleteOriginal() throws Exception
    {
        Company company = new Company();
        AccountingTransaction original = fixtureTransaction(42L, false);
        company.getLedger().getJournal().addTransaction(original);
        final int[] persists = {0};
        AlternateJournalAuditService service = new AlternateJournalAuditService(() -> company, () -> true, () -> persists[0]++);

        AccountingTransaction reversal = service.reverse(42L, "Wrong account");

        assertEquals(2, company.getLedger().getJournal().getJournalTransactions().size());
        assertSame(original, company.getLedger().getJournal().getJournalTransactions().get(0));
        assertEquals("REVERSAL", reversal.getInfo().get("action"));
        assertEquals("42", reversal.getInfo().get("source_booking_date_timestamp"));
        assertTrue(reversal.isBalanced());
        assertEquals(1, persists[0]);
        AccountingEntry first = reversal.getEntries().iterator().next();
        assertEquals(AccountSide.CREDIT, first.getAccountSide());
    }

    @Test
    void reconciledTransactionsAreReversedWithoutDeletingOriginal() throws Exception
    {
        Company company = new Company();
        AccountingTransaction original = fixtureTransaction(77L, true);
        company.getLedger().getJournal().addTransaction(original);
        AlternateJournalAuditService service = new AlternateJournalAuditService(() -> company, () -> true, () -> {});

        AccountingTransaction reversal = service.reverse(77L, "void");

        assertEquals(2, company.getLedger().getJournal().getJournalTransactions().size());
        assertSame(original, company.getLedger().getJournal().getJournalTransactions().get(0));
        assertTrue(original.isReconciled());
        assertEquals("true", reversal.getInfo().get("source_reconciled"));
        assertEquals("77", reversal.getInfo().get("source_booking_date_timestamp"));
    }

    private static AccountingTransaction fixtureTransaction(long timestamp, boolean reconciled)
    {
        AccountingEntry debit = new AccountingEntry(new BigDecimal("12.00"), "5000", AccountSide.DEBIT, "Supplies");
        debit.setFundNumber("General");
        AccountingEntry credit = new AccountingEntry(new BigDecimal("12.00"), "1000", AccountSide.CREDIT, "Cash");
        credit.setFundNumber("General");
        AccountingTransaction tx = new AccountingTransaction();
        tx.setDate("2026-06-20");
        tx.setMemo("Fixture transaction");
        tx.setToFrom("Fixture Vendor");
        tx.setBookingDateTimestamp(timestamp);
        tx.setReconciled(reconciled);
        tx.setEntries(new LinkedHashSet<>(List.of(debit, credit)));
        return tx;
    }
}
