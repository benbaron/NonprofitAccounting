
package nonprofitbookkeeping.model;

import javax.annotation.Nullable;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Represents a group of related account entries.
 */
@Builder              // generates AccountingTransaction.Builder
@Data

public class AccountingTransaction implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -8821254116304310L;
	
	final private Set<AccountingEntry> entries;
	
	final private long bookingDateTimestamp;
	
	final private Map<String, String> info;
	
	final private Account account;
	
	final private Integer transactionId;
	
	final private String date;
	
	private String memo;
	
	/**
	 * 
	 * Constructor AccountingTransaction
	 * @param entries
	 * @param info
	 * @param bookingDateTimestamp
	 */
	public AccountingTransaction(Account account,
		Set<AccountingEntry> entries,
		@Nullable Map<String, String> info,
		long bookingDateTimestamp)
	{
		
		if (info == null)
		{
			info = new HashMap<>();
		}
		
		this.account = account;
		this.info = info;
		this.entries = checkNotNull(entries);
		this.bookingDateTimestamp = bookingDateTimestamp;
		this.transactionId = null;
		this.date = "";
		this.memo = "";
		
		checkArgument(!entries.isEmpty());
		checkArgument(entries.size() >= 2,
			"A transaction consists of at least two entries");
		checkArgument(isBalanced(), "Transaction unbalanced");
		
		entries.forEach(e -> e.setTransaction(this));
	}
	

	/**  
	 * Constructor AccountingTransaction
	 */
	public AccountingTransaction()
	{
		this.entries = null;
		this.bookingDateTimestamp = 0;
		this.info = null;
		this.account = null;
		this.transactionId = null;
		this.date = "";
		this.memo = "";
	}


	/**
	 * @return total of the transaction
	 */
	public BigDecimal getTotalAmount()
	{
		BigDecimal debitTotal = new BigDecimal(0);
		BigDecimal creditTotal = new BigDecimal(0);
		
		for (AccountingEntry e : this.entries)
		{
			
			if (e.getAccountSide() == AccountSide.DEBIT)
			{
				debitTotal = debitTotal.add(e.getAmount());
			}
			else
			{
				creditTotal = creditTotal.add(e.getAmount());
			}
			
		}
		
		return debitTotal;
	}
	
	/**
	 * Is Balanced comparator
	 * @return true/false
	 */
	public boolean isBalanced()
	{
		BigDecimal debits = this.entries.stream().
			map(e -> e.getAccountSide() == AccountSide.DEBIT ?
			e.getAmount() : e.getAmount().negate())
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		return debits.compareTo(BigDecimal.ZERO) == 0;
	}
	
	/**
	 * 
	 * Override @see java.lang.Object#toString()
	 */
	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Transaction ")
			.append(Instant.ofEpochMilli(this.bookingDateTimestamp).toString())
			.append("\n");
		this.entries.stream().forEach(e -> sb.append(e).append("\n"));
		return sb.toString();
	}

	/**
	 * @return the entries
	 */
	public Set<AccountingEntry> getEntries()
	{
		return this.entries;
	}

	/**
	 * @return the bookingDateTimestamp
	 */
	public long getBookingDateTimestamp()
	{
		return this.bookingDateTimestamp;
	}

	/**
	 * @return the info
	 */
	public Map<String, String> getInfo()
	{
		return this.info;
	}

	/**
	 * @return the account
	 */
	public Account getAccount()
	{
		return this.account;
	}

	/**
	 * @return the transactionId
	 */
	public Integer getTransactionId()
	{
		return this.transactionId;
	}

	/**
	 * @return the date
	 */
	public String getDate()
	{
		return this.date;
	}

	/**
	 * @return the memo
	 */
	public String getMemo()
	{
		return this.memo;
	}

	/**
	 * @return
	 */
	public String getId()
	{
		return this.transactionId.toString();
	}
	
	/**
	 * @param account1
	 * @return net amount
	 */
	public BigDecimal getNetAmountForAccount(String account1)
	{
		return this.account.getBalance();
	}
	
	/**
	 * @return
	 */
	public String getDescription()
	{
		return this.memo;
	}
	
	/**
	 * @return
	 */
	public String getAccountName()
	{
		return this.account.getAccountDetails().getAccountName();
	}
	
	/**
	 * 
	 * @return
	 */
	public BigDecimal getAccountBalance()
	{
		return this.account.getBalance();
	}


	/**
	 * @param object
	 */
	public void setDescription(String description)
	{
		this.memo = description;
		
	}


	/**
	 * @param valueOf
	 */
	public void setTotalAmount(BigDecimal valueOf)
	{
		// TODO Auto-generated method stub
		
	}


	/**
	 * @param string
	 */
	public void setDate(String string)
	{
		// TODO Auto-generated method stub
		
	}


}
