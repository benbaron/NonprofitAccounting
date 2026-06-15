
package nonprofitbookkeeping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;


/**
 * Represents an account with entries and a many-to-many relationship with
 * {@link Fund}s.
 */
public final class Account implements Serializable
{
	
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1149966185433260549L;
	
	/* ───────────────── fields ──────────── */
	/** The associated fund ids. */
	// Store fund references by ID to avoid circular serialization
	@JsonProperty private List<String> associatedFundIds = new ArrayList<>();
	
	/** The account number. */
	@JsonProperty private String accountNumber;
	
	/** The increase side. */
	@JsonProperty private AccountSide increaseSide;
	
	/** The name. */
	@JsonProperty private String name;
	
	/** The account code. */
	@JsonProperty private String accountCode;
	
	/** The account type. */
	@JsonProperty private AccountType accountType;
	
	/** The parent account id. */
	@JsonProperty private String parentAccountId;
	
	/** The currency. */
	@JsonProperty private String currency;
	
	/** The opening balance. */
	@JsonProperty private BigDecimal openingBalance = BigDecimal.ZERO;
	
	/** The supplemental line kinds. */
	@JsonProperty private List<SupplementalLineKind> supplementalLineKinds =
		new ArrayList<>();
	
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
		this.accountNumber = accountNumber;
		this.name = name;
		this.increaseSide = increaseSide;
		
	}
	
	
	/**
	 * Constructor Account.
	 *
	 * @param string the string
	 * @param bankAccountName the bank account name
	 * @param asset the asset
	 * @param zero the zero
	 */
	public Account(String string, String bankAccountName,
		AccountType asset, BigDecimal zero)
	{
		this.accountNumber = string;
		this.name = bankAccountName;
		this.accountType = asset;
		this.openingBalance = zero;
		this.increaseSide = defaultIncreaseSide(asset);
		
	}
	
	/* ================= fund helpers =================================== */
	
	/**
	 * Associates a fund with this account.
	 * If the fund is not already associated, it adds the fund to this account's list
	 * of associated funds and adds this account to the fund's list of associated accounts.
	 *
	 * @param fundId the fund id
	 */
	public void addFund(String fundId)
	{
		if (fundId == null)
			return;
		
		if (!this.associatedFundIds.contains(fundId))
		{
			this.associatedFundIds.add(fundId);
		}
		
	}
	
	/**
	 * Disassociates a fund from this account.
	 * Removes the fund from this account's list of associated funds and
	 * removes this account from the fund's list of associated accounts.
	 *
	 * @param fundId the fund id
	 */
	public void removeFund(String fundId)
	{
		if (fundId == null)
			return;
		this.associatedFundIds.remove(fundId);
		
	}
	
	/* =================== IMPLEMENTED METHODS ========================== */
	
	/**
	 * Calculates the account’s balance by summing its opening balance and
	 * all {@link AccountingEntry} records for this account in the provided
	 * {@link Ledger}.
	 *
	 * @param ledger the ledger containing transactions and entries. If
	 *                {@code null}, only the opening balance is returned
	 * @return the calculated balance as a {@link BigDecimal}
	 */
	public BigDecimal totalAccountBalance(Ledger ledger)
	{
		
		if (ledger == null)
		{
			return this.openingBalance == null ? BigDecimal.ZERO :
				this.openingBalance;
		}
		
		List<AccountingEntry> entries =
			ledger.getEntriesForAccount(this.accountNumber);
		return ReportService.calculateBalanceForAccount(this, entries);
		
	}
	
	/**
	 * Checks for parent.
	 *
	 * @return {@code true} if this account is a child of another.
	 */
	public boolean hasParent()
	{
		return this.parentAccountId != null && !this.parentAccountId.isBlank();
		
	}
	
	/**
	 * Returns a one-element collection containing this account.  The method is
	 * here mainly for compatibility with map-like code that expects a
	 * {@code Collection<Account>}.  If you later store sub-accounts directly
	 * inside the class, change this to return an unmodifiable view of that list.
	 *
	 * @return the collection
	 */
	public Collection<Account> values()
	{
		return Collections.singleton(this);
		
	}
	
	/**
	 * Gets the list of funds associated with this account.
	 * @return The list of associated funds.
	 */
	public List<String> getAssociatedFundIds()
	{
		return this.associatedFundIds;
		
	}
	
	/**
	 * Sets the list of fund identifiers associated with this account.
	 * @param associatedFundIds The list of fund IDs to associate.
	 */
	public void setAssociatedFundIds(List<String> associatedFundIds)
	{
		this.associatedFundIds = associatedFundIds;
		
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
	 * Determines the effective increase side for this account.
	 * If an explicit increase side has been set, it is returned.
	 * Otherwise the side is derived from the account type and falls
	 * back to {@link AccountSide#DEBIT} when no type mapping exists.
	 *
	 * @return the effective increase side, never {@code null}
	 */
	public AccountSide getEffectiveIncreaseSide()
	{
		
		if (this.increaseSide != null)
		{
			return this.increaseSide;
		}
		
		AccountSide derived = defaultIncreaseSide(this.accountType);
		
		if (derived != null)
		{
			return derived;
		}
		
		return AccountSide.DEBIT;
		
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
	public String getParentAccountId()
	{
		return this.parentAccountId;
		
	}
	
	/**
	 * Sets the parent account of this account using its account number.
	 * @param parentAccountId The parent account number to set.
	 */
	public void setParentAccountId(String parentAccountId)
	{
		
		if (parentAccountId != null)
		{
			this.parentAccountId = parentAccountId;
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

	/**
	 * Returns the supplemental line kinds associated with this account.
	 *
	 * @return list of supplemental line kinds (never null)
	 */
	public List<SupplementalLineKind> getSupplementalLineKinds()
	{
		return this.supplementalLineKinds;
		
	}

	/**
	 * Sets the supplemental line kinds associated with this account.
	 *
	 * @param supplementalLineKinds kinds to set
	 */
	public void setSupplementalLineKinds(
		List<SupplementalLineKind> supplementalLineKinds)
	{
		this.supplementalLineKinds =
			supplementalLineKinds == null ? new ArrayList<>() :
				new ArrayList<>(supplementalLineKinds);
		
	}
	
	/**
	 * Default increase side.
	 *
	 * @param type the type
	 * @return the account side
	 */
	private static AccountSide defaultIncreaseSide(AccountType type)
	{
		
		if (type == null)
		{
			return null;
		}
		
		return switch(type)
		{
			case
				ASSET, BANK, CASH, CHECKING, EXPENSE, INVEST, SIMPLEINVEST,
				MONEYMKRT, MUTUAL, FIXED_ASSET -> AccountSide.DEBIT;
			case
				CREDIT, EQUITY, INCOME, LIABILITY, LONG_TERM_LIABILITY,
				CREDITCARD -> AccountSide.CREDIT;
			case ROOT, UNKNOWN -> null;
		};
		
	}
	
}
