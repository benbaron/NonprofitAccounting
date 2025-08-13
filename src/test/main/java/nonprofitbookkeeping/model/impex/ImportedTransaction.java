
package nonprofitbookkeeping.model.impex;


import java.math.BigDecimal;
import java.time.LocalDate;

import com.webcohesion.ofx4j.domain.data.common.Currency;

/**
 * Represents a transaction imported from an external source, such as an OFX file.
 * This class holds the raw details of the transaction as imported, before it is
 * processed and converted into the application's internal transaction format.
 */
public class ImportedTransaction
{
	/** The date the transaction was posted. */
	private LocalDate datePosted;
	/** The monetary amount of the transaction. */
	private BigDecimal amount;
	/** The description of the transaction, often representing the payee or a general name. */
	private String description; // Payee or name of the transaction
	/** Additional memo or details associated with the transaction. */
	private String memo; // Additional memo or details
	/** The unique identifier for the transaction from the original source (e.g., FITID from OFX). */
	private String transactionId; // e.g., FITID from OFX
	/** The currency code of the transaction amount (e.g., "USD"). Optional. */
	private String currency; // Optional, currency code
	/** The type of account from which the transaction was imported (e.g., "BANK", "CREDITCARD"). */
	private String originalAccountType; // e.g., "BANK", "CREDITCARD"
	/** The account number from the imported file. */
	private String originalAccountNumber; // Account number from the imported file
	
	/**
	 * Default constructor.
	 * Initializes all fields to their default values (null for objects, 0 for primitives if any).
	 */
	public ImportedTransaction()
	{
		
	}
	/**  
	 * Constructs an ImportedTransaction with all its details.
	 * @param datePosted The date the transaction was posted.
	 * @param amount The monetary amount of the transaction.
	 * @param description The primary description or payee of the transaction.
	 * @param memo Additional memo or details for the transaction.
	 * @param transactionId The unique ID of the transaction from its original source.
	 * @param currency The currency code of the transaction amount.
	 * @param originalAccountType The type of the account from the source file (e.g., "BANK").
	 * @param originalAccountNumber The account number from the source file.
	 */
	public ImportedTransaction(LocalDate datePosted, BigDecimal amount, String description,
		String memo, String transactionId, String currency, String originalAccountType,
		String originalAccountNumber)
	{
		this.datePosted = datePosted;
		this.amount = amount;
		this.description = description;
		this.memo = memo;
		this.transactionId = transactionId;
		this.currency = currency;
		this.originalAccountType = originalAccountType;
		this.originalAccountNumber = originalAccountNumber;
	}

	/**
	 * Gets the date when the transaction was posted.
	 * @return The posting date.
	 */
	public LocalDate getDatePosted()
	{
		return this.datePosted;
	}
	
	/**
	 * Sets the date when the transaction was posted.
	 * @param datePosted The posting date to set.
	 */
	public void setDatePosted(LocalDate datePosted)
	{
		this.datePosted = datePosted;
	}
	
	/**
	 * Gets the monetary amount of the transaction.
	 * @return The transaction amount.
	 */
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	/**
	 * Sets the monetary amount of the transaction.
	 * @param amount The transaction amount to set.
	 */
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	
	/**
	 * Gets the primary description of the transaction (often the payee).
	 * @return The transaction description.
	 */
	public String getDescription()
	{
		return this.description;
	}
	
	/**
	 * Sets the primary description of the transaction.
	 * @param description The transaction description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Gets the additional memo or details for the transaction.
	 * @return The transaction memo.
	 */
	public String getMemo()
	{
		return this.memo;
	}
	
	/**
	 * Sets the additional memo or details for the transaction.
	 * @param memo The transaction memo to set.
	 */
	public void setMemo(String memo)
	{
		this.memo = memo;
	}
	
	/**
	 * Gets the unique transaction identifier from the original source.
	 * @return The transaction ID (e.g., FITID).
	 */
	public String getTransactionId()
	{
		return this.transactionId;
	}
	
	/**
	 * Sets the unique transaction identifier from the original source.
	 * @param transactionId The transaction ID to set.
	 */
	public void setTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
	}
	
	/**
	 * Gets the currency code of the transaction.
	 * @return The currency code (e.g., "USD"), or null if not set.
	 */
	public String getCurrency()
	{
		return this.currency;
	}
	
	/**
	 * Sets the currency code of the transaction.
	 * @param currency The currency code to set (e.g., "USD").
	 */
	public void setCurrency(String currency)
	{
		this.currency = currency;
	}
	
	/**
	 * Gets the original account type from the imported file (e.g., "BANK", "CREDITCARD").
	 * @return The original account type.
	 */
	public String getOriginalAccountType()
	{
		return this.originalAccountType;
	}
	
	/**
	 * Sets the original account type from the imported file.
	 * @param originalAccountType The original account type to set.
	 */
	public void setOriginalAccountType(String originalAccountType)
	{
		this.originalAccountType = originalAccountType;
	}
	
	/**
	 * Gets the original account number from the imported file.
	 * @return The original account number.
	 */
	public String getOriginalAccountNumber()
	{
		return this.originalAccountNumber;
	}
	
	/**
	 * Sets the original account number from the imported file.
	 * @param originalAccountNumber The original account number to set.
	 */
	public void setOriginalAccountNumber(String originalAccountNumber)
	{
		this.originalAccountNumber = originalAccountNumber;
	}
	
	/**
	 * Sets the currency of the transaction using an OFX4J {@link Currency} object.
	 * The currency code is stored as a string.
	 * @param currencyInstance The {@link Currency} object from which to set the currency. If null, the behavior depends on {@code toString()}.
	 */
	public void setCurrency(Currency currencyInstance)
	{
		this.currency = (currencyInstance != null) ? currencyInstance.toString() : null;
		
	}
	
}
