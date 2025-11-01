
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
import nonprofitbookkeeping.reports.datasource.TrialBalanceRowBean;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Generates a Trial Balance report using JasperReports.
 * This class extends {@link AbstractReportGenerator} and is responsible for
 * providing the specific data, parameters, and JRXML template path for the
 * Trial Balance report. It utilizes a {@link ReportService} to prepare the data
 * based on the provided {@link ReportContext}.
 */
public class TrialBalanceJasperGenerator extends AbstractReportGenerator
{
	
	private ReportContext reportContext;
	/**
	 * Constructs a {@code TrialBalanceJasperGenerator}.
	 *
	 * @param reportContext The {@link ReportContext} containing criteria and settings for the report,
	 *                      such as the end date for the trial balance.
	 * @param reportService The {@link ReportService} used to prepare the data (list of {@link TrialBalanceRowBean})
	 *                      for the report.
	 */
	public TrialBalanceJasperGenerator(ReportContext reportContext, ReportService reportService)
	{
		this.reportContext = reportContext;
	}
	
	/**
	 * {@inheritDoc}
	 * @return The classpath resource path "reports/TrialBalanceReport.jrxml" for the Trial Balance template.
	 * @throws ActionCancelledException Not directly thrown by this implementation, but declared due to the interface.
	 * @throws NoFileCreatedException Not directly thrown by this implementation, but declared due to the interface.
	 */
	@Override protected String getReportPath()	throws ActionCancelledException,
												NoFileCreatedException
	{
		// Path within the resources directory
		return bundledReportPath();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Prepares and returns the data for the Trial Balance report.
	 * It retrieves the current company's ledger and chart of accounts, then uses the
	 * {@link ReportService} to generate a list of {@link TrialBalanceRowBean} objects
	 * based on the provided {@link ReportContext}.
	 * If essential company data is missing, an error is logged, and an empty list is returned.
	 * </p>
	 * @return A list of {@link TrialBalanceRowBean} objects for the report, or an empty list if data cannot be prepared.
	 */
	@Override protected List<TrialBalanceRowBean> getReportData()
	{
		Company company = CurrentCompany.getCompany();
		
		if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null)
		{
			System.err.println(
				"TrialBalanceJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
			return Collections.emptyList();
		}
		
		Ledger ledger = company.getLedger();
		ChartOfAccounts coa = company.getChartOfAccounts();
		
		return TrialBalanceJasperGenerator.prepareTrialBalanceJasperData(this.reportContext, ledger, coa);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>Provides parameters for the Trial Balance report. This includes:
	 * <ul>
	 *   <li>Standard parameters (prefixed with {@code P_}): {@code P_REPORT_TITLE}, {@code P_COMPANY_NAME},
	 *       {@code P_AS_OF_DATE}, {@code P_GENERATION_DATE}.</li>
	 *   <li>JRXML-specific parameters: {@code reporttitle}, {@code dateToday}, {@code companyname}.</li>
	 * </ul>
	 * The company name and "as of" date are derived from the current company and report context.
	 * </p>
	 * @return A map of parameters for the JasperReport.
	 */
	@Override protected Map<String, Object> getReportParameters()
	{
		Map<String, Object> params = new HashMap<>();
		
		// Standardized Parameters (using P_ convention for clarity)
		params.put("P_REPORT_TITLE", "Trial Balance");
		
		Company company = CurrentCompany.getCompany();
		String companyName = "N/A";
		
		if (company != null && company.getCompanyProfile() != null &&
			company.getCompanyProfile().getCompanyName() != null)
		{
			companyName = company.getCompanyProfile().getCompanyName();
		}
		
		params.put("P_COMPANY_NAME", companyName);
		
		String reportAsOfDate = "N/A";
		
		if (this.reportContext.getEndDate() != null)
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
			reportAsOfDate = "As of " + this.reportContext.getEndDate().format(formatter);
		}
		
		params.put("P_AS_OF_DATE", reportAsOfDate);
		params.put("P_GENERATION_DATE",
			LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		
		// Parameters matching the existing TrialBalanceReport.jrxml:
		// <parameter name="reporttitle" class="java.lang.String"/>
		// <parameter name="dateToday" class="java.lang.String"/>
		// <parameter name="companyname" class="java.lang.String"/>
		// It seems the JRXML uses lowercase for these specific ones. Let's ensure they
		// are provided.		
		params.put("reporttitle", "Trial Balance"); // Specific for JRXML
		params.put("dateToday", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		params.put("companyname", companyName);
		
		return params;
	}
	
	/**
	 * Prepares a list of {@link TrialBalanceRowBean} objects for use as a JasperReports data source.
	 * This method calculates the debit and credit balances for each account in the
	 * {@link ChartOfAccounts} as of the end date specified in the {@link ReportContext}.
	 * It considers transactions within the optional start and end date range and can filter by fund IDs.
	 *
	 * @param context The {@link ReportContext} containing report criteria (end date, optional start date, fund IDs).
	 * @param ledger The {@link Ledger} containing all accounting transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} providing the list of accounts.
	 * @return A list of {@link TrialBalanceRowBean}s for the report. Returns an empty list if
	 *         required data (end date, ledger, COA) is missing.
	 */
	public static
			List<TrialBalanceRowBean> prepareTrialBalanceJasperData(ReportContext context,
																	nonprofitbookkeeping.model.Ledger ledger,
																	nonprofitbookkeeping.model.ChartOfAccounts chartOfAccounts)
	{
		
		List<TrialBalanceRowBean> reportData = new ArrayList<>();
		
		if (context.getEndDate() == null || ledger == null || chartOfAccounts == null)
		{
			ReportService.LOGGER.warning("End date, ledger, or COA missing for Trial Balance data preparation.");
			return reportData; // Return empty list
		}
		
		LocalDate reportEndDate = context.getEndDate();
		long reportEndDateMillisInclusive =
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(); // Exclusive
																								// end
		
		long reportStartDateMillis = 0; // Default to include all transactions up to end
										// date if start date is null
		
		if (context.getStartDate() != null)
		{
			reportStartDateMillis =
				context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		}
		
		List<String> selectedFundIds = context.getFundIds();
		boolean applyFundFilter = (selectedFundIds != null && !selectedFundIds.isEmpty());
		
		List<Account> accountsToList = chartOfAccounts.getAccounts();
		
		if (accountsToList == null)
		{
			accountsToList = new ArrayList<>(); // Ensure non-null
		}
		
		// Sort accounts by account number for consistent report output
		accountsToList.sort(Comparator.comparing(Account::getAccountNumber,
			Comparator.nullsLast(String::compareTo)));
		
		
		for (Account account : accountsToList)
		{
			
			if (account == null || account.getAccountNumber() == null ||
				account.getName() == null || account.getAccountType() == null)
			{
				ReportService.LOGGER.warning("TB Data: Skipping account with missing critical information: " +
					(account != null ? account.getAccountNumber() : "null account object"));
				continue;
			}
			
			if (applyFundFilter &&
				!ReportService.doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts))
			{
				continue; // Skip account if it doesn't match fund filter
			}
			
			BigDecimal accountBalance =
				account.getOpeningBalance() != null ? account.getOpeningBalance() : BigDecimal.ZERO;
			
			List<AccountingTransaction> transactions = ledger.getTransactions();
			
			if (transactions != null)
			{
				
				for (AccountingTransaction transaction : transactions)
				{
					
					if (transaction == null ||
						transaction.getBookingDateTimestamp() >= reportEndDateMillisInclusive)
					{ 
						// Strictly before end of end date + 1 day
						continue;
					}
					
					// Apply start date filter for transactions if start date is specified
					if (reportStartDateMillis > 0 &&
						transaction.getBookingDateTimestamp() < reportStartDateMillis)
					{
						continue;
					}
					
					if (transaction.getEntries() == null)
						continue;
					
					for (AccountingEntry entry : transaction.getEntries())
					{
						
						if (entry == null ||
							!account.getAccountNumber().equals(entry.getAccountNumber()) ||
							entry.getAmount() == null)
						{
							continue;
						}
						
						// Fund filtering for transactions: Already handled by filtering the
						// account itself.
						// If the account is relevant to the selected funds, all its
						// transactions contribute to its balance for this filtered view.
						
						AccountType type = account.getAccountType();
						AccountSide increaseSide = account.getIncreaseSide();
						
						if (type == null || increaseSide == null)
						{
							ReportService.LOGGER.warning("TB Data: Account " + account.getAccountNumber() +
								" missing type or increase side.");
							continue;
						}
						
						if (increaseSide == AccountSide.DEBIT)
						{ 
							// For ASSET and EXPENSE typically
							
							if (entry.getAccountSide() == AccountSide.DEBIT)
							{
								accountBalance = accountBalance.add(entry.getAmount());
							}
							else
							{ 
								// CREDIT
								accountBalance = accountBalance.subtract(entry.getAmount());
							}
							
						}
						else
						{ 
							// increaseSide is CREDIT (for LIABILITY, EQUITY, INCOME
							// typically)
							
							if (entry.getAccountSide() == AccountSide.CREDIT)
							{
								accountBalance = accountBalance.add(entry.getAmount());
							}
							else
							{ 
								// DEBIT
								accountBalance = accountBalance.subtract(entry.getAmount());
							}
							
						}
						
					}
					
				}
				
			}
			
			BigDecimal debitAmount = BigDecimal.ZERO;
			BigDecimal creditAmount = BigDecimal.ZERO;
			
			AccountType type = account.getAccountType();
			AccountSide increaseSide = account.getIncreaseSide();
			if (type == null || increaseSide == null)
			{
				continue; // Already logged above
			}
				
			if (increaseSide == AccountSide.DEBIT)
			{ 
				// ASSET, EXPENSE
				
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
				{
					debitAmount = accountBalance;
				}
				else
				{ 
					// Negative balance for a debit-normal account implies a credit nature in
					// TB
					creditAmount = accountBalance.abs();
				}
				
			}
			else
			{ 
				// Credit-normal accounts: LIABILITY, EQUITY, INCOME				
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
				{
					creditAmount = accountBalance;
				}
				else
				{ 
					// Negative balance for a credit-normal account implies a debit nature in
					// TB
					debitAmount = accountBalance.abs();
				}
				
			}
			
			// Only include accounts with non-zero balances or if they had an opening
			// balance (even if ending is zero)
			if (debitAmount.compareTo(BigDecimal.ZERO) != 0 ||
				creditAmount.compareTo(BigDecimal.ZERO) != 0 ||
				(account.getOpeningBalance() != null &&
					account.getOpeningBalance().compareTo(BigDecimal.ZERO) != 0))
			{
				reportData.add(new TrialBalanceRowBean(account.getAccountNumber(),
					account.getName(), debitAmount, creditAmount));
			}
			
		}
		
		return reportData;
	}

	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName() 
	 */
	@Override public String getBaseName()
	{
		String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String reportBaseName = "Trial_Balance_Report_" + (this.reportContext.getEndDate() != null ?
			this.reportContext.getEndDate().toString() : currentDateStr);
		return reportBaseName;
		
	}
	
}
