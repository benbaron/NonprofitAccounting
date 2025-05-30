package nonprofitbookkeeping.model.ofx;

import java.math.BigDecimal;
// Removed: import nonprofitbookkeeping.model.Account; // Not used in this revised version

/**
 * Represents an investment transaction, extending the generic {@link Transaction} class
 * with details specific to security trades, such as security information, quantity, price, and fees.
 */
public class InvestmentTransaction extends Transaction
{
	private SecurityNode securityNode;
	private BigDecimal quantity;
	private BigDecimal price;
	private BigDecimal fees;

	/**
	 * Constructs a new InvestmentTransaction.
	 *
	 * @param fitId The unique identifier for this transaction (maps to FITID in OFX).
	 * @param dtPosted The date the transaction was posted, in "yyyyMMdd" format.
	 * @param memo A memo or description for the transaction.
	 * @param transactionName A name for the transaction, often the security name or a general description.
	 * @param transactionType The type of transaction (e.g., "BUY", "SELL", "DIVIDEND"), maps to TRNTYPE in OFX.
	 * @param securityNode The {@link SecurityNode} representing the security involved.
	 * @param quantity The quantity of the security transacted.
	 * @param price The price per unit of the security.
	 * @param fees Any fees associated with the transaction.
	 */
	public InvestmentTransaction(
			String fitId,
			String dtPosted,
			String memo,
			String transactionName,
			String transactionType,
			SecurityNode securityNode,
			BigDecimal quantity,
			BigDecimal price,
			BigDecimal fees) {
		
		// Calculate the 'trnAmt' for the superclass.
		// This interpretation assumes 'trnAmt' is the net cash effect or principal value.
		// For a BUY, this might be negative. For a SELL, positive.
		// The getTotal() method calculates (quantity * price) - fees.
		// Let's assume transactionType ("BUY", "SELL") will determine the sign for trnAmt if needed,
		// or trnAmt is always positive and trnType defines the cash flow direction.
		// For simplicity, passing the result of getTotal() as trnAmt.
		// A more nuanced approach might be needed depending on OFX conventions for specific trnType.
		BigDecimal calculatedSuperAmount;
		if (quantity != null && price != null) {
			calculatedSuperAmount = quantity.multiply(price);
			BigDecimal transactionFees = (fees == null) ? BigDecimal.ZERO : fees;
			calculatedSuperAmount = calculatedSuperAmount.subtract(transactionFees); // Or .add for cost basis of a buy
		} else {
			calculatedSuperAmount = BigDecimal.ZERO; 
		}
		// The parent constructor is: Transaction(String trnType, String dtPosted, BigDecimal trnAmt, String fitId, String checkNum, String name, String memo)
		super(transactionType, dtPosted, calculatedSuperAmount, fitId, null /* checkNum */, transactionName, memo);

		this.securityNode = securityNode;
		this.quantity = quantity;
		this.price = price;
		this.fees = fees;
	}

	/**
	 * Gets the security node associated with this investment transaction.
	 * @return The {@link SecurityNode} containing ISIN and symbol.
	 */
	public SecurityNode getSecurityNode()
	{
		return this.securityNode;
	}

	/**
	 * Gets the quantity of the security transacted.
	 * @return The quantity as a {@link BigDecimal}.
	 */
	public BigDecimal getQuantity()
	{
		return this.quantity;
	}

	/**
	 * Gets the price per unit of the security.
	 * @return The price as a {@link BigDecimal}.
	 */
	public BigDecimal getPrice()
	{
		return this.price;
	}

	/**
	 * Gets any fees associated with the transaction.
	 * @return The fees as a {@link BigDecimal}, or null if no fees.
	 */
	public BigDecimal getFees()
	{
		return this.fees;
	}

	/**
	 * Calculates the total value of the transaction, typically (quantity * price) - fees.
	 * This represents the net proceeds for a sale or the gross cost before fees for a buy,
	 * depending on context. The sign might need adjustment based on transaction type (BUY/SELL)
	 * for cash flow representation.
	 *
	 * @return The calculated total as a {@link BigDecimal}. Returns {@code null} if
	 *         quantity or price is null, as a meaningful total cannot be determined.
	 */
	public BigDecimal getTotal()
	{
		if (this.quantity == null || this.price == null) {
			return null; // Or BigDecimal.ZERO, depending on desired behavior for incomplete data
		}
		BigDecimal grossAmount = this.quantity.multiply(this.price);
		BigDecimal transactionFees = (this.fees == null) ? BigDecimal.ZERO : this.fees;
		return grossAmount.subtract(transactionFees);
	}
}
