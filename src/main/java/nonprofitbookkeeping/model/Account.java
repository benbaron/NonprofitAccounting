
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
	
	/* ─────────────────────────────────────────────── fields ──────────── */
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
	public Account(String accountNumber, String name, AccountSide increaseSide)
	{
		this.accountNumber = checkNotNull(accountNumber);
		this.name = name;
		this.increaseSide = increaseSide;
	}
	
	public Account()
	{
		/* default ctor for Jackson / JPA */ }
		
	/* ================= fund helpers =================================== */
	public void addFund(Fund fund)
	{
		
		if (!this.associatedFunds.contains(fund))
		{
			this.associatedFunds.add(fund);
			fund.addAccount(this);
		}
		
	}
	
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
	 * @return the associatedFunds
	 */
	public List<Fund> getAssociatedFunds()
	{
		return this.associatedFunds;
	}

	/**
	 * @param associatedFunds the associatedFunds to set
	 */
	public void setAssociatedFunds(List<Fund> associatedFunds)
	{
		this.associatedFunds = associatedFunds;
	}

	/**
	 * @return the accountNumber
	 */
	public String getAccountNumber()
	{
		return this.accountNumber;
	}

	/**
	 * @param accountNumber the accountNumber to set
	 */
	public void setAccountNumber(String accountNumber)
	{
		this.accountNumber = accountNumber;
	}

	/**
	 * @return the increaseSide
	 */
	public AccountSide getIncreaseSide()
	{
		return this.increaseSide;
	}

	/**
	 * @param increaseSide the increaseSide to set
	 */
	public void setIncreaseSide(AccountSide increaseSide)
	{
		this.increaseSide = increaseSide;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the accountCode
	 */
	public String getAccountCode()
	{
		return this.accountCode;
	}

	/**
	 * @param accountCode the accountCode to set
	 */
	public void setAccountCode(String accountCode)
	{
		this.accountCode = accountCode;
	}

	/**
	 * @return the accountType
	 */
	public AccountType getAccountType()
	{
		return this.accountType;
	}

	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(AccountType accountType)
	{
		this.accountType = accountType;
	}

	/**
	 * @return the parentAccount
	 */
	public Account getParentAccount()
	{
		return this.parentAccount;
	}

	/**
	 * @param parentAccount the parentAccount to set
	 */
	public void setParentAccount(Account parentAccount)
	{
		this.parentAccount = parentAccount;
	}

	/**
	 * @return the currency
	 */
	public String getCurrency()
	{
		return this.currency;
	}

	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	/**
	 * @return the openingBalance
	 */
	public BigDecimal getOpeningBalance()
	{
		return this.openingBalance;
	}

	/**
	 * @param openingBalance the openingBalance to set
	 */
	public void setOpeningBalance(BigDecimal openingBalance)
	{
		this.openingBalance = openingBalance;
	}

	

}
