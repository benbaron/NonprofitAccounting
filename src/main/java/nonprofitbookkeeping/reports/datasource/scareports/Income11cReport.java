
package nonprofitbookkeeping.reports.datasource.scareports;

import java.util.List;

public class Income11cReport implements SupplementalRecord
{
	private String orgName;
	private String reportTitle;
	private List<Income11cRow> rows;
	
	public Income11cReport()
	{
		
	}
	
	public Income11cReport(String orgName, String reportTitle,
		List<Income11cRow> rows)
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
	
	public List<Income11cRow> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<Income11cRow> rows)
	{
		this.rows = rows;
		
	}
	
}
