package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class LedgerQueryFacade
{
	public <T> List<T> queryFromTransactions(
		List<AccountingTransaction> transactions,
		LedgerQueryCriteria criteria,
		Function<AccountingTransaction, T> mapper)
	{
		
		if (transactions == null || transactions.isEmpty() || mapper == null)
		{
			return List.of();
		}
		
		LedgerQueryCriteria effective =
			criteria == null ? LedgerQueryCriteria.builder().build() : criteria;
		List<T> results = new ArrayList<>();
		
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
	
	private static boolean matches(AccountingTransaction transaction,
		LedgerQueryCriteria criteria)
	{
		return matchesDate(transaction, criteria.getStartDate(),
			criteria.getEndDate()) &&
			matchesMemo(transaction, criteria.getMemoContains()) &&
			matchesAccounts(transaction, criteria.getAccountNumbers(),
				criteria.isRequireAllAccounts()) &&
			matchesSide(transaction, criteria.getTransactionSide()) &&
			matchesPredicates(transaction, criteria.getPredicates());
		
	}
	
	private static boolean matchesDate(AccountingTransaction transaction,
		LocalDate startDate,
		LocalDate endDate)
	{
		Long timestamp = transaction.getBookingDateTimestamp();
		
		if (timestamp == null || timestamp <= 0)
		{
			return startDate == null && endDate == null;
		}
		
		LocalDate date = Instant.ofEpochMilli(timestamp)
			.atZone(ZoneOffset.UTC)
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
	
	private static boolean matchesMemo(AccountingTransaction transaction,
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
		
		return memo.toLowerCase()
			.contains(memoContains.toLowerCase());
		
	}
	
	private static boolean matchesAccounts(AccountingTransaction transaction,
		Collection<String> accountNumbers,
		boolean requireAll)
	{
		
		if (accountNumbers == null || accountNumbers.isEmpty())
		{
			return true;
		}
		
		if (transaction.getEntries() == null ||
			transaction.getEntries().isEmpty())
		{
			return false;
		}
		
		List<String> present = new ArrayList<>();
		
		for (AccountingEntry entry : transaction.getEntries())
		{
			
			if (entry != null && entry.getAccountNumber() != null)
			{
				present.add(entry.getAccountNumber());
			}
			
		}
		
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
	
	private static boolean matchesSide(AccountingTransaction transaction,
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
	
	private static boolean matchesPredicates(AccountingTransaction transaction,
		List<java.util.function.Predicate<AccountingTransaction>> predicates)
	{
		
		if (predicates == null || predicates.isEmpty())
		{
			return true;
		}
		
		for (java.util.function.Predicate<AccountingTransaction> predicate :
			predicates)
		{
			
			if (!predicate.test(transaction))
			{
				return false;
			}
			
		}
		
		return true;
		
	}
}
