
package nonprofitbookkeeping.reports.datasource.scareports;

import java.util.List;

public class Income11bReport extends ScaRowBase {
	private String orgName;
	private String reportTitle;
	private List<Income11bRow> rows;
	
	public Income11bReport()
	{
		
	}
	
	public Income11bReport(String orgName, String reportTitle,
		List<Income11bRow> rows)
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
	
	public List<Income11bRow> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<Income11bRow> rows)
	{
		this.rows = rows;
		
	}
	
}
