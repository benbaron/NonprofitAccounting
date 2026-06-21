package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

/**
 * Query layer for the alternate ledger register.
 *
 * <p>The service intentionally returns DTOs so JavaFX panels do not inspect ledger
 * internals directly. When an account filter is supplied, matching rows expose the
 * amount posted to that selected account rather than the full transaction total.</p>
 */
public class LedgerTransactionQueryService
{
    private final Supplier<Company> companySupplier;
    private final BooleanSupplier companyOpenSupplier;

    public LedgerTransactionQueryService()
    {
        this(CurrentCompany::getCompany, CurrentCompany::isOpen);
    }

    public LedgerTransactionQueryService(Supplier<Company> companySupplier, BooleanSupplier companyOpenSupplier)
    {
        this.companySupplier = Objects.requireNonNull(companySupplier, "companySupplier");
        this.companyOpenSupplier = Objects.requireNonNull(companyOpenSupplier, "companyOpenSupplier");
    }

    public boolean hasOpenCompany()
    {
        return openCompany() != null;
    }

    public List<String> accountChoices()
    {
        Company company = openCompany();
        if (company == null || company.getChartOfAccounts() == null)
        {
            return List.of();
        }
        return company.getChartOfAccounts().getAccounts().stream()
            .map(this::accountChoice)
            .filter(choice -> !choice.isBlank())
            .distinct()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
    }

    public List<LedgerTransactionRow> query(LedgerTransactionFilter filter)
    {
        LedgerTransactionFilter activeFilter = filter == null ? LedgerTransactionFilter.empty() : filter;
        Company company = openCompany();
        if (company == null || company.getLedger() == null || company.getLedger().getJournal() == null)
        {
            return List.of();
        }

        List<LedgerTransactionRow> rows = new ArrayList<>();
        for (AccountingTransaction transaction : company.getLedger().getJournal().getJournalTransactions())
        {
            if (transaction == null || !matchesHeader(transaction, activeFilter))
            {
                continue;
            }
            List<LedgerEntryLine> matchingLines = linesFor(transaction, activeFilter);
            if (matchingLines.isEmpty())
            {
                continue;
            }
            BigDecimal displayAmount = activeFilter.hasDetailFilter()
                ? matchingLines.stream().map(LedgerEntryLine::signedAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                : transaction.getTotalAmount();
            if (activeFilter.amount() != null && displayAmount.abs().compareTo(activeFilter.amount().abs()) != 0)
            {
                continue;
            }
            rows.add(new LedgerTransactionRow(transaction, displayAmount, matchingLines));
        }
        rows.sort(Comparator.comparing((LedgerTransactionRow row) -> nullSafe(row.transaction().getDate()))
            .thenComparing(row -> row.transaction().getBookingDateTimestamp()));
        return rows;
    }

    private Company openCompany()
    {
        return this.companyOpenSupplier.getAsBoolean() ? this.companySupplier.get() : null;
    }

    private boolean matchesHeader(AccountingTransaction transaction, LedgerTransactionFilter filter)
    {
        LocalDate date = parseDate(transaction.getDate());
        if (filter.fromDate() != null && (date == null || date.isBefore(filter.fromDate()))) return false;
        if (filter.toDate() != null && (date == null || date.isAfter(filter.toDate()))) return false;
        if (!contains(transaction.getMemo(), filter.memo()) && !contains(transaction.getToFrom(), filter.memo())) return false;
        if (filter.cleared() != null && !Objects.equals(isCleared(transaction), filter.cleared())) return false;
        return filter.reconciled() == null || transaction.isReconciled() == filter.reconciled();
    }

    private List<LedgerEntryLine> linesFor(AccountingTransaction transaction, LedgerTransactionFilter filter)
    {
        if (transaction.getEntries() == null)
        {
            return List.of();
        }
        return transaction.getEntries().stream()
            .filter(entry -> matchesAccount(entry, filter.account()))
            .filter(entry -> contains(entry.getFundNumber(), filter.fund()) || contains(transaction.getAssociatedFundName(), filter.fund()))
            .map(entry -> toLine(transaction, entry))
            .toList();
    }

    private LedgerEntryLine toLine(AccountingTransaction transaction, AccountingEntry entry)
    {
        BigDecimal debit = entry.getAccountSide() == AccountSide.DEBIT ? entry.getAmount() : BigDecimal.ZERO;
        BigDecimal credit = entry.getAccountSide() == AccountSide.CREDIT ? entry.getAmount() : BigDecimal.ZERO;
        return new LedgerEntryLine(accountLabel(entry), entry.getFundNumber(), debit, credit, transaction.getMemo());
    }

    private boolean matchesAccount(AccountingEntry entry, String account)
    {
        return account == null || account.isBlank() || contains(entry.getAccountNumber(), account) || contains(entry.getAccountName(), account);
    }

    private boolean isCleared(AccountingTransaction transaction)
    {
        return transaction.getClearBank() != null && !transaction.getClearBank().isBlank();
    }

    private boolean contains(String value, String filter)
    {
        return filter == null || filter.isBlank() || (value != null && value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)));
    }

    private LocalDate parseDate(String value)
    {
        try { return value == null || value.isBlank() ? null : LocalDate.parse(value); }
        catch (RuntimeException ex) { return null; }
    }

    private String accountChoice(Account account)
    {
        String number = nullSafe(account.getAccountNumber());
        String name = nullSafe(account.getName());
        return number.isBlank() ? name : name.isBlank() ? number : number + " — " + name;
    }

    private String accountLabel(AccountingEntry entry)
    {
        String number = nullSafe(entry.getAccountNumber());
        String name = nullSafe(entry.getAccountName());
        return number.isBlank() ? name : name.isBlank() ? number : number + " — " + name;
    }

    private String nullSafe(String value) { return value == null ? "" : value; }

    public record LedgerTransactionFilter(LocalDate fromDate, LocalDate toDate, String account, String memo,
                                          BigDecimal amount, String fund, Boolean cleared, Boolean reconciled)
    {
        public static LedgerTransactionFilter empty() { return new LedgerTransactionFilter(null, null, null, null, null, null, null, null); }
        boolean hasDetailFilter()
        {
            return this.account != null && !this.account.isBlank() || this.fund != null && !this.fund.isBlank();
        }
    }

    public record LedgerTransactionRow(AccountingTransaction transaction, BigDecimal displayAmount, List<LedgerEntryLine> lines) {}

    public record LedgerEntryLine(String account, String fund, BigDecimal debit, BigDecimal credit, String memo)
    {
        public BigDecimal signedAmount() { return this.debit.subtract(this.credit); }
    }
}
