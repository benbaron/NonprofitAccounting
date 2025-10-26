
package nonprofitbookkeeping.reports.generator;

import java.util.*;

import nonprofitbookkeeping.reports.datasource.scareports.Balance3Beanv2;

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
                // Use the updated Balance 3 template which now resides at the root of sca-reports
                return "jrxml/sca-reports/BALANCE_3_FIXED_SEMANTIC_STRINGS_v2.jrxml";

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
        protected List<Balance3Beanv2> getReportData()
        {
                Balance3Beanv2 bean = new Balance3Beanv2();
                return java.util.Collections.singletonList(bean);

        }


	
}
