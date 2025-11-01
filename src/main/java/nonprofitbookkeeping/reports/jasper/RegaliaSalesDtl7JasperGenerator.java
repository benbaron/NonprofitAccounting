
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.RegaliaSalesDtl7Bean;

public class RegaliaSalesDtl7JasperGenerator extends AbstractReportGenerator
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
		return "RegaliaSalesDtl7";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<RegaliaSalesDtl7Bean> getReportData()
	{
                RegaliaSalesDtl7Bean bean = new RegaliaSalesDtl7Bean();
                bean.setREGALIA_SALES_DTL_7_I20(Double.valueOf(1));
                return java.util.Collections.singletonList(bean);

        }
	
	
}
