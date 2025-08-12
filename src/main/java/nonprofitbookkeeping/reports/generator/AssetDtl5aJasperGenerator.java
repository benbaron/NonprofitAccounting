
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
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
		return "jrxml/sca-reports/ASSET_DTL_5a_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "AssetDtl5a";
		
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<AssetDtl5aBean> getReportData()
        {
                AssetDtl5aBean bean = new AssetDtl5aBean();
                bean.set_1_contact_info(BigDecimal.ZERO);
                bean.setAdd_total_to_pg_3_i_a_end(BigDecimal.ZERO);
                bean.setAmount(BigDecimal.ZERO);
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
