
package nonprofitbookkeeping.api;

import java.math.BigDecimal;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;

/**
 * 
 */
public interface AccountDetails
{	

	/**
	 * Gets the account number.
	 * @return The account number.
	 */
	String getAccountNumber();
	
	/**
	 * Gets the side where the account increases.
	 * @return The side where the account increases.
	 */
	AccountSide getIncreaseSide();
	
	/**
	 * Gets the name of the account.
	 * @return The name of the account.
	 */
	String getName();
	
	/**
	 * Sets the account number.
	 * @param accountNumber The account number to set.
	 */
	void setAccountNumber(String accountNumber);
	
	/**
	 * Sets the side where the account increases.
	 * @param increaseSide The side where the account increases.
	 */
	void setIncreaseSide(AccountSide increaseSide);
	
	/**
	 * Sets the name of the account.
	 * @param name The name of the account to set.
	 */
	void setName(String name);
	
	/**
	 * Gets the name of the account.
	 * This method seems redundant with {@link #getName()}.
	 * @return The name of the account.
	 */
	String getAccountName();

	/**
	 * Gets the account code.
	 * @return The account code.
	 */
	String getAccountCode();

	/**
	 * Gets the account type.
	 * @return The account type.
	 */
	String getAccountType();

	/**
	 * Gets the parent account.
	 * @return The parent account.
	 */
	Account getParentAccount();

	/**
	 * Gets the currency of the account.
	 * @return The currency of the account.
	 */
	String getCurrency();

	/**
	 * Gets the opening balance of the account.
	 * @return The opening balance of the account.
	 */
	BigDecimal getOpeningBalance();
	
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * @param o The reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
	 */
	@Override boolean equals(java.lang.Object o);
	
	/**
	 * Returns a hash code value for the object.
	 * @return A hash code value for this object.
	 */
	@Override int hashCode();
	
	
	/**
	 * Returns a string representation of the object.
	 * @return A string representation of the object.
	 */
	@Override String toString();
	
	
}
