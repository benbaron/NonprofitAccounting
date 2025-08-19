/**
 * NonprofitAccounting AssetDtl5aRows.java AssetDtl5aRows
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;


public class AssetDtl5aRow implements SupplementalRecord
{
	private String label; // e.g., "a) Undeposited and Non-Interest Bearing Cash"
	private String fromPage; // e.g., "(2, 5a)" or descriptive source text
	private BigDecimal start;
	private BigDecimal end;
	private BigDecimal diff;
	
	public String getLabel()
	{
		return label;
		
	}
	
	public void setLabel(String label)
	{
		this.label = label;
		
	}
	
	public String getFromPage()
	{
		return fromPage;
		
	}
	
	public void setFromPage(String fromPage)
	{
		this.fromPage = fromPage;
		
	}
	
	public BigDecimal getStart()
	{
		return start;
		
	}
	
	public void setStart(BigDecimal start)
	{
		this.start = start;
		
	}
	
	public BigDecimal getEnd()
	{
		return end;
		
	}
	
	public void setEnd(BigDecimal end)
	{
		this.end = end;
		
	}
	
	public BigDecimal getDiff()
	{
		return diff;
		
	}
	
	public void setDiff(BigDecimal diff)
	{
		this.diff = diff;
		
	}
	
}
