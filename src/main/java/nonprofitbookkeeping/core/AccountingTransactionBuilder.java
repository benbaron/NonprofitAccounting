
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
	 * 
	 * Constructor AccountingTransactionBuilder
	 * @param account
	 * @param info
	 */
	private AccountingTransactionBuilder(Account account, @Nullable Map<String, String> info)
	{
		this.info = info;
		this.account = account;
	}
	
	/**
	 * 
	 * @param info
	 * @return
	 */
	public static AccountingTransactionBuilder create
		(@Nullable Map<String, String> info)
	{
		return new AccountingTransactionBuilder(null, info);
	}
	
	/**
	 * 
	 * @param account
	 * @return
	 */
	public static AccountingTransactionBuilder create(Account account)
	{
		return new AccountingTransactionBuilder(account, null);
	}
	
	/**
	 * 
	 * @param amount
	 * @param accountNumber
	 * @return
	 */
	public AccountingTransactionBuilder debit(BigDecimal amount, String accountNumber)
	{
		this.entries.add(new AccountingEntry(amount, accountNumber, AccountSide.DEBIT));
		return this;
	}
	
	/**
	 * 
	 * @param amount
	 * @param accountNumber
	 * @return
	 */
	public AccountingTransactionBuilder credit(BigDecimal amount, String accountNumber)
	{
		this.entries.add(new AccountingEntry(amount, accountNumber, AccountSide.CREDIT));
		return this;
	}
	
	/**
	 * 
	 * @return
	 */
	public AccountingTransaction build()
	{
		return new AccountingTransaction(this.account, 
			this.entries, 
			this.info, 
			Instant.now().toEpochMilli());
	}
	
}
