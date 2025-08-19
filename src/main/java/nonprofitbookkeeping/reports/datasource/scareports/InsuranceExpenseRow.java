
package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;


public class InsuranceExpenseRow implements SupplementalRecord
{
	private String organizationOrPerson;
	private String checkNumber;
	private String checkDate;
	private BigDecimal amount;
	
	public InsuranceExpenseRow()
	{
	
	}
	
	public InsuranceExpenseRow(String organizationOrPerson, String checkNumber, String checkDate,
			BigDecimal amount)
	{
		this.organizationOrPerson = organizationOrPerson;
		this.checkNumber = checkNumber;
		this.checkDate = checkDate;
		this.amount = amount;
		
	}
	
	/**  
	 * Constructor InsuranceExpenseRow
	 * @param string
	 * @param string2
	 * @param bigDecimal
	 */
	public InsuranceExpenseRow(String string, String string2, BigDecimal bigDecimal)
	{
		// TODO Auto-generated constructor stub
		
	}

	public String getOrganizationOrPerson()
	{
		return organizationOrPerson;
		
	}
	
	public void setOrganizationOrPerson(String organizationOrPerson)
	{
		this.organizationOrPerson = organizationOrPerson;
		
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
		
	}
	
	public void setCheckNumber(String checkNumber)
	{
		this.checkNumber = checkNumber;
		
	}
	
	public String getCheckDate()
	{
		return checkDate;
		
	}
	
	public void setCheckDate(String checkDate)
	{
		this.checkDate = checkDate;
		
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
