
package nonprofitbookkeeping.api;

import java.math.BigDecimal;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;

/**
 * 
 */
public interface AccountDetails
{	

	String getAccountNumber();
	
	AccountSide getIncreaseSide();
	
	String getName();
	
	void setAccountNumber(String accountNumber);
	
	void setIncreaseSide(AccountSide increaseSide);
	
	void setName(String name);
	
	String getAccountName();

	/**
	 * @return
	 */
	String getAccountCode();

	/**
	 * @return
	 */
	String getAccountType();

	/**
	 * @return
	 */
	Account getParentAccount();

	/**
	 * @return
	 */
	String getCurrency();

	/**
	 * @return
	 */
	BigDecimal getOpeningBalance();
	
	
	@Override boolean equals(java.lang.Object o);
	
	@Override int hashCode();
	
	
	@Override String toString();
	
	
}
