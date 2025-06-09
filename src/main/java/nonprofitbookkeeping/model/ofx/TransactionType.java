package nonprofitbookkeeping.model.ofx;

/**
 * Defines the various types of financial transactions, particularly relevant for
 * investment activities or specific accounting entries.
 */
public enum TransactionType
{
	/** Represents an unspecified or null transaction type. */
	NONE, 
	/** Represents a transaction for adding shares to an account (e.g., stock split, transfer in). */
	ADDSHARE,
	/** Represents a transaction for purchasing shares. */
	BUYSHARE,
	/** Represents a transaction for removing shares from an account (e.g., reverse stock split, transfer out). */
	REMOVESHARE,
	/** Represents a transaction for selling shares. */
	SELLSHARE,
	/** Represents a dividend payment received. */
	DIVIDEND,
	/** Represents a transaction where dividends are reinvested to purchase more shares. */
	REINVESTDIV,
	/** Represents a double-entry bookkeeping transaction, typically involving balancing entries. */
	DOUBLEENTRY
	
}
