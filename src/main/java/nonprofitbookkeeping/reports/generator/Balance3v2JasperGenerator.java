
package nonprofitbookkeeping.reports.generator;

import java.util.*;

public class Balance3v2JasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Constructor Balance3v2JasperGenerator
	 * @param ctx
	 * @param svc
	 */
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/BALANCE_3_AUTO_STYLED_fixed_labeled.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "Balance3v2";
		
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
