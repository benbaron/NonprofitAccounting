
package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a fund within the nonprofit bookkeeping system.
 * A fund is a distinct financial entity used to track resources designated for specific purposes
 * or by specific donors. It holds a name (which also serves as its ID), a list of associated
 * accounts, and a calculated balance derived from these accounts.
 * This class uses Lombok for boilerplate code like getters, setters, and constructors.
 */
@Data @AllArgsConstructor public class Fund
{
	
	/** The name of the fund, serving as its primary identifier. */
	@JsonProperty private String name;
	/** List of account numbers associated with this fund. */
	@JsonProperty private List<String> accountIds;
	/**
	 * The calculated balance of the fund.
	 * This balance is derived from the sum of balances of all accounts associated with this fund.
	     * It is updated via the {@link #updateBalance(Ledger)} method.
	 */
	@JsonProperty private BigDecimal balance;
	
	/**
	 * Constructs a new Fund with the specified name.
	 * Initializes the list of associated accounts as empty and the balance to zero.
	 *
	 * @param name The name of the fund. This name also serves as the fund's identifier via {@link #getFundId()}.
	 */
	public Fund(String name)
	{
		this.name = name;
		this.accountIds = new ArrayList<>();
		this.balance = BigDecimal.ZERO;
	}
	
	
	/**  
	 * Constructor Fund
	 */
	public Fund()
	{		
	}


	/**
	 * Gets the name of the fund.
	 * @return The name of the fund.
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Gets the current calculated balance of the fund.
	 * The balance is updated by calling {@link #updateBalance(Ledger)}.
	 * @return The current balance of the fund as a {@link BigDecimal}.
	 */
	public BigDecimal getBalance()
	{
		return this.balance;
	}
	
	/**
	 * Gets the list of accounts associated with this fund.
	 * Note: This method returns a direct reference to the internal list.
	 * Modifications to the returned list will directly affect the fund's state.
	 * Consider using {@link #addAccount(Account)} and {@link #removeAccount(Account)}
	 * for managing associated accounts to ensure balance updates are triggered.
	 *
	 * @return A list of {@link Account} objects associated with this fund.
	 */
	public List<String> getAccountIds()
	{
		return this.accountIds;
	}
	
	/**
	 * Adds an account to this fund if it's not already present.
	 * This method establishes a bi-directional relationship by also adding this fund
	 * to the specified account's list of associated funds. After adding the account,
	 * the fund's balance is recalculated by calling {@link #updateBalance(Ledger)}.
	 *
	 * @param account The {@link Account} to associate with this fund. If null, the method does nothing.
	 */
	/**
	 * Adds an account to this fund. This overload does not immediately
	 * recalculate the balance using a ledger and therefore behaves like the
	 * original single-argument method.
	 *
	 * @param account the {@link Account} to associate with this fund
	 */
	public void addAccount(String accountId)
	{
		addAccount(accountId, null);
	}
	
	/**
	 * Adds an account to this fund if it's not already present and updates the
	 * balance using the provided ledger.
	 *
	 * @param account The {@link Account} to associate with this fund. If null,
	 *                the method does nothing.
	 * @param ledger  The {@link Ledger} used to calculate balances. May be null
	 *                to skip ledger-based updates.
	 */
	public void addAccount(String accountId, Ledger ledger)
	{
		if (accountId == null)
			return; // Guard against null
			
		if (!this.accountIds.contains(accountId))
		{
			this.accountIds.add(accountId);
			updateBalance(ledger); // Recalculate the fund's balance
		}
		
	}
	
	/**
	 * Removes an account from this fund.
	 * This overload mirrors the original method and does not require a
	 * ledger for balance recalculation.
	 *
	 * @param account the {@link Account} to disassociate from this fund
	 */
	public void removeAccount(String accountId)
	{
		removeAccount(accountId, null);
	}
	
	/**
	 * Removes an account from this fund and optionally recalculates the
	 * balance using the provided ledger.
	 *
	 * @param account the {@link Account} to disassociate from this fund. If
	 *                null, or if the account is not associated with this fund,
	 *                the method does nothing.
	 * @param ledger  the ledger used to recalculate balances. May be null.
	 */
	public void removeAccount(String accountId, Ledger ledger)
	{
		if (accountId == null)
			return; // Guard against null account
			
		boolean removed = this.accountIds.remove(accountId);
		
		if (removed)
		{
			updateBalance(ledger); // Recalculate the fund's balance
		}
		
	}
	
	/**
	 * Updates (recalculates) the balance of this fund by summing the balances
	 * (obtained via {@link Account#totalAccountBalance(Ledger)}) of all accounts currently
	 * associated with this fund.
	 */
	public void updateBalance(Ledger ledger)
	{
		BigDecimal totalBalance = BigDecimal.ZERO;
		
		ChartOfAccounts coa = CurrentCompany.getCompany().getChartOfAccounts();
		
		for (String accountId : this.accountIds)
		{
			Account account = coa.getAccount(accountId);
			
			if (account != null)
			{
				totalBalance = totalBalance.add(account.totalAccountBalance(ledger));
			}
			
		}
		
		this.balance = totalBalance;
	}
	
	
	/**
	 * Sets the balance of this fund directly.
	 * Note: This method bypasses the automatic balance calculation based on associated accounts.
	 * It should be used with caution, primarily for scenarios like initial data loading from
	 * a source where balances are pre-calculated, or for testing purposes.
	 * Consider using {@link #updateBalance(Ledger)} for standard balance recalculation.
	 *
	 * @param newBalance The new balance to set for the fund.
	 */
	public void setBalance(BigDecimal newBalance)
	{
		this.balance = newBalance;
		
	}
	
	@Override public String toString()
	{
		return "Fund{" + "name='" + this.name + '\'' + ", balance=" + this.balance + '}';
	}
	
	/**
	 * Gets the identifier for this fund. In the current implementation,
	 * the fund's name serves as its unique identifier.
	 *
	 * @return The name of the fund, used as its ID.
	 */
	public String getFundId()
	{
		return this.name;
	}
	
	/**
	 * @param string
	 */
	public void setName(String string)
	{
		this.name = string;
	}
	
	/**
	 * @param string
	 */
	public void setFundId(String string)
	{
		this.name = string;
	}
	
	
}
