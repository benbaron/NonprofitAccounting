
package nonprofitbookkeeping.reports.datasource.scareports;

import java.util.List;

public class TransferIn9Report
{
	private String orgName;
	private String reportTitle;
	private List<TransferIn9Row> rows;
	
	public TransferIn9Report()
	{
	
	}
	
	public TransferIn9Report(String orgName, String reportTitle, List<TransferIn9Row> rows)
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
	
	public List<TransferIn9Row> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<TransferIn9Row> rows)
	{
		this.rows = rows;
		
	}
	
}
