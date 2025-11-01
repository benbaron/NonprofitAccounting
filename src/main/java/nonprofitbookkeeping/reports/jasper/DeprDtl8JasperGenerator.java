
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.DeprDtl8Bean;

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
                return bundledReportPath();

        }
	
	@Override
	public String getBaseName()
	{
		return "DeprDtl8";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<DeprDtl8Bean> getReportData()
        {
                DeprDtl8Bean bean = new DeprDtl8Bean();
                return java.util.Collections.singletonList(bean);

        }

	
	
}
