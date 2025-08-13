
package nonprofitbookkeeping.reports.generator;

import java.math.BigDecimal;
import java.util.*;
import nonprofitbookkeeping.reports.datasource.scareports.PrimaryAccountReconciliationBean;

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
		return "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml";
		
	}
	
	@Override
	public String getBaseName()
	{
		return "PrimaryAccountReconciliation";
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.generator.AbstractReportGenerator#getReportData() 
	 */
        @Override
        protected List<PrimaryAccountReconciliationBean> getReportData()
        {
                PrimaryAccountReconciliationBean bean = new PrimaryAccountReconciliationBean();
                bean.setBankName("Sample Bank");
                bean.setBankAccountTitle("Checking");
                bean.setBankAccountType("Checking");
                bean.setBankAccountNumber("123456");
                bean.setBankBranchPhoneAndName("555-1234 Branch");
                bean.setBalanceFromBankStatement(BigDecimal.TEN);
                bean.setDeposit1Date("2023-01-01");
                bean.setDeposit1Amount(BigDecimal.ONE);
                bean.setEndingLedgerOrRegisterBalance(BigDecimal.TEN);
                bean.setAccountEarnsInterest("NO");
                bean.setDepositsNotClearedTotal(BigDecimal.ZERO);
                bean.setChecksNotClearedTotal(BigDecimal.ZERO);
                bean.setAdjustedAccountBalance(BigDecimal.ZERO);
                bean.setReconciliationDifference(BigDecimal.ZERO);
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
