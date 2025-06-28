
package nonprofitbookkeeping.core;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AccountingTransactionBuilder
{
	final private Set<AccountingEntry> entries = new HashSet<>();
	final private Map<String, String> info;
	
	final private Account account;
	
	/**
	 * Constructs an AccountingTransactionBuilder.
	 * @param account The account associated with the transaction. Can be null.
	 * @param info Additional information about the transaction. Can be null.
	 */
	private AccountingTransactionBuilder(@Nullable Account account, @Nullable Map<String, String> info)
	{
		this.info = info;
		this.account = account;
	}
	
	/**
	 * Creates a new AccountingTransactionBuilder with the given information.
	 * @param info Additional information about the transaction. Can be null.
	 * @return A new AccountingTransactionBuilder instance.
	 */
	public static AccountingTransactionBuilder create
		(@Nullable Map<String, String> info)
	{
		return new AccountingTransactionBuilder(null, info);
	}
	
	/**
	 * Creates a new AccountingTransactionBuilder with the given account.
	 * @param account The account associated with the transaction.
	 * @return A new AccountingTransactionBuilder instance.
	 */
	public static AccountingTransactionBuilder create(Account account)
	{
		return new AccountingTransactionBuilder(account, null);
	}
	
	/**
	 * Adds a debit entry to the transaction.
	 * @param amount The amount to debit.
	 * @param accountNumber The account number to debit.
	 * @return This AccountingTransactionBuilder instance for chaining.
	 */
	public AccountingTransactionBuilder debit(BigDecimal amount, String accountNumber)
	{
		this.entries.add(new AccountingEntry(amount, accountNumber, AccountSide.DEBIT));
		return this;
	}
	
	/**
	 * Adds a credit entry to the transaction.
	 * @param amount The amount to credit.
	 * @param accountNumber The account number to credit.
	 * @return This AccountingTransactionBuilder instance for chaining.
	 */
	public AccountingTransactionBuilder credit(BigDecimal amount, String accountNumber)
	{
		this.entries.add(new AccountingEntry(amount, accountNumber, AccountSide.CREDIT));
		return this;
	}
	
	/**
	 * Builds the AccountingTransaction.
	 * @return A new AccountingTransaction instance.
	 */
	public AccountingTransaction build()
	{
		return new AccountingTransaction(this.account, 
			this.entries, 
			this.info, 
			Instant.now().toEpochMilli());
	}

	/**
	 * @return A new AccountingTransaction instance.
	 */
	public static AccountingTransactionBuilder create()
	{
		return new AccountingTransactionBuilder(null, null);
	}
	
}
