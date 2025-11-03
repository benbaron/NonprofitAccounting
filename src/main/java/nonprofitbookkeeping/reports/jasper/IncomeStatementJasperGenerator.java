
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.IncomeStatementRowBean;
import nonprofitbookkeeping.service.ReportService;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.math.BigDecimal;

/**
 * Generates an Income Statement (Profit & Loss) report using JasperReports.
 * This class extends {@link AbstractReportGenerator} and is responsible for
 * providing the specific data, parameters, and JRXML template path for the
 * Income Statement. It utilizes a {@link ReportService} to prepare the data and
 * a {@link ReportContext} for report criteria.
 */
public class IncomeStatementJasperGenerator extends AbstractReportGenerator
{
	
	private static final DateTimeFormatter FILE_DATE_FORMAT =
		DateTimeFormatter.BASIC_ISO_DATE;
	
	private ReportContext reportContext;
	
	/**
	 * Constructs an {@code IncomeStatementJasperGenerator}.
	 *
	 * @param reportContext The {@link ReportContext} containing criteria and settings for the report.
	 * @param reportService The {@link ReportService} used to prepare the data for the report.
	 */
	public IncomeStatementJasperGenerator(ReportContext reportContext,
		ReportService reportService)
	{
		this.reportContext = reportContext;
		
	}
	
	/** {@inheritDoc} */
	@Override
	protected String getReportPath()
	{
		return bundledReportPath();
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Prepares and returns the data for the Income Statement.
	 * It retrieves the current company's ledger and chart of accounts, then uses the
	 * {@link ReportService} to generate a list of {@link IncomeStatementRowBean} objects
	 * based on the provided {@link ReportContext}.
	 * If essential company data is missing, an error is logged, and an empty list is returned.
	 * </p>
	 * @return A list of {@link IncomeStatementRowBean} objects for the report, or an empty list if data cannot be prepared.
	 */
	@Override
	protected List<IncomeStatementRowBean> getReportData()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null ||
			company.getLedger() == null ||
			company.getChartOfAccounts() == null)
		{
			System.err.println(
				"IncomeStatementJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
			return Collections.emptyList();
		}
		
		Ledger ledger = company.getLedger();
		ChartOfAccounts coa = company.getChartOfAccounts();
		
		return IncomeStatementJasperGenerator
			.prepareIncomeStatementJasperData(this.reportContext, ledger, coa);
		
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Provides parameters for the Income Statement report. This includes:
	 * <ul>
	 *   <li>{@code P_REPORT_TITLE}: "Income Statement"</li>
	 *   <li>{@code P_COMPANY_NAME}: The name of the current company, or "N/A".</li>
	 *   <li>{@code P_REPORT_PERIOD}: A formatted string representing the report period (start date - end date), or "N/A".</li>
	 *   <li>{@code P_GENERATION_DATE}: The current date, formatted.</li>
	 * </ul>
	 * Parameters for Net Income are assumed to be calculated within the JRXML or are part of the bean list.
	 * </p>
	 * @return A map of parameters for the JasperReport.
	 */
	@Override
	protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		params.put("P_REPORT_TITLE", "Income Statement");
		
		Company company = CurrentCompany.getCompany();
		String companyName = "N/A";
		
		if (company != null && company.getCompanyProfile() != null &&
			company.getCompanyProfile().getCompanyName() != null)
		{
			companyName = company.getCompanyProfile().getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		
		String reportPeriod = "N/A";
		
		if (this.reportContext.getStartDate() != null &&
			this.reportContext.getEndDate() != null)
		{
			DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern("MMMM d, yyyy");
			reportPeriod =
				this.reportContext.getStartDate().format(formatter) + " - " +
					this.reportContext.getEndDate().format(formatter);
		}
		
		params.put("P_REPORT_PERIOD", reportPeriod);
		params.put("P_GENERATION_DATE",
			LocalDate.now()
				.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		
		// Net Income parameter calculation can be added here if needed by JRXML
		// List<IncomeStatementRowBean> data = getReportData(); // This might be
		// inefficient if called again
		// Consider calculating sums from the data if not done by Jasper itself.
		// For now, assuming JRXML handles summary or it's part of the bean
		// list.
		
		return params;
		
	}
	
	/**
	 * Prepares the context map with data needed for generating an Income Statement using JXLS.
	 * Calculates income and expense totals based on transactions within the report period
	 * and optionally filters by selected funds.
	 *
	 * @param context The {@link ReportContext} containing report criteria like start/end dates and fund IDs.
	 * @param ledger The {@link Ledger} containing all accounting transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} used to look up account details and types.
	 * @return A {@link Map} suitable for use as a JXLS context, containing lists of income/expense items,
	 *         totals, net income, and report date information.
	 * @throws IllegalArgumentException if start date or end date is not provided in the {@code context}.
	 */
	public static Map<String, Object> prepareIncomeStatementContext(
		ReportContext context, nonprofitbookkeeping.model.Ledger ledger,
		nonprofitbookkeeping.model.ChartOfAccounts chartOfAccounts)
	{
		Map<String, BigDecimal> incomeTotals = new HashMap<>();
		Map<String, BigDecimal> expenseTotals = new HashMap<>();
		
		if (context.getStartDate() == null || context.getEndDate() == null)
		{
			throw new IllegalArgumentException(
				"Start date and end date must be provided for income statement.");
		}
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter =
			(selectedFundNames != null && !selectedFundNames.isEmpty());
		
		long startDateMillis =
			context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		long endDateMillis =
			context.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
				.toInstant().toEpochMilli();
		
		List<nonprofitbookkeeping.model.AccountingTransaction> transactions =
			ledger.getTransactions();
		
		if (transactions == null)
		{
			ReportService.LOGGER.info("No transactions found in the ledger.");
			transactions = new ArrayList<
				nonprofitbookkeeping.model.AccountingTransaction>();
		}
		
		for (nonprofitbookkeeping.model.AccountingTransaction transaction : transactions)
		{
			if (transaction == null)
				continue;
			
			if (transaction.getBookingDateTimestamp() >= startDateMillis &&
				transaction.getBookingDateTimestamp() < endDateMillis)
			{
				Set<nonprofitbookkeeping.model.AccountingEntry> entries =
					transaction.getEntries();
				
				if (entries == null)
				{
					ReportService.LOGGER.fine("Transaction with ID " +
						transaction.getBookingDateTimestamp() +
						" has no entries.");
					entries = new java.util.HashSet<>(); // Ensure non-null for
															// iteration
				}
				
				for (nonprofitbookkeeping.model.AccountingEntry entry : entries)
				{
					if (entry == null || entry.getAccountNumber() == null)
						continue;
					
					nonprofitbookkeeping.model.Account account =
						chartOfAccounts.getAccount(entry.getAccountNumber());
					
					if (account == null)
					{
						ReportService.LOGGER.warning(
							"IS: Account not found for number: " +
								entry.getAccountNumber());
						continue;
					}
					
					if (applyFundFilter)
					{
						
						if (!ReportService.doesAccountMatchFunds(account,
							selectedFundNames, chartOfAccounts))
						{
							continue;
						}
						
					}
					
					nonprofitbookkeeping.model.AccountType accountType =
						account.getAccountType(); // Prefer direct
					// enum usage
					
					if (accountType == null)
					{
						ReportService.LOGGER
							.warning("IS: Account type is null for account: " +
								account.getName() + " (ID: " +
								account.getAccountNumber() + ")");
						continue;
					}
					
					String accountName = account.getName();
					BigDecimal amount = entry.getAmount();
					
					if (amount == null)
					{
						ReportService.LOGGER.warning(
							"Entry amount is null for account: " + accountName);
						continue;
					}
					
					nonprofitbookkeeping.model.AccountSide side =
						entry.getAccountSide();
					
					if (accountType == AccountType.INCOME)
					{
						BigDecimal currentTotal =
							incomeTotals.getOrDefault(accountName,
								BigDecimal.ZERO);
						if (side == AccountSide.CREDIT) // Income typically
														// increases on
														// credit side
							incomeTotals.put(accountName,
								currentTotal.add(amount));
						else if (side == AccountSide.DEBIT)
							incomeTotals.put(accountName,
								currentTotal.subtract(amount));
					}
					else if (accountType == AccountType.EXPENSE)
					{
						BigDecimal currentTotal =
							expenseTotals.getOrDefault(accountName,
								BigDecimal.ZERO);
						if (side == AccountSide.DEBIT) // Expenses typically
														// increase on
														// debit side
							expenseTotals.put(accountName,
								currentTotal.add(amount));
						else if (side == AccountSide.CREDIT)
							expenseTotals.put(accountName,
								currentTotal.subtract(amount));
					}
					
				}
				
			}
			
		}
		
		BigDecimal totalIncome =
			incomeTotals.values().stream().reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal totalExpenses =
			expenseTotals.values().stream().reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal netIncome = totalIncome.subtract(totalExpenses);
		
		List<Map<String, Object>> incomeItems = new ArrayList<>();
		incomeTotals.forEach((name, bal) -> incomeItems
			.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> expenseItems = new ArrayList<>();
		expenseTotals.forEach((name, bal) -> expenseItems
			.add(Map.of("name", name, "amount", bal)));
		
		Map<String, Object> jxlsContext = new HashMap<>();
		jxlsContext.put("incomeItems", incomeItems);
		jxlsContext.put("expenseItems", expenseItems);
		jxlsContext.put("totalIncome", totalIncome);
		jxlsContext.put("totalExpenses", totalExpenses);
		jxlsContext.put("netIncome", netIncome);
		jxlsContext.put("reportStartDate", context.getStartDate().toString());
		jxlsContext.put("reportEndDate", context.getEndDate().toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		return jxlsContext;
		
	}
	
	/**
	 * Prepares a list of {@link IncomeStatementRowBean} objects for use as a JasperReports data source.
	 * This method calculates income and expense account balances for the period specified in the
	 * {@link ReportContext}, optionally filtering by fund IDs.
	 *
	 * @param context The {@link ReportContext} containing report criteria (start/end dates, fund IDs).
	 * @param ledger The {@link Ledger} containing all accounting transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} for account lookups.
	 * @return A list of {@link IncomeStatementRowBean}s for the report. Returns an empty list if
	 *         required data (dates, ledger, COA) is missing or no relevant transactions are found.
	 */
	public static
		List<IncomeStatementRowBean>
		prepareIncomeStatementJasperData(ReportContext context,
			nonprofitbookkeeping.model.Ledger ledger,
			nonprofitbookkeeping.model.ChartOfAccounts chartOfAccounts)
	{
		
		List<IncomeStatementRowBean> reportData = new ArrayList<>();
		
		if (context.getStartDate() == null || context.getEndDate() == null)
		{
			ReportService.LOGGER.warning(
				"Start date and end date must be provided for income statement data preparation.");
			return reportData; // Return empty list
		}
		
		if (ledger == null || chartOfAccounts == null)
		{
			ReportService.LOGGER.warning(
				"Ledger or Chart of Accounts not available for income statement data.");
			return reportData;
		}
		
		Map<String, BigDecimal> incomeAccountBalances = new HashMap<>();
		Map<String, BigDecimal> expenseAccountBalances = new HashMap<>();
		
		List<String> selectedFundIds = context.getFundIds();
		boolean applyFundFilter =
			(selectedFundIds != null && !selectedFundIds.isEmpty());
		
		long startDateMillis =
			context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		long endDateMillisExclusive =
			context.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
				.toInstant().toEpochMilli();
		
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions == null)
		{
			transactions = new ArrayList<>(); // Ensure non-null
		}
		
		for (AccountingTransaction transaction : transactions)
		{
			if (transaction == null)
				continue;
			
			if (transaction.getBookingDateTimestamp() >= startDateMillis &&
				transaction.getBookingDateTimestamp() < endDateMillisExclusive)
			{
				
				Set<AccountingEntry> entries = transaction.getEntries();
				if (entries == null)
					continue;
				
				for (AccountingEntry entry : entries)
				{
					if (entry == null || entry.getAccountNumber() == null ||
						entry.getAmount() == null)
						continue;
					
					Account account =
						chartOfAccounts.getAccount(entry.getAccountNumber());
					
					if (account == null || account.getAccountType() == null ||
						account.getName() == null)
					{
						ReportService.LOGGER.warning(
							"IS Data: Account or critical account info not found for number: " +
								entry.getAccountNumber());
						continue;
					}
					
					if (applyFundFilter)
					{
						
						if (!ReportService.doesAccountMatchFunds(account,
							selectedFundIds, chartOfAccounts))
						{
							continue;
						}
						
					}
					
					AccountType accountType = account.getAccountType();
					String accountName = account.getName();
					BigDecimal amount = entry.getAmount();
					AccountSide side = entry.getAccountSide();
					
					if (accountType == AccountType.INCOME)
					{
						BigDecimal currentTotal =
							incomeAccountBalances.getOrDefault(accountName,
								BigDecimal.ZERO);
						
						if (side == AccountSide.CREDIT)
						{ // Income increases on credit
							incomeAccountBalances.put(accountName,
								currentTotal.add(amount));
						}
						else if (side == AccountSide.DEBIT)
						{
							incomeAccountBalances.put(accountName,
								currentTotal.subtract(amount));
						}
						
					}
					else if (accountType == AccountType.EXPENSE)
					{
						BigDecimal currentTotal =
							expenseAccountBalances.getOrDefault(accountName,
								BigDecimal.ZERO);
						
						if (side == AccountSide.DEBIT)
						{ // Expense increases on debit
							expenseAccountBalances.put(accountName,
								currentTotal.add(amount));
						}
						else if (side == AccountSide.CREDIT)
						{
							expenseAccountBalances.put(accountName,
								currentTotal.subtract(amount));
						}
						
					}
					
				}
				
			}
			
		}
		
		// Add income items to reportData
		for (Map.Entry<String, BigDecimal> entry : incomeAccountBalances
			.entrySet())
		{
			
			if (entry.getValue().compareTo(BigDecimal.ZERO) != 0)
			{ // Only include accounts with non-zero balance for the period
				reportData
					.add(new IncomeStatementRowBean("Income", entry.getKey(),
						entry.getValue()));
			}
			
		}
		
		// Add expense items to reportData
		for (Map.Entry<String, BigDecimal> entry : expenseAccountBalances
			.entrySet())
		{
			
			if (entry.getValue().compareTo(BigDecimal.ZERO) != 0)
			{ // Only include accounts with non-zero balance for the period
				reportData
					.add(new IncomeStatementRowBean("Expenses", entry.getKey(),
						entry.getValue()));
			}
			
		}
		
		return reportData;
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override
	public String getBaseName()
	{
		String companyPart = resolveCompanyName();
		String periodPart = resolvePeriodSuffix();
		return String.format("Income_Statement_%s_%s", companyPart, periodPart);
		
	}
	
	private String resolveCompanyName()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company != null && company.getCompanyProfile() != null)
		{
			String name = company.getCompanyProfile().getCompanyName();
			
			if (name != null && !name.isBlank())
			{
				return sanitizeForFileName(name);
			}
			
		}
		
		return "Company";
		
	}
	
	private String resolvePeriodSuffix()
	{
		LocalDate start = (this.reportContext != null) ?
			this.reportContext.getStartDate() : null;
		LocalDate end = (this.reportContext != null) ?
			this.reportContext.getEndDate() : null;
		
		if (start != null && end != null)
		{
			return FILE_DATE_FORMAT.format(start) + "-" +
				FILE_DATE_FORMAT.format(end);
		}
		
		LocalDate fallback = (end != null) ? end : LocalDate.now();
		return FILE_DATE_FORMAT.format(fallback);
		
	}
	
	private String sanitizeForFileName(String value)
	{
		String sanitized = value.replaceAll("[^A-Za-z0-9]+", "_");
		sanitized = sanitized.replaceAll("_+", "_");
		sanitized = sanitized.replaceAll("^_", "").replaceAll("_$", "");
		return sanitized.isEmpty() ? "Company" : sanitized;
		
	}
	
}
