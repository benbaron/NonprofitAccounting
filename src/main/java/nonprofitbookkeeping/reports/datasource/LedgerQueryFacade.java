package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Facade that applies {@link LedgerQueryCriteria} filters to ledger
 * transactions and projects them into JavaBeans for Jasper reports.
 */
public class LedgerQueryFacade
{
    private final JournalRepository journalRepository;

    public LedgerQueryFacade()
    {
        this(new JournalRepository());
    }

    public LedgerQueryFacade(JournalRepository journalRepository)
    {
        this.journalRepository = Objects.requireNonNull(journalRepository);
    }

    /**
     * Load transactions from persistence, apply filters, and map them into the
     * provided bean projection.
     *
     * @param criteria filtering criteria
     * @param mapper   mapper from transaction to bean type
     * @param <T>      target bean type
     * @return mapped beans that satisfy the criteria
     */
    public <T> List<T> query(LedgerQueryCriteria criteria, Function<AccountingTransaction, T> mapper)
    {
        List<AccountingTransaction> transactions = Collections.emptyList();
        try
        {
            transactions = this.journalRepository.listTransactions();
        }
        catch (SQLException ex)
        {
            // Defer to caller for handling by surfacing as unchecked
            throw new IllegalStateException("Failed to load transactions", ex);
        }

        return queryFromTransactions(transactions, criteria, mapper);
    }

    /**
     * Apply criteria and projection to an in-memory collection. Useful for
     * re-using the facade with already-loaded ledger data (e.g. SCA workbook).
     *
     * @param transactions source transactions (can be null)
     * @param criteria     filtering criteria
     * @param mapper       mapper from transaction to bean type
     * @param <T>          target bean type
     * @return mapped beans that satisfy the criteria
     */
    public <T> List<T> queryFromTransactions(Collection<AccountingTransaction> transactions,
            LedgerQueryCriteria criteria,
            Function<AccountingTransaction, T> mapper)
    {
        if (transactions == null || transactions.isEmpty())
        {
            return Collections.emptyList();
        }

        List<AccountingTransaction> filtered = transactions.stream()
                .filter(t -> t != null && t.getEntries() != null && !t.getEntries().isEmpty())
                .filter(t -> matchesTransactionSide(t, criteria))
                .filter(t -> matchesDateRange(t, criteria))
                .filter(t -> matchesAccounts(t, criteria))
                .filter(t -> matchesMemo(t, criteria))
                .filter(t -> matchesCustomPredicates(t, criteria))
                .collect(Collectors.toList());

        if (mapper == null)
        {
            return Collections.emptyList();
        }

        List<T> mapped = new ArrayList<>(filtered.size());
        for (AccountingTransaction transaction : filtered)
        {
            T bean = mapper.apply(transaction);
            if (bean != null)
            {
                mapped.add(bean);
            }
        }
        return mapped;
    }

    private boolean matchesTransactionSide(AccountingTransaction transaction, LedgerQueryCriteria criteria)
    {
        if (criteria == null || criteria.getTransactionSide() == null)
        {
            return true;
        }

        AccountSide desiredSide = criteria.getTransactionSide();
        for (AccountingEntry entry : transaction.getEntries())
        {
            if (entry != null && entry.getAccountSide() == desiredSide)
            {
                return true;
            }
        }
        return false;
    }

    private boolean matchesDateRange(AccountingTransaction transaction, LedgerQueryCriteria criteria)
    {
        if (criteria == null)
        {
            return true;
        }

        LocalDate txnDate = resolveDate(transaction);
        if (txnDate == null)
        {
            return false;
        }

        LocalDate start = criteria.getStartDate();
        LocalDate end = criteria.getEndDate();

        if (start != null && txnDate.isBefore(start))
        {
            return false;
        }

        if (end != null && txnDate.isAfter(end))
        {
            return false;
        }

        return true;
    }

    private LocalDate resolveDate(AccountingTransaction transaction)
    {
        if (transaction.getBookingDateTimestamp() != null && transaction.getBookingDateTimestamp() > 0)
        {
            return Instant.ofEpochMilli(transaction.getBookingDateTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        try
        {
            return LocalDate.parse(transaction.getDate());
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private boolean matchesAccounts(AccountingTransaction transaction, LedgerQueryCriteria criteria)
    {
        if (criteria == null)
        {
            return true;
        }

        Set<String> desired = criteria.getAccountNumbers();
        if (desired == null || desired.isEmpty())
        {
            return true;
        }

        Set<String> present = transaction.getEntries().stream()
                .filter(Objects::nonNull)
                .map(AccountingEntry::getAccountNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (criteria.isRequireAllAccounts())
        {
            return present.containsAll(desired);
        }

        for (String account : desired)
        {
            if (present.contains(account))
            {
                return true;
            }
        }
        return false;
    }

    private boolean matchesMemo(AccountingTransaction transaction, LedgerQueryCriteria criteria)
    {
        if (criteria == null)
        {
            return true;
        }

        String memoFilter = criteria.getMemoContains();
        if (memoFilter == null || memoFilter.isBlank())
        {
            return true;
        }

        String memo = transaction.getMemo();
        if (memo == null)
        {
            return false;
        }

        return memo.toLowerCase().contains(memoFilter.toLowerCase());
    }

    private boolean matchesCustomPredicates(AccountingTransaction transaction, LedgerQueryCriteria criteria)
    {
        if (criteria == null)
        {
            return true;
        }

        for (java.util.function.Predicate<AccountingTransaction> predicate : criteria.getAdditionalPredicates())
        {
            if (!predicate.test(transaction))
            {
                return false;
            }
        }
        return true;
    }
}
