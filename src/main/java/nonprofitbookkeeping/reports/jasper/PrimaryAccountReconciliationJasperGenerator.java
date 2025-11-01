
package nonprofitbookkeeping.reports.jasper;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.PrimaryAccount2aBean;

public class PrimaryAccountReconciliationJasperGenerator
	extends AbstractReportGenerator
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
		return "PrimaryAccountReconciliation";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData() 
	 */
	@Override
        protected List<PrimaryAccount2aBean> getReportData()
        {
                PrimaryAccount2aBean bean =
                        new PrimaryAccount2aBean();
                bean.setPRIMARY_ACCOUNT_2a_C2("Sample Bank");
                bean.setPRIMARY_ACCOUNT_2a_C3("Checking");
                bean.setPRIMARY_ACCOUNT_2a_C4("Checking");
                bean.setPRIMARY_ACCOUNT_2a_C6("123456");
                bean.setPRIMARY_ACCOUNT_2a_H18("555-1234 Branch");
                bean.setPRIMARY_ACCOUNT_2a_H24(BigDecimal.TEN.doubleValue());
                bean.setPRIMARY_ACCOUNT_2a_E42("2023-01-01");
                bean.setPRIMARY_ACCOUNT_2a_H42(BigDecimal.ONE.doubleValue());
                bean.setPRIMARY_ACCOUNT_2a_H35(BigDecimal.TEN.doubleValue());
                bean.setPRIMARY_ACCOUNT_2a_F43("NO");
                bean.setPRIMARY_ACCOUNT_2a_G36(BigDecimal.ZERO.doubleValue());
                bean.setPRIMARY_ACCOUNT_2a_H36(BigDecimal.ZERO.doubleValue());
                bean.setPRIMARY_ACCOUNT_2a_H43("0.00");
                bean.setPRIMARY_ACCOUNT_2a_C65("Reconciled");
                return java.util.Collections.singletonList(bean);

        }
	
	
}
