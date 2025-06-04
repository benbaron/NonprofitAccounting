
package nonprofitbookkeeping.model;

import javax.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

/**
 * Represents a group of related account entries.
 */
public class AccountingTransaction implements Serializable
{
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = -8821254116304310L;
	
	@JsonProperty private Account account;
	@JsonProperty private Set<AccountingEntry> entries;
	@JsonProperty private Map<String, String> info;
	@JsonProperty private long bookingDateTimestamp;
	@JsonProperty private String date;
	@JsonProperty private String memo;
	
	/**
	 * No-argument constructor for Jackson deserialization and general use.
	 */
	public AccountingTransaction()
	{
		// Initialize collections to avoid NullPointerExceptions if not set by Jackson
		this.entries = new HashSet<>();
		this.info = new HashMap<>();
		// Initialize other fields to default values
		this.date = "";
		this.memo = "";
	}
	
	/**
	 * Public constructor for existing code.
	 */
	public AccountingTransaction(Account account, Set<AccountingEntry> entries,
		@Nullable Map<String, String> info, long bookingDateTimestamp)
	{
		this.account =
			com.google.common.base.Preconditions.checkNotNull(account,
				"account cannot be null");
		
		// Temporarily assign to a modifiable collection
		Set<nonprofitbookkeeping.model.AccountingEntry> modifiableEntries = 
			new java.util.HashSet<>(
			com.google.common.base.Preconditions.checkNotNull(entries, 
				"entries cannot be null"));
		
		if (modifiableEntries != null)
		{ 
			// Though checkNotNull above handles entries being null
			
			for (nonprofitbookkeeping.model.AccountingEntry entry : modifiableEntries)
			{
				
				if (entry != null)
				{
					entry.setTransaction(this);
				}
				
			}
			
		}
		
		this.entries = java.util.Collections.unmodifiableSet(modifiableEntries);
		this.info = (info == null) ? java.util.Collections.emptyMap() :
			java.util.Collections.unmodifiableMap(new java.util.HashMap<>(info));
		this.bookingDateTimestamp = bookingDateTimestamp;
		this.date = ""; // Default initialization
		this.memo = ""; // Default initialization
		
		com.google.common.base.Preconditions.checkArgument(!this.entries.isEmpty(),
			"Transaction must have at least one entry (ideally 2+ for balance)");
		com.google.common.base.Preconditions.checkArgument(this.entries.size() >= 2,
			"A transaction consists of at least two entries");
		
		// this.entries.forEach(e -> e.setTransaction(this)); // Keep commented for now
		// com.google.common.base.Preconditions.checkArgument(isBalanced(),
		// "Transaction unbalanced"); // Keep commented for now
	}
	
	// Getters and Setters
	
	public Account getAccount()
	{
		return this.account;
	}
	
	public void setAccount(Account account)
	{
		this.account = account;
	}
	
	public Set<AccountingEntry> getEntries()
	{
		return this.entries;
	}
	
	public void setEntries(Set<AccountingEntry> entries)
	{
		this.entries = entries;
		
		if (this.entries != null)
		{
			
			for (nonprofitbookkeeping.model.AccountingEntry entry : this.entries)
			{
				
				if (entry != null)
				{
					entry.setTransaction(this);
				}
				
			}
			
		}
		
	}
	
	public Map<String, String> getInfo()
	{
		return this.info;
	}
	
	public void setInfo(Map<String, String> info)
	{
		this.info = info;
	}
	
	public long getBookingDateTimestamp()
	{
		return this.bookingDateTimestamp;
	}
	
	public void setBookingDateTimestamp(long bookingDateTimestamp)
	{
		this.bookingDateTimestamp = bookingDateTimestamp;
	}
	
	public String getDate()
	{
		return this.date;
	}
	
	public void setDate(String date)
	{
		this.date = date;
	}
	
	public String getMemo()
	{
		return this.memo;
	}
	
	public void setMemo(String memo)
	{
		this.memo = memo;
	}
	
	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}
	
	/**
	 * getTotalAmount
	 * @return
	 */
	public BigDecimal getTotalAmount()
	{
		BigDecimal debitTotal = BigDecimal.ZERO;
		// BigDecimal creditTotal = BigDecimal.ZERO; // Not used in current logic
		
		if (this.entries == null)
		{
			return BigDecimal.ZERO; // Guard against null entries
		}
		
		for (AccountingEntry e : this.entries)
		{
			
			if (e.getAccountSide() == AccountSide.DEBIT)
			{
				debitTotal = debitTotal.add(e.getAmount());
			}
			
			// else {
			// creditTotal = creditTotal.add(e.getAmount());
			// }
		}
		
		return debitTotal;
	}
	
	public boolean isBalanced()
	{
		
		if (this.entries == null || this.entries.isEmpty() || this.entries.size() < 2)
		{
			return false; // Or true, depending on definition for empty/single-entry transactions
		}
		
		BigDecimal balance = this.entries.stream()
			.map(e -> e.getAccountSide() == AccountSide.DEBIT ? e.getAmount() :
				e.getAmount().negate())
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		return balance.compareTo(BigDecimal.ZERO) == 0;
	}
	
	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Transaction ").append(Instant.ofEpochMilli(this.bookingDateTimestamp).toString())
			.append("\n");
		
		if (this.entries != null)
		{
			this.entries.forEach(e -> sb.append(e).append("\n"));
		}
		else
		{
			sb.append("No entries\n");
		}
		
		return sb.toString();
	}
	
	// Other existing methods like getDescription, getAccountName, etc.
	// Note: some of these might be redundant if direct setters/getters are used,
	// or could be refactored. For now, keeping them as per instruction.
	
	public String getDescription()
	{
		return this.memo;
	}
	
	public String getAccountName()
	{
		return this.account != null ? this.account.getName() : null;
	}
	
	public void setDescription(String description)
	{
		this.memo = description;
	}
	
	// Note: getDate() and setDate(String) are already defined above.
	// The original file had a duplicate setDate(String string).
	
	public BigDecimal countAccountBalance()
	{
		
		if (this.account == null)
		{
			return BigDecimal.ZERO;
		}
		
		return this.account.totalAccountBalance();
	}
	
	// Note: setMemo(String) is already defined above.
	// The original file had a duplicate setMemo(String memo2).
	
	
	/**
	 * 
	 * @param valueOf
	 */
	public void setTotalAmount(BigDecimal valueOf)
	{
		// This method was a stub. If it's meant to do something,
		// it would likely need to adjust entries.
		// Given entries are typically managed as a set, directly setting a total amount
		// without affecting entries might be misleading.
		// For now, keeping it as is, as per instruction.
	}
	
}
