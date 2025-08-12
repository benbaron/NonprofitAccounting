
package nonprofitbookkeeping.reports.generator;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.LedgerQ1Bean;

public class LedgerQ1JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/Ledger_Q1.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "LedgerQ1";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<LedgerQ1Bean> getReportData()
        {
                LedgerQ1Bean bean = new LedgerQ1Bean();
                bean.setCol_0("Sample");
                bean.setCol_1("Sample");
                bean.setBalance("0");
                return java.util.Collections.singletonList(bean);

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
