/**
 * NonprofitAccounting OtherIncomeRow.java OtherIncomeRow
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

import nonprofitbookkeeping.reports.datasource.IncomeRowBase;

/** Catch-all income row for ‘other’ revenue lines. */
public final class OtherIncomeRow extends IncomeRowBase
{
	
	public OtherIncomeRow()
	{
	
	}
	
	public OtherIncomeRow(String description, BigDecimal amount)
	{
		super(description, amount);
		
	}
	
}
