package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter          // Automatically generates getter methods
@Setter          // Automatically generates setter methods
@AllArgsConstructor // Generates a constructor with all fields
@ToString         // Automatically generates a toString() method
public class Summary implements Serializable
{
    /**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 2708260956061642813L;

	
	private List<SummaryAccount> accounts = new ArrayList<>();
	private List<SummaryFund> funds = new ArrayList<>();
    
	/**  
	 * Constructor Summary
	 */
	public Summary()
	{
	}
	/**
	 * @return the accounts
	 */
	public List<SummaryAccount> getAccounts()
	{
		return this.accounts;
	}
	/**
	 * @param accounts the accounts to set
	 */
	public void setAccounts(List<SummaryAccount> accounts)
	{
		this.accounts = accounts;
	}
	/**
	 * @return the funds
	 */
	public List<SummaryFund> getFunds()
	{
		return this.funds;
	}
	/**
	 * @param funds the funds to set
	 */
	public void setFunds(List<SummaryFund> funds)
	{
		this.funds = funds;
	}
}