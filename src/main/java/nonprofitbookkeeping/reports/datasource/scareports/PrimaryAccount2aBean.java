/**
 * NonprofitAccounting PrimaryAccount2aBean.java PrimaryAccount2aBean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.io.Serializable;

public class PrimaryAccount2aBean implements Serializable, SupplementalRecord
{
	
	/* aqua-fill “Deposit Date” rows */
	private String depositDate1;
	private String depositDate2;
	private String depositDate3;
	
	/* aqua-fill check number */
	private String checkNumber;
	
	/* formula output from C2 (sheetRef1) */
	private String sheetRef1;
	
	
}
