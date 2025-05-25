/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * Grant.java
 * Grant
 */
package nonprofitbookkeeping.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * 
 */
public class Grant
{
	@JsonProperty String grantId = "";
	@JsonProperty String grantor = "";
	@JsonProperty BigDecimal amount = new BigDecimal(0);
	@JsonProperty String dateAwarded= "";
	@JsonProperty String purpose= "";
	@JsonProperty String status= "";
	
	
	/**  
	 * Constructor Grant
	 * @param grantId
	 * @param grantor
	 * @param amount
	 * @param dateAwarded
	 * @param purpose
	 * @param status
	 */
	public Grant(String grantId, String grantor, 
	             BigDecimal amount, String dateAwarded,
	             String purpose, String status)
	{
		this.grantId = grantId;
		this.grantor = grantor;
		this.amount = amount;
		this.dateAwarded = dateAwarded;
		this.purpose = purpose;
		this.status = status;
	}
	/**
	 * @return the grantId
	 */
	public String getGrantId()
	{
		return this.grantId;
	}
	/**
	 * @param grantId the grantId to set
	 */
	public void setGrantId(String grantId)
	{
		this.grantId = grantId;
	}
	/**
	 * @return the grantor
	 */
	public String getGrantor()
	{
		return this.grantor;
	}
	/**
	 * @param grantor the grantor to set
	 */
	public void setGrantor(String grantor)
	{
		this.grantor = grantor;
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
	 * @return the dateAwarded
	 */
	public String getDateAwarded()
	{
		return this.dateAwarded;
	}
	/**
	 * @param dateAwarded the dateAwarded to set
	 */
	public void setDateAwarded(String dateAwarded)
	{
		this.dateAwarded = dateAwarded;
	}
	/**
	 * @return the purpose
	 */
	public String getPurpose()
	{
		return this.purpose;
	}
	/**
	 * @param purpose the purpose to set
	 */
	public void setPurpose(String purpose)
	{
		this.purpose = purpose;
	}
	/**
	 * @return the status
	 */
	public String getStatus()
	{
		return this.status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status)
	{
		this.status = status;
	}

	
}
