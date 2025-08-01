/**
 * NonprofitAccounting Balance3Bean.java Balance3Bean
 */

package nonprofitbookkeeping.reports.datasource.scareports;

import java.io.Serializable;
import java.math.BigDecimal;

public class Balance3Bean implements Serializable
{
	private BigDecimal beginBalStart, beginBalEnd;
	private BigDecimal cashEarningInterestStart, cashEarningInterestEnd;
	private BigDecimal receivablesStart, receivablesEnd;
	private BigDecimal inventoryForSaleStart, inventoryForSaleEnd;
	private BigDecimal regaliaEquipmentStart, regaliaEquipmentEnd;
	private BigDecimal depreciatedEquipmentStart, depreciatedEquipmentEnd;
	private BigDecimal accumulatedDepreciationStart, accumulatedDepreciationEnd;
	private BigDecimal prepaidExpensesStart, prepaidExpensesEnd;
	private BigDecimal otherAssetsStart, otherAssetsEnd;
	private BigDecimal totalAssetsStart, totalAssetsEnd;
	
	private BigDecimal newsletterSubsDueStart, newsletterSubsDueEnd;
	private BigDecimal deferredRevenueStart, deferredRevenueEnd;
	private BigDecimal payablesStart, payablesEnd;
	private BigDecimal otherLiabilitiesStart, otherLiabilitiesEnd;
	private BigDecimal totalLiabilitiesStart, totalLiabilitiesEnd;
	
	private BigDecimal netWorthStart, netWorthEnd;
	
	private BigDecimal changeInNetWorth;
	private BigDecimal netIncome;
	
	/* getters & setters … */
}
