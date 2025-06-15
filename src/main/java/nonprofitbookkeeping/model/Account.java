
package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import static com.google.common.base.Preconditions.checkNotNull;
/**
 * Represents an account with entries and a many-to-many relationship with
 * {@link Fund}s.
 */
public final class Account implements Serializable
{
	


	private static final long serialVersionUID = -1149966185433260549L;
	
	/* ───────────────── fields ──────────── */
	@JsonProperty private List<Fund> associatedFunds = new ArrayList<>();
	@JsonProperty private String accountNumber;
	@JsonProperty private AccountSide increaseSide;
	@JsonProperty private String name;
	@JsonProperty private String accountCode;
	@JsonProperty private AccountType accountType;
	@JsonProperty private Account parentAccount;
	@JsonProperty private String currency;
	@JsonProperty private BigDecimal openingBalance = BigDecimal.ZERO;
	
	/* ------------------------------------------------------------------ */
	/**
	 * Default constructor for Jackson deserialization or JPA.
	 */
	public Account()
	{
		/* default ctor for Jackson / JPA */ 
	}
	
	/**
	 * Constructs an Account with the specified account number, name, and increase side.
	 * @param accountNumber The account number. Must not be null.
	 * @param name The name of the account.
	 * @param increaseSide The side (Debit or Credit) where the account balance increases.
	 * @throws NullPointerException if accountNumber is null.
	 */
	public Account(String accountNumber, String name, AccountSide increaseSide)
	{
		this.accountNumber = checkNotNull(accountNumber);
		this.name = name;
		this.increaseSide = increaseSide;
	}
	


	/**  
	 * Constructor Account
	 * @param string
	 * @param bankAccountName
	 * @param asset
	 * @param zero
	 */
	public Account(String string, String bankAccountName, 
	               AccountType asset, BigDecimal zero)
	{
		// TODO Auto-generated constructor stub
	}

	/* ================= fund helpers =================================== */
	
	/**
	 * Associates a fund with this account.
	 * If the fund is not already associated, it adds the fund to this account's list
	 * of associated funds and adds this account to the fund's list of associated accounts.
	 * @param fund The fund to associate with this account.
	 */
	public void addFund(Fund fund)
	{
		
		if (!this.associatedFunds.contains(fund))
		{
			this.associatedFunds.add(fund);
			fund.addAccount(this);
		}
		
	}
	
	/**
	 * Disassociates a fund from this account.
	 * Removes the fund from this account's list of associated funds and
	 * removes this account from the fund's list of associated accounts.
	 * @param fund The fund to disassociate from this account.
	 */
	public void removeFund(Fund fund)
	{
		this.associatedFunds.remove(fund);
		fund.removeAccount(this);
	}
	
	/* =================== IMPLEMENTED METHODS ========================== */
	
	/** Calculates the account’s balance. */
	public BigDecimal totalAccountBalance()
	{
		// In the current model we only know the opening balance. If you later
		// attach transactions or sub-accounts, extend this method to include them.
		return this.openingBalance == null ? BigDecimal.ZERO : this.openingBalance;
	}
	
	/** @return {@code true} if this account is a child of another. */
	public boolean hasParent()
	{
		return this.parentAccount != null;
	}

	/**
	 * Returns a one-element collection containing this account.  The method is
	 * here mainly for compatibility with map-like code that expects a
	 * {@code Collection<Account>}.  If you later store sub-accounts directly
	 * inside the class, change this to return an unmodifiable view of that list.
	 */
	public Collection<Account> values()
	{
		return Collections.singleton(this);
	}

	/**
	 * Gets the list of funds associated with this account.
	 * @return The list of associated funds.
	 */
	public List<Fund> getAssociatedFunds()
	{
		return this.associatedFunds;
	}

	/**
	 * Sets the list of funds associated with this account.
	 * @param associatedFunds The list of funds to associate.
	 */
	public void setAssociatedFunds(List<Fund> associatedFunds)
	{
		this.associatedFunds = associatedFunds;
	}

	/**
	 * Gets the account number.
	 * @return The account number.
	 */
	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	/**
	 * Sets the account number.
	 * @param accountNumber The account number to set.
	 */
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	/**
	 * Gets the side (Debit or Credit) where the account balance increases.
	 * @return The increase side of the account.
	 */
	public AccountSide getIncreaseSide()
	{
		return this.increaseSide;
	}

	/**
	 * Sets the side (Debit or Credit) where the account balance increases.
	 * @param increaseSide The increase side to set.
	 */
	public void setIncreaseSide(AccountSide increaseSide)
	{
		this.increaseSide = increaseSide;
	}

	/**
	 * Gets the name of the account.
	 * @return The name of the account.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Sets the name of the account.
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the account code.
	 * @return The account code.
	 */
	public String getAccountCode()
	{
		return this.accountCode;
	}

	/**
	 * Sets the account code.
	 * @param accountCode The account code to set.
	 */
	public void setAccountCode(String accountCode)
	{
		this.accountCode = accountCode;
	}

	/**
	 * Gets the type of the account.
	 * @return The account type.
	 */
	public AccountType getAccountType()
	{
		return this.accountType;
	}

	/**
	 * Sets the type of the account.
	 * @param accountType The account type to set.
	 */
	public void setAccountType(AccountType accountType)
	{
		this.accountType = accountType;
	}

	/**
	 * Gets the parent account of this account, if any.
	 * @return The parent account, or null if this is a top-level account.
	 */
	public Account getParentAccount()
	{
		return this.parentAccount;
	}

	/**
	 * Sets the parent account of this account.
	 * @param parentAccount The parent account to set.
	 */
	public void setParentAccount(Account parentAccount)
	{
                if (parentAccount != null)
                {
                        this.parentAccount = parentAccount;
                }
		
	}

	/**
	 * Gets the currency of the account.
	 * @return The currency code (e.g., "USD").
	 */
	public String getCurrency()
	{
		return this.currency;
	}

	/**
	 * Sets the currency of the account.
	 * @param currency The currency code to set (e.g., "USD").
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	/**
	 * Gets the opening balance of the account.
	 * @return The opening balance.
	 */
	public BigDecimal getOpeningBalance()
	{
		return this.openingBalance;
	}

	/**
	 * Sets the opening balance of the account.
	 * @param openingBalance The opening balance to set.
	 */
	public void setOpeningBalance(BigDecimal openingBalance)
	{
		this.openingBalance = openingBalance;
	}

        // The following child account helpers were removed. The application
        // manages parent/child relationships solely via {@link #parentAccount}
        // and the {@code ChartOfAccounts} container.
}
