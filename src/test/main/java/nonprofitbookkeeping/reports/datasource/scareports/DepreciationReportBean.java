package nonprofitbookkeeping.reports.datasource.scareports;


import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.util.List;

public class DepreciationReportBean implements SupplementalRecord
{
	private final List<DepreciationRow> rows;
	
	public DepreciationReportBean(List<DepreciationRow> rows)
	{
		this.rows = rows;
		
	}
	
	public JRBeanCollectionDataSource getDataSource()
	{
		return new JRBeanCollectionDataSource(rows);
		
	}
	
}
