
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.Fund;

import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 */
public class FundAccountingService
{
	
	@JsonProperty private final Map<String, Fund> fundMap;
	@JsonProperty private final Map<String, Account> accountMap;
	
	/**
	 * 
	 * Constructor FundAccountingService
	 */
	public FundAccountingService()
	{
		this.fundMap = new HashMap<>();
		this.accountMap = new HashMap<>();
	}
	
	// Add a new fund
	public void addFund(Fund fund)
	{
		
		if (this.fundMap.containsKey(fund.getName()))
		{
			throw new IllegalArgumentException(
				"Fund with name '" + fund.getName() + "' already exists.");
		}
		
		this.fundMap.put(fund.getName(), fund);
	}
	
	// Remove a fund by name
	public boolean removeFund(String fundName)
	{
		Fund fund = this.fundMap.get(fundName);
		
		if (fund != null)
		{
			
			// Remove this fund from all accounts
			for (Account account : fund.getAccounts())
			{
				account.removeFund(fund);
			}
			
			this.fundMap.remove(fundName);
			return true;
		}
		
		return false;
	}
	
	// Add a new account
	public void addAccount(Account account)
	{
		
		if (this.accountMap.containsKey(account.getName()))
		{
			throw new IllegalArgumentException(
				"Account with name '" + account.getName() + "' already exists.");
		}
		
		this.accountMap.put(account.getName(), account);
	}
	
	// Remove an account by name
	public boolean removeAccount(String accountName)
	{
		Account account = this.accountMap.get(accountName);
		
		if (account != null)
		{
			
			// Remove this account from all funds
			for (Fund fund : account.getAssociatedFunds())
			{
				fund.removeAccount(account);
			}
			
			this.accountMap.remove(accountName);
			return true;
		}
		
		return false;
	}
	
	// List all funds
	public List<Fund> listFunds()
	{
		return new ArrayList<>(this.fundMap.values());
	}
	
	// List all accounts
	public List<Account> listAccounts()
	{
		return new ArrayList<>(this.accountMap.values());
	}
	
	// Get the balances for all funds (using BigDecimal)
	public Map<String, BigDecimal> getFundBalances()
	{
		Map<String, BigDecimal> balances = new HashMap<>();
		
		for (Fund fund : this.fundMap.values())
		{
			balances.put(fund.getName(), fund.getBalance());
		}
		
		return balances;
	}
	
	// Transfer funds between two funds or accounts (using BigDecimal)
	public void transferFunds(String fromFund, String toFund, BigDecimal amount)
	{
		
		if (amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			throw new IllegalArgumentException("Amount must be greater than zero.");
		}
		
		Fund from = this.fundMap.get(fromFund);
		Fund to = this.fundMap.get(toFund);
		
		if (from == null || to == null)
		{
			throw new IllegalArgumentException("Source or destination fund does not exist.");
		}
		
		// Perform the transfer logic (using BigDecimal for balance operations)
		from.setBalance(from.getBalance().subtract(amount));
		to.setBalance(to.getBalance().add(amount));
	}
	
}
