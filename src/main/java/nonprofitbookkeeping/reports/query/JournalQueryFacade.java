package nonprofitbookkeeping.reports.query;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Facade for querying {@link AccountingTransaction} records and mapping them
 * into report beans with a consistent set of filters.
 */
public class JournalQueryFacade
{
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

        private final JournalRepository journalRepository;

        public JournalQueryFacade()
        {
                this(new JournalRepository());
        }

        public JournalQueryFacade(JournalRepository journalRepository)
        {
                this.journalRepository = journalRepository;
        }

        public List<AccountingTransaction> fetchTransactions(JournalQueryCriteria criteria)
        {
                if (criteria == null)
                {
                        return Collections.emptyList();
                }

                List<AccountingTransaction> allTransactions;
                try
                {
                        allTransactions = this.journalRepository.listTransactions();
                }
                catch (Exception ex)
                {
                        throw new IllegalStateException("Unable to load journal transactions", ex);
                }

                return allTransactions.stream()
                        .filter(txn -> matchesCriteria(txn, criteria))
                        .collect(Collectors.toList());
        }

        public <T> List<T> fetchBeans(JournalQueryCriteria criteria,
                Function<AccountingTransaction, T> mapper)
        {
                Objects.requireNonNull(mapper, "mapper is required");

                return fetchTransactions(criteria).stream()
                        .map(mapper)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        }

        private boolean matchesCriteria(AccountingTransaction txn, JournalQueryCriteria criteria)
        {
                if (txn == null)
                {
                        return false;
                }

                if (criteria.getTransactionType() != null)
                {
                        String infoType = txn.getInfo() == null ? null
                                : txn.getInfo().getOrDefault("transactionType",
                                        txn.getInfo().get("type"));
                        if (infoType == null
                                || !infoType.equalsIgnoreCase(criteria.getTransactionType()))
                        {
                                return false;
                        }
                }

                if (!isWithinDateRange(txn, criteria.getStartDate(), criteria.getEndDate()))
                {
                        return false;
                }

                if (criteria.getMemoContains() != null)
                {
                        if (txn.getMemo() == null || !txn.getMemo().toLowerCase(Locale.ROOT)
                                .contains(criteria.getMemoContains()
                                        .toLowerCase(Locale.ROOT)))
                        {
                                return false;
                        }
                }

                if (!criteria.getInfoEquals().isEmpty())
                {
                        if (txn.getInfo() == null)
                        {
                                return false;
                        }
                        for (var entry : criteria.getInfoEquals().entrySet())
                        {
                                if (!Objects.equals(txn.getInfo().get(entry.getKey()),
                                        entry.getValue()))
                                {
                                        return false;
                                }
                        }
                }

                if (!criteria.getAccountNumbers().isEmpty())
                {
                        Set<String> txnAccounts = collectAccountNumbers(txn);
                        if (criteria.getAccountMatchMode() == JournalQueryCriteria.AccountMatchMode.ALL)
                        {
                                if (!txnAccounts.containsAll(criteria.getAccountNumbers()))
                                {
                                        return false;
                                }
                        }
                        else if (Collections.disjoint(txnAccounts, criteria.getAccountNumbers()))
                        {
                                return false;
                        }
                }

                if (criteria.getCustomPredicate() != null
                        && !criteria.getCustomPredicate().test(txn))
                {
                        return false;
                }

                return true;
        }

        private boolean isWithinDateRange(AccountingTransaction txn, LocalDate start,
                LocalDate end)
        {
                if (start == null && end == null)
                {
                        return true;
                }
                LocalDate txnDate = parseDate(txn.getDate());
                if (txnDate == null)
                {
                        return false;
                }
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

        private LocalDate parseDate(String rawDate)
        {
                if (rawDate == null || rawDate.isBlank())
                {
                        return null;
                }
                try
                {
                        return LocalDate.parse(rawDate.trim(), DATE_FORMATTER);
                }
                catch (DateTimeParseException ex)
                {
                        return null;
                }
        }

        private Set<String> collectAccountNumbers(AccountingTransaction txn)
        {
                if (txn.getEntries() == null || txn.getEntries().isEmpty())
                {
                        return Collections.emptySet();
                }
                Set<String> numbers = new HashSet<>();
                for (AccountingEntry entry : txn.getEntries())
                {
                        if (entry != null && entry.getAccountNumber() != null)
                        {
                                numbers.add(entry.getAccountNumber());
                        }
                }
                return numbers;
        }

        public BigDecimal sumAbsoluteEntryAmounts(AccountingTransaction txn)
        {
                if (txn == null || txn.getEntries() == null)
                {
                        return BigDecimal.ZERO;
                }
                return txn.getEntries().stream()
                        .filter(Objects::nonNull)
                        .map(AccountingEntry::getAmount)
                        .filter(Objects::nonNull)
                        .map(BigDecimal::abs)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
}
