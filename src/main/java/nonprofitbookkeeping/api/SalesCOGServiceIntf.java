
package nonprofitbookkeeping.api;

public interface SalesCOGServiceIntf
{
	double calculateCOGSForPeriod(String startDate, String endDate);
	
	double getTotalSales(String startDate, String endDate);
	
}
