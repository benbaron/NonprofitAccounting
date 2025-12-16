
package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Facade for querying {@link AccountingTransaction} objects with a fluent API.
 * <p>
 * The facade is intentionally simple and composable so it can be reused to
 * populate different Jasper beans (for example the SCA spreadsheet beans) by
 * supplying a mapper function via {@link #mapToBeans(Collection, Function)}.
 * Filters currently supported:
 * <ul>
 *     <li>Transaction type (looked up in {@code transactionType} or {@code type}
 *     keys of {@link AccountingTransaction#getInfo()}).</li>
 *     <li>Date range (inclusive) based on {@link AccountingTransaction#getBookingDateTimestamp()}.</li>
 *     <li>Account matching (any or all of the provided account numbers).</li>
 *     <li>Memo substring match (case-insensitive).</li>
 *     <li>Arbitrary predicates via {@link #addFilter(Predicate)}.</li>
 * </ul>
 * Additional filters can be layered on without modifying calling code.
 */
public class TransactionQueryFacade
{
	private String transactionType;
	private LocalDate startDate;
	private LocalDate endDate;
	private final Set<String> accountNumbers = new LinkedHashSet<>();
	private boolean requireAllAccounts;
	private String memoContains;
	private final List<Predicate<AccountingTransaction>> extraFilters =
		new ArrayList<>();
	
	/**
	 * Restricts results to a transaction type. The value is compared against
	 * the {@code transactionType} or {@code type} keys of the transaction's
	 * info map.
	 *
	 * @param transactionType the type to match, case-insensitive
	 * @return this facade for chaining
	 */
	public
		TransactionQueryFacade filterByTransactionType(String transactionType)
	{
		this.transactionType = transactionType;
		return this;
		
	}
	
	/**
	 * Restricts results to transactions booked between the supplied dates
	 * (inclusive). Null values are ignored.
	 *
	 * @param start inclusive start date
	 * @param end   inclusive end date
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade dateRange(LocalDate start, LocalDate end)
	{
		this.startDate = start;
		this.endDate = end;
		return this;
		
	}
	
	/**
	 * Restricts results to transactions touching the provided account
	 * numbers.
	 *
	 * @param accounts         account numbers to match
	 * @param requireAllMatch  if true, all provided account numbers must be
	 *                         present on a transaction; if false, any match
	 *                         will pass the filter
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade accounts(Collection<String> accounts,
		boolean requireAllMatch)
	{
		this.accountNumbers.clear();
		
		if (accounts != null)
		{
			this.accountNumbers.addAll(accounts.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedHashSet::new)));
		}
		
		this.requireAllAccounts = requireAllMatch;
		return this;
		
	}
	
	/**
	 * Restricts results to transactions whose memo contains the provided
	 * text (case-insensitive).
	 *
	 * @param memoContains memo substring to look for
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade memoContains(String memoContains)
	{
		this.memoContains = memoContains;
		return this;
		
	}
	
	/**
	 * Adds an arbitrary filter predicate that must match each transaction.
	 *
	 * @param predicate filter predicate; ignored if null
	 * @return this facade for chaining
	 */
	public TransactionQueryFacade addFilter(
		Predicate<AccountingTransaction> predicate)
	{
		
		if (predicate != null)
		{
			this.extraFilters.add(predicate);
		}
		
		return this;
		
	}
	
	/**
	 * Filters the supplied transactions according to the configured
	 * criteria.
	 *
	 * @param transactions transactions to filter
	 * @return filtered list preserving iteration order of the input
	 */
	public List<AccountingTransaction> filterTransactions(
		Collection<AccountingTransaction> transactions)
	{
		
		if (transactions == null || transactions.isEmpty())
		{
			return Collections.emptyList();
		}
		
		List<AccountingTransaction> filtered = new ArrayList<>();
		
		for (AccountingTransaction transaction : transactions)
		{
			
			if (transaction == null)
			{
				continue;
			}
			
			if (matchesTransaction(transaction))
			{
				filtered.add(transaction);
			}
			
		}
		
		return filtered;
		
	}
	
	/**
	 * Filters and maps the supplied transactions using the provided mapper
	 * function.
	 *
	 * @param transactions transactions to filter
	 * @param mapper       mapping function to convert a transaction into a
	 *                     bean suitable for a report
	 * @param <T>          target bean type
	 * @return list of mapped beans
	 */
	public <T> List<T> mapToBeans(
		Collection<AccountingTransaction> transactions,
		Function<AccountingTransaction, T> mapper)
	{
		
		if (mapper == null)
		{
			return Collections.emptyList();
		}
		
		List<AccountingTransaction> filtered = filterTransactions(transactions);
		List<T> mapped = new ArrayList<>(filtered.size());
		
		for (AccountingTransaction transaction : filtered)
		{
			mapped.add(mapper.apply(transaction));
		}
		
		return mapped;
		
	}
	
	private boolean matchesTransaction(AccountingTransaction transaction)
	{
		return matchesType(transaction) && matchesDateRange(transaction) &&
			matchesAccount(transaction) && matchesMemo(transaction) &&
			matchesCustomPredicates(transaction);
		
	}
	
	private boolean matchesType(AccountingTransaction transaction)
	{
		
		if (this.transactionType == null || this.transactionType.isBlank())
		{
			return true;
		}
		
		String desired = this.transactionType.toLowerCase(Locale.ROOT);
		String typeValue = null;
		
		if (transaction.getInfo() != null)
		{
			typeValue = transaction.getInfo().get("transactionType");
			
			if (typeValue == null)
			{
				typeValue = transaction.getInfo().get("type");
			}
			
		}
		
		return typeValue != null &&
			typeValue.toLowerCase(Locale.ROOT).equals(desired);
		
	}
	
	private boolean matchesDateRange(AccountingTransaction transaction)
	{
		
		if (this.startDate == null && this.endDate == null)
		{
			return true;
		}
		
		LocalDate bookingDate = Instant
			.ofEpochMilli(transaction.getBookingDateTimestamp())
			.atZone(ZoneId.systemDefault())
			.toLocalDate();
		
		if (this.startDate != null && bookingDate.isBefore(this.startDate))
		{
			return false;
		}
		
		return this.endDate == null || !bookingDate.isAfter(this.endDate);
		
	}
	
	private boolean matchesAccount(AccountingTransaction transaction)
	{
		
		if (this.accountNumbers.isEmpty())
		{
			return true;
		}
		
		if (transaction.getEntries() == null)
		{
			return false;
		}
		
		Set<String> txnAccounts = transaction.getEntries().stream()
			.map(AccountingEntry::getAccountNumber)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		
		if (this.requireAllAccounts)
		{
			return txnAccounts.containsAll(this.accountNumbers);
		}
		
		for (String accountNumber : this.accountNumbers)
		{
			
			if (txnAccounts.contains(accountNumber))
			{
				return true;
			}
			
		}
		
		return false;
		
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
		
		return memo.toLowerCase(Locale.ROOT)
			.contains(this.memoContains.toLowerCase(Locale.ROOT));
		
	}
	
	private boolean matchesCustomPredicates(AccountingTransaction transaction)
	{
		
		for (Predicate<AccountingTransaction> predicate : this.extraFilters)
		{
			
			if (!predicate.test(transaction))
			{
				return false;
			}
			
		}
		
		return true;
		
	}
	
}
