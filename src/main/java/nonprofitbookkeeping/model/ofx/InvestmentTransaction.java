
package nonprofitbookkeeping.model.ofx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;


/**
 * Represents an investment transaction, extending the basic {@link Transaction} class.
 * This class is intended to hold details specific to investment activities such as
 * security information, quantity, price, and fees.
 * Note: Currently, all methods in this class are stub implementations.
 */
public class InvestmentTransaction extends Transaction
{
	
	/** The security node. */
	private final SecurityNode securityNode;
	
	/** The quantity. */
	private final BigDecimal quantity;
	
	/** The price. */
	private final BigDecimal price;
	
	/** The fees. */
	private final BigDecimal fees;
	
	/**
	 * Constructs an InvestmentTransaction with the given details.
	 *
	 * @param fitId            the financial institution transaction ID
	 * @param dtPosted         the posting date in yyyyMMdd format
	 * @param memo             a memo describing the transaction
	 * @param transactionName  the name of the transaction or security
	 * @param transactionType  the type of transaction (e.g. BUY or SELL)
	 * @param securityNode     the security involved
	 * @param quantity         how many units were transacted
	 * @param price            the price per unit
	 * @param fees             any fees applied
	 */
	public InvestmentTransaction(String fitId, String dtPosted, String memo, String transactionName,
		String transactionType, SecurityNode securityNode, BigDecimal quantity, BigDecimal price,
		BigDecimal fees)
	{
		super(transactionType, dtPosted, computeTotalAmount(quantity, price, fees), fitId, null,
			transactionName, memo);
		this.securityNode = securityNode;
		this.quantity = quantity;
		this.price = price;
		this.fees = fees;
	}
	
	/**
	 * Compute total amount.
	 *
	 * @param quantity the quantity
	 * @param price the price
	 * @param fees the fees
	 * @return the big decimal
	 */
	private static BigDecimal computeTotalAmount(	BigDecimal quantity, BigDecimal price,
													BigDecimal fees)
	{
		
		if (quantity == null || price == null)
		{
			return BigDecimal.ZERO;
		}
		
		BigDecimal total = quantity.multiply(price);
		
		if (fees != null)
		{
			total = total.add(fees);
		}
		
		return total;
	}
	
	/**
	 * Returns the security involved in this transaction.
	 *
	 * @return the security node
	 */
	public SecurityNode getSecurityNode()
	{
		return this.securityNode;
	}
	
	/**
	 * Returns the quantity of the security transacted.
	 *
	 * @return the quantity
	 */
	public BigDecimal getQuantity()
	{
		return this.quantity;
	}
	
	/**
	 * Returns the price per unit.
	 *
	 * @return the price
	 */
	public BigDecimal getPrice()
	{
		return this.price;
	}
	
	/**
	 * Returns any fees applied to the transaction.
	 *
	 * @return the fees
	 */
	public BigDecimal getFees()
	{
		return this.fees;
	}
	
	/**
	 * Calculates the total amount of the transaction for a given account by
	 * summing the totals of all {@link InvestmentTransaction} objects stored
	 * in that account's transaction list. If the account has no entries or
	 * none are investment transactions, {@link BigDecimal#ZERO} is returned.
	 *
	 * @param account the account
	 * @return the total
	 */
        public static BigDecimal getTotal(Account account)
        {
                if (account == null || account.getAccountNumber() == null
                        || account.getAccountNumber().isBlank())
                {
                        return BigDecimal.ZERO;
                }

                Ledger ledger = null;
                if (CurrentCompany.getCompany() != null)
                {
                        ledger = CurrentCompany.getCompany().getLedger();
                }

                if (ledger == null)
                {
                        return BigDecimal.ZERO;
                }

                List<AccountingEntry> entries = ledger.getEntriesForAccount(account.getAccountNumber());

                if (entries == null || entries.isEmpty())
                {
                        return BigDecimal.ZERO;
                }

                AccountSide naturalSide = account.getIncreaseSide();

                if (naturalSide == null)
                {
                        naturalSide = AccountSide.DEBIT;
                }
                BigDecimal total = BigDecimal.ZERO;

                for (AccountingEntry entry : entries)
                {
                        if (entry == null || entry.getAmount() == null)
                        {
                                continue;
                        }

                        BigDecimal amount = entry.getAmount();
                        AccountSide entrySide = entry.getAccountSide();

                        if (entrySide == null || naturalSide == null)
                        {
                                total = total.add(amount);
                        }
                        else if (entrySide == naturalSide)
                        {
                                total = total.add(amount);
                        }
                        else
                        {
                                total = total.subtract(amount);
                        }
                }

                return total.setScale(Math.max(total.scale(), 2), RoundingMode.HALF_UP);
        }
	
	/**
	 * Gets the total amount of this investment transaction calculated from
	 * quantity, price and fees.
	 *
	 * @return the total
	 */
	public BigDecimal getTotal()
	{
		return computeTotalAmount(this.quantity, this.price, this.fees);
	}
	
	
}
