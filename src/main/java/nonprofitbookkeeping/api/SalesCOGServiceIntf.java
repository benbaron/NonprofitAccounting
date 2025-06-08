
package nonprofitbookkeeping.api;

public interface SalesCOGServiceIntf
{
	/**
	 * Calculates the Cost of Goods Sold (COGS) for a given period.
	 * @param startDate The start date of the period (inclusive).
	 * @param endDate The end date of the period (inclusive).
	 * @return The calculated COGS for the period.
	 */
	double calculateCOGSForPeriod(String startDate, String endDate);
	
	/**
	 * Calculates the total sales for a given period.
	 * @param startDate The start date of the period (inclusive).
	 * @param endDate The end date of the period (inclusive).
	 * @return The total sales for the period.
	 */
	double getTotalSales(String startDate, String endDate);
	
}
