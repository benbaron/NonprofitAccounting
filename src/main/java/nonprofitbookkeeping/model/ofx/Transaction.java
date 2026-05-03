
package nonprofitbookkeeping.model.ofx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


/**
 * Represents a banking transaction for OFX export.
 * <p>
 * The Transaction class is annotated for JAXB XML binding and contains
 * fields corresponding to OFX 2.0 elements such as TRNTYPE, DTPOSTED, TRNAMT,
 * FITID, CHECKNUM, NAME, and MEMO.
 * </p>
 */
@XmlAccessorType(XmlAccessType.FIELD) public class Transaction
{
	
	/** The trn type. */
	@XmlElement(name = "TRNTYPE") private String trnType;
	
	/** The dt posted. */
	@XmlElement(name = "DTPOSTED") private String dtPosted; // Expected in yyyyMMdd format.
	
	/** The trn amt. */
	@XmlElement(name = "TRNAMT") private BigDecimal trnAmt;
	
	/** The fit id. */
	@XmlElement(name = "FITID") private String fitId;
	
	/** The check num. */
	@XmlElement(name = "CHECKNUM") private String checkNum;
	
	/** The name. */
	@XmlElement(name = "NAME") private String name;
	
	/** The memo. */
	@XmlElement(name = "MEMO") private String memo;
	
	/**
	 * Instantiates a new transaction.
	 */
	// Default constructor for JAXB.
	public Transaction()
	{
	}
	
	/**
	 * Constructs a Transaction with the specified values.
	 *
	 * @param trnType  the transaction type (e.g., DEBIT, CREDIT).
	 * @param dtPosted the date posted in yyyyMMdd format.
	 * @param trnAmt   the transaction amount.
	 * @param fitId    the unique FITID.
	 * @param checkNum the check number (optional).
	 * @param name     the payee name.
	 * @param memo     the transaction memo.
	 */
	public Transaction(String trnType, String dtPosted, BigDecimal trnAmt, String fitId,
		String checkNum, String name, String memo)
	{
		this.trnType = trnType;
		this.dtPosted = dtPosted;
		this.trnAmt = trnAmt;
		this.fitId = fitId;
		this.checkNum = checkNum;
		this.name = name;
		this.memo = memo;
	}
	
	/**
	 * Returns the transaction type.
	 *
	 * @return the transaction type.
	 */
	public String getTransactionType()
	{
		return this.trnType;
	}
	
	/**
	 * Returns the date posted as a LocalDate.
	 *
	 * @return the LocalDate parsed from dtPosted.
	 */
	public LocalDate getLocalDate()
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		return LocalDate.parse(this.dtPosted, formatter);
	}
	
	/**
	 * Returns the transaction amount.
	 *
	 * @return the transaction amount as a BigDecimal.
	 */
	public BigDecimal getAmount()
	{
		return this.trnAmt;
	}
	
	/**
	 * Returns the FITID.
	 *
	 * @return the FITID string.
	 */
	public String getFitid()
	{
		return this.fitId;
	}
	
	/**
	 * Returns the check number.
	 *
	 * @return the check number or null if not available.
	 */
	public String getNumber()
	{
		return this.checkNum;
	}
	
	/**
	 * Returns the payee name.
	 *
	 * @return the payee name.
	 */
	public String getPayee()
	{
		return this.name;
	}
	
	/**
	 * Returns the transaction memo.
	 *
	 * @return the memo.
	 */
	public String getMemo()
	{
		return this.memo;
	}
	
	/**
	 * Generates a UUID from the FITID if present; otherwise, generates a random UUID.
	 *
	 * @return a UUID string.
	 */
	public String getUuid()
	{
		
		if (this.fitId != null && !this.fitId.isEmpty())
		{
			return UUID.nameUUIDFromBytes(this.fitId.getBytes()).toString();
		}
		
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Returns a list containing this transaction, useful for contexts where a list is required.
	 *
	 * @return a list containing this transaction.
	 */
	public List<Transaction> getTransactionEntries()
	{
		return List.of(this);
	}
	
}
