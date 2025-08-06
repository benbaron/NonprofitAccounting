
package nonprofitbookkeeping.reports.datasource.scareports;

import java.util.List;

public class Income11aReport
{
	private String orgName;
	private String reportTitle;
	private List<Income11aRow> rows;
	
	public Income11aReport()
	{
	
	}
	
	public Income11aReport(String orgName, String reportTitle, List<Income11aRow> rows)
	{
		this.orgName = orgName;
		this.reportTitle = reportTitle;
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
	
	public List<Income11aRow> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<Income11aRow> rows)
	{
		this.rows = rows;
		
	}
	
}
