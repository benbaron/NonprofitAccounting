
package nonprofitbookkeeping.reports.jasper.query;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Facade that provides a configurable way to query transactions and map the
 * results into arbitrary JavaBeans (including the SCA report beans).
 *
 * Filters supported out of the box:
 * <ul>
 *     <li>Transaction type (debit or credit via {@link AccountSide}).</li>
 *     <li>Date range (inclusive) based on booking timestamp.</li>
 *     <li>Account filters (match any or require all provided accounts).</li>
 *     <li>Memo substring match.</li>
 * </ul>
 * Additional predicates can be attached to extend the query for other
 * parameters or bean-specific needs.
 */
public class TransactionQueryFacade
{
	private final Supplier<List<AccountingTransaction>> transactionSupplier;
	private String transactionTypeFilter;
	private LocalDate startDateFilter;
	private LocalDate endDateFilter;
	private Set<String> accountNumberFilter;
	private boolean requireAllAccounts;
	private String memoFilter;
	
	
	/**
	 * Builds a facade that reads transactions from the provided supplier.
	 *
	 * @param transactionSupplier supplies the transactions to search
	 */
	public TransactionQueryFacade(
		Supplier<List<AccountingTransaction>> transactionSupplier)
	{
		this.transactionSupplier = transactionSupplier == null ?
			Collections::emptyList : transactionSupplier;
		
	}

	/**
	 * Filters transactions by the transaction type stored in the info map
	 * (matching {@code transactionType} or {@code type} keys).
	 *
	 * @param transactionType type string to match
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade filterByTransactionType(String transactionType)
	{
		this.transactionTypeFilter = normalize(transactionType);
		return this;
	}

	/**
	 * Filters transactions by date range (inclusive) using the booking date
	 * timestamp.
	 *
	 * @param startDate inclusive start date
	 * @param endDate inclusive end date
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade dateRange(LocalDate startDate,
		LocalDate endDate)
	{
		this.startDateFilter = startDate;
		this.endDateFilter = endDate;
		return this;
	}

	/**
	 * Filters transactions by account membership.
	 *
	 * @param accounts account numbers to match
	 * @param requireAll true to require all accounts, false to match any
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade accounts(Collection<String> accounts,
		boolean requireAll)
	{
		this.accountNumberFilter =
			accounts == null ? Collections.emptySet() : new HashSet<>(accounts);
		this.requireAllAccounts = requireAll;
		return this;
	}
	
	/**
	 * Filters by memo substring.
	 */
	public TransactionQueryFacade memoContains(String memoContains)
	{
		this.memoFilter = normalize(memoContains);
		return this;
	}
	
	/**
	 * Queries transactions according to the provided configuration and
	 * returns a lightweight record containing the transaction and the
	 * entries that matched the account filter (if any).
	 *
	 * @param config configuration describing the desired filters
	 * @return ordered list of matching transaction records
	 */
	public List<TransactionRecord> query(QueryConfig config)
	{
		QueryConfig effectiveConfig =
			config == null ? QueryConfig.builder().build() : config;
		
		List<AccountingTransaction> transactions =
			new ArrayList<>(this.transactionSupplier.get());
		transactions.sort(Comparator.comparingLong(
			tx -> tx.getBookingDateTimestamp() == null ? Long.MAX_VALUE :
				tx.getBookingDateTimestamp()));
		
		List<TransactionRecord> records = new ArrayList<>();
		
		for (AccountingTransaction transaction : transactions)
		{
			
			if (transaction == null)
			{
				continue;
			}
			
			List<AccountingEntry> scopedEntries =
				scopeEntries(transaction, effectiveConfig.accountNumbers);
			TransactionRecord record =
				new TransactionRecord(transaction, scopedEntries);
			
			if (matches(record, effectiveConfig))
			{
				records.add(record);
			}
			
		}
		
		return records;
		
	}

	/**
	 * Queries and immediately maps the resulting records into bean
	 * instances.
	 *
	 * @param config configuration describing the desired filters
	 * @param mapper mapping function to transform a record into a bean
	 * @param <T>    bean type
	 * @return mapped beans (null results are skipped)
	 */
	public <T> List<T> queryAndMap(QueryConfig config,
		Function<TransactionRecord, T> mapper)
	{
		
		if (mapper == null)
		{
			return Collections.emptyList();
		}
		
		return query(config).stream()
			.map(mapper)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
		
	}

	/**
	 * Filters the provided transactions using the fluent filter configuration.
	 *
	 * @param transactions transactions to filter
	 * @return matching transactions
	 */
	public List<AccountingTransaction> filterTransactions(
		Collection<AccountingTransaction> transactions)
	{
		if (transactions == null)
		{
			return Collections.emptyList();
		}

		List<AccountingTransaction> results = new ArrayList<>();

		for (AccountingTransaction transaction : transactions)
		{
			if (transaction != null && matchesConfiguredFilters(transaction))
			{
				results.add(transaction);
			}
		}

		return results;
	}

	/**
	 * Filters and maps the provided transactions to bean instances.
	 *
	 * @param transactions transactions to filter
	 * @param mapper mapper to transform transactions into beans
	 * @param <T> bean type
	 * @return mapped beans
	 */
	public <T> List<T> mapToBeans(Collection<AccountingTransaction> transactions,
		Function<AccountingTransaction, T> mapper)
	{
		if (mapper == null)
		{
			return Collections.emptyList();
		}

		return filterTransactions(transactions).stream()
			.map(mapper)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
	private static List<AccountingEntry> scopeEntries(
		AccountingTransaction transaction, Set<String> accounts)
	{
		
		if (transaction.getEntries() == null)
		{
			return Collections.emptyList();
		}
		
		if (accounts == null || accounts.isEmpty())
		{
			return new ArrayList<>(transaction.getEntries());
		}
		
		List<AccountingEntry> scoped = new ArrayList<>();
		
		for (AccountingEntry entry : transaction.getEntries())
		{
			
			if (entry != null && accounts.contains(entry.getAccountNumber()))
			{
				scoped.add(entry);
			}
			
		}
		
		return scoped;
		
	}
	
	private static boolean matches(TransactionRecord record, QueryConfig config)
	{
		return record != null && matchesDate(record, config) &&
			matchesMemo(record, config.memoSubstring) &&
			matchesAccounts(record, config.accountNumbers,
				config.requireAllAccounts) &&
			matchesType(record, config.transactionType) &&
			config.extraPredicates.stream().allMatch(p -> p.test(record));
		
	}

	private boolean matchesConfiguredFilters(AccountingTransaction transaction)
	{
		return matchesTransactionType(transaction) &&
			matchesConfiguredDate(transaction) &&
			matchesConfiguredAccounts(transaction) &&
			matchesConfiguredMemo(transaction);
		
	}

	private boolean matchesTransactionType(AccountingTransaction transaction)
	{
		
		if (this.transactionTypeFilter == null)
		{
			return true;
		}
		
		Map<String, String> info = transaction.getInfo();
		
		if (info == null || info.isEmpty())
		{
			return false;
		}
		
		String raw = info.get("transactionType");
		
		if (raw == null)
		{
			raw = info.get("type");
		}
		
		String normalized = normalize(raw);
		return normalized != null &&
			normalized.equals(this.transactionTypeFilter);
		
	}

	private boolean matchesConfiguredDate(AccountingTransaction transaction)
	{
		Long timestamp = transaction.getBookingDateTimestamp();
		
		if (timestamp == null || timestamp <= 0)
		{
			return this.startDateFilter == null && this.endDateFilter == null;
		}
		
		LocalDate date = Instant.ofEpochMilli(timestamp)
			.atZone(ZoneId.systemDefault())
			.toLocalDate();
		
		if (this.startDateFilter != null && date.isBefore(this.startDateFilter))
		{
			return false;
		}
		
		if (this.endDateFilter != null && date.isAfter(this.endDateFilter))
		{
			return false;
		}
		
		return true;
		
	}

	private boolean matchesConfiguredAccounts(AccountingTransaction transaction)
	{
		
		if (this.accountNumberFilter == null ||
			this.accountNumberFilter.isEmpty())
		{
			return true;
		}
		
		Set<String> presentAccounts =
			transaction.getEntries() == null ? Collections.emptySet() :
				transaction.getEntries().stream()
					.filter(Objects::nonNull)
					.map(AccountingEntry::getAccountNumber)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
		
		if (this.requireAllAccounts)
		{
			return presentAccounts.containsAll(this.accountNumberFilter);
		}
		
		for (String account : this.accountNumberFilter)
		{
			
			if (presentAccounts.contains(account))
			{
				return true;
			}
			
		}
		
		return false;
		
	}

	private boolean matchesConfiguredMemo(AccountingTransaction transaction)
	{
		
		if (this.memoFilter == null)
		{
			return true;
		}
		
		String memo = transaction.getMemo();
		
		if (memo == null)
		{
			return false;
		}
		
		return memo.toLowerCase().contains(this.memoFilter);
		
	}

	private static String normalize(String value)
	{
		
		if (value == null)
		{
			return null;
		}
		
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed.toLowerCase();
		
	}
	
	private static boolean matchesDate(TransactionRecord record,
		QueryConfig config)
	{
		Long timestamp = record.transaction().getBookingDateTimestamp();
		
		if (timestamp == null || timestamp <= 0)
		{
			return config.startDate == null && config.endDate == null;
		}
		
		LocalDate date = Instant.ofEpochMilli(timestamp)
			.atZone(ZoneId.systemDefault())
			.toLocalDate();
		
		if (config.startDate != null && date.isBefore(config.startDate))
		{
			return false;
		}
		
		if (config.endDate != null && date.isAfter(config.endDate))
		{
			return false;
		}
		
		return true;
		
	}
	
	private static boolean matchesMemo(TransactionRecord record,
		String memoSubstring)
	{
		
		if (memoSubstring == null || memoSubstring.isBlank())
		{
			return true;
		}
		
		String memo = record.transaction().getMemo();
		
		if (memo == null)
		{
			return false;
		}
		
		return memo.toLowerCase().contains(memoSubstring.toLowerCase());
		
	}

	private static boolean matchesAccounts(TransactionRecord record,
		Set<String> accounts,
		boolean requireAll)
	{
		
		if (accounts == null || accounts.isEmpty())
		{
			return true;
		}
		
		Set<String> presentAccounts =
			record.transaction().getEntries() == null ? Collections.emptySet() :
				record.transaction().getEntries().stream()
					.filter(Objects::nonNull)
					.map(AccountingEntry::getAccountNumber)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
		
		if (requireAll)
		{
			return presentAccounts.containsAll(accounts);
		}
		
		for (String candidate : accounts)
		{
			
			if (presentAccounts.contains(candidate))
			{
				return true;
			}
			
		}
		
		return false;
		
	}
	
	/**
	 * Matches type.
	 *
	 * @param record the record
	 * @param type the type
	 * @return true, if successful
	 */
	private static boolean matchesType(TransactionRecord record,
		AccountSide type)
	{
		
		if (type == null || type == AccountSide.UNKNOWN)
		{
			return true;
		}
		
		return record.entries().stream()
			.anyMatch(
				entry -> entry != null && type.equals(entry.getAccountSide()));
		
	}
	
	/**
	 * Immutable configuration for running a transaction query.
	 */
	public static final class QueryConfig
	{
		private final AccountSide transactionType;
		private final LocalDate startDate;
		private final LocalDate endDate;
		private final Set<String> accountNumbers;
		private final boolean requireAllAccounts;
		private final String memoSubstring;
		private final List<Predicate<TransactionRecord>> extraPredicates;
		
		private QueryConfig(Builder builder)
		{
			this.transactionType = builder.transactionType;
			this.startDate = builder.startDate;
			this.endDate = builder.endDate;
			this.accountNumbers = builder.accountNumbers == null ?
				Collections.emptySet() : Collections
					.unmodifiableSet(new HashSet<>(builder.accountNumbers));
			this.requireAllAccounts = builder.requireAllAccounts;
			this.memoSubstring = builder.memoSubstring;
			this.extraPredicates = builder.extraPredicates == null ?
				Collections.emptyList() : Collections
					.unmodifiableList(new ArrayList<>(builder.extraPredicates));
			
		}
		
		public static Builder builder()
		{
			return new Builder();
			
		}
		
		
		/** Builder for {@link QueryConfig}. */
		public static final class Builder
		{
			private AccountSide transactionType;
			private LocalDate startDate;
			private LocalDate endDate;
			private Set<String> accountNumbers;
			private boolean requireAllAccounts;
			private String memoSubstring;
			private List<Predicate<TransactionRecord>> extraPredicates;			

			public QueryConfig build()
			{
				return new QueryConfig(this);
				
			}
			
		}
		
	}
	
	/**
	 * Result of a transaction query that captures the parent transaction and
	 * the entries that were used for filtering.
	 */
	public record TransactionRecord(AccountingTransaction transaction,
		List<AccountingEntry> entries)
	{
		public TransactionRecord
		{
			entries = entries == null ? Collections.emptyList() : entries;
			
		}
		
	}
	
}
