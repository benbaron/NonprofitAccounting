
package nonprofitbookkeeping.model;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	
	@JsonProperty final private Set<AccountingEntry> entries;
	
	@JsonProperty final private long bookingDateTimestamp;
	
	@JsonProperty final private Map<String, String> info;
	
	@JsonProperty final private Account account;
	
	@JsonProperty final private Integer transactionId;
	
	@JsonProperty private String date;
	
	@JsonProperty private String memo;
	
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
	  * setTotalAmount
	  * @param valueOf
	  */
	public void setTotalAmount(BigDecimal valueOf)
	{
		// TODO Auto-generated method stub
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
		return this.account.getName();
	}
	

	/**
	 * @param object
	 */
	public void setDescription(String description)
	{
		this.memo = description;
		
	}


	/**
	 * @param string
	 */
	public void setDate(String string)
	{
		this.date = string;
		
	}

	/**
	 * @return
	 */
	public BigDecimal countAccountBalance()
	{
		return this.account.totalAccountBalance();
	}


	/**
	 * @param memo2
	 */
	public void setMemo(String memo2)
	{
		this.memo = memo2;
		
	}


}
