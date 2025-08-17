/**
 * NonprofitAccounting SecondaryAccountBean.java SecondaryAccountBean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

public class SecondaryAccountBean implements SupplementalRecord
{
	
	private String bankName;
	private String accountNumber;
	private java.math.BigDecimal statementBalance;
	private java.math.BigDecimal unclearedDeposits;
	private java.math.BigDecimal unclearedChecks;
	private java.math.BigDecimal adjustedBalance;
	
}
