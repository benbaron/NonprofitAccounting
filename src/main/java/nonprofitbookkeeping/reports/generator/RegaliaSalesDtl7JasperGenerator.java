
package nonprofitbookkeeping.reports.generator;

import java.util.Collections;
import java.util.Map;

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
                return "jrxml/sca-reports/SCA_REGALIA_SALES_DTL_7.jrxml";

        }
	
	@Override
	public String getBaseName()
	{
		return "RegaliaSalesDtl7";
		
	}
	
}
