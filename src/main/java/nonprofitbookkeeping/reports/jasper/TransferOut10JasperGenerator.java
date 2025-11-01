
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.TransferOut10Bean;

public class TransferOut10JasperGenerator extends AbstractReportGenerator
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
		return "TransferOut10";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<TransferOut10Bean> getReportData()
	{
                TransferOut10Bean bean = new TransferOut10Bean();
                bean.setTRANSFER_OUT_10_F25(Double.valueOf(1));
                bean.setTRANSFER_OUT_10_F39(Double.valueOf(1));
                bean.setTRANSFER_OUT_10_F51(Double.valueOf(1));
                bean.setTRANSFER_OUT_10_F52(Double.valueOf(1));
                return java.util.Collections.singletonList(bean);

        }
	
	
}
