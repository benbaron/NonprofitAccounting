
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportMetadata;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import nonprofitbookkeeping.reports.datasource.IncomeStatementRowBean;
import nonprofitbookkeeping.reports.datasource.CashFlowStatementRowBean;
import nonprofitbookkeeping.reports.datasource.TrialBalanceRowBean; 
import nonprofitbookkeeping.reports.datasource.ChartOfAccountsRowBean;
import nonprofitbookkeeping.reports.generator.AbstractReportGenerator;
import nonprofitbookkeeping.reports.generator.AccountLedgerJasperGenerator;
import nonprofitbookkeeping.reports.generator.AccountSummaryJasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeStatementJasperGenerator;
import nonprofitbookkeeping.reports.generator.TransactionReportJasperGenerator;
import nonprofitbookkeeping.reports.generator.CashFlowStatementJasperGenerator;
import nonprofitbookkeeping.reports.generator.ChartOfAccountsJasperGenerator;
import nonprofitbookkeeping.reports.generator.FundLedgerJasperGenerator;
import nonprofitbookkeeping.reports.generator.GeneralJournalJasperGenerator;
import nonprofitbookkeeping.reports.generator.GeneralLedgerJasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeStatementAltJasperGenerator;
import nonprofitbookkeeping.reports.generator.TrialBalanceJasperGenerator;
import nonprofitbookkeeping.reports.generator.BalanceResultReportGenerator;
import nonprofitbookkeeping.reports.generator.BalanceSheetJasperGenerator;
import nonprofitbookkeeping.reports.generator.BankReconciliationJasperGenerator;


/**
 * Service class responsible for preparing data contexts for various financial reports
 * and orchestrating the generation of reports using templating engines like JXLS or JasperReports.
 * It interacts with other services and models (e.g., {@link Ledger}, {@link ChartOfAccounts}, {@link Budget})
 * to gather and process data according to the criteria specified in a {@link ReportContext}.
 */
public class ReportService
{
	/** Logger for this class. */
	private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
	/** Standard date formatter (ISO Local Date, e.g., "YYYY-MM-DD") used in some report outputs. */
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	
	/** Map of registered report writers keyed by report type. */
	private final Map<String, LedgerReportWriter> writerMap = new HashMap<>();
	
	
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
	static Map<String, Object> prepareIncomeStatementContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts)
	{
		Map<String, BigDecimal> incomeTotals = new HashMap<>();
		Map<String, BigDecimal> expenseTotals = new HashMap<>();
		
		if (context.getStartDate() == null || context.getEndDate() == null)
		{
			throw new IllegalArgumentException(
				"Start date and end date must be provided for income statement.");
		}
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
		long startDateMillis =
			context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		long endDateMillis = context.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
			.toInstant().toEpochMilli();
		
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions == null)
		{
			LOGGER.info("No transactions found in the ledger.");
			transactions = new ArrayList<AccountingTransaction>();
		}
		
		for (AccountingTransaction transaction : transactions)
		{
			if (transaction == null)
				continue;
			
			if (transaction.getBookingDateTimestamp() >= startDateMillis &&
				transaction.getBookingDateTimestamp() < endDateMillis)
			{
				Set<AccountingEntry> entries = transaction.getEntries();
				
				if (entries == null)
				{
					LOGGER.fine("Transaction with ID " + transaction.getBookingDateTimestamp() + 
						" has no entries.");
					entries = new java.util.HashSet<>(); // Ensure non-null for iteration
				}
				
				for (AccountingEntry entry : entries)
				{
					if (entry == null || entry.getAccountNumber() == null)
						continue;
					
					Account account = chartOfAccounts.getAccount(entry.getAccountNumber());
					
					if (account == null)
					{
						LOGGER.warning(
							"IS: Account not found for number: " + entry.getAccountNumber());
						continue;
					}
					
					if (applyFundFilter)
					{
						
						if (!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
						{
							continue;
						}
						
					}
					
					AccountType accountType = account.getAccountType(); // Prefer direct
																		// enum usage
					
					if (accountType == null)
					{
						LOGGER.warning("IS: Account type is null for account: " +
							account.getName() + " (ID: " + account.getAccountNumber() + ")");
						continue;
					}
					
					String accountName = account.getName();
					BigDecimal amount = entry.getAmount();
					
					if (amount == null)
					{
						LOGGER.warning("Entry amount is null for account: " + accountName);
						continue;
					}
					
					AccountSide side = entry.getAccountSide();
					
					if (accountType == AccountType.INCOME)
					{
						BigDecimal currentTotal =
							incomeTotals.getOrDefault(accountName, BigDecimal.ZERO);
						if (side == AccountSide.CREDIT) // Income typically increases on
														// credit side
							incomeTotals.put(accountName, currentTotal.add(amount));
						else if (side == AccountSide.DEBIT)
							incomeTotals.put(accountName, currentTotal.subtract(amount));
					}
					else if (accountType == AccountType.EXPENSE)
					{
						BigDecimal currentTotal =
							expenseTotals.getOrDefault(accountName, BigDecimal.ZERO);
						if (side == AccountSide.DEBIT) // Expenses typically increase on
														// debit side
							expenseTotals.put(accountName, currentTotal.add(amount));
						else if (side == AccountSide.CREDIT)
							expenseTotals.put(accountName, currentTotal.subtract(amount));
					}
					
				}
				
			}
			
		}
		
		BigDecimal totalIncome =
			incomeTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalExpenses =
			expenseTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal netIncome = totalIncome.subtract(totalExpenses);
		
		List<Map<String, Object>> incomeItems = new ArrayList<>();
		incomeTotals.forEach((name, bal) -> incomeItems.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> expenseItems = new ArrayList<>();
		expenseTotals.forEach((name, bal) -> expenseItems.add(Map.of("name", name, "amount", bal)));
		
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
	 * Checks if a given account is associated with any of the selected funds.
	 * This is a helper method used for filtering report data based on fund selections.
	 *
	 * @param account The {@link Account} to check.
	 * @param selectedFundNames A list of names of the funds selected for filtering.
	 * @param chartOfAccounts The {@link ChartOfAccounts} (currently unused in this specific method logic but could be for future enhancements).
	 * @return {@code true} if {@code selectedFundNames} is null or empty (implying no filter),
	 *         or if the {@code account} is not null, has associated funds, and at least one of its
	 *         associated funds is in the {@code selectedFundNames} list. Returns {@code false} otherwise.
	 */
	private static boolean doesAccountMatchFunds(	Account account, List<String> selectedFundNames,
													ChartOfAccounts chartOfAccounts)
	{
		
		if (selectedFundNames == null || selectedFundNames.isEmpty())
		{
			return true; // No fund filter applied, so account matches by default.
		}
		
		if (account == null)
		{
			return false; // Null account cannot match.
		}
		
		List<String> associatedFunds = account.getAssociatedFundIds();
		
		if (associatedFunds == null || associatedFunds.isEmpty())
		{
			return false; // Account has no associated funds, so cannot match specific fund
			// selection.
		}
		
		for (String fundId : associatedFunds)
		{
			
			if (fundId != null && selectedFundNames.contains(fundId))
			{
				return true; // Account is associated with at least one of the selected
								// funds.
			}
			
		}
		
		return false; // Account is not associated with any of the selected funds.
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
	static Map<String, Object> prepareBalanceSheetContext(	ReportContext context, 
	                                                      	Ledger ledger,
															ChartOfAccounts chartOfAccounts)
	{
		Map<String, BigDecimal> assetTotals = new HashMap<>();
		Map<String, BigDecimal> liabilityTotals = new HashMap<>();
		Map<String, BigDecimal> equityTotals = new HashMap<>();
		
		if (context.getEndDate() == null)
		{
			throw new IllegalArgumentException("End date must be provided for balance sheet.");
		}
		
		LocalDate reportEndDate = context.getEndDate();
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && 
			!selectedFundNames.isEmpty());
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			if (account == null || account.getAccountType() == null || account.getName() == null) 

				continue;
			
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
			{
				continue;
			}
			
			BigDecimal finalBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger,
				chartOfAccounts, selectedFundNames, applyFundFilter);
			
			AccountType accountType = account.getAccountType(); // Use direct enum
			
			if (accountType == null)
			{
				LOGGER.warning("BS: Account type is null for account: " + account.getName());
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
		LocalDate netIncomeStartDate = (context.getStartDate() != null) ? context.getStartDate() :
			reportEndDate.withDayOfYear(1); // Default to start of the year of
											// reportEndDate
		
		if (netIncomeStartDate.isAfter(reportEndDate))
		{
			// If user somehow provides start date after end date for BS context, default to
			// year start
			netIncomeStartDate = reportEndDate.withDayOfYear(1);
		}
		
		netIncomeCalcContext.setStartDate(netIncomeStartDate);
		netIncomeCalcContext.setEndDate(reportEndDate);
		netIncomeCalcContext.setFundIds(selectedFundNames);
		
		Map<String, Object> incomeStatementData =
			prepareIncomeStatementContext(netIncomeCalcContext, ledger, chartOfAccounts);
		BigDecimal currentPeriodNetIncome =
			(BigDecimal) incomeStatementData.getOrDefault("netIncome", BigDecimal.ZERO);
		

		equityTotals.put("Current Period Net Income", currentPeriodNetIncome);
		
		
		BigDecimal totalAssets =
			assetTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalLiabilities =
			liabilityTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalEquity = // This now includes the conceptual "Current Period Net
									// Income"
			equityTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);
		
		if (totalAssets.compareTo(totalLiabilitiesAndEquity) != 0)
		{
			LOGGER.warning(
				"Balance Sheet (fund-filtered: " + applyFundFilter + ") out of balance! Assets: " +
					totalAssets + ", Liabilities + Equity: " + totalLiabilitiesAndEquity +
					". Difference: " + totalAssets.subtract(totalLiabilitiesAndEquity));
		}
		
		List<Map<String, Object>> assetItems = new ArrayList<>();
		assetTotals.forEach((name, bal) -> assetItems.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> liabilityItems = new ArrayList<>();
		liabilityTotals
			.forEach((name, bal) -> liabilityItems.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> equityItems = new ArrayList<>();
		equityTotals.entrySet().stream()
			// .filter(entry -> !entry.getKey().equals("Current Period Net Income")) // No
			// longer filter here if it's part of totalEquity
			.forEach(entry -> equityItems
				.add(Map.of("name", entry.getKey(), "amount", entry.getValue())));
		
		Map<String, Object> jxlsContext = new HashMap<>();
		jxlsContext.put("assetItems", assetItems);
		jxlsContext.put("liabilityItems", liabilityItems);
		jxlsContext.put("equityItems", equityItems); // Will include "Current Period Net
														// Income" line
		jxlsContext.put("totalAssets", totalAssets);
		jxlsContext.put("totalLiabilities", totalLiabilities);
		jxlsContext.put("totalEquity", totalEquity); // This total now reflects equity
														// including current net income
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
	 * Calculates the balance of the provided account using the supplied
	 * collection of accounting entries. The account's opening balance is used
	 * as the starting value and each entry is applied according to the account's
	 * {@link AccountSide increase side}.
	 *
	 * @param account the {@link Account} whose balance should be calculated
	 * @param entries the accounting entries affecting the account. Entries for
	 *                other accounts are ignored
	 * @return the resulting balance as a {@link BigDecimal}
	 */
	public static BigDecimal calculateBalanceForAccount(Account account,
														Collection<AccountingEntry> entries)
	{
		
		if (account == null)
		{
			throw new NullPointerException("Account cannot be null for balance calculation.");
		}
		
		BigDecimal balance =
			account.getOpeningBalance() == null ? BigDecimal.ZERO : account.getOpeningBalance();
		
		if (entries == null)
		{
			return balance;
		}
		
		AccountSide increaseSide = account.getIncreaseSide();
		
		if (increaseSide == null)
		{
			LOGGER.warning(
				"Account " + account.getAccountNumber() + " has no defined increase side.");
			return balance;
		}
		
		for (AccountingEntry entry : entries)
		{
			if (entry == null || entry.getAmount() == null)
				continue;
			
			if (!account.getAccountNumber().equals(entry.getAccountNumber()))
			{
				continue;
			}
			
			if (increaseSide == AccountSide.DEBIT)
			{
				if (entry.getAccountSide() == AccountSide.DEBIT)
					balance = balance.add(entry.getAmount());
				else
					balance = balance.subtract(entry.getAmount());
			}
			else
			{
				if (entry.getAccountSide() == AccountSide.CREDIT)
					balance = balance.add(entry.getAmount());
				else
					balance = balance.subtract(entry.getAmount());
			}
			
		}
		
		return balance;
	}
	
	/**
	 * Calculates the balance of a specific account as of a given date.
	 * This involves starting with the account's opening balance and then applying all relevant
	 * transaction entries up to (and including) the specified date.
	 * If {@code applyFundFilter} is true, the opening balance and transactions are considered only if the account
	 * matches the {@code selectedFundNames}.
	 *
	 * @param account The {@link Account} for which to calculate the balance.
	 * @param date The date as of which the balance should be calculated.
	 * @param ledger The {@link Ledger} containing all transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} (currently used by {@code doesAccountMatchFunds}).
	 * @param selectedFundNames A list of fund names for filtering if {@code applyFundFilter} is true.
	 * @param applyFundFilter A boolean indicating whether to filter by {@code selectedFundNames}.
	 * @return The calculated balance of the account as a {@link BigDecimal}.
	 * @throws NullPointerException if {@code account}, {@code date}, or {@code ledger} is null.
	 */
	static BigDecimal getAccountBalanceAsOfDate(Account account, LocalDate date, Ledger ledger,
												ChartOfAccounts chartOfAccounts,
												List<String> selectedFundNames,
												boolean applyFundFilter)
	{
		if (account == null)
			throw new NullPointerException("Account cannot be null for balance calculation.");
		if (date == null)
			throw new NullPointerException("Date cannot be null for balance calculation.");
		if (ledger == null)
			throw new NullPointerException("Ledger cannot be null for balance calculation.");
		
		if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
		{
			return BigDecimal.ZERO;
		}
		
		long endDateMillisInclusive = // Inclusive of the 'date'
			date.atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		List<AccountingEntry> relevantEntries = new ArrayList<>();
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions != null)
		{
			
			for (AccountingTransaction transaction : transactions)
			{
				
				if (transaction == null ||
					transaction.getBookingDateTimestamp() > endDateMillisInclusive)
				{
					continue;
				}
				
				Set<AccountingEntry> entries = transaction.getEntries();
				if (entries == null)
					continue;
				
				for (AccountingEntry entry : entries)
				{
					if (entry == null || entry.getAmount() == null)
						continue;
					
					if (!account.getAccountNumber().equals(entry.getAccountNumber()))
					{
						continue;
					}
					
					relevantEntries.add(entry);
				}
				
			}
			
		}
		
		return calculateBalanceForAccount(account, relevantEntries);
	}
	
	/**
	 * Prepares the context map with data needed for generating a Trial Balance report using JXLS.
	 * Calculates debit and credit balances for all accounts as of the report end date,
	 * optionally filtering by selected funds and considering a start date for transaction inclusion.
	 *
	 * @param context The {@link ReportContext} containing report criteria like end date, start date (optional), and fund IDs.
	 * @param ledger The {@link Ledger} containing all accounting transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} used to get the list of accounts.
	 * @return A {@link Map} suitable for use as a JXLS context, containing a list of trial balance items (account number, name, debit, credit),
	 *         total debits, total credits, report date information, and a flag indicating if totals match.
	 * @throws IllegalArgumentException if end date is not provided in the {@code context}.
	 */
	static Map<String, Object> prepareTrialBalanceContext(	ReportContext context, Ledger ledger,
															ChartOfAccounts chartOfAccounts)
	{
		List<Map<String, Object>> trialBalanceItems = new ArrayList<>();
		BigDecimal totalDebits = BigDecimal.ZERO;
		BigDecimal totalCredits = BigDecimal.ZERO;
		
		if (context.getEndDate() == null)
		{
			throw new IllegalArgumentException("End date must be provided for Trial Balance.");
		}
		
		LocalDate reportEndDate = context.getEndDate();
		long reportEndDateMillisExclusive = // Transactions strictly before the start of the
											// next day
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
		long reportStartDateMillis = 0; // Default to beginning of time if no start date
		
		if (context.getStartDate() != null)
		{
			reportStartDateMillis =
				context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		}
		
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions == null)
		{
			transactions = new ArrayList<AccountingTransaction>(); // Ensure non-null
			LOGGER.info("No transactions found in the ledger for Trial Balance.");
		}
		
		List<Account> accounts =
			(chartOfAccounts != null && chartOfAccounts.getAccounts() != null) ?
				chartOfAccounts.getAccounts() : new ArrayList<Account>(); // Ensure non-null
		
		for (Account account : accounts)
		{
			
			if (account == null || account.getAccountNumber() == null ||
				account.getName() == null || account.getIncreaseSide() == null ||
				account.getAccountType() == null) 
			{
				LOGGER.warning("TB: Skipping account with missing critical information: " +
					(account != null ? account.getAccountNumber() : "null account object"));
				continue;
			}
			
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
			{
				continue; // Skip account if it doesn't match fund filter
			}
			
			// Calculate balance for this account considering the period
			BigDecimal accountBalance =
				account.getOpeningBalance() == null ? BigDecimal.ZERO : account.getOpeningBalance();
			
			for (AccountingTransaction transaction : transactions)
			{
				
				if (transaction == null ||
					transaction.getBookingDateTimestamp() >= reportEndDateMillisExclusive || 
					transaction.getBookingDateTimestamp() < reportStartDateMillis) 
				{
					continue;
				}
				
				Set<AccountingEntry> entries = transaction.getEntries();
				if (entries == null)
					continue;
				
				for (AccountingEntry entry : entries)
				{
					
					if (entry == null ||
						!account.getAccountNumber().equals(entry.getAccountNumber()) ||
						entry.getAmount() == null)
					{
						continue;
					}
					
					
					AccountSide increaseSide = account.getIncreaseSide(); // Use direct enum
					if (increaseSide == null)
						continue; // Should not happen
						
					if (increaseSide == AccountSide.DEBIT)
					{
						if (entry.getAccountSide() == AccountSide.DEBIT)
							accountBalance = accountBalance.add(entry.getAmount());
						else // CREDIT
							accountBalance = accountBalance.subtract(entry.getAmount());
					}
					else // increaseSide is CREDIT
					{
						if (entry.getAccountSide() == AccountSide.CREDIT)
							accountBalance = accountBalance.add(entry.getAmount());
						else // DEBIT
							accountBalance = accountBalance.subtract(entry.getAmount());
					}
					
				}
				
			}
			
			BigDecimal finalDebitAmount = BigDecimal.ZERO;
			BigDecimal finalCreditAmount = BigDecimal.ZERO;
			
			AccountSide increaseSide = account.getIncreaseSide(); // Use direct enum
			if (increaseSide == null)
				continue;
			
			if (increaseSide == AccountSide.DEBIT)
			{
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
					finalDebitAmount = accountBalance;
				else
					finalCreditAmount = accountBalance.abs(); // Negative balance for
																// debit-normal account
																// shown as credit
			}
			else // increaseSide is CREDIT
			{
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
					finalCreditAmount = accountBalance;
				else
					finalDebitAmount = accountBalance.abs(); // Negative balance for
																// credit-normal account
																// shown as debit
			}
			
			if (finalDebitAmount.compareTo(BigDecimal.ZERO) != 0 ||
				finalCreditAmount.compareTo(BigDecimal.ZERO) != 0 ||
				account.getOpeningBalance().compareTo(BigDecimal.ZERO) != 0)
			{ // Add if opening balance was non-zero even if period end is zero
				Map<String, Object> item = new HashMap<>();
				item.put("accountNumber", account.getAccountNumber());
				item.put("accountName", account.getName());
				item.put("debit", finalDebitAmount);
				item.put("credit", finalCreditAmount);
				trialBalanceItems.add(item);
				
				totalDebits = totalDebits.add(finalDebitAmount);
				totalCredits = totalCredits.add(finalCreditAmount);
			}
			
		}
		
		// Round totals to 2 decimal places for comparison, common in financial reports
		totalDebits = totalDebits.setScale(2, RoundingMode.HALF_UP);
		totalCredits = totalCredits.setScale(2, RoundingMode.HALF_UP);
		
		if (totalDebits.compareTo(totalCredits) != 0)
		{
			LOGGER.warning("Trial Balance (fund-filtered: " + applyFundFilter +
				") totals do not match! Debits: " + totalDebits + ", Credits: " + totalCredits +
				". Difference: " + totalDebits.subtract(totalCredits));
		}
		
		Map<String, Object> jxlsContext = new HashMap<>();
		jxlsContext.put("trialBalanceItems", trialBalanceItems);
		jxlsContext.put("totalDebits", totalDebits);
		jxlsContext.put("totalCredits", totalCredits);
		jxlsContext.put("reportStartDate", context.getStartDate() != null ?
			context.getStartDate().toString() : "Beginning of Records");
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		jxlsContext.put("totalsMatch", totalDebits.compareTo(totalCredits) == 0);
		
		return jxlsContext;
	}
	
	/**
	 * Prepares the context map with data needed for generating a Cash Flow Statement using JXLS.
	 * This method calculates cash flows from operating, investing, and financing activities
	 * for the specified report period, optionally filtering by funds.
	 * It uses an indirect method starting with net income for operating activities.
	 *
	 * @param context The {@link ReportContext} containing report criteria (start/end dates, fund IDs).
	 * @param ledger The {@link Ledger} with all transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} for account lookups and type information.
	 * @return A {@link Map} suitable for JXLS context, containing lists of items for each cash flow section,
	 *         subtotals, net changes in cash, and beginning/ending cash balances.
	 * @throws IllegalArgumentException if start date or end date is not provided in {@code context}.
	 */
	static Map<String, Object> prepareCashFlowStatementContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts)
	{
		
		if (context.getStartDate() == null || context.getEndDate() == null)
		{
			throw new IllegalArgumentException(
				"Start date and end date must be provided for Cash Flow Statement.");
		}
		
		Map<String, Object> jxlsContext = new HashMap<>();
		List<Map<String, Object>> operatingActivitiesItems = new ArrayList<>();
		List<Map<String, Object>> investingActivitiesItems = new ArrayList<>();
		List<Map<String, Object>> financingActivitiesItems = new ArrayList<>();
		
		LocalDate reportStartDate = context.getStartDate();
		LocalDate reportEndDate = context.getEndDate();
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
		jxlsContext.put("reportStartDate", reportStartDate.toString());
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		
		ReportContext incomeStatementPeriodContext = new ReportContext();
		incomeStatementPeriodContext.setStartDate(reportStartDate);
		incomeStatementPeriodContext.setEndDate(reportEndDate);
		incomeStatementPeriodContext.setFundIds(selectedFundNames);
		Map<String, Object> incomeStatementContext =
			prepareIncomeStatementContext(incomeStatementPeriodContext, ledger, chartOfAccounts);
		BigDecimal netIncome =
			(BigDecimal) incomeStatementContext.getOrDefault("netIncome", BigDecimal.ZERO);
		jxlsContext.put("netIncome", netIncome);
		
		List<Account> cashEquivalentAccounts = new ArrayList<>();
		Set<String> cashEquivalentAccountNames = new HashSet<>(); // Using names for lookup
																	// in working capital
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			AccountType accType = account.getAccountType(); // Use direct enum
			
			if (accType != null)
			{
				
				if (accType == AccountType.BANK || accType == AccountType.CASH ||
					accType == AccountType.CHECKING) // Assuming CHECKING is cash equivalent
				{
					
					if (applyFundFilter &&
						!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
					{
						continue; // Skip if this cash account doesn't match fund filter
					}
					
					cashEquivalentAccounts.add(account);
					
					if (account.getName() != null)
					{ // Guard against null name
						cashEquivalentAccountNames.add(account.getName());
					}
					
				}
				
			}
			
		}
		
		BigDecimal cashAtEndOfPeriod = BigDecimal.ZERO;
		
		for (Account acc : cashEquivalentAccounts)
		{
			cashAtEndOfPeriod = cashAtEndOfPeriod.add(getAccountBalanceAsOfDate(acc, reportEndDate,
				ledger, chartOfAccounts, selectedFundNames, true)); 
		}
		
		jxlsContext.put("cashAtEndOfPeriod", cashAtEndOfPeriod);
		
		BigDecimal cashAtBeginningOfPeriod = BigDecimal.ZERO;
		LocalDate beginningDate = reportStartDate.minusDays(1);
		
		for (Account acc : cashEquivalentAccounts)
		{
			cashAtBeginningOfPeriod = cashAtBeginningOfPeriod.add(getAccountBalanceAsOfDate(acc,
				beginningDate, ledger, chartOfAccounts, selectedFundNames, true));
		}
		
		jxlsContext.put("cashAtBeginningOfPeriod", cashAtBeginningOfPeriod);
		
		BigDecimal netChangeInCashActual = cashAtEndOfPeriod.subtract(cashAtBeginningOfPeriod);
		jxlsContext.put("netChangeInCashActual", netChangeInCashActual);
		
		BigDecimal totalOperatingAdjustments = BigDecimal.ZERO;
		BigDecimal totalDepreciationAmortization = BigDecimal.ZERO;
		Set<String> deprAmortAccountNames =
			Set.of("Depreciation Expense", "Amortization Expense", "Depreciation", "Amortization"); 
		long periodStartDateMillis =
			reportStartDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		long periodEndDateMillisExclusive =
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			
			if (account.getName() != null && deprAmortAccountNames.contains(account.getName()))
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				// Sum debit entries to depreciation/amortization accounts during the period
				for (AccountingTransaction transaction : ledger.getTransactions())
				{
					
					if (transaction.getBookingDateTimestamp() >= periodStartDateMillis &&
						transaction.getBookingDateTimestamp() < periodEndDateMillisExclusive)
					{
						
						for (AccountingEntry entry : transaction.getEntries())
						{
							
							if (entry.getAccountNumber().equals(account.getAccountNumber()) &&
								entry.getAccountSide() == AccountSide.DEBIT) // Depreciation/Amortization
																				// is an
																				// expense
							{
								totalDepreciationAmortization =
									totalDepreciationAmortization.add(entry.getAmount());
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		if (totalDepreciationAmortization.compareTo(BigDecimal.ZERO) != 0)
		{
			operatingActivitiesItems.add(Map.of("name", "Depreciation & Amortization", "amount",
				totalDepreciationAmortization));
			totalOperatingAdjustments =
				totalOperatingAdjustments.add(totalDepreciationAmortization);
		}
		
		// Define standard working capital accounts
		Map<String,
			String> workingCapitalConfig = new HashMap<>(Map.of("Accounts Receivable", "asset",
				"Inventory", "asset", "Prepaid Expenses", "asset", "Accounts Payable", "liability",
				"Accrued Expenses", "liability", "Deferred Revenue", "liability"));
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			
			if (account.getName() != null && workingCapitalConfig.containsKey(account.getName()))
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				String category = workingCapitalConfig.get(account.getName());
				BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger,
					chartOfAccounts, selectedFundNames, true); // applyFundFilter true
				BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDate, // Use
																								// same
																								// beginningDate
																								// as
																								// cash
																								// balances
					ledger, chartOfAccounts, selectedFundNames, true); // applyFundFilter
																		// true
				BigDecimal change = endBalance.subtract(beginningBalance);
				
				if (change.compareTo(BigDecimal.ZERO) == 0)
					continue; // No change, no cash flow impact
					
				// Determine cash flow impact:
				// Increase in asset = cash outflow (negative adjustment)
				// Decrease in asset = cash inflow (positive adjustment)
				// Increase in liability = cash inflow (positive adjustment)
				// Decrease in liability = cash outflow (negative adjustment)
				BigDecimal adjustmentAmount = "asset".equals(category) ? change.negate() : change;
				String itemName = (change.compareTo(BigDecimal.ZERO) > 0 ?
					("asset".equals(category) ? "Increase in " : "Increase in ") :
					("asset".equals(category) ? "Decrease in " : "Decrease in ")) +
					account.getName();
				
				operatingActivitiesItems.add(Map.of("name", itemName, "amount", adjustmentAmount));
				totalOperatingAdjustments = totalOperatingAdjustments.add(adjustmentAmount);
			}
			
		}
		
		jxlsContext.put("operatingActivitiesItems", operatingActivitiesItems);
		BigDecimal cashFromOperations = netIncome.add(totalOperatingAdjustments);
		jxlsContext.put("cashFromOperations", cashFromOperations);
		
		BigDecimal totalInvestingAdjustments = BigDecimal.ZERO;
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			AccountType accType = account.getAccountType();
			
			if (accType != null && accType == AccountType.FIXED_ASSET &&
				!cashEquivalentAccountNames.contains(account.getName()) && 
				!workingCapitalConfig.containsKey(account.getName())) 
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger,
					chartOfAccounts, selectedFundNames, true);
				BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDate,
					ledger, chartOfAccounts, selectedFundNames, true);
				BigDecimal change = endBalance.subtract(beginningBalance); // Increase in
																			// fixed asset
				
				if (change.compareTo(BigDecimal.ZERO) != 0)
				{
					BigDecimal cashFlowImpact = change.negate(); // Increase in asset = cash
																	// outflow
					investingActivitiesItems.add(
						Map.of("name", "Change in " + account.getName(), "amount", cashFlowImpact));
					totalInvestingAdjustments = totalInvestingAdjustments.add(cashFlowImpact);
				}
				
			}
			
		}
		
		jxlsContext.put("investingActivitiesItems", investingActivitiesItems);
		jxlsContext.put("cashFromInvesting", totalInvestingAdjustments);
		
		BigDecimal totalFinancingAdjustments = BigDecimal.ZERO;
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			AccountType accType = account.getAccountType();
			
			if (accType != null &&
				(accType == AccountType.LONG_TERM_LIABILITY || accType == AccountType.EQUITY))
			{
				

				if ("Current Period Net Income".equalsIgnoreCase(account.getName()))
				{
					continue;
				}
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger,
					chartOfAccounts, selectedFundNames, true);
				BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDate,
					ledger, chartOfAccounts, selectedFundNames, true);
				BigDecimal change = endBalance.subtract(beginningBalance); // Increase in
																			// L/E = cash
																			// inflow
				
				if (change.compareTo(BigDecimal.ZERO) != 0)
				{
					financingActivitiesItems
						.add(Map.of("name", "Change in " + account.getName(), "amount", change));
					totalFinancingAdjustments = totalFinancingAdjustments.add(change);
				}
				
			}
			
		}
		
		jxlsContext.put("financingActivitiesItems", financingActivitiesItems);
		jxlsContext.put("cashFromFinancing", totalFinancingAdjustments);
		
		BigDecimal netChangeInCashCalculated =
			cashFromOperations.add(totalInvestingAdjustments).add(totalFinancingAdjustments);
		jxlsContext.put("netChangeInCash", netChangeInCashCalculated); 
		jxlsContext.put("netChangeInCashCalculated", netChangeInCashCalculated); 
		
		// Add discrepancy check line
		BigDecimal discrepancy = netChangeInCashCalculated.subtract(netChangeInCashActual);
		jxlsContext.put("discrepancy", discrepancy);
		
		if (discrepancy.abs().compareTo(new BigDecimal("0.01")) > 0) 
		{
			LOGGER.warning(
				"Cash Flow Statement (fund-filtered: " + applyFundFilter + ") discrepancy: " +
					discrepancy + ". Calculated Net Change: " + netChangeInCashCalculated +
					", Actual Net Change (from balance sheet of cash accounts): " +
					netChangeInCashActual);
		}
		
		return jxlsContext;
	}
	
	/**
	 * Prepares the context map with data for a Budget vs. Actuals report using JXLS.
	 * Compares budgeted amounts (from the provided {@link Budget}) against actual transaction
	 * amounts for income and expense accounts within the report period.
	 * Supports fund filtering and pro-rating of annual budget amounts if periodic amounts are not available.
	 *
	 * @param context The {@link ReportContext} with report criteria (start/end dates, fund IDs).
	 * @param ledger The {@link Ledger} containing actual transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} for account lookups.
	 * @param budget The {@link Budget} object to compare against.
	 * @return A {@link Map} for JXLS context, including report items (account, budgeted, actual, variance),
	 *         totals for income/expenses/net, and report metadata.
	 * @throws IllegalArgumentException if required context (dates, budget) or models (ledger, COA) are null.
	 */
	static Map<String, Object> prepareBudgetVsActualsContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts,
																Budget budget)
	{
		
		if (context.getStartDate() == null || context.getEndDate() == null || budget == null ||
			ledger == null || chartOfAccounts == null)
		{
			throw new IllegalArgumentException(
				"Required context or models (dates, budget, ledger, COA) cannot be null for Budget vs. Actuals.");
		}
		
		Map<String, Object> jxlsContext = new HashMap<>();
		List<Map<String, Object>> reportItems = new ArrayList<>();
		
		LocalDate reportStartDate = context.getStartDate();
		LocalDate reportEndDate = context.getEndDate();
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
		jxlsContext.put("budgetName", budget.getBudgetName());
		jxlsContext.put("fiscalYear", budget.getFiscalYear());
		jxlsContext.put("reportStartDate", reportStartDate.toString());
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		
		Map<String, BigDecimal> actualAmounts = new HashMap<>();
		BigDecimal totalActualIncome = BigDecimal.ZERO;
		BigDecimal totalActualExpenses = BigDecimal.ZERO;
		
		long periodStartDateMillis =
			reportStartDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		long periodEndDateMillisExclusive =
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		if (ledger != null && ledger.getTransactions() != null)
		{
			
			for (AccountingTransaction transaction : ledger.getTransactions())
			{
				
				if (transaction.getBookingDateTimestamp() >= periodStartDateMillis &&
					transaction.getBookingDateTimestamp() < periodEndDateMillisExclusive)
				{
					
					for (AccountingEntry entry : transaction.getEntries())
					{
						Account account = chartOfAccounts.getAccount(entry.getAccountNumber());
						if (account == null || account.getAccountType() == null) 
							continue;
						
						if (applyFundFilter &&
							!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
						{
							continue;
						}
						
						AccountType accountType = account.getAccountType(); 
						BigDecimal currentActual =
							actualAmounts.getOrDefault(account.getAccountNumber(), BigDecimal.ZERO);
						BigDecimal amount = entry.getAmount();
						
						if (accountType == AccountType.INCOME)
						{
							if (entry.getAccountSide() == AccountSide.CREDIT) 
								currentActual = currentActual.add(amount);
							else // DEBIT
								currentActual = currentActual.subtract(amount);
						}
						else if (accountType == AccountType.EXPENSE)
						{
							if (entry.getAccountSide() == AccountSide.DEBIT) // Expense
																				// increases
																				// on debit
								currentActual = currentActual.add(amount);
							else // CREDIT
								currentActual = currentActual.subtract(amount);
						}
						
						actualAmounts.put(account.getAccountNumber(), currentActual);
					}
					
				}
				
			}
			
		}
		
		// Calculate total actual income and expenses from the aggregated actualAmounts
		// map
		for (Map.Entry<String, BigDecimal> entry : actualAmounts.entrySet())
		{
			Account account = chartOfAccounts.getAccount(entry.getKey());
			
			if (account != null && account.getAccountType() != null) // Use direct enum
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				AccountType type = account.getAccountType(); // Use direct enum
				if (type == AccountType.INCOME)
					totalActualIncome = totalActualIncome.add(entry.getValue());
				else if (type == AccountType.EXPENSE)
					totalActualExpenses = totalActualExpenses.add(entry.getValue());
			}
			
		}
		
		BigDecimal totalBudgetedIncome = BigDecimal.ZERO;
		BigDecimal totalBudgetedExpenses = BigDecimal.ZERO;
		int budgetFiscalYear = budget.getFiscalYear(); 
		long daysInFiscalYear = LocalDate.of(budgetFiscalYear, 1, 1).isLeapYear() ? 366 : 365;
		long daysInReportPeriod = ChronoUnit.DAYS.between(reportStartDate, reportEndDate) + 1; // Inclusive
																								// of
																								// end
																								// date
		
		for (BudgetLine line : budget.getBudgetLines())
		{
			String accountId = line.getAccountId();
			Account account = chartOfAccounts.getAccount(accountId);
			String accountName = (account != null && account.getName() != null) ?
				account.getName() : line.getAccountName(); // Prefer COA name
			
			if (account == null || account.getAccountType() == null) // Use direct enum
			{
				LOGGER.warning("BvA: Skipping budget line for account ID " + accountId +
					" as account or type is not found/valid in COA.");
				continue;
			}
			
			// Filter budget lines based on fund selection
			boolean lineIsRelevantForFundFilter = !applyFundFilter; // If no filter, all
																	// lines are relevant
			
			if (applyFundFilter)
			{
				
				if (line.getFundId() != null && !line.getFundId().trim().isEmpty())
				{
					
					// Line has a specific fund, check if it's in selected funds
					if (selectedFundNames.contains(line.getFundId()))
					{
						lineIsRelevantForFundFilter = true;
					}
					
				}
				else
				{
					
					// Line has no specific fund, check if its associated account matches
					// the general fund filter
					if (doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
					{
						lineIsRelevantForFundFilter = true;
					}
					
				}
				
			}
			
			if (!lineIsRelevantForFundFilter)
			{
				continue; // Skip this budget line as it doesn't match fund criteria
			}
			
			AccountType accountType = account.getAccountType(); // Use direct enum
			BigDecimal budgetedAmountForPeriod = BigDecimal.ZERO;
			boolean useProRatedAnnual = true; // Default to pro-rating annual total
			
			// Attempt to use periodic amounts if available and periodicity matches common
			// scenarios
			if (line.getPeriodicAmounts() != null && !line.getPeriodicAmounts().isEmpty())
			{
				
				if (line.getPeriodicity() == Periodicity.MONTHLY &&
					line.getPeriodicAmounts().size() == 12)
				{
					useProRatedAnnual = false; // We have monthly data, use it
					budgetedAmountForPeriod = BigDecimal.ZERO; // Reset for summing monthly
					
					for (int i = 0; i < 12; i++)
					{
						// Construct date for the i-th month of the budget's fiscal year
						// This assumes budget.fiscalYear is correctly aligned with the
						// budget lines' year context.
						LocalDate monthInFiscalYear =
							LocalDate.of(budget.getFiscalYear(), i + 1, 1);
						
						// Check if this month falls within the report's date range
						if (!monthInFiscalYear.isBefore(reportStartDate.withDayOfMonth(1)) &&
							!monthInFiscalYear.isAfter(reportEndDate.withDayOfMonth(1)))
						{
							budgetedAmountForPeriod =
								budgetedAmountForPeriod.add(line.getPeriodicAmounts().get(i));
						}
						
					}
					
				}
				else if (line.getPeriodicity() == Periodicity.QUARTERLY &&
					line.getPeriodicAmounts().size() == 4)
				{
					useProRatedAnnual = false; // We have quarterly data
					budgetedAmountForPeriod = BigDecimal.ZERO;
					
					for (int i = 0; i < 4; i++)
					{
						LocalDate quarterStartDate =
							LocalDate.of(budget.getFiscalYear(), (i * 3) + 1, 1);
						LocalDate quarterEndDate = quarterStartDate.plusMonths(3).minusDays(1);
						
						// Check if the quarter overlaps with the report period
						if (!(quarterEndDate.isBefore(reportStartDate) ||
							quarterStartDate.isAfter(reportEndDate)))
						{
							budgetedAmountForPeriod =
								budgetedAmountForPeriod.add(line.getPeriodicAmounts().get(i));
						}
						
					}
					
				} // Add other periodicities like ANNUAL if periodicAmounts might contain a
					// single annual value
				else if (line.getPeriodicity() == Periodicity.ANNUAL &&
					line.getPeriodicAmounts().size() == 1)
				{
					// If ANNUAL and one periodic amount, assume it's the annual total, then
					// pro-rate it.
					// This is similar to using getTotalBudgetedAmount(), so pro-rating
					// logic below will handle it.
					// No need to set useProRatedAnnual = false here if we intend to
					// pro-rate this annual figure.
					// If it's meant to be used as-is ONLY if report period is full year,
					// logic would be different.
				}
				
			}
			
			// If periodic amounts weren't suitable or available, pro-rate the total annual
			// budgeted amount
			if (useProRatedAnnual && line.getTotalBudgetedAmount() != null)
			{
				
				if (daysInFiscalYear > 0)
				{ // Avoid division by zero
					budgetedAmountForPeriod =
						line.getTotalBudgetedAmount().multiply(new BigDecimal(daysInReportPeriod))
							.divide(new BigDecimal(daysInFiscalYear), 2, RoundingMode.HALF_UP);
				}
				else
				{
					budgetedAmountForPeriod = BigDecimal.ZERO; // Or handle error
				}
				
			}
			
			if (accountType == AccountType.INCOME)
				totalBudgetedIncome = totalBudgetedIncome.add(budgetedAmountForPeriod);
			else if (accountType == AccountType.EXPENSE)
				totalBudgetedExpenses = totalBudgetedExpenses.add(budgetedAmountForPeriod);
			
			BigDecimal actualAmountForPeriod =
				actualAmounts.getOrDefault(accountId, BigDecimal.ZERO);
			BigDecimal variance = actualAmountForPeriod.subtract(budgetedAmountForPeriod); // Actual
																							// -
																							// Budgeted
			BigDecimal variancePercent = BigDecimal.ZERO;
			
			if (budgetedAmountForPeriod.compareTo(BigDecimal.ZERO) != 0) // Avoid division
																			// by zero
			{
				variancePercent =
					variance.divide(budgetedAmountForPeriod.abs(), 4, RoundingMode.HALF_UP) // Use
																							// abs
																							// for
																							// percentage
																							// base
						.multiply(new BigDecimal("100"));
			}
			else if (actualAmountForPeriod.compareTo(BigDecimal.ZERO) != 0)
			{
				variancePercent = new BigDecimal("100.00"); // Or -100.00 if actual is
															// negative, or handle as "N/A"
			}
			
			Map<String, Object> item = new HashMap<>();
			item.put("accountCategory", accountType.toString()); // Add category for
																	// grouping in report
			item.put("accountId", accountId);
			item.put("accountName", accountName);
			item.put("budgetedAmount", budgetedAmountForPeriod);
			item.put("actualAmount", actualAmountForPeriod);
			item.put("variance", variance);
			item.put("variancePercent", variancePercent);
			reportItems.add(item);
		}
		
		// Sort items: Income first, then Expenses, then by account name
		reportItems.sort(Comparator
			.comparing((Map<String, Object> item) -> "INCOME"
				.equals(item.get("accountCategory").toString()) ? 0 : 1)
			.thenComparing(item -> (String) item.get("accountName")));
		
		jxlsContext.put("reportItems", reportItems);
		jxlsContext.put("totalBudgetedIncome", totalBudgetedIncome);
		jxlsContext.put("totalActualIncome", totalActualIncome);
		jxlsContext.put("totalIncomeVariance", totalActualIncome.subtract(totalBudgetedIncome));
		jxlsContext.put("totalBudgetedExpenses", totalBudgetedExpenses);
		jxlsContext.put("totalActualExpenses", totalActualExpenses);
		jxlsContext.put("totalExpenseVariance",
			totalActualExpenses.subtract(totalBudgetedExpenses));
		BigDecimal totalBudgetedNet = totalBudgetedIncome.subtract(totalBudgetedExpenses);
		BigDecimal totalActualNet = totalActualIncome.subtract(totalActualExpenses);
		jxlsContext.put("totalBudgetedNet", totalBudgetedNet);
		jxlsContext.put("totalActualNet", totalActualNet);
		jxlsContext.put("totalNetVariance", totalActualNet.subtract(totalBudgetedNet));
		return jxlsContext;
	}
	
	/**
	 * Prepares the context map with data for an Account Activity Detail report using JXLS.
	 * For each specified account, it lists all transactions within the report period,
	 * calculating a running balance. It also shows opening and closing balances for each account.
	 * Supports fund filtering if fund IDs are provided in the context.
	 *
	 * @param context The {@link ReportContext} providing report criteria, including a list of account IDs
	 *                for which to generate details, start/end dates, and optional fund IDs.
	 * @param ledger The {@link Ledger} containing all transactions.
	 * @param chartOfAccounts The {@link ChartOfAccounts} for account lookups.
	 * @return A {@link Map} for JXLS context, containing a list of "accountsDetail". Each item in this list
	 *         is a map itself, holding details for one account (name, number, opening/closing balance, and a list of its entries).
	 * @throws IllegalArgumentException if account IDs, start date, or end date are missing in {@code context}.
	 */
	static Map<String, Object> prepareAccountActivityContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts)
	{
		
		if (context.getAccountIdsForDetailReport() == null ||
			context.getAccountIdsForDetailReport().isEmpty() || context.getStartDate() == null ||
			context.getEndDate() == null)
		{
			LOGGER.warning( 
				"Account Activity Detail: Missing required criteria (account IDs, start date, or end date).");
			throw new IllegalArgumentException(
				"Account IDs, Start Date, and End Date are required for Account Activity Detail report.");
		}
		
		Map<String, Object> jxlsContext = new HashMap<>();
		List<Map<String, Object>> accountsReportDataList = new ArrayList<>();
		
		jxlsContext.put("reportStartDate", context.getStartDate().toString());
		jxlsContext.put("reportEndDate", context.getEndDate().toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		
		List<String> selectedAccountIds = context.getAccountIdsForDetailReport();
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
		
		for (String accountId : selectedAccountIds)
		{
			Account account = chartOfAccounts.getAccount(accountId);
			
			if (account == null)
			{
				LOGGER.warning("Account Activity Detail: Account not found for ID: " + accountId);
				continue;
			}
			
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
			{
				LOGGER.info("Account Activity Detail: Skipping account " + accountId +
					" as it does not match selected fund criteria.");
				continue;
			}
			
			BigDecimal openingBalance =
				getAccountBalanceAsOfDate(account, context.getStartDate().minusDays(1), ledger,
					chartOfAccounts, selectedFundNames, applyFundFilter); 
			
			List<Map<String, Object>> entryItems = new ArrayList<>();
			BigDecimal runningBalance = openingBalance;
			
			List<AccountingTransaction> accountTransactions = new ArrayList<>();
			
			if (ledger.getTransactions() != null)
			{
				
				for (AccountingTransaction tx : ledger.getTransactions())
				{
					
					// Filter transactions by date range
					if (tx.getBookingDateTimestamp() >=
						context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant()
							.toEpochMilli() &&
						tx.getBookingDateTimestamp() < context.getEndDate().plusDays(1) // Exclusive
																						// end
							.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
					{
						boolean relevantToThisAccount = false;
						
						for (AccountingEntry entry : tx.getEntries())
						{
							
							if (entry.getAccountNumber().equals(accountId))
							{
								relevantToThisAccount = true;
								break;
							}
							
						}
						
						if (relevantToThisAccount)
						{
							accountTransactions.add(tx);
						}
						
					}
					
				}
				
			}
			
			// Sort transactions by date, then perhaps by an internal order if available
			accountTransactions
				.sort(Comparator.comparingLong(AccountingTransaction::getBookingDateTimestamp));
			
			for (AccountingTransaction transaction : accountTransactions)
			{
				LocalDate transactionDate =
					Instant.ofEpochMilli(transaction.getBookingDateTimestamp())
						.atZone(ZoneId.systemDefault()).toLocalDate();
				
				// Each transaction might have multiple entries; we are interested in the
				// one for `accountId`
				for (AccountingEntry entry : transaction.getEntries())
				{
					
					if (entry.getAccountNumber().equals(accountId))
					{
						Map<String, Object> entryData = new HashMap<>();
						entryData.put("date", transactionDate.format(DATE_FORMATTER));
						entryData.put("transactionId", transaction.getBookingDateTimestamp()); // Or
																								// a
																								// more
																								// user-friendly
																								// ID
																								// if
																								// available
						
						// Attempt to find the "other side" of the transaction for a more
						// meaningful description
						String description =
							transaction.getMemo() != null ? transaction.getMemo() : "";
						
						if (transaction.getEntries().size() > 1)
						{
							
							for (AccountingEntry otherEntry : transaction.getEntries())
							{
								
								if (!otherEntry.getAccountNumber().equals(accountId))
								{
									Account otherAccount =
										chartOfAccounts.getAccount(otherEntry.getAccountNumber());
									
									if (otherAccount != null && otherAccount.getName() != null)
									{
										description =
											description.isEmpty() ? otherAccount.getName() :
												description + " / " + otherAccount.getName();
										// Take the first "other" account name for
										// simplicity
										break;
									}
									
								}
								
							}
							
						}
						
						if (description.isEmpty())
							description = "Journal Entry";
						entryData.put("description", description);
						
						BigDecimal debitAmount = (entry.getAccountSide() == AccountSide.DEBIT) ?
							entry.getAmount() : BigDecimal.ZERO;
						BigDecimal creditAmount = (entry.getAccountSide() == AccountSide.CREDIT) ?
							entry.getAmount() : BigDecimal.ZERO;
						entryData.put("debit", debitAmount);
						entryData.put("credit", creditAmount);
						
						AccountSide increaseSide = account.getIncreaseSide();
						if (increaseSide == null)
							continue;
						
						if (increaseSide == AccountSide.DEBIT)
						{
							runningBalance = runningBalance.add(debitAmount).subtract(creditAmount);
						}
						else // CREDIT
						{
							runningBalance = runningBalance.subtract(debitAmount).add(creditAmount);
						}
						
						entryData.put("runningBalance", runningBalance);
						entryItems.add(entryData);

						break;
					}
					
				}
				
			}
			
			Map<String, Object> singleAccountReportData = new HashMap<>();
			singleAccountReportData.put("accountName", account.getName());
			singleAccountReportData.put("accountNumber", account.getAccountNumber());
			singleAccountReportData.put("openingBalance", openingBalance);
			singleAccountReportData.put("entries", entryItems);
			singleAccountReportData.put("closingBalance", runningBalance);
			accountsReportDataList.add(singleAccountReportData);
		}
		
		jxlsContext.put("accountsDetail", accountsReportDataList);
		return jxlsContext;
	}
	
	
	/**
	 * Generates a financial report based on the provided {@link ReportContext}, {@link Ledger},
	 * and {@link ChartOfAccounts} using JXLS templates.
	 * The specific report generated (e.g., Income Statement, Balance Sheet) is determined by
	 * {@link ReportContext#getReportType()}.
	 *
	 * @param context The context defining the report to be generated, including type, dates, and filters.
	 * @param ledger The ledger containing transaction data.
	 * @param chartOfAccounts The chart of accounts.
	 * @return A {@link File} object representing the generated Excel report.
	 * @throws IOException If an error occurs during template loading or file writing.
	 * @throws IllegalArgumentException If required parameters in {@code context} are missing for the specified report type,
	 *                                  or if {@code context} or {@code reportType} is null.
	 */
	public static File generate(ReportContext context, Ledger ledger,
								ChartOfAccounts chartOfAccounts) throws IOException
	{
		
		if (context == null || context.getReportType() == null)
		{
			throw new IllegalArgumentException("ReportContext and reportType cannot be null.");
		}
		
		String reportType = context.getReportType();
		Map<String, Object> jxlsContext;
		String templateName;
		String outputFileNamePrefix;
		// Use ISO_LOCAL_DATE for filenames for consistency and to avoid locale-specific
		// characters
		String reportDateSuffix = (context.getEndDate() != null) ?
			context.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE) :
			LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
		String reportPeriodSuffix = (context.getStartDate() != null ?
			context.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE) + "_to_" : "") +
			reportDateSuffix;
		
		
		if ("income_statement".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getStartDate() == null ||
				context.getEndDate() == null)
			{
				throw new IllegalArgumentException(
					"Ledger, ChartOfAccounts, StartDate, and EndDate must be provided for Income Statement.");
			}
			
			jxlsContext = prepareIncomeStatementContext(context, ledger, chartOfAccounts);
			templateName = "income_statement_template.xlsx";
			outputFileNamePrefix = "income_statement_" + reportPeriodSuffix;
		}
		else if ("balance_sheet".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getEndDate() == null)
			{
				throw new IllegalArgumentException(
					"Ledger, ChartOfAccounts, and EndDate must be provided for Balance Sheet.");
			}
			
			jxlsContext = prepareBalanceSheetContext(context, ledger, chartOfAccounts);
			templateName = "balance_sheet_template.xlsx";
			outputFileNamePrefix = "balance_sheet_" + reportDateSuffix;
		}
		else if ("trial_balance".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getEndDate() == null)
			{ // StartDate can be null for TB
				throw new IllegalArgumentException(
					"Ledger, ChartOfAccounts, and EndDate must be provided for Trial Balance.");
			}
			
			jxlsContext = prepareTrialBalanceContext(context, ledger, chartOfAccounts);
			templateName = "trial_balance_template.xlsx";
			outputFileNamePrefix = "trial_balance_" + reportDateSuffix;
		}
		else if ("cash_flow_statement".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getStartDate() == null ||
				context.getEndDate() == null)
			{
				throw new IllegalArgumentException(
					"Ledger, ChartOfAccounts, StartDate, and EndDate must be provided for Cash Flow Statement.");
			}
			
			jxlsContext = prepareCashFlowStatementContext(context, ledger, chartOfAccounts);
			templateName = "cash_flow_statement_template.xlsx";
			outputFileNamePrefix = "cash_flow_statement_" + reportPeriodSuffix;
		}
		else if ("budget_vs_actuals".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getStartDate() == null ||
				context.getEndDate() == null || context.getSelectedBudget() == null)
			{
				throw new IllegalArgumentException(
					"Ledger, ChartOfAccounts, StartDate, EndDate, and a selected Budget must be provided for Budget vs. Actuals report.");
			}
			
			Budget selectedBudget = context.getSelectedBudget();
			jxlsContext =
				prepareBudgetVsActualsContext(context, ledger, chartOfAccounts, selectedBudget);
			templateName = "budget_vs_actuals_template.xlsx";
			outputFileNamePrefix = "budget_vs_actuals_" +
				(selectedBudget.getBudgetName() != null ?
					selectedBudget.getBudgetName().replace(" ", "_") : "UnknownBudget") +
				"_" + reportPeriodSuffix;
		}
		else if ("account_activity_detail".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getStartDate() == null ||
				context.getEndDate() == null || context.getAccountIdsForDetailReport() == null ||
				context.getAccountIdsForDetailReport().isEmpty())
			{
				throw new IllegalArgumentException(
					"Ledger, COA, StartDate, EndDate, and AccountIDs must be provided for Account Activity Detail.");
			}
			
			jxlsContext = prepareAccountActivityContext(context, ledger, chartOfAccounts);
			templateName = "account_activity_detail_template.xlsx";
			// Create a more generic filename if multiple accounts, or a specific one if
			// only one account.
			String accountPart = context.getAccountIdsForDetailReport().size() == 1 ?
				context.getAccountIdsForDetailReport().get(0).replace(" ", "_") :
				"multiple_accounts"; // Sanitize accountId for filename
			outputFileNamePrefix = "account_activity_" + accountPart + "_" + reportPeriodSuffix;
		}
		else
		{
			LOGGER
				.warning("Generating generic report (stub) for unknown report type: " + reportType);
			// Create a temporary file for the stub report
			File tempDir = new File(System.getProperty("java.io.tmpdir"));
			File outputFile = new File(tempDir,
				"generated_report_" + reportType + "_" + System.currentTimeMillis() + ".txt");
			
			try (java.io.PrintWriter writer =
				new java.io.PrintWriter(new FileOutputStream(outputFile)))
			{
				writer.println("Report Type: " + reportType);
				if (context.getStartDate() != null)
					writer.println("Start Date: " + context.getStartDate().toString());
				if (context.getEndDate() != null)
					writer.println("End Date: " + context.getEndDate().toString());
				writer.println("Generated on: " + LocalDate.now().toString());
				writer.println("This is a stub report for an unrecognized report type.");
			}
			
			LOGGER.info("Stub report generated: " + outputFile.getAbsolutePath());
			return outputFile;
		}
		
		// Define output directory within user's home directory for generated reports
		File outputDirectory =
			new File(System.getProperty("user.home"), "NonprofitBookkeepingReports");
		
		if (!outputDirectory.exists())
		{
			outputDirectory.mkdirs();
		}
		
		File outputFile = new File(outputDirectory, outputFileNamePrefix + ".xlsx");
		
		String templatePath = "/templates/" + templateName; // Assumes templates are in
															// src/main/resources/templates
		
		try (InputStream is = ReportService.class.getResourceAsStream(templatePath))
		{
			
			if (is == null)
			{
				LOGGER.log(Level.SEVERE, reportType + " template not found at " + templatePath);
				throw new IOException(reportType + " template not found at " + templatePath +
					". Ensure it is in the classpath under the 'templates' directory.");
			}
			
			try (OutputStream os = new FileOutputStream(outputFile))
			{
				Context jxlsInnerContext = new Context(jxlsContext);
				JxlsHelper.getInstance().processTemplate(is, os, jxlsInnerContext);
				LOGGER.info(
					reportType + " generated successfully at: " + outputFile.getAbsolutePath());
			}
			
		}
		
		return outputFile;
	}
	
	/**
	 * Registers a {@link LedgerReportWriter} for a specific report type.
	 * Note: This method is currently a stub and does not implement any registration logic.
	 *
	 * @param reportType The type of report the writer is for.
	 * @param writer The {@link LedgerReportWriter} instance.
	 */
	public void registerWriter(String reportType, LedgerReportWriter writer)
	{
		
		if (reportType == null || writer == null)
		{
			return;
		}
		
		this.writerMap.put(reportType, writer);
	}
	
	/**
	 * Lists metadata of previously generated reports.
	 * <p>
	 * This implementation scans the user's {@code NonprofitBookkeepingReports}
	 * directory (under the home folder) and returns basic information about
	 * any files it finds there. Each file's name, last-modified timestamp, and
	 * absolute path are captured in a {@link ReportMetadata} object.
	 * </p>
	 *
	 * @return A list of {@link ReportMetadata} describing files in the reports
	 *         output directory. If the directory does not exist or contains no
	 *         files, an empty list is returned.
	 */
	public static List<ReportMetadata> listGeneratedReports()
	{
		List<ReportMetadata> results = new ArrayList<>();
		
		File dir = new File(System.getProperty("user.home"), "NonprofitBookkeepingReports");
		
		if (dir.exists() && dir.isDirectory())
		{
			File[] files = dir.listFiles();
			
			if (files != null)
			{
				
				for (File f : files)
				{
					
					if (!f.isFile())
					{
						continue;
					}
					
					String created = java.time.Instant.ofEpochMilli(f.lastModified()).toString();
					results.add(new ReportMetadata(f.getName(), created, f.getAbsolutePath()));
				}
				
				results.sort((a, b) -> b.getCreated().compareTo(a.getCreated()));
			}
			
		}
		
		return results;
	}
	
	/**
	 * Generates a report based on the provided {@link ReportContext}.
	 * Note: This is a stub implementation and currently returns null.
	 * The actual report generation logic, potentially involving selection of different
	 * report engines (JXLS, JasperReports) or data preparation, needs to be implemented.
	 * This method might be intended as a primary entry point for report generation.
	 *
	 * @param ctx The {@link ReportContext} defining the report to be generated.
	 * @return A {@link File} object representing the generated report, or null if generation fails or is not implemented.
	 */
	public static File generate(ReportContext ctx)
	{
		
		try
		{
			return new ReportService().generateJasperReport(ctx, ctx.getOutputFormat());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
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
			prepareIncomeStatementJasperData(	ReportContext context, Ledger ledger,
												ChartOfAccounts chartOfAccounts)
	{
		
		List<IncomeStatementRowBean> reportData = new ArrayList<>();
		
		if (context.getStartDate() == null || context.getEndDate() == null)
		{
			LOGGER.warning(
				"Start date and end date must be provided for income statement data preparation.");
			return reportData; // Return empty list
		}
		
		if (ledger == null || chartOfAccounts == null)
		{
			LOGGER.warning("Ledger or Chart of Accounts not available for income statement data.");
			return reportData;
		}
		
		Map<String, BigDecimal> incomeAccountBalances = new HashMap<>();
		Map<String, BigDecimal> expenseAccountBalances = new HashMap<>();
		
		List<String> selectedFundIds = context.getFundIds();
		boolean applyFundFilter = (selectedFundIds != null && !selectedFundIds.isEmpty());
		
		long startDateMillis =
			context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		long endDateMillisExclusive = context.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC)
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
					
					Account account = chartOfAccounts.getAccount(entry.getAccountNumber());
					
					if (account == null || account.getAccountType() == null ||
						account.getName() == null)
					{
						LOGGER.warning(
							"IS Data: Account or critical account info not found for number: " +
								entry.getAccountNumber());
						continue;
					}
					
					if (applyFundFilter)
					{
						
						if (!doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts))
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
							incomeAccountBalances.getOrDefault(accountName, BigDecimal.ZERO);
						
						if (side == AccountSide.CREDIT)
						{ // Income increases on credit
							incomeAccountBalances.put(accountName, currentTotal.add(amount));
						}
						else if (side == AccountSide.DEBIT)
						{
							incomeAccountBalances.put(accountName, currentTotal.subtract(amount));
						}
						
					}
					else if (accountType == AccountType.EXPENSE)
					{
						BigDecimal currentTotal =
							expenseAccountBalances.getOrDefault(accountName, BigDecimal.ZERO);
						
						if (side == AccountSide.DEBIT)
						{ // Expense increases on debit
							expenseAccountBalances.put(accountName, currentTotal.add(amount));
						}
						else if (side == AccountSide.CREDIT)
						{
							expenseAccountBalances.put(accountName, currentTotal.subtract(amount));
						}
						
					}
					
				}
				
			}
			
		}
		
		// Add income items to reportData
		for (Map.Entry<String, BigDecimal> entry : incomeAccountBalances.entrySet())
		{
			
			if (entry.getValue().compareTo(BigDecimal.ZERO) != 0)
			{ // Only include accounts with non-zero balance for the period
				reportData
					.add(new IncomeStatementRowBean("Income", entry.getKey(), entry.getValue()));
			}
			
		}
		
		// Add expense items to reportData
		for (Map.Entry<String, BigDecimal> entry : expenseAccountBalances.entrySet())
		{
			
			if (entry.getValue().compareTo(BigDecimal.ZERO) != 0)
			{ // Only include accounts with non-zero balance for the period
				reportData
					.add(new IncomeStatementRowBean("Expenses", entry.getKey(), entry.getValue()));
			}
			
		}		
		
		return reportData;
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
	public static List<TrialBalanceRowBean> prepareTrialBalanceJasperData(	ReportContext context,
																	Ledger ledger,
																	ChartOfAccounts chartOfAccounts)
	{
		
		List<TrialBalanceRowBean> reportData = new ArrayList<>();
		
		if (context.getEndDate() == null || ledger == null || chartOfAccounts == null)
		{
			LOGGER.warning("End date, ledger, or COA missing for Trial Balance data preparation.");
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
				LOGGER.warning("TB Data: Skipping account with missing critical information: " +
					(account != null ? account.getAccountNumber() : "null account object"));
				continue;
			}
			
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts))
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
					{ // Strictly before end of end date + 1 day
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
							LOGGER.warning("TB Data: Account " + account.getAccountNumber() +
								" missing type or increase side.");
							continue;
						}
						
						if (increaseSide == AccountSide.DEBIT)
						{ // For ASSET and EXPENSE typically
							
							if (entry.getAccountSide() == AccountSide.DEBIT)
							{
								accountBalance = accountBalance.add(entry.getAmount());
							}
							else
							{ // CREDIT
								accountBalance = accountBalance.subtract(entry.getAmount());
							}
							
						}
						else
						{ // increaseSide is CREDIT (for LIABILITY, EQUITY, INCOME
							// typically)
							
							if (entry.getAccountSide() == AccountSide.CREDIT)
							{
								accountBalance = accountBalance.add(entry.getAmount());
							}
							else
							{ // DEBIT
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
				continue; // Already logged above
				
			if (increaseSide == AccountSide.DEBIT)
			{ // ASSET, EXPENSE
				
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
				{
					debitAmount = accountBalance;
				}
				else
				{ // Negative balance for a debit-normal account implies a credit nature in
					// TB
					creditAmount = accountBalance.abs();
				}
				
			}
			else
			{ // Credit-normal accounts: LIABILITY, EQUITY, INCOME
				
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
				{
					creditAmount = accountBalance;
				}
				else
				{ // Negative balance for a credit-normal account implies a debit nature in
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
	 * Generates and exports a report using JasperReports based on the specified {@link ReportContext} and output format.
	 * This method acts as a dispatcher, selecting the appropriate {@link AbstractReportGenerator} subclass
	 * (e.g., {@link IncomeStatementJasperGenerator}, {@link CashFlowStatementJasperGenerator}) based on the
	 * {@code reportType} in the context.
	 *
	 * @param context The {@link ReportContext} defining the report to be generated, including its type,
	 *                date ranges, filters, etc.
	 * @param outputFormat The desired output format for the report (e.g., "pdf", "html").
	 * @return A {@link File} object representing the generated and exported report.
	 * @throws IllegalStateException If no company is currently open.
	 * @throws IllegalArgumentException If the report type in the context is null, empty, or unsupported.
	 * @throws Exception If any error occurs during the report generation or export process,
	 *                   including {@link java.io.FileNotFoundException} if a required JRXML template is not found
	 *                   or if the generator fails to produce an output file.
	 */
	public File generateJasperReport(ReportContext context, String outputFormat) throws Exception
	{
		Company currentCompany = CurrentCompany.getCompany();
		
		if (currentCompany == null)
		{
			System.err.println("No company is currently open. Cannot generate report."); // Consider
																							// logger
			throw new IllegalStateException(
				"No company is currently open. Cannot generate report.");
		}
		
		AbstractReportGenerator reportGeneratorInstance = null;
		String reportType = context.getReportType();
		
		if (reportType == null || reportType.trim().isEmpty())
		{
			throw new IllegalArgumentException("Report type must be specified in ReportContext.");
		}
		
		switch(reportType)
		{
			case "income_statement_jasper":
				reportGeneratorInstance = new IncomeStatementJasperGenerator(context, this);
				break;
			
			case "cash_flow_statement_jasper":
				reportGeneratorInstance = new CashFlowStatementJasperGenerator(context, this);
				break;
			
			case "trial_balance_jasper":
				reportGeneratorInstance = new TrialBalanceJasperGenerator(context, this);
				break;
			
			case "balance_sheet_jasper":
				reportGeneratorInstance = new BalanceResultReportGenerator(null);
				break;
			
			case "":
				reportGeneratorInstance = new AccountLedgerJasperGenerator();
				reportGeneratorInstance = new AccountSummaryJasperGenerator();
				//reportGeneratorInstance = new BalanceResultReportGenerator(null);
				reportGeneratorInstance = new BalanceSheetJasperGenerator();
				reportGeneratorInstance = new BankReconciliationJasperGenerator();
				//reportGeneratorInstance = new CashFlowStatementJasperGenerator(null, null);
				reportGeneratorInstance = new ChartOfAccountsJasperGenerator(null);
				reportGeneratorInstance = new FundLedgerJasperGenerator();
				reportGeneratorInstance = new GeneralJournalJasperGenerator();
				reportGeneratorInstance = new GeneralLedgerJasperGenerator();
				reportGeneratorInstance = new IncomeStatementAltJasperGenerator();
				//reportGeneratorInstance = new IncomeStatementJasperGenerator(null, null);
				reportGeneratorInstance = new TransactionReportJasperGenerator();
				//reportGeneratorInstance = new TrialBalanceJasperGenerator(null, null);
				break;
				
			// Additional Jasper-based reports can be added here
			default:
				System.err.println("Unsupported or unknown Jasper report type: " + reportType); // Consider
																								// logger
				throw new IllegalArgumentException("Unsupported Jasper report type: " + reportType);
		}
		
		File generatedFile = reportGeneratorInstance.generateAndExportReport(outputFormat);
		
		if (generatedFile != null && generatedFile.exists())
		{
			System.out.println("ReportService: Successfully received generated file: " +
				generatedFile.getAbsolutePath()); // Consider logger
			return generatedFile;
		}
		else if (generatedFile != null && !generatedFile.exists())
		{
			System.err.println(
				"ReportService: Generator returned a File object, but the file does not exist at: " +
					generatedFile.getAbsolutePath()); // Consider logger
			throw new java.io.FileNotFoundException(
				"Generated report file reference returned by generator, but file not found: " +
					generatedFile.getAbsolutePath());
		}
		else
		{ // generatedFile is null
			System.err.println(
				"ReportService: Report generator failed to return a valid file object for report type: " +
					reportType + "."); // Consider logger
			throw new Exception(
				"Report generation failed to produce a file for report type: " + reportType + ".");
		}
		
	}
	
	/**
	 * Prepares a list of {@link ChartOfAccountsRowBean} objects for the
	 * Chart of Accounts Jasper report.
	 *
	 * @param chartOfAccounts The company's {@link ChartOfAccounts}.
	 * @return List of beans representing each account. Returns an empty list if
	 *         the chart is null or contains no accounts.
	 */
	public
			List<ChartOfAccountsRowBean>
			prepareChartOfAccountsJasperData(ChartOfAccounts chartOfAccounts)
	{
		List<ChartOfAccountsRowBean> data = new ArrayList<>();
		
		if (chartOfAccounts == null)
		{
			LOGGER.warning("ChartOfAccounts is null - cannot prepare COA report data.");
			return data;
		}
		
		List<Account> accounts = chartOfAccounts.getAccounts();
		
		if (accounts == null)
		{
			return data;
		}
		
		accounts.sort(Comparator.comparing(Account::getAccountNumber,
			Comparator.nullsLast(String::compareTo)));
		
		for (Account acct : accounts)
		{
			if (acct == null)
				continue;
			
			String type = (acct.getAccountType() != null) ? acct.getAccountType().name() : "";
			data.add(new ChartOfAccountsRowBean(acct.getAccountNumber(), acct.getName(), type));
		}
		
		return data;
	}
	
	/**
	 * @param reportContext
	 * @param ledger
	 * @param coa
	 * @return
	 */
	public static
			List<CashFlowStatementRowBean>
			prepareCashFlowStatementJasperData(	ReportContext reportContext, Ledger ledger,
												ChartOfAccounts coa)
	{
		return new ArrayList<>();
	}
	
	
}

