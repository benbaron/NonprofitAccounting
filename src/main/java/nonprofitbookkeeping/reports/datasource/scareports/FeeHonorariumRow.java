
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

public class FeeHonorariumRow extends ScaRowBase {
	private String code;
	private String organizationOrPerson;
	private String serviceProvided;
	private BigDecimal amount;
	
	public FeeHonorariumRow()
	{
	
	}
	
	public FeeHonorariumRow(String code, String organizationOrPerson, String serviceProvided,
			BigDecimal amount)
	{
		this.code = code;
		this.organizationOrPerson = organizationOrPerson;
		this.serviceProvided = serviceProvided;
		this.amount = amount;
		
	}
	
	/**  
	 * Constructor FeeHonorariumRow
	 * @param string
	 * @param string2
	 * @param bigDecimal
	 */
	public FeeHonorariumRow(String string, String string2, BigDecimal bigDecimal)
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
	
	public String getServiceProvided()
	{
		return serviceProvided;
		
	}
	
	public void setServiceProvided(String serviceProvided)
	{
		this.serviceProvided = serviceProvided;
		
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
