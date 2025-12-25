package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.AccountSide;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Facade for querying ledger transactions using {@link LedgerQueryCriteria}.
 */
public class LedgerQueryFacade
{
    /**
     * Filters the provided transactions according to the criteria and maps
     * the matching transactions into the requested result type.
     *
     * @param transactions transactions to search
     * @param criteria criteria describing filters to apply
     * @param mapper mapping function for each matching transaction
     * @param <T> result type
     * @return list of mapped results
     */
    public <T> List<T> queryFromTransactions(
        Collection<AccountingTransaction> transactions,
        LedgerQueryCriteria criteria,
        java.util.function.Function<AccountingTransaction, T> mapper)
    {
        if (transactions == null || mapper == null)
        {
            return List.of();
        }

        List<T> results = new ArrayList<>();

        for (AccountingTransaction transaction : transactions)
        {
            if (!matches(transaction, criteria))
            {
                continue;
            }

            T mapped = mapper.apply(transaction);
            if (mapped != null)
            {
                results.add(mapped);
            }
        }

        return results;
    }

    private boolean matches(AccountingTransaction transaction,
        LedgerQueryCriteria criteria)
    {
        if (transaction == null)
        {
            return false;
        }

        if (criteria == null)
        {
            return true;
        }

        if (!matchesDate(transaction, criteria.getStartDate(),
            criteria.getEndDate()))
        {
            return false;
        }

        if (!matchesMemo(transaction, criteria.getMemoContains()))
        {
            return false;
        }

        if (!matchesAccounts(transaction, criteria.getAccountNumbers(),
            criteria.isRequireAllAccounts()))
        {
            return false;
        }

        if (!matchesSide(transaction, criteria.getTransactionSide()))
        {
            return false;
        }

        if (criteria.getPredicate() != null &&
            !criteria.getPredicate().test(transaction))
        {
            return false;
        }

        return true;
    }

    private boolean matchesDate(AccountingTransaction transaction,
        LocalDate startDate,
        LocalDate endDate)
    {
        if (startDate == null && endDate == null)
        {
            return true;
        }

        Long timestamp = transaction.getBookingDateTimestamp();
        if (timestamp == null || timestamp <= 0)
        {
            return false;
        }

        LocalDate date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate();

        if (startDate != null && date.isBefore(startDate))
        {
            return false;
        }

        if (endDate != null && date.isAfter(endDate))
        {
            return false;
        }

        return true;
    }

    private boolean matchesMemo(AccountingTransaction transaction,
        String memoContains)
    {
        if (memoContains == null || memoContains.isBlank())
        {
            return true;
        }

        String memo = transaction.getMemo();
        if (memo == null)
        {
            return false;
        }

        return memo.toLowerCase(Locale.ROOT)
            .contains(memoContains.toLowerCase(Locale.ROOT));
    }

    private boolean matchesAccounts(AccountingTransaction transaction,
        Set<String> accountNumbers,
        boolean requireAll)
    {
        if (accountNumbers == null || accountNumbers.isEmpty())
        {
            return true;
        }

        Set<String> presentAccounts =
            transaction.getEntries() == null ? Set.of() :
                transaction.getEntries().stream()
                    .filter(Objects::nonNull)
                    .map(AccountingEntry::getAccountNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

        if (requireAll)
        {
            return presentAccounts.containsAll(accountNumbers);
        }

        for (String account : accountNumbers)
        {
            if (presentAccounts.contains(account))
            {
                return true;
            }
        }

        return false;
    }

    private boolean matchesSide(AccountingTransaction transaction,
        AccountSide transactionSide)
    {
        if (transactionSide == null || transactionSide == AccountSide.UNKNOWN)
        {
            return true;
        }

        if (transaction.getEntries() == null)
        {
            return false;
        }

        return transaction.getEntries().stream()
            .filter(Objects::nonNull)
            .anyMatch(entry -> transactionSide.equals(entry.getAccountSide()));
    }
}
