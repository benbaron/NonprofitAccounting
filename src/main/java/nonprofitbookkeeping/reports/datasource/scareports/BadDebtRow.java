
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class BadDebtRow extends ScaRowBase {
	private String code;
	private String organizationOrPerson;
	private String reason;
	private BigDecimal amount;
	
	public BadDebtRow()
	{
	
	}
	
	public BadDebtRow(String code, String organizationOrPerson, String reason, BigDecimal amount)
	{
		this.code = code;
		this.organizationOrPerson = organizationOrPerson;
		this.reason = reason;
		this.amount = amount;
		
	}
	
	/**  
	 * Constructor BadDebtRow
	 * @param string
	 * @param string2
	 * @param bigDecimal
	 */
	public BadDebtRow(String string, String string2, BigDecimal bigDecimal)
	{
		// TODO Auto-generated constructor stub
		
	}

	public String getCode()
	{
		return code;
		
	}
	
	public void setCode(String code)
	{
		this.code = code;
		
	}
	
	public String getOrganizationOrPerson()
	{
		return organizationOrPerson;
		
	}
	
	public void setOrganizationOrPerson(String organizationOrPerson)
	{
		this.organizationOrPerson = organizationOrPerson;
		
	}
	
	public String getReason()
	{
		return reason;
		
	}
	
	public void setReason(String reason)
	{
		this.reason = reason;
		
	}
	
	public BigDecimal getAmount()
	{
		return amount;
		
	}
	
	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
		
	}
	
}
