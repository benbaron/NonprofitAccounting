/**
 * NonprofitAccounting SecondaryAccount2bBean.java SecondaryAccount2bBean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

<<<<<<< HEAD
public class SecondaryAccount2bBean extends ScaRowBase {
=======
public class SecondaryAccount2bBean implements SupplementalRecord
{
>>>>>>> refs/remotes/origin/codex/add-interface-and-extend-ledgerentry
	
	private String bankName;
	private String accountNumber;
	private java.math.BigDecimal statementBalance;
	private java.math.BigDecimal unclearedDeposits;
	private java.math.BigDecimal unclearedChecks;
	private java.math.BigDecimal adjustedBalance;
	
}
