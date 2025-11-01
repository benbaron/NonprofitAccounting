
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.FinanceComm13Bean;

public class FinanceComm13JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
        @Override
        protected String getReportPath()
        {
                return bundledReportPath();

        }
	
	@Override
	public String getBaseName()
	{
		return "FinanceComm13";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<FinanceComm13Bean> getReportData()
	{
		FinanceComm13Bean bean = new FinanceComm13Bean();
		bean.setFINANCE_COMM_13_E19(Double.valueOf(1.0));
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
