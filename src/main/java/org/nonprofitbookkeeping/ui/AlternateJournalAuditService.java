package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Audit-safe journal correction behavior for alternate UI migration. */
class AlternateJournalAuditService
{
    private final Supplier<Company> companySupplier;
    private final BooleanSupplier companyOpenSupplier;
    private final AlternateFundsService.Persister persister;

    AlternateJournalAuditService(Supplier<Company> companySupplier, BooleanSupplier companyOpenSupplier,
        AlternateFundsService.Persister persister)
    {
        this.companySupplier = Objects.requireNonNull(companySupplier, "companySupplier");
        this.companyOpenSupplier = Objects.requireNonNull(companyOpenSupplier, "companyOpenSupplier");
        this.persister = Objects.requireNonNull(persister, "persister");
    }

    AccountingTransaction reverse(long bookingDateTimestamp, String reason) throws IOException
    {
        Company company = companyOpenSupplier.getAsBoolean() ? companySupplier.get() : null;
        if (company == null || company.getLedger() == null || company.getLedger().getJournal() == null)
        {
            throw new IllegalArgumentException("Open a company before reversing journal entries.");
        }
        AccountingTransaction original = company.getLedger().getJournal().getJournalTransactions().stream()
            .filter(tx -> tx != null && tx.getBookingDateTimestamp() == bookingDateTimestamp)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Transaction was not found."));
        AccountingTransaction reversal = new AccountingTransaction();
        reversal.setDate(LocalDate.now().toString());
        reversal.setBookingDateTimestamp(System.currentTimeMillis());
        reversal.setMemo("Reversal: " + (reason == null || reason.isBlank() ? original.getMemo() : reason.trim()));
        reversal.setToFrom(original.getToFrom());
        reversal.setAssociatedFundName(original.getAssociatedFundName());
        Map<String, String> info = new LinkedHashMap<>();
        info.put("action", "REVERSAL");
        info.put("source_booking_date_timestamp", Long.toString(bookingDateTimestamp));
        info.put("source_reconciled", Boolean.toString(original.isReconciled()));
        reversal.setInfo(info);

        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        for (AccountingEntry entry : original.getEntries())
        {
            AccountingEntry reversed = new AccountingEntry(entry.getAmount(), entry.getAccountNumber(),
                entry.getAccountSide() == AccountSide.DEBIT ? AccountSide.CREDIT : AccountSide.DEBIT, entry.getAccountName());
            reversed.setFundNumber(entry.getFundNumber());
            entries.add(reversed);
        }
        reversal.setEntries(entries);
        company.getLedger().getJournal().addTransaction(reversal);
        persister.persist();
        return reversal;
    }
}
