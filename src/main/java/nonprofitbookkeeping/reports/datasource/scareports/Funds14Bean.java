
package nonprofitbookkeeping.reports.datasource.scareports;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple container bean for the FUNDS_14 report. Holds the collection of
 * {@link Funds14Row} entries representing each dedicated fund.
 */
public class Funds14Bean
{
	private List<Funds14Row> rows = new ArrayList<>();
	
	public List<Funds14Row> getRows()
	{
		return rows;
		
	}
	
	public void setRows(List<Funds14Row> rows)
	{
		this.rows = rows;
		
	}
	
}
