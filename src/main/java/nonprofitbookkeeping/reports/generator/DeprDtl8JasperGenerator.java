
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class DeprDtl8JasperGenerator extends AbstractReportGenerator
{
	
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/DEPR_DTL_8_AUTO_STYLED_labeled.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "DeprDtl8";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<?> getReportData()
	{
		// TODO Auto-generated method stub
		return null;
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#setReportData(java.util.List) 
	 */
	@Override
	public void setReportData(List<?> data)
	{
		// TODO Auto-generated method stub
		
		
	}
	
}
