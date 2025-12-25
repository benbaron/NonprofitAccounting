package nonprofitbookkeeping.reports.datasource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Facade for filtering ledger transactions and mapping results into beans.
 */
public class LedgerQueryFacade
{
	public <T> List<T> queryFromTransactions(
		List<AccountingTransaction> transactions,
		LedgerQueryCriteria criteria,
		Function<AccountingTransaction, T> mapper)
	{
		
		if (transactions == null || mapper == null)
		{
			return Collections.emptyList();
		}
		
		List<T> results = new ArrayList<>();
		
		for (AccountingTransaction transaction : transactions)
		{
			
			if (transaction == null || !matches(transaction, criteria))
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
		
		for (var predicate : criteria.getPredicates())
		{
			
			if (!predicate.test(transaction))
			{
				return false;
			}
			
		}
		
		return true;
		
	}
	
	private boolean matchesDate(AccountingTransaction transaction,
		LocalDate start,
		LocalDate end)
	{
		
		if (start == null && end == null)
		{
			return true;
		}
		
		LocalDate date = resolveDate(transaction);
		
		if (date == null)
		{
			return false;
		}
		
		if (start != null && date.isBefore(start))
		{
			return false;
		}
		
		if (end != null && date.isAfter(end))
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
		
		return memo.toLowerCase().contains(memoContains.toLowerCase());
		
	}
	
	private boolean matchesAccounts(AccountingTransaction transaction,
		Set<String> accounts,
		boolean requireAll)
	{
		
		if (accounts == null || accounts.isEmpty())
		{
			return true;
		}
		
		Set<String> present = new HashSet<>();
		
		if (transaction.getEntries() != null)
		{
			
			for (AccountingEntry entry : transaction.getEntries())
			{
				
				if (entry != null && entry.getAccountNumber() != null)
				{
					present.add(entry.getAccountNumber());
				}
				
			}
			
		}
		
		if (requireAll)
		{
			return present.containsAll(accounts);
		}
		
		return !Collections.disjoint(present, accounts);
		
	}
	
	private boolean matchesSide(AccountingTransaction transaction,
		nonprofitbookkeeping.model.AccountSide side)
	{
		
		if (side == null || side == nonprofitbookkeeping.model.AccountSide.UNKNOWN)
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
