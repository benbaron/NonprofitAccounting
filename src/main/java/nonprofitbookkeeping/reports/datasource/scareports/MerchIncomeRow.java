/**
 * NonprofitAccounting MerchIncomeRow.java MerchIncomeRow
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/** Merchandise sales (qty & total). */
public final class MerchIncomeRow extends IncomeRowBase
{
	
	private int quantity;
	
	public MerchIncomeRow()
	{
	
	}
	
	public MerchIncomeRow(String item,
		int qty,
		BigDecimal total)
	{
		super(item, total);
		this.quantity = qty;
		
	}
	
	public int getQuantity()
	{
		return quantity;
		
	}
	
	public void setQuantity(int q)
	{
		this.quantity = q;
		
	}
	
}
