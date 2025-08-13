
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.InventoryDtl6Bean;

public class InventoryDtl6JasperGenerator extends AbstractReportGenerator
{
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return "jrxml/sca-reports/INVENTORY_DTL_6_AUTO_STYLED.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "InventoryDtl6";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
	@Override
	protected List<InventoryDtl6Bean> getReportData()
	{
		InventoryDtl6Bean bean = new InventoryDtl6Bean();
		bean.setActual_gross_income_from_inventory_sales(BigDecimal.ONE);
		return java.util.Collections.singletonList(bean);
		
	}
	
	
}
