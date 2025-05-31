package nonprofitbookkeeping.model.impex;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.webcohesion.ofx4j.domain.data.common.Currency;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportedTransaction {
    private LocalDate datePosted;
    private BigDecimal amount;
    private String description; // Payee or name of the transaction
    private String memo; // Additional memo or details
    private String transactionId; // e.g., FITID from OFX
    private String currency; // Optional, currency code
    private String originalAccountType; // e.g., "BANK", "CREDITCARD"
    private String originalAccountNumber; // Account number from the imported file
	/**  
	 * Constructor ImportedTransaction
	 * @param datePosted
	 * @param amount
	 * @param description
	 * @param memo
	 * @param transactionId
	 * @param currency
	 * @param originalAccountType
	 * @param originalAccountNumber
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
	 * Constructor ImportedTransaction
	 */
	public ImportedTransaction()
	{
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the datePosted
	 */
	public LocalDate getDatePosted()
	{
		return this.datePosted;
	}
	/**
	 * @param datePosted the datePosted to set
	 */
	public void setDatePosted(LocalDate datePosted)
	{
		this.datePosted = datePosted;
	}
	/**
	 * @return the amount
	 */
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}
	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return this.description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	/**
	 * @return the memo
	 */
	public String getMemo()
	{
		return this.memo;
	}
	/**
	 * @param memo the memo to set
	 */
	public void setMemo(String memo)
	{
		this.memo = memo;
	}
	/**
	 * @return the transactionId
	 */
	public String getTransactionId()
	{
		return this.transactionId;
	}
	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
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
	 * @return the originalAccountType
	 */
	public String getOriginalAccountType()
	{
		return this.originalAccountType;
	}
	/**
	 * @param originalAccountType the originalAccountType to set
	 */
	public void setOriginalAccountType(String originalAccountType)
	{
		this.originalAccountType = originalAccountType;
	}
	/**
	 * @return the originalAccountNumber
	 */
	public String getOriginalAccountNumber()
	{
		return this.originalAccountNumber;
	}
	/**
	 * @param originalAccountNumber the originalAccountNumber to set
	 */
	public void setOriginalAccountNumber(String originalAccountNumber)
	{
		this.originalAccountNumber = originalAccountNumber;
	}
	/**
	 * @param currency2
	 */
	public void setCurrency(Currency currency2)
	{
		this.currency = currency2.toString();
		
	}

}
