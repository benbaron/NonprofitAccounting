
package nonprofitbookkeeping.reports.generator;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.Balance3Beanv2;

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
        protected List<Balance3Beanv2> getReportData()
        {
                Balance3Beanv2 bean = new Balance3Beanv2();
                return java.util.Collections.singletonList(bean);

        }


	
}
