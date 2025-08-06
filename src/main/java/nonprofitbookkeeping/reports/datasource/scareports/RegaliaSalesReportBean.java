// RegaliaSalesReportBean.java

package nonprofitbookkeeping.reports.datasource.scareports;

import java.util.List;

public class RegaliaSalesReportBean
{
	private String orgName;
	private String reportTitle;
	private String contactInfo;
	private List<RegaliaSalesRow> rows;
	
	public RegaliaSalesReportBean()
	{
	
	}
	
	public RegaliaSalesReportBean(String orgName, String reportTitle, String contactInfo,
			List<RegaliaSalesRow> rows)
	{
		this.orgName = orgName;
		this.reportTitle = reportTitle;
		this.contactInfo = contactInfo;
		this.rows = rows;
		
	}
	
	public String getOrgName()
	{
		return orgName;
		
	}
	
	public void setOrgName(String orgName)
	{
		this.orgName = orgName;
		
	}
	
	public String getReportTitle()
	{
		return reportTitle;
		
	}
	
	public void setReportTitle(String reportTitle)
	{
		this.reportTitle = reportTitle;
		
	}
	
	public String getContactInfo()
	{
		return contactInfo;
		
	}
	
	public void setContactInfo(String contactInfo)
	{
		this.contactInfo = contactInfo;
		
	}
	
	public List<RegaliaSalesRow> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<RegaliaSalesRow> rows)
	{
		this.rows = rows;
		
	}
	
}
