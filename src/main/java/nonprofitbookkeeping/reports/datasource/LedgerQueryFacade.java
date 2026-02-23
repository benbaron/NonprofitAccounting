package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

// TODO: Auto-generated Javadoc
/**
 * Filters transactions based on {@link LedgerQueryCriteria}.
 */
public class LedgerQueryFacade
{
	
	/**
	 * Query from transactions.
	 *
	 * @param <T> the generic type
	 * @param transactions the transactions
	 * @param criteria the criteria
	 * @param mapper the mapper
	 * @return the list
	 */
	public <T> List<T> queryFromTransactions(
		List<AccountingTransaction> transactions,
		LedgerQueryCriteria criteria,
		Function<AccountingTransaction, T> mapper)
	{
		
		if (mapper == null)
		{
			return Collections.emptyList();
		}
		
		LedgerQueryCriteria effective =
			criteria == null ? LedgerQueryCriteria.builder().build() :
				criteria;
		
		List<T> results = new ArrayList<>();
		
		if (transactions == null)
		{
			return results;
		}
		
		for (AccountingTransaction transaction : transactions)
		{
			
			if (transaction == null)
			{
				continue;
			}
			
			if (!matches(transaction, effective))
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
	
	/**
	 * Matches.
	 *
	 * @param transaction the transaction
	 * @param criteria the criteria
	 * @return true, if successful
	 */
	private boolean matches(AccountingTransaction transaction,
		LedgerQueryCriteria criteria)
	{
		return matchesDate(transaction, criteria) &&
			matchesMemo(transaction, criteria.getMemoContains()) &&
			matchesAccounts(transaction, criteria.getAccountNumbers(),
				criteria.isRequireAllAccounts()) &&
			matchesSide(transaction, criteria.getTransactionSide()) &&
			criteria.getPredicates().stream()
				.allMatch(predicate -> predicate.test(transaction));
	}
	
	/**
	 * Matches date.
	 *
	 * @param transaction the transaction
	 * @param criteria the criteria
	 * @return true, if successful
	 */
	private boolean matchesDate(AccountingTransaction transaction,
		LedgerQueryCriteria criteria)
	{
		LocalDate date = resolveDate(transaction);
		
		if (date == null)
		{
			return criteria.getStartDate() == null &&
				criteria.getEndDate() == null;
		}
		
		if (criteria.getStartDate() != null &&
			date.isBefore(criteria.getStartDate()))
		{
			return false;
		}
		
		if (criteria.getEndDate() != null &&
			date.isAfter(criteria.getEndDate()))
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Resolve date.
	 *
	 * @param transaction the transaction
	 * @return the local date
	 */
	private LocalDate resolveDate(AccountingTransaction transaction)
	{
		Long timestamp = transaction.getBookingDateTimestamp();
		
		if (timestamp != null && timestamp > 0)
		{
			return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.toLocalDate();
		}
		
		String date = transaction.getDate();
		
		if (date == null)
		{
			return null;
		}
		
		try
		{
			return LocalDate.parse(date);
		}
		catch (RuntimeException e)
		{
			return null;
		}
	}
	
	/**
	 * Matches memo.
	 *
	 * @param transaction the transaction
	 * @param memoContains the memo contains
	 * @return true, if successful
	 */
	private boolean matchesMemo(AccountingTransaction transaction,
		String memoContains)
	{
		
		if (memoContains == null || memoContains.isBlank())
		{
			return true;
		}
		
		String memo = transaction.getMemo();
		return memo != null &&
			memo.toLowerCase().contains(memoContains.toLowerCase());
	}
	
	/**
	 * Matches accounts.
	 *
	 * @param transaction the transaction
	 * @param accountNumbers the account numbers
	 * @param requireAll the require all
	 * @return true, if successful
	 */
	private boolean matchesAccounts(AccountingTransaction transaction,
		Set<String> accountNumbers,
		boolean requireAll)
	{
		
		if (accountNumbers == null || accountNumbers.isEmpty())
		{
			return true;
		}
		
		if (transaction.getEntries() == null)
		{
			return false;
		}
		
		List<String> present = transaction.getEntries().stream()
			.filter(Objects::nonNull)
			.map(AccountingEntry::getAccountNumber)
			.filter(Objects::nonNull)
			.toList();
		
		if (requireAll)
		{
			return present.containsAll(accountNumbers);
		}
		
		for (String account : accountNumbers)
		{
			
			if (present.contains(account))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Matches side.
	 *
	 * @param transaction the transaction
	 * @param side the side
	 * @return true, if successful
	 */
	private boolean matchesSide(AccountingTransaction transaction,
		AccountSide side)
	{
		
		if (side == null || side == AccountSide.UNKNOWN)
		{
			return true;
		}
		
		if (transaction.getEntries() == null)
		{
			return false;
		}
		
		return transaction.getEntries().stream()
			.filter(Objects::nonNull)
			.anyMatch(entry -> side.equals(entry.getAccountSide()));
	}
}
