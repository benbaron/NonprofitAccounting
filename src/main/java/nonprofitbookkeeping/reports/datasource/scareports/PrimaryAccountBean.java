/**
 * NonprofitAccounting PrimaryAccountBean.java PrimaryAccountBean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.io.Serializable;
import java.math.BigDecimal;

public class PrimaryAccountBean implements Serializable
{
	
	/* aqua-fill “Deposit Date” rows */
	private String depositDate1;
	private String depositDate2;
	private String depositDate3;
	
	/* aqua-fill check number */
	private String checkNumber;
	
	/* formula output from C2 (sheetRef1) */
	private String sheetRef1;
	
	/* --- getters & setters --- */
	
	public String getDepositDate1()
	{
		return depositDate1;
		
	}
	
	public void setDepositDate1(String v)
	{
		this.depositDate1 = v;
		
	}
	
	public String getDepositDate2()
	{
		return depositDate2;
		
	}
	
	public void setDepositDate2(String v)
	{
		this.depositDate2 = v;
		
	}
	
	public String getDepositDate3()
	{
		return depositDate3;
		
	}
	
	public void setDepositDate3(String v)
	{
		this.depositDate3 = v;
		
	}
	
	public String getCheckNumber()
	{
		return checkNumber;
		
	}
	
	public void setCheckNumber(String v)
	{
		this.checkNumber = v;
		
	}
	
	public String getSheetRef1()
	{
		return sheetRef1;
		
	}
	
	public void setSheetRef1(String v)
	{
		this.sheetRef1 = v;
		
	}
	
}
