
package nonprofitbookkeeping.reports.jasper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.reports.datasource.scareports.AssetDtl5aBean;

public class AssetDtl5aJasperGenerator extends AbstractReportGenerator
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
		return "AssetDtl5a";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
	@Override
	protected List<AssetDtl5aBean> getReportData()
	{
		AssetDtl5aBean bean = new AssetDtl5aBean();
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
