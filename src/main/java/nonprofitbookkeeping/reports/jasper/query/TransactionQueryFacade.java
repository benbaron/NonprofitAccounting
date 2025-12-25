
package nonprofitbookkeeping.reports.jasper.query;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
	private String transactionType;
	private LocalDate startDate;
	private LocalDate endDate;
	private Set<String> accountNumbers;
	private boolean requireAllAccounts;
	private String memoContains;
	
	/**
	 * Builds a facade that reads transactions from the current company
	 * context. If no company or ledger is available, the query returns an
	 * empty result set.
	 */
	public TransactionQueryFacade()
	{
		this(TransactionQueryFacade::loadTransactionsFromCurrentCompany);
		
	}
	
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
	 * Filters by transaction type stored in the transaction info map.
	 *
	 * @param transactionType case-insensitive type string
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade filterByTransactionType(String transactionType)
	{
		this.transactionType = normalize(transactionType);
		return this;
		
	}

	/**
	 * Sets an inclusive date range filter.
	 *
	 * @param startDate start date
	 * @param endDate end date
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade dateRange(LocalDate startDate,
		LocalDate endDate)
	{
		this.startDate = startDate;
		this.endDate = endDate;
		return this;
		
	}

	/**
	 * Filters by account numbers.
	 *
	 * @param accounts account numbers to match
	 * @param requireAll if true all accounts must be present
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade accounts(List<String> accounts,
		boolean requireAll)
	{
		this.requireAllAccounts = requireAll;
		
		if (accounts == null)
		{
			this.accountNumbers = null;
			return this;
		}
		
		Set<String> normalized = new HashSet<>();
		
		for (String account : accounts)
		{
			
			if (account != null && !account.isBlank())
			{
				normalized.add(account.trim());
			}
			
		}
		
		this.accountNumbers = normalized;
		return this;
		
	}

	/**
	 * Filters by memo substring (case-insensitive).
	 *
	 * @param memo memo fragment
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade memoContains(String memo)
	{
		this.memoContains = memo;
		return this;
		
	}

	/**
	 * Filters the supplied transactions according to the configured criteria.
	 *
	 * @param transactions list of transactions to filter
	 * @return filtered list
	 */
	public List<AccountingTransaction> filterTransactions(
		List<AccountingTransaction> transactions)
	{
		
		if (transactions == null)
		{
			return Collections.emptyList();
		}
		
		List<AccountingTransaction> results = new ArrayList<>();
		
		for (AccountingTransaction transaction : transactions)
		{
			
			if (transaction == null)
			{
				continue;
			}
			
			if (!matchesInfoType(transaction))
			{
				continue;
			}
			
			if (!matchesDate(transaction))
			{
				continue;
			}
			
			if (!matchesAccounts(transaction))
			{
				continue;
			}
			
			if (!matchesMemo(transaction))
			{
				continue;
			}
			
			results.add(transaction);
			
		}
		
		return results;
		
	}

	/**
	 * Maps filtered transactions into beans.
	 *
	 * @param transactions transactions to filter and map
	 * @param mapper mapping function
	 * @param <T> bean type
	 * @return mapped beans
	 */
	public <T> List<T> mapToBeans(List<AccountingTransaction> transactions,
		Function<AccountingTransaction, T> mapper)
	{
		
		if (mapper == null)
		{
			return Collections.emptyList();
		}
		
		List<T> results = new ArrayList<>();
		
		for (AccountingTransaction transaction : filterTransactions(transactions))
		{
			T mapped = mapper.apply(transaction);
			
			if (mapped != null)
			{
				results.add(mapped);
			}
			
		}
		
		return results;
		
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
	 * Load transactions from current company
	 * @return List of transactions
	 */
	private static
		List<AccountingTransaction> loadTransactionsFromCurrentCompany()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null)
		{
			return Collections.emptyList();
		}
		
		List<AccountingTransaction> transactions =
			company.getLedger().getTransactions();
		return transactions == null ? Collections.emptyList() : transactions;
		
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

	private boolean matchesInfoType(AccountingTransaction transaction)
	{
		
		if (this.transactionType == null)
		{
			return true;
		}
		
		if (transaction.getInfo() == null)
		{
			return false;
		}
		
		String infoType =
			transaction.getInfo().getOrDefault("transactionType",
				transaction.getInfo().get("type"));
		
		return normalize(infoType) != null &&
			normalize(infoType).equals(this.transactionType);
		
	}

	private boolean matchesDate(AccountingTransaction transaction)
	{
		
		if (this.startDate == null && this.endDate == null)
		{
			return true;
		}
		
		LocalDate date = resolveDate(transaction);
		
		if (date == null)
		{
			return false;
		}
		
		if (this.startDate != null && date.isBefore(this.startDate))
		{
			return false;
		}
		
		if (this.endDate != null && date.isAfter(this.endDate))
		{
			return false;
		}
		
		return true;
		
	}

	private LocalDate resolveDate(AccountingTransaction transaction)
	{
		Long timestamp = transaction.getBookingDateTimestamp();
		
		if (timestamp != null && timestamp > 0)
		{
			return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		}
		
		String dateText = transaction.getDate();
		
		if (dateText == null || dateText.isBlank())
		{
			return null;
		}
		
		try
		{
			return LocalDate.parse(dateText);
		}
		catch (Exception ex)
		{
			return null;
		}
		
	}

	private boolean matchesAccounts(AccountingTransaction transaction)
	{
		
		if (this.accountNumbers == null || this.accountNumbers.isEmpty())
		{
			return true;
		}
		
		Set<String> presentAccounts = transaction.getEntries() == null ?
			Collections.emptySet() :
			transaction.getEntries().stream()
				.filter(Objects::nonNull)
				.map(AccountingEntry::getAccountNumber)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		if (this.requireAllAccounts)
		{
			return presentAccounts.containsAll(this.accountNumbers);
		}
		
		return !Collections.disjoint(presentAccounts, this.accountNumbers);
		
	}

	private boolean matchesMemo(AccountingTransaction transaction)
	{
		
		if (this.memoContains == null || this.memoContains.isBlank())
		{
			return true;
		}
		
		String memo = transaction.getMemo();
		
		if (memo == null)
		{
			return false;
		}
		
		return memo.toLowerCase().contains(this.memoContains.toLowerCase());
		
	}

	private String normalize(String value)
	{
		return value == null || value.isBlank() ? null :
			value.trim().toLowerCase();
		
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
		
		public AccountSide getTransactionType()
		{
			return this.transactionType;
			
		}
		
		public LocalDate getStartDate()
		{
			return this.startDate;
			
		}
		
		public LocalDate getEndDate()
		{
			return this.endDate;
			
		}
		
		public Set<String> getAccountNumbers()
		{
			return this.accountNumbers;
			
		}
		
		public boolean isRequireAllAccounts()
		{
			return this.requireAllAccounts;
			
		}
		
		public String getMemoSubstring()
		{
			return this.memoSubstring;
			
		}
		
		public List<Predicate<TransactionRecord>> getExtraPredicates()
		{
			return this.extraPredicates;
			
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
			
			public Builder withTransactionType(AccountSide transactionType)
			{
				this.transactionType = transactionType;
				return this;
				
			}
			
			public Builder withDateRange(LocalDate startDate, LocalDate endDate)
			{
				this.startDate = startDate;
				this.endDate = endDate;
				return this;
				
			}
			
			public Builder withAccounts(Collection<String> accounts,
				boolean requireAll)
			{
				
				if (accounts != null)
				{
					this.accountNumbers = new HashSet<>();
					
					for (String account : accounts)
					{
						
						if (account != null && !account.isBlank())
						{
							this.accountNumbers.add(account);
						}
						
					}
					
				}
				
				this.requireAllAccounts = requireAll;
				return this;
				
			}
			
			public Builder withMemoSubstring(String memoSubstring)
			{
				this.memoSubstring = memoSubstring;
				return this;
				
			}
			
			public Builder addPredicate(Predicate<TransactionRecord> predicate)
			{
				
				if (predicate == null)
				{
					return this;
				}
				
				if (this.extraPredicates == null)
				{
					this.extraPredicates = new ArrayList<>();
				}
				
				this.extraPredicates.add(predicate);
				return this;
				
			}
			
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
