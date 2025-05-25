/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * JournalEntry.java
 * JournalEntry
 */
package nonprofitbookkeeping.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntry
{
	@JsonProperty private String id;
	@JsonProperty private String date;
	@JsonProperty private BigDecimal debit;
    @JsonProperty private BigDecimal credit;
    @JsonProperty private String memo;
    @JsonProperty private String account;
	
	
	/**  
	 * Constructor JournalEntry
	 * @param id
	 * @param string
	 * @param value
	 * @param debit
	 * @param credit
	 * @param memo
	 */
	public JournalEntry(String id, 
	                    String date, 
	                    String value, 
	                    BigDecimal debit, 
	                    BigDecimal credit,
	                    String text)
	{
		this.id = id;
		this.date = date;
		this.debit = debit;
		this.credit = credit;
		this.memo = text;
	}

	/**
	 * @return
	 */
	public String getId()
	{
		return this.id;
	}

	/**
	 * @return
	 */
	public String getDate()
	{
		return this.date;
	}

	/**
	 * @return
	 */
	public String getAccount()
	{
		return this.account;
	}

	/**
	 * @return
	 */
	public BigDecimal getDebit()
	{
		return this.debit;
	}

	/**
	 * @return
	 */
	public BigDecimal getCredit()
	{
		return this.credit;
	}

	/**
	 * @return
	 */
	public String getMemo()
	{
		return this.memo;
	}
	
}
