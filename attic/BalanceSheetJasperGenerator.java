
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.reports.datasource.BalanceSheetRowBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a simple Balance Sheet report using JasperReports.
 */
public class BalanceSheetJasperGenerator extends AbstractReportGenerator
{
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
	@Override
	protected List<BalanceSheetRowBean> getReportData()
	{
		BalanceSheetRowBean bean =
			new BalanceSheetRowBean("Assets", "Cash", BigDecimal.TEN);
		bean.setCategory("Assets");
		bean.setAccount("Cash");
		bean.setAmount(BigDecimal.TEN);
		return java.util.Collections.singletonList(bean);
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Balance Sheet");
		
		String companyName = "N/A";
		
		if (CurrentCompany.getCompany() != null &&
			CurrentCompany.getCompany().getCompanyProfile() != null &&
			CurrentCompany.getCompany().getCompanyProfile()
				.getCompanyName() != null)
		{
			companyName = CurrentCompany.getCompany().getCompanyProfile()
				.getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		params.put("P_REPORT_DATE",
			LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		params.put("P_GENERATION_DATE",
			LocalDate.now().format(DateTimeFormatter.ISO_DATE));
		return params;
		
	}
	
	@Override
	protected String getReportPath()
	{
		return bundledReportPath();
		
	}
	
	/**
	 * Prepares the context map with data needed for generating a Balance Sheet using JXLS.
	 * Calculates totals for assets, liabilities, and equity as of the report end date,
	 * optionally filtering by selected funds. It also includes current period net income in equity.
	 *
	 * @param context The {@link ReportContext} containing report criteria like end date and fund IDs.
	 * @param ledger The {@link Ledger} containing all accounting transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} used to look up account details and types.
	 * @return A {@link Map} suitable for use as a JXLS context, containing lists of asset/liability/equity items,
	 *         totals, net income, and report date information.
	 * @throws IllegalArgumentException if end date is not provided in the {@code context}.
	 */
	public static Map<String, Object> prepareBalanceSheetContext(
		ReportContext context,
		Ledger ledger,
		ChartOfAccounts chartOfAccounts)
	{
		Map<String, BigDecimal> assetTotals = new HashMap<>();
		Map<String, BigDecimal> liabilityTotals = new HashMap<>();
		Map<String, BigDecimal> equityTotals = new HashMap<>();
		
		if (context.getEndDate() == null)
		{
			throw new IllegalArgumentException(
				"End date must be provided for balance sheet.");
		}
		
		LocalDate reportEndDate = context.getEndDate();
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null &&
			!selectedFundNames.isEmpty());
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			if (account == null || account.getAccountType() == null ||
				account.getName() == null)
				
				continue;
			
			if (applyFundFilter &&
				!ReportService.doesAccountMatchFunds(account, selectedFundNames,
					chartOfAccounts))
			{
				continue;
			}
			
			BigDecimal finalBalance = ReportService.getAccountBalanceAsOfDate(
				account, reportEndDate, ledger,
				chartOfAccounts, selectedFundNames, applyFundFilter);
			
			AccountType accountType = account.getAccountType(); // Use direct
																// enum
			
			if (accountType == null)
			{
				ReportService.LOGGER
					.warning("BS: Account type is null for account: " +
						account.getName());
				continue;
			}
			
			switch(accountType)
			{
				case ASSET:
				case BANK:
				case CASH:
				case CHECKING:
				case FIXED_ASSET: // Added missing asset type
					assetTotals.put(account.getName(), finalBalance);
					break;
				
				case LIABILITY:
				case LONG_TERM_LIABILITY:
				case CREDITCARD: // Added missing liability type
					liabilityTotals.put(account.getName(), finalBalance);
					break;
				
				case EQUITY:
					equityTotals.put(account.getName(), finalBalance);
					break;
				
				default:
					// Potentially log unhandled account types if necessary
					break;
			}
			
		}
		
		ReportContext netIncomeCalcContext = new ReportContext();
		LocalDate netIncomeStartDate =
			(context.getStartDate() != null) ? context.getStartDate() :
				reportEndDate.withDayOfYear(1); // Default to start of the year
												// of
												// reportEndDate
		
		if (netIncomeStartDate.isAfter(reportEndDate))
		{
			// If user somehow provides start date after end date for BS
			// context, default to
			// year start
			netIncomeStartDate = reportEndDate.withDayOfYear(1);
		}
		
		netIncomeCalcContext.setStartDate(netIncomeStartDate);
		netIncomeCalcContext.setEndDate(reportEndDate);
		netIncomeCalcContext.setFundIds(selectedFundNames);
		
		Map<String, Object> incomeStatementData =
			IncomeStatementJasperGenerator.prepareIncomeStatementContext(
				netIncomeCalcContext, ledger, chartOfAccounts);
		BigDecimal currentPeriodNetIncome =
			(BigDecimal) incomeStatementData.getOrDefault("netIncome",
				BigDecimal.ZERO);
		
		equityTotals.put("Current Period Net Income", currentPeriodNetIncome);
		
		BigDecimal totalAssets =
			assetTotals.values().stream().reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal totalLiabilities =
			liabilityTotals.values().stream().reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal totalEquity = // This now includes the conceptual "Current
									// Period Net
									// Income"
			equityTotals.values().stream().reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal totalLiabilitiesAndEquity =
			totalLiabilities.add(totalEquity);
		
		if (totalAssets.compareTo(totalLiabilitiesAndEquity) != 0)
		{
			ReportService.LOGGER.warning(
				"Balance Sheet (fund-filtered: " + applyFundFilter +
					") out of balance! Assets: " +
					totalAssets + ", Liabilities + Equity: " +
					totalLiabilitiesAndEquity +
					". Difference: " +
					totalAssets.subtract(totalLiabilitiesAndEquity));
		}
		
		List<Map<String, Object>> assetItems = new ArrayList<>();
		assetTotals.forEach(
			(name, bal) -> assetItems.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> liabilityItems = new ArrayList<>();
		liabilityTotals
			.forEach((name, bal) -> liabilityItems
				.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> equityItems = new ArrayList<>();
		equityTotals.entrySet().stream()
			// .filter(entry -> !entry.getKey().equals("Current Period Net
			// Income")) // No
			// longer filter here if it's part of totalEquity
			.forEach(entry -> equityItems
				.add(Map.of("name", entry.getKey(), "amount",
					entry.getValue())));
		
		Map<String, Object> jxlsContext = new HashMap<>();
		jxlsContext.put("assetItems", assetItems);
		jxlsContext.put("liabilityItems", liabilityItems);
		jxlsContext.put("equityItems", equityItems); // Will include "Current
														// Period Net
														// Income" line
		jxlsContext.put("totalAssets", totalAssets);
		jxlsContext.put("totalLiabilities", totalLiabilities);
		jxlsContext.put("totalEquity", totalEquity); // This total now reflects
														// equity
														// including current net
														// income
		// jxlsContext.put("currentPeriodNetIncome", currentPeriodNetIncome);
		// Already part of equityItems and totalEquity
		jxlsContext.put("totalLiabilitiesAndEquity", totalLiabilitiesAndEquity);
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportStartDate", netIncomeStartDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		jxlsContext.put("assetsEqualsLiabilitiesPlusEquity",
			totalAssets.compareTo(totalLiabilitiesAndEquity) == 0);
		
		return jxlsContext;
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override
	public String getBaseName()
	{
		return "Balance_Sheet_" + LocalDate.now();
		
	}
	
	
}
