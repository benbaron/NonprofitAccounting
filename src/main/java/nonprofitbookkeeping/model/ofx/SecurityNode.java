package nonprofitbookkeeping.model.ofx;

/**
 * Represents a security, typically identified by an ISIN (International Securities Identification Number)
 * and a ticker symbol. This class stores these identifiers.
 */
public class SecurityNode
{
	/**
	 * The International Securities Identification Number (ISIN) of the security.
	 */
	private String isin;

	/**
	 * The ticker symbol of the security.
	 */
	private String symbol;

	/**
	 * Constructs a new SecurityNode with the specified ISIN and symbol.
	 *
	 * @param isin The ISIN of the security.
	 * @param symbol The ticker symbol of the security.
	 */
	public SecurityNode(String isin, String symbol) {
		this.isin = isin;
		this.symbol = symbol;
	}

	/**
	 * Gets the ISIN (International Securities Identification Number) of the security.
	 *
	 * @return The ISIN of the security.
	 */
	public String getISIN()
	{
		return this.isin;
	}

	/**
	 * Gets the ticker symbol of the security.
	 *
	 * @return The ticker symbol of the security.
	 */
	public String getSymbol()
	{
		return this.symbol;
	}
	
}
