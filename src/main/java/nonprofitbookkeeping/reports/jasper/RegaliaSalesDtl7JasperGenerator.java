
package nonprofitbookkeeping.reports.jasper;

import java.math.BigDecimal;
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
                return "jrxml/sca-reports/REGALIA_SALES_DTL_7_ROWS_3SECTION.jrxml";
		
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
		bean.setA_b_value_lost(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
