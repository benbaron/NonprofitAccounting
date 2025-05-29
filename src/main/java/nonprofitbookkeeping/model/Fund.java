
package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fund
{
	
	@JsonProperty private String name;
	@JsonProperty private List<Account> accounts; // List to hold associated accounts (many-to-many relationship)
	@JsonProperty private BigDecimal balance; // The balance of the fund is the sum of all associated accounts'
												// balances
	
	// Constructor
	public Fund(String name)
	{
		this.name = name;
		this.accounts = new ArrayList<>();
		this.balance = BigDecimal.ZERO; // Initial balance set to BigDecimal.ZERO
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public BigDecimal getBalance()
	{
		return this.balance;
	}
	
	public List<Account> getAccounts()
	{
		return this.accounts;
	}
	
	/**
	 * Adds an account to this fund.
	 */
	public void addAccount(Account account)
	{
		
		if (!this.accounts.contains(account))
		{
			this.accounts.add(account);
			account.addFund(this); // Add this fund to the account
			updateBalance(); // Recalculate the fund's balance
		}
		
	}
	
	/**
	 * Removes an account from this fund.
	 */
	public void removeAccount(Account account)
	{
		this.accounts.remove(account);
		account.removeFund(this); // Remove this fund from the account
		updateBalance(); // Recalculate the fund's balance
	}
	
	/**
	 * Updates the balance of this fund by summing the balances of all associated accounts.
	 */
	public void updateBalance()
	{
		BigDecimal totalBalance = BigDecimal.ZERO; // Start with a BigDecimal.ZERO balance
		
		for (Account account : this.accounts)
		{
			totalBalance = totalBalance.add(account.totalAccountBalance()); // Sum up the balance of each
																	// associated account
		}
		
		this.balance = totalBalance; // Update the fund's balance
	}
	

	/**
	 * @param balance2
	 */
	public void setBalance(BigDecimal balance2)
	{
		this.balance = balance2;
		
	}
	
	@Override public String toString()
	{
		return "Fund{" +
			"name='" + this.name + '\'' +
			", balance=" + this.balance +
			'}';
	}

	/**
	 * @return
	 */
	public String getFundId()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
