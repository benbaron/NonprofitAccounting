package nonprofitbookkeeping.reports.datasource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Criteria for querying ledger transactions.
 */
public class LedgerQueryCriteria
{
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final String memoContains;
	private final Set<String> accountNumbers;
	private final boolean requireAllAccounts;
	private final AccountSide transactionSide;
	private final List<Predicate<AccountingTransaction>> predicates;
	
	private LedgerQueryCriteria(Builder builder)
	{
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.memoContains = builder.memoContains;
		this.accountNumbers = Collections
			.unmodifiableSet(new LinkedHashSet<>(builder.accountNumbers));
		this.requireAllAccounts = builder.requireAllAccounts;
		this.transactionSide = builder.transactionSide;
		this.predicates = builder.predicates == null ? List.of() :
			Collections.unmodifiableList(new ArrayList<>(builder.predicates));
		
	}
	
	public LocalDate getStartDate()
	{
		return this.startDate;
		
	}
	
	public LocalDate getEndDate()
	{
		return this.endDate;
		
	}
	
	public String getMemoContains()
	{
		return this.memoContains;
		
	}
	
	public Set<String> getAccountNumbers()
	{
		return this.accountNumbers;
		
	}
	
	public boolean isRequireAllAccounts()
	{
		return this.requireAllAccounts;
		
	}
	
	public AccountSide getTransactionSide()
	{
		return this.transactionSide;
		
	}
	
	public List<Predicate<AccountingTransaction>> getPredicates()
	{
		return this.predicates;
		
	}
	
	public static Builder builder()
	{
		return new Builder();
		
	}
	
	public static final class Builder
	{
		private LocalDate startDate;
		private LocalDate endDate;
		private String memoContains;
		private final Set<String> accountNumbers = new LinkedHashSet<>();
		private boolean requireAllAccounts;
		private AccountSide transactionSide;
		private List<Predicate<AccountingTransaction>> predicates;
		
		public Builder startDate(LocalDate startDate)
		{
			this.startDate = startDate;
			return this;
			
		}
		
		public Builder endDate(LocalDate endDate)
		{
			this.endDate = endDate;
			return this;
			
		}
		
		public Builder memoContains(String memoContains)
		{
			this.memoContains = memoContains;
			return this;
			
		}
		
		public Builder addAccountNumber(String accountNumber)
		{
			
			if (accountNumber != null && !accountNumber.isBlank())
			{
				this.accountNumbers.add(accountNumber.trim());
			}
			
			return this;
			
		}
		
		public Builder requireAllAccounts(boolean requireAllAccounts)
		{
			this.requireAllAccounts = requireAllAccounts;
			return this;
			
		}
		
		public Builder transactionSide(AccountSide transactionSide)
		{
			this.transactionSide = transactionSide;
			return this;
			
		}
		
		public Builder addPredicate(
			Predicate<AccountingTransaction> predicate)
		{
			
			if (predicate == null)
			{
				return this;
			}
			
			if (this.predicates == null)
			{
				this.predicates = new ArrayList<>();
			}
			
			this.predicates.add(predicate);
			return this;
			
		}
		
		public LedgerQueryCriteria build()
		{
			return new LedgerQueryCriteria(this);
			
		}
		
	}
}
