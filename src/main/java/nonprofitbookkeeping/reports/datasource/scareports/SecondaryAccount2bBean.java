/**
 * NonprofitAccounting SecondaryAccount2bBean.java SecondaryAccount2bBean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecondaryAccount2bBean
{

        private String bankName;
	private String accountNumber;
	private java.math.BigDecimal statementBalance;
	private java.math.BigDecimal unclearedDeposits;
	private java.math.BigDecimal unclearedChecks;
	private java.math.BigDecimal adjustedBalance;
	
}
