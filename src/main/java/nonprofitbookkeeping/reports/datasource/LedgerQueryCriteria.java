package nonprofitbookkeeping.reports.datasource;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingTransaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


/**
 * Criteria for filtering accounting transactions.
 */
public class LedgerQueryCriteria
{
	
	/** The start date. */
	private final LocalDate startDate;
	
	/** The end date. */
	private final LocalDate endDate;
	
	/** The memo contains. */
	private final String memoContains;
	
	/** The account numbers. */
	private final Set<String> accountNumbers;
	
	/** The require all accounts. */
	private final boolean requireAllAccounts;
	
	/** The transaction side. */
	private final AccountSide transactionSide;
	
	/** The predicates. */
	private final List<Predicate<AccountingTransaction>> predicates;
	
	/**
	 * Instantiates a new ledger query criteria.
	 *
	 * @param builder the builder
	 */
	private LedgerQueryCriteria(Builder builder)
	{
		this.startDate = builder.startDate;
		this.endDate = builder.endDate;
		this.memoContains = builder.memoContains;
		this.accountNumbers = builder.accountNumbers == null ?
			Collections.emptySet() :
			Collections.unmodifiableSet(new HashSet<>(builder.accountNumbers));
		this.requireAllAccounts = builder.requireAllAccounts;
		this.transactionSide = builder.transactionSide;
		this.predicates = builder.predicates == null ?
			Collections.emptyList() :
			Collections.unmodifiableList(new ArrayList<>(builder.predicates));
	}
	
	/**
	 * Builder.
	 *
	 * @return the builder
	 */
	public static Builder builder()
	{
		return new Builder();
	}
	
	/**
	 * Gets the start date.
	 *
	 * @return the start date
	 */
	public LocalDate getStartDate()
	{
		return this.startDate;
	}
	
	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 */
	public LocalDate getEndDate()
	{
		return this.endDate;
	}
	
	/**
	 * Gets the memo contains.
	 *
	 * @return the memo contains
	 */
	public String getMemoContains()
	{
		return this.memoContains;
	}
	
	/**
	 * Gets the account numbers.
	 *
	 * @return the account numbers
	 */
	public Set<String> getAccountNumbers()
	{
		return this.accountNumbers;
	}
	
	/**
	 * Checks if is require all accounts.
	 *
	 * @return true, if is require all accounts
	 */
	public boolean isRequireAllAccounts()
	{
		return this.requireAllAccounts;
	}
	
	/**
	 * Gets the transaction side.
	 *
	 * @return the transaction side
	 */
	public AccountSide getTransactionSide()
	{
		return this.transactionSide;
	}
	
	/**
	 * Gets the predicates.
	 *
	 * @return the predicates
	 */
	public List<Predicate<AccountingTransaction>> getPredicates()
	{
		return this.predicates;
	}
	
	/**
	 * The Class Builder.
	 */
	public static final class Builder
	{
		
		/** The start date. */
		private LocalDate startDate;
		
		/** The end date. */
		private LocalDate endDate;
		
		/** The memo contains. */
		private String memoContains;
		
		/** The account numbers. */
		private Set<String> accountNumbers;
		
		/** The require all accounts. */
		private boolean requireAllAccounts;
		
		/** The transaction side. */
		private AccountSide transactionSide;
		
		/** The predicates. */
		private List<Predicate<AccountingTransaction>> predicates;
		
		/**
		 * Start date.
		 *
		 * @param startDate the start date
		 * @return the builder
		 */
		public Builder startDate(LocalDate startDate)
		{
			this.startDate = startDate;
			return this;
		}
		
		/**
		 * End date.
		 *
		 * @param endDate the end date
		 * @return the builder
		 */
		public Builder endDate(LocalDate endDate)
		{
			this.endDate = endDate;
			return this;
		}
		
		/**
		 * Memo contains.
		 *
		 * @param memoContains the memo contains
		 * @return the builder
		 */
		public Builder memoContains(String memoContains)
		{
			this.memoContains = memoContains;
			return this;
		}
		
		/**
		 * Adds the account number.
		 *
		 * @param accountNumber the account number
		 * @return the builder
		 */
		public Builder addAccountNumber(String accountNumber)
		{
			
			if (accountNumber == null || accountNumber.isBlank())
			{
				return this;
			}
			
			if (this.accountNumbers == null)
			{
				this.accountNumbers = new HashSet<>();
			}
			
			this.accountNumbers.add(accountNumber);
			return this;
		}
		
		/**
		 * Require all accounts.
		 *
		 * @param requireAllAccounts the require all accounts
		 * @return the builder
		 */
		public Builder requireAllAccounts(boolean requireAllAccounts)
		{
			this.requireAllAccounts = requireAllAccounts;
			return this;
		}
		
		/**
		 * Transaction side.
		 *
		 * @param transactionSide the transaction side
		 * @return the builder
		 */
		public Builder transactionSide(AccountSide transactionSide)
		{
			this.transactionSide = transactionSide;
			return this;
		}
		
		/**
		 * Adds the predicate.
		 *
		 * @param predicate the predicate
		 * @return the builder
		 */
		public Builder addPredicate(Predicate<AccountingTransaction> predicate)
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
		
		/**
		 * Builds the.
		 *
		 * @return the ledger query criteria
		 */
		public LedgerQueryCriteria build()
		{
			return new LedgerQueryCriteria(this);
		}
	}
}
