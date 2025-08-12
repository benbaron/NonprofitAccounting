
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
                bean.setDeposit2Date("2023-01-02");
                bean.setDeposit2Amount(BigDecimal.ONE);
                bean.setDeposit3Date("2023-01-03");
                bean.setDeposit3Amount(BigDecimal.ONE);
                bean.setDeposit4Date("2023-01-04");
                bean.setDeposit4Amount(BigDecimal.ONE);
                bean.setDeposit5Date("2023-01-05");
                bean.setDeposit5Amount(BigDecimal.ONE);
                bean.setDeposit6Date("2023-01-06");
                bean.setDeposit6Amount(BigDecimal.ONE);
                bean.setCheck1Number("1001");
                bean.setCheck1Date("2023-02-01");
                bean.setCheck1Amount(BigDecimal.ONE);
                bean.setCheck2Number("1002");
                bean.setCheck2Date("2023-02-02");
                bean.setCheck2Amount(BigDecimal.ONE);
                bean.setCheck3Number("1003");
                bean.setCheck3Date("2023-02-03");
                bean.setCheck3Amount(BigDecimal.ONE);
                bean.setCheck4Number("1004");
                bean.setCheck4Date("2023-02-04");
                bean.setCheck4Amount(BigDecimal.ONE);
                bean.setCheck5Number("1005");
                bean.setCheck5Date("2023-02-05");
                bean.setCheck5Amount(BigDecimal.ONE);
                bean.setCheck6Number("1006");
                bean.setCheck6Date("2023-02-06");
                bean.setCheck6Amount(BigDecimal.ONE);
                bean.setCheck7Number("1007");
                bean.setCheck7Date("2023-02-07");
                bean.setCheck7Amount(BigDecimal.ONE);
                bean.setCheck8Number("1008");
                bean.setCheck8Date("2023-02-08");
                bean.setCheck8Amount(BigDecimal.ONE);
                bean.setCheck9Number("1009");
                bean.setCheck9Date("2023-02-09");
                bean.setCheck9Amount(BigDecimal.ONE);
                bean.setCheck10Number("1010");
                bean.setCheck10Date("2023-02-10");
                bean.setCheck10Amount(BigDecimal.ONE);
                bean.setCheck11Number("1011");
                bean.setCheck11Date("2023-02-11");
                bean.setCheck11Amount(BigDecimal.ONE);
                bean.setCheck12Number("1012");
                bean.setCheck12Date("2023-02-12");
                bean.setCheck12Amount(BigDecimal.ONE);
                bean.setCheck13Number("1013");
                bean.setCheck13Date("2023-02-13");
                bean.setCheck13Amount(BigDecimal.ONE);
                bean.setCheck14Number("1014");
                bean.setCheck14Date("2023-02-14");
                bean.setCheck14Amount(BigDecimal.ONE);
                bean.setCheck15Number("1015");
                bean.setCheck15Date("2023-02-15");
                bean.setCheck15Amount(BigDecimal.ONE);
                bean.setCheck16Number("1016");
                bean.setCheck16Date("2023-02-16");
                bean.setCheck16Amount(BigDecimal.ONE);
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
