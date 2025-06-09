package nonprofitbookkeeping.model.ofx;

import java.math.BigDecimal;
import nonprofitbookkeeping.model.Account;

/**
 * Represents an investment transaction, extending the basic {@link Transaction} class.
 * This class is intended to hold details specific to investment activities such as
 * security information, quantity, price, and fees.
 * Note: Currently, all methods in this class are stub implementations.
 */
public class InvestmentTransaction extends Transaction
{

	/**  
	 * Constructs an InvestmentTransaction.
	 * Note: The body of this constructor is currently a stub and does not initialize fields.
	 * This constructor is intended to set up an investment transaction with its specific details.
	 *
	 * @param fitId The financial institution transaction ID.
	 * @param dtPosted The date the transaction was posted.
	 * @param memo A memo or description for the transaction.
	 * @param transactionName The name of the transaction (e.g., security name or action).
	 * @param transactionType The type of transaction (e.g., BUY, SELL).
	 * @param securityNode Information about the security involved in the transaction.
	 * @param quantity The quantity of the security transacted.
	 * @param price The price per unit of the security.
	 * @param fees Any fees associated with the transaction.
	 */
	public InvestmentTransaction(String fitId, String dtPosted, String memo, String transactionName,
		String transactionType, SecurityNode securityNode, BigDecimal quantity, BigDecimal price,
		BigDecimal fees)
	{
		// TODO Auto-generated constructor stub
		// super(fitId, dtPosted, memo, transactionName, transactionType); // Example call to super
		// Initialize investment-specific fields here, e.g.:
		// this.securityNode = securityNode;
		// this.quantity = quantity;
		// this.price = price;
		// this.fees = fees;
	}

	/**
	 * Gets information about the security involved in this transaction.
	 * Note: This is a stub implementation and currently returns null.
	 * @return The {@link SecurityNode} associated with this transaction, or null if not implemented.
	 */
	public static SecurityNode getSecurityNode()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the quantity of the security transacted.
	 * Note: This is a stub implementation and currently returns null.
	 * @return The quantity as a {@link BigDecimal}, or null if not implemented.
	 */
	public static BigDecimal getQuantity()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the price per unit of the security.
	 * Note: This is a stub implementation and currently returns null.
	 * @return The price as a {@link BigDecimal}, or null if not implemented.
	 */
	public static BigDecimal getPrice()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets any fees associated with the transaction.
	 * Note: This is a stub implementation and currently returns null.
	 * @return The fees as a {@link BigDecimal}, or null if not implemented.
	 */
	public static BigDecimal getFees()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Calculates the total amount of the transaction for a given account.
	 * Note: This is a stub implementation and currently returns null.
	 * The calculation logic would depend on the transaction type (buy/sell), quantity, price, and fees.
	 * @param account The account for which to calculate the total. (Currently unused in stub)
	 * @return The total transaction amount as a {@link BigDecimal}, or null if not implemented.
	 */
	public static BigDecimal getTotal(Account account)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets the total amount of this investment transaction.
	 * This might be calculated as (quantity * price) +/- fees, depending on the transaction type.
	 * Note: This is a stub implementation and currently returns null.
	 * @return The total amount as a {@link BigDecimal}, or null if not implemented.
	 */
	public BigDecimal getTotal()
	{
		// TODO Auto-generated method stub
		return null;
	}


	
}
