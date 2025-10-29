
package nonprofitbookkeeping.reports.jasper;

import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.TransferIn9Bean;

public class TransferIn9JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
        @Override
        protected String getReportPath()
        {
                return "jrxml/sca-reports/TRANSFER_IN_9.jrxml";

        }
	
	@Override
	public String getBaseName()
	{
		return "TransferIn9";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<TransferIn9Bean> getReportData()
	{
                TransferIn9Bean bean = new TransferIn9Bean();
                bean.setTRANSFER_IN_9_F38(Double.valueOf(1));
                bean.setTRANSFER_IN_9_F58(Double.valueOf(1));
                return java.util.Collections.singletonList(bean);

        }
	
	
}
