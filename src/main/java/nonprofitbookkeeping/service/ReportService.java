
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.reports.writer.LedgerReportWriter;

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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import nonprofitbookkeeping.reports.datasource.IncomeStatementRowBean;
import nonprofitbookkeeping.reports.datasource.CashFlowStatementRowBean;
import nonprofitbookkeeping.reports.datasource.TrialBalanceRowBean; // Added import
import nonprofitbookkeeping.reports.generator.AbstractReportGenerator;
import nonprofitbookkeeping.reports.generator.IncomeStatementJasperGenerator;
import nonprofitbookkeeping.reports.generator.CashFlowStatementJasperGenerator;


public class ReportService
{
	private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
	
	
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
					entries = new java.util.HashSet<>();
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
					
					String accountTypeStr = account.getAccountType().toString();					
					if (accountTypeStr == null)
					{
						LOGGER.warning("IS: Account type is null for account: " +
							account.getName() + " (ID: " + account.getAccountNumber() + ")");
						continue;
					}
					
					AccountType accountType;
					
					try
					{
						accountType = AccountType.valueOf(accountTypeStr.toUpperCase());
					}
					catch (IllegalArgumentException e)
					{
						LOGGER.warning(
							"IS: Unknown account type: " + accountTypeStr + " for account: " +
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
						if (side == AccountSide.CREDIT)
							incomeTotals.put(accountName, currentTotal.add(amount));
						else if (side == AccountSide.DEBIT)
							incomeTotals.put(accountName, currentTotal.subtract(amount));
					}
					else if (accountType == AccountType.EXPENSE)
					{
						BigDecimal currentTotal =
							expenseTotals.getOrDefault(accountName, BigDecimal.ZERO);
						if (side == AccountSide.DEBIT)
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
	
	private static boolean doesAccountMatchFunds(	Account account, List<String> selectedFundNames,
													ChartOfAccounts chartOfAccounts)
	{
		
		if (selectedFundNames == null || selectedFundNames.isEmpty())
		{
			return true;
		}
		
		if (account == null)
		{
			return false;
		}
		
		if (account.getAssociatedFunds() == null || account.getAssociatedFunds().isEmpty())
		{
			return false;
		}
		
		for (Fund fund : account.getAssociatedFunds())
		{
			
			if (fund != null && fund.getName() != null &&
				selectedFundNames.contains(fund.getName()))
			{
				return true;
			}
			
		}
		
		return false;
	}
	
	static Map<String, Object> prepareBalanceSheetContext(	ReportContext context, Ledger ledger,
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
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
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
			
			AccountType accountType;
			
			try
			{
				accountType = AccountType.valueOf(account.getAccountType().toUpperCase());
			}
			catch (IllegalArgumentException e)
			{
				LOGGER.warning("BS: Unknown account type: " + account.getAccountType() +
					" for account: " + account.getName());
				continue;
			}
			
			switch(accountType)
			{
				case ASSET:
				case BANK:
				case CASH:
				case CHECKING:
					assetTotals.put(account.getName(), finalBalance);
					break;
					
				case LIABILITY:
				case LONG_TERM_LIABILITY:
					liabilityTotals.put(account.getName(), finalBalance);
					break;
					
				case EQUITY:
					equityTotals.put(account.getName(), finalBalance);
					break;
					
				default:
					break;
			}
			
		}
		
		ReportContext netIncomeCalcContext = new ReportContext();
		LocalDate netIncomeStartDate = (context.getStartDate() != null) ? context.getStartDate() :
			reportEndDate.withDayOfYear(1);
		
		if (netIncomeStartDate.isAfter(reportEndDate))
		{
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
		BigDecimal totalEquity =
			equityTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);
		
		if (totalAssets.compareTo(totalLiabilitiesAndEquity) != 0)
		{
			LOGGER.warning("Balance Sheet (fund-filtered: " + applyFundFilter +
				") out of balance! Assets: " + totalAssets +
				", Liabilities + Equity: " + totalLiabilitiesAndEquity +
				". Difference: " + totalAssets.subtract(totalLiabilitiesAndEquity));
		}
		
		List<Map<String, Object>> assetItems = new ArrayList<>();
		assetTotals.forEach((name, bal) -> assetItems.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> liabilityItems = new ArrayList<>();
		liabilityTotals
			.forEach((name, bal) -> liabilityItems.add(Map.of("name", name, "amount", bal)));
		List<Map<String, Object>> equityItems = new ArrayList<>();
		equityTotals.entrySet().stream()
			.filter(entry -> !entry.getKey().equals("Current Period Net Income"))
			.forEach(entry -> equityItems
				.add(Map.of("name", entry.getKey(), "amount", entry.getValue())));
		
		Map<String, Object> jxlsContext = new HashMap<>();
		jxlsContext.put("assetItems", assetItems);
		jxlsContext.put("liabilityItems", liabilityItems);
		jxlsContext.put("equityItems", equityItems);
		jxlsContext.put("totalAssets", totalAssets);
		jxlsContext.put("totalLiabilities", totalLiabilities);
		jxlsContext.put("totalEquity", totalEquity);
		jxlsContext.put("currentPeriodNetIncome", currentPeriodNetIncome);
		jxlsContext.put("totalLiabilitiesAndEquity", totalLiabilitiesAndEquity);
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportStartDate", netIncomeStartDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		jxlsContext.put("assetsEqualsLiabilitiesPlusEquity",
			totalAssets.compareTo(totalLiabilitiesAndEquity) == 0);
		
		return jxlsContext;
	}
	
	static BigDecimal getAccountBalanceAsOfDate(Account account, LocalDate date, Ledger ledger,
												ChartOfAccounts chartOfAccounts,
												List<String> selectedFundNames,
												boolean applyFundFilter)
	{
		BigDecimal balance = BigDecimal.ZERO;
		
		if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
		{
			// If account doesn't match fund filter, its opening balance is not considered
			// for this filtered view.
			// Transactions for this account will also be effectively skipped for this
			// specific calculation.
		}
		else
		{
			balance =
				account.getOpeningBalance() == null ? BigDecimal.ZERO : account.getOpeningBalance();
		}
		
		long endDateMillis =
			date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		List<AccountingTransaction> transactions = ledger.getTransactions();
		if (transactions == null)
		{
			transactions = new ArrayList<AccountingTransaction>();
		}
		
		for (AccountingTransaction transaction : transactions)
		{
			
			if (transaction == null || transaction.getBookingDateTimestamp() >= endDateMillis)
			{
				continue;
			}
			
			for (AccountingEntry entry : transaction.getEntries())
			{
				if (entry == null || entry.getAmount() == null)
					continue;
					
				if (!account.getAccountNumber().equals(entry.getAccountNumber()))
				{
					continue;
				}
				
				// V1 Simplification: If the account (for which balance is being calculated) has
				// passed the
				// initial fund filter (in prepareBalanceSheetContext or other callers),
				// all its transactions are included in its balance. No further filtering of
				// individual
				// transactions for THIS account based on fund.
				// The `applyFundFilter` and `selectedFundNames` are mainly for the opening
				// balance decision above.
				
				if (account.getIncreaseSide() == AccountSide.DEBIT)
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
			
		}
		
		return balance;
	}
	
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
		long reportEndDateMillis =
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter = (selectedFundNames != null && !selectedFundNames.isEmpty());
		
		long reportStartDateMillis = 0;
		
		if (context.getStartDate() != null)
		{
			reportStartDateMillis =
				context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
		}
		
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions == null)
		{
			transactions = new ArrayList<AccountingTransaction>();
			LOGGER.info("No transactions found in the ledger for Trial Balance.");
		}
		
		List<Account> accounts =
			(chartOfAccounts != null && chartOfAccounts.getAccounts() != null) ?
				chartOfAccounts.getAccounts() : new ArrayList<Account>();
		
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
				continue;
			}
			
			BigDecimal accountBalance =
				account.getOpeningBalance() == null ? BigDecimal.ZERO : account.getOpeningBalance();
			
			for (AccountingTransaction transaction : transactions)
			{
				
				if (transaction == null ||
					transaction.getBookingDateTimestamp() >= reportEndDateMillis ||
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
					
					if (account.getIncreaseSide() == AccountSide.DEBIT)
					{
						if (entry.getAccountSide() == AccountSide.DEBIT)
							accountBalance = accountBalance.add(entry.getAmount());
						else
							accountBalance = accountBalance.subtract(entry.getAmount());
					}
					else
					{
						if (entry.getAccountSide() == AccountSide.CREDIT)
							accountBalance = accountBalance.add(entry.getAmount());
						else
							accountBalance = accountBalance.subtract(entry.getAmount());
					}
					
				}
				
			}
			
			BigDecimal finalDebitAmount = BigDecimal.ZERO;
			BigDecimal finalCreditAmount = BigDecimal.ZERO;
			
			if (account.getIncreaseSide() == AccountSide.DEBIT)
			{
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
					finalDebitAmount = accountBalance;
				else
					finalCreditAmount = accountBalance.abs();
			}
			else
			{
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
					finalCreditAmount = accountBalance;
				else
					finalDebitAmount = accountBalance.abs();
			}
			
			Map<String, Object> item = new HashMap<>();
			item.put("accountNumber", account.getAccountNumber());
			item.put("accountName", account.getName());
			item.put("debit", finalDebitAmount);
			item.put("credit", finalCreditAmount);
			trialBalanceItems.add(item);
			
			totalDebits = totalDebits.add(finalDebitAmount);
			totalCredits = totalCredits.add(finalCreditAmount);
		}
		
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
	
	static Map<String, Object> prepareCashFlowStatementContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts)
	{
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
		Set<String> cashEquivalentAccountNames = new HashSet<>();
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			
			if (account.getAccountType() != null)
			{
				String accTypeUpper = account.getAccountType().toUpperCase();
				
				if (accTypeUpper.equals(AccountType.BANK.name()) ||
					accTypeUpper.equals(AccountType.CASH.name()) ||
					accTypeUpper.equals("CHECKING"))
				{
					
					if (applyFundFilter &&
						!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
					{
						continue;
					}
					
					cashEquivalentAccounts.add(account);
					cashEquivalentAccountNames.add(account.getName());
				}
				
			}
			
		}
		
		BigDecimal cashAtEndOfPeriod = BigDecimal.ZERO;
		
		for (Account acc : cashEquivalentAccounts)
		{
			cashAtEndOfPeriod = cashAtEndOfPeriod.add(getAccountBalanceAsOfDate(acc, reportEndDate,
				ledger, chartOfAccounts, selectedFundNames, applyFundFilter));
		}
		
		jxlsContext.put("cashAtEndOfPeriod", cashAtEndOfPeriod);
		
		BigDecimal cashAtBeginningOfPeriod = BigDecimal.ZERO;
		LocalDate beginningDate = reportStartDate.minusDays(1);
		
		for (Account acc : cashEquivalentAccounts)
		{
			cashAtBeginningOfPeriod = cashAtBeginningOfPeriod.add(getAccountBalanceAsOfDate(acc,
				beginningDate, ledger, chartOfAccounts, selectedFundNames, applyFundFilter));
		}
		
		jxlsContext.put("cashAtBeginningOfPeriod", cashAtBeginningOfPeriod);
		
		BigDecimal netChangeInCashActual = cashAtEndOfPeriod.subtract(cashAtBeginningOfPeriod);
		jxlsContext.put("netChangeInCashActual", netChangeInCashActual);
		
		BigDecimal totalOperatingAdjustments = BigDecimal.ZERO;
		BigDecimal totalDepreciationAmortization = BigDecimal.ZERO;
		Set<String> deprAmortAccountNames = Set.of("Depreciation Expense", "Amortization Expense");
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
				
				for (AccountingTransaction transaction : ledger.getTransactions())
				{
					
					if (transaction.getBookingDateTimestamp() >= periodStartDateMillis &&
						transaction.getBookingDateTimestamp() < periodEndDateMillisExclusive)
					{
						
						for (AccountingEntry entry : transaction.getEntries())
						{
							
							if (entry.getAccountNumber().equals(account.getAccountNumber()) &&
								entry.getAccountSide() == AccountSide.DEBIT)
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
		
		Map<String, String> workingCapitalConfig = new HashMap<>(Map.of(
			"Accounts Receivable", "asset", "Inventory", "asset", "Prepaid Expenses", "asset",
			"Accounts Payable", "liability", "Accrued Expenses", "liability", "Deferred Revenue",
			"liability"));
		
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
					chartOfAccounts, selectedFundNames, applyFundFilter);
				BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDate,
					ledger, chartOfAccounts, selectedFundNames, applyFundFilter);
				BigDecimal change = endBalance.subtract(beginningBalance);
				
				if (change.compareTo(BigDecimal.ZERO) == 0)
					continue;
				String itemName =
					(change.compareTo(BigDecimal.ZERO) > 0 ? "Increase in " : "Decrease in ") +
						account.getName();
				BigDecimal adjustmentAmount = "asset".equals(category) ? change.negate() : change;
				
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
			
			if (account.getAccountType() != null &&
				account.getAccountType().toUpperCase().equals(AccountType.FIXED_ASSET.name()) &&
				!cashEquivalentAccountNames.contains(account.getName()) &&
				!workingCapitalConfig.containsKey(account.getName()))
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger,
					chartOfAccounts, selectedFundNames, applyFundFilter);
				BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDate,
					ledger, chartOfAccounts, selectedFundNames, applyFundFilter);
				BigDecimal change = endBalance.subtract(beginningBalance);
				
				if (change.compareTo(BigDecimal.ZERO) != 0)
				{
					BigDecimal adjustment = change.negate();
					investingActivitiesItems.add(
						Map.of("name", "Net Change in " + account.getName(), "amount", adjustment));
					totalInvestingAdjustments = totalInvestingAdjustments.add(adjustment);
				}
				
			}
			
		}
		
		jxlsContext.put("investingActivitiesItems", investingActivitiesItems);
		jxlsContext.put("cashFromInvesting", totalInvestingAdjustments);
		
		BigDecimal totalFinancingAdjustments = BigDecimal.ZERO;
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			
			if (account.getAccountType() != null &&
				(account.getAccountType().toUpperCase()
					.equals(AccountType.LONG_TERM_LIABILITY.name()) ||
					account.getAccountType().toUpperCase().equals(AccountType.EQUITY.name())) &&
				!cashEquivalentAccountNames.contains(account.getName()) &&
				!workingCapitalConfig.containsKey(account.getName()))
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{
					continue;
				}
				
				BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger,
					chartOfAccounts, selectedFundNames, applyFundFilter);
				BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDate,
					ledger, chartOfAccounts, selectedFundNames, applyFundFilter);
				BigDecimal change = endBalance.subtract(beginningBalance);
				
				if (change.compareTo(BigDecimal.ZERO) != 0)
				{
					financingActivitiesItems.add(
						Map.of("name", "Net Change in " + account.getName(), "amount", change));
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
		
		BigDecimal discrepancy = netChangeInCashCalculated.subtract(netChangeInCashActual);
		jxlsContext.put("discrepancy", discrepancy);
		
		if (discrepancy.abs().compareTo(new BigDecimal("0.01")) > 0)
		{
			LOGGER.warning("Cash Flow Statement (fund-filtered: " + applyFundFilter +
				") discrepancy: " + discrepancy +
				". Calculated Net Change: " + netChangeInCashCalculated +
				", Actual Net Change (from balance sheet): " + netChangeInCashActual);
		}
		
		return jxlsContext;
	}
	
	static Map<String, Object> prepareBudgetVsActualsContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts,
																Budget budget)
	{
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
						
						AccountType accountType =
							AccountType.valueOf(account.getAccountType().toUpperCase());
						BigDecimal currentActual =
							actualAmounts.getOrDefault(account.getAccountNumber(), BigDecimal.ZERO);
						BigDecimal amount = entry.getAmount();
						
						if (accountType == AccountType.INCOME)
						{
							if (entry.getAccountSide() == AccountSide.CREDIT)
								currentActual = currentActual.add(amount);
							else
								currentActual = currentActual.subtract(amount);
						}
						else if (accountType == AccountType.EXPENSE)
						{
							if (entry.getAccountSide() == AccountSide.DEBIT)
								currentActual = currentActual.add(amount);
							else
								currentActual = currentActual.subtract(amount);
						}
						
						actualAmounts.put(account.getAccountNumber(), currentActual);
					}
					
				}
				
			}
			
		}
		
		for (Map.Entry<String, BigDecimal> entry : actualAmounts.entrySet())
		{
			Account account = chartOfAccounts.getAccount(entry.getKey());
			
			if (account != null && account.getAccountType() != null)
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
				{ // Check again for safety, though loop above should handle
					continue;
				}
				
				AccountType type = AccountType.valueOf(account.getAccountType().toUpperCase());
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
		long daysInReportPeriod =
			ChronoUnit.DAYS.between(reportStartDate, reportEndDate.plusDays(1));
		
		for (BudgetLine line : budget.getBudgetLines())
		{
			String accountId = line.getAccountId();
			Account account = chartOfAccounts.getAccount(accountId);
			String accountName = (account != null) ? account.getName() : line.getAccountName();
			
			if (account == null || account.getAccountType() == null)
			{
				LOGGER.warning("BvA: Skipping budget line for account ID " + accountId +
					" as account or type is not found/valid.");
				continue;
			}
			
			// Filter budget lines:
			// 1. If the line has a specific fundId, that fundId must be in
			// selectedFundNames (if filter is active).
			// 2. If the line has no fundId, it's included if no fund filter is active, OR
			// if the account itself matches the general fund filter.
			boolean lineFundMatches = false;
			
			if (applyFundFilter)
			{
				if (line.getFundId() != null && !line.getFundId().trim().isEmpty())
				{
					
					// Assuming line.getFundId() stores a name directly comparable to
					// selectedFundNames
					if (selectedFundNames.contains(line.getFundId()))
					{
						lineFundMatches = true;
					}
					
				}
				else
				{
					
					// Budget line is not tied to a specific fund, so check if the account itself
					// matches the filter
					if (doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
					{
						lineFundMatches = true;
					}
					
				}
				
				if (!lineFundMatches)
					continue;
			}
			else
			{ // No fund filter active, include all budget lines
				lineFundMatches = true;
			}
			
			AccountType accountType = AccountType.valueOf(account.getAccountType().toUpperCase());
			BigDecimal budgetedAmountForPeriod = BigDecimal.ZERO;
			boolean useProRatedAnnual = true;
			
			if (line.getPeriodicAmounts() != null && !line.getPeriodicAmounts().isEmpty())
			{
				
				if (line.getPeriodicity() == Periodicity.MONTHLY &&
					line.getPeriodicAmounts().size() == 12)
				{
					useProRatedAnnual = false;
					
					for (int i = 0; i < 12; i++)
					{
						LocalDate monthInFiscalYear = LocalDate.of(budgetFiscalYear, i + 1, 1);
						
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
					useProRatedAnnual = false;
					
					for (int i = 0; i < 4; i++)
					{
						LocalDate quarterStartDate = LocalDate.of(budgetFiscalYear, (i * 3) + 1, 1);
						
						if (!quarterStartDate.isBefore(reportStartDate.withDayOfMonth(1)) &&
							!quarterStartDate.isAfter(reportEndDate.withDayOfMonth(1)))
						{
							budgetedAmountForPeriod =
								budgetedAmountForPeriod.add(line.getPeriodicAmounts().get(i));
						}
						
					}
					
				}
				else if (line.getPeriodicity() == Periodicity.ANNUAL &&
					line.getPeriodicAmounts().size() == 1 &&
					daysInReportPeriod >= daysInFiscalYear - 5)
				{
					useProRatedAnnual = false;
					budgetedAmountForPeriod = line.getPeriodicAmounts().get(0);
				}
				
			}
			
			if (useProRatedAnnual && line.getTotalBudgetedAmount() != null)
			{
				budgetedAmountForPeriod = line.getTotalBudgetedAmount()
					.multiply(new BigDecimal(daysInReportPeriod))
					.divide(new BigDecimal(daysInFiscalYear), 2, RoundingMode.HALF_UP);
			}
			
			if (accountType == AccountType.INCOME)
				totalBudgetedIncome = totalBudgetedIncome.add(budgetedAmountForPeriod);
			else if (accountType == AccountType.EXPENSE)
				totalBudgetedExpenses = totalBudgetedExpenses.add(budgetedAmountForPeriod);
			
			BigDecimal actualAmountForPeriod =
				actualAmounts.getOrDefault(accountId, BigDecimal.ZERO);
			BigDecimal variance = budgetedAmountForPeriod.subtract(actualAmountForPeriod);
			BigDecimal variancePercent = BigDecimal.ZERO;
			
			if (budgetedAmountForPeriod.compareTo(BigDecimal.ZERO) != 0)
			{
				variancePercent = variance.divide(budgetedAmountForPeriod, 4, RoundingMode.HALF_UP)
					.multiply(new BigDecimal("100"));
			}
			
			reportItems.add(Map.of("accountId", accountId, "accountName", accountName, "budgeted",
				budgetedAmountForPeriod, "actual", actualAmountForPeriod, "variance", variance,
				"variancePercent", variancePercent));
		}
		
		jxlsContext.put("reportItems", reportItems);
		jxlsContext.put("totalBudgetedIncome", totalBudgetedIncome);
		jxlsContext.put("totalActualIncome", totalActualIncome);
		jxlsContext.put("totalIncomeVariance", totalBudgetedIncome.subtract(totalActualIncome));
		jxlsContext.put("totalBudgetedExpenses", totalBudgetedExpenses);
		jxlsContext.put("totalActualExpenses", totalActualExpenses);
		jxlsContext.put("totalExpenseVariance",
			totalBudgetedExpenses.subtract(totalActualExpenses));
		BigDecimal totalBudgetedNet = totalBudgetedIncome.subtract(totalBudgetedExpenses);
		BigDecimal totalActualNet = totalActualIncome.subtract(totalActualExpenses);
		jxlsContext.put("totalBudgetedNet", totalBudgetedNet);
		jxlsContext.put("totalActualNet", totalActualNet);
		jxlsContext.put("totalNetVariance", totalBudgetedNet.subtract(totalActualNet));
		return jxlsContext;
	}
	
	static Map<String, Object> prepareAccountActivityContext(	ReportContext context, Ledger ledger,
																ChartOfAccounts chartOfAccounts)
	{
		
		if (context.getAccountIdsForDetailReport() == null ||
			context.getAccountIdsForDetailReport().isEmpty() ||
			context.getStartDate() == null || context.getEndDate() == null)
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
		// Fund filtering for Account Activity Detail is not explicitly defined for V1,
		// but if context.getFundIds() is populated, we could apply it.
		// For now, assume no fund filtering at this report's level, or it's handled by
		// selected accounts.
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
			
			// If fund filtering is active, check if this primary account for detail matches
			// the funds.
			// If not, skip this account's detail report.
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
			{
				LOGGER.info("Account Activity Detail: Skipping account " + accountId +
					" as it does not match selected fund criteria.");
				continue;
			}
			
			// Opening balance calculation - pass fund filter context.
			// The balance will be specific to these funds if applyFundFilter is true.
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
					
					if (tx.getBookingDateTimestamp() >=
						context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant()
							.toEpochMilli() &&
						tx.getBookingDateTimestamp() < context.getEndDate().plusDays(1)
							.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
					{
						
						for (AccountingEntry entry : tx.getEntries())
						{
							
							if (entry.getAccountNumber().equals(accountId))
							{
								// If fund filtering, ensure the account for this entry (which is
								// `account`) matches the fund criteria.
								// This is implicitly handled if `getAccountBalanceAsOfDate`
								// correctly considers funds for all its calculations.
								// For the transaction list, we only add transactions for this
								// account if the account itself is fund-relevant.
								// The `doesAccountMatchFunds` check above for the primary account
								// handles this.
								accountTransactions.add(tx);
								break;
							}
							
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
				
				for (AccountingEntry entry : transaction.getEntries())
				{
					
					if (entry.getAccountNumber().equals(accountId))
					{
						Map<String, Object> entryData = new HashMap<>();
						entryData.put("date", transactionDate.format(DATE_FORMATTER));
						entryData.put("transactionId", transaction.getBookingDateTimestamp());
						entryData.put("description",
							transaction.getMemo() != null ? transaction.getMemo() : "");
						
						BigDecimal debitAmount = (entry.getAccountSide() == AccountSide.DEBIT) ?
							entry.getAmount() : BigDecimal.ZERO;
						BigDecimal creditAmount = (entry.getAccountSide() == AccountSide.CREDIT) ?
							entry.getAmount() : BigDecimal.ZERO;
						entryData.put("debit", debitAmount);
						entryData.put("credit", creditAmount);
						
						if (account.getIncreaseSide() == AccountSide.DEBIT)
						{
							runningBalance = runningBalance.add(debitAmount).subtract(creditAmount);
						}
						else
						{
							runningBalance = runningBalance.subtract(debitAmount).add(creditAmount);
						}
						
						entryData.put("runningBalance", runningBalance);
						entryItems.add(entryData);
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
		String reportDateSuffix = (context.getEndDate() != null) ? context.getEndDate().toString() :
			LocalDate.now().toString();
		String reportPeriodSuffix =
			(context.getStartDate() != null ? context.getStartDate().toString() + "_to_" : "") +
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
				selectedBudget.getBudgetName().replace(" ", "_") + "_" + reportPeriodSuffix;
		}
		else if ("account_activity_detail".equals(reportType))
		{
			
			if (ledger == null || chartOfAccounts == null || context.getStartDate() == null ||
				context.getEndDate() == null ||
				context.getAccountIdsForDetailReport() == null ||
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
				context.getAccountIdsForDetailReport().get(0) : "multiple_accounts";
			outputFileNamePrefix = "account_activity_" + accountPart + "_" + reportPeriodSuffix;
		}
		else
		{
			LOGGER
				.warning("Generating generic report (stub) for unknown report type: " + reportType);
			File outputFile = new File("generated_report_" + reportType + ".txt");
			
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
		
		File outputFile = new File(outputFileNamePrefix + ".xlsx");
		String templatePath = "/templates/" + templateName;
		// All templates are now expected to be in /templates directly for simplicity.
		// If specific ones like budget_vs_actuals were in a subfolder, adjust here.
		// The previous budget_vs_actuals was /nonprofitbookkeeping/service/templates/
		// For consistency, moving all to /templates/
		// If budget_vs_actuals_template.xlsx was NOT moved, this needs if/else for its
		// path.
		// Assuming all are now in /templates/ for this general structure.
		
		try (InputStream is = ReportService.class.getResourceAsStream(templatePath))
		{
			
			if (is == null)
			{
				LOGGER.log(Level.SEVERE, reportType + " template not found at " + templatePath);
				throw new IOException(reportType + " template not found at " + templatePath +
					". Ensure it is in the resources/templates directory.");
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
	
	public void registerWriter(String reportType, LedgerReportWriter writer)
	{
		/* ... */ }
		
	public List<ReportMetadata> listGeneratedReports()
	{
		return new ArrayList<>();
	}

	/**
	 * @param ctx
	 * @return
	 */
	public static File generate(ReportContext ctx)
	{
		// TODO Auto-generated method stub
		return null;
	}

    // New method for JasperReports data
    public List<IncomeStatementRowBean> prepareIncomeStatementJasperData(
            ReportContext context, Ledger ledger, ChartOfAccounts chartOfAccounts) {

        List<IncomeStatementRowBean> reportData = new ArrayList<>();

        if (context.getStartDate() == null || context.getEndDate() == null) {
            LOGGER.warning("Start date and end date must be provided for income statement data preparation.");
            return reportData; // Return empty list
        }
        if (ledger == null || chartOfAccounts == null) {
             LOGGER.warning("Ledger or Chart of Accounts not available for income statement data.");
             return reportData;
        }

        Map<String, BigDecimal> incomeAccountBalances = new HashMap<>();
        Map<String, BigDecimal> expenseAccountBalances = new HashMap<>();

        List<String> selectedFundIds = context.getFundIds();
        boolean applyFundFilter = (selectedFundIds != null && !selectedFundIds.isEmpty());

        long startDateMillis = context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long endDateMillisExclusive = context.getEndDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        List<AccountingTransaction> transactions = ledger.getTransactions();
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        for (AccountingTransaction transaction : transactions) {
            if (transaction == null) continue;

            if (transaction.getBookingDateTimestamp() >= startDateMillis &&
                transaction.getBookingDateTimestamp() < endDateMillisExclusive) {

                Set<AccountingEntry> entries = transaction.getEntries();
                if (entries == null) continue;

                for (AccountingEntry entry : entries) {
                    if (entry == null || entry.getAccountNumber() == null || entry.getAmount() == null) continue;

                    Account account = chartOfAccounts.getAccount(entry.getAccountNumber());
                    if (account == null || account.getAccountTypeEnum() == null || account.getName() == null) { // Changed to getAccountTypeEnum
                        LOGGER.warning("IS Data: Account or critical account info not found for number: " + entry.getAccountNumber());
                        continue;
                    }

                    if (applyFundFilter) {
                        if (!doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts)) {
                             continue;
                        }
                    }

                    AccountType accountType = account.getAccountTypeEnum(); // Changed to getAccountTypeEnum
                    String accountName = account.getName();
                    BigDecimal amount = entry.getAmount();
                    AccountSide side = entry.getAccountSide();

                    if (accountType == AccountType.INCOME) {
                        BigDecimal currentTotal = incomeAccountBalances.getOrDefault(accountName, BigDecimal.ZERO);
                        if (side == AccountSide.CREDIT) {
                            incomeAccountBalances.put(accountName, currentTotal.add(amount));
                        } else if (side == AccountSide.DEBIT) {
                            incomeAccountBalances.put(accountName, currentTotal.subtract(amount));
                        }
                    } else if (accountType == AccountType.EXPENSE) {
                        BigDecimal currentTotal = expenseAccountBalances.getOrDefault(accountName, BigDecimal.ZERO);
                        if (side == AccountSide.DEBIT) {
                            expenseAccountBalances.put(accountName, currentTotal.add(amount));
                        } else if (side == AccountSide.CREDIT) {
                            expenseAccountBalances.put(accountName, currentTotal.subtract(amount));
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, BigDecimal> entry : incomeAccountBalances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                reportData.add(new IncomeStatementRowBean("Income", entry.getKey(), entry.getValue()));
            }
        }

        for (Map.Entry<String, BigDecimal> entry : expenseAccountBalances.entrySet()) {
             if (entry.getValue().compareTo(BigDecimal.ZERO) != 0) {
                reportData.add(new IncomeStatementRowBean("Expenses", entry.getKey(), entry.getValue()));
             }
        }

        // Optional: Sort data if JRXML doesn't handle it or specific order is needed before grouping
        // reportData.sort(Comparator.comparing(IncomeStatementRowBean::getAccountCategory)
        //                          .thenComparing(IncomeStatementRowBean::getAccountName));

        return reportData;
    }

    // New method for Cash Flow Statement JasperReports data
    public List<CashFlowStatementRowBean> prepareCashFlowStatementJasperData(
            ReportContext context, Ledger ledger, ChartOfAccounts chartOfAccounts) {

        List<CashFlowStatementRowBean> reportData = new ArrayList<>();
        int sortOrder = 0;

        if (context.getStartDate() == null || context.getEndDate() == null || ledger == null || chartOfAccounts == null) {
            LOGGER.warning("Missing required data (dates, ledger, or COA) for Cash Flow Statement data preparation.");
            return reportData; // Return empty list
        }

        LocalDate reportStartDate = context.getStartDate();
        LocalDate reportEndDate = context.getEndDate();
        List<String> selectedFundIds = context.getFundIds();
        boolean applyFundFilter = (selectedFundIds != null && !selectedFundIds.isEmpty());

        // --- Net Income (from Operating Activities) ---
        ReportContext incomeStatementPeriodContext = new ReportContext();
        incomeStatementPeriodContext.setStartDate(reportStartDate);
        incomeStatementPeriodContext.setEndDate(reportEndDate);
        incomeStatementPeriodContext.setFundIds(selectedFundIds);

        Map<String, Object> incomeStatementContextMap = prepareIncomeStatementContext(incomeStatementPeriodContext, ledger, chartOfAccounts);
        BigDecimal netIncome = (BigDecimal) incomeStatementContextMap.getOrDefault("netIncome", BigDecimal.ZERO);
        reportData.add(new CashFlowStatementRowBean("Operating Activities", "Net Income", netIncome, false, sortOrder++));

        // --- Adjustments for Non-Cash Items (Operating Activities) ---
        BigDecimal totalOperatingAdjustments = BigDecimal.ZERO;
        BigDecimal totalDepreciationAmortization = BigDecimal.ZERO;
        Set<String> deprAmortAccountNames = Set.of("Depreciation Expense", "Amortization Expense", "Depreciation", "Amortization");

        long periodStartDateMillis = reportStartDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long periodEndDateMillisExclusive = reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        for (Account account : chartOfAccounts.getAccounts()) {
            if (account.getName() != null && deprAmortAccountNames.contains(account.getName())) {
                if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts)) {
                    continue;
                }
                for (AccountingTransaction transaction : ledger.getTransactions()) {
                    if (transaction.getBookingDateTimestamp() >= periodStartDateMillis &&
                        transaction.getBookingDateTimestamp() < periodEndDateMillisExclusive) {
                        for (AccountingEntry entry : transaction.getEntries()) {
                            if (entry.getAccountNumber().equals(account.getAccountNumber()) &&
                                entry.getAccountSide() == AccountSide.DEBIT) {
                                totalDepreciationAmortization = totalDepreciationAmortization.add(entry.getAmount());
                            }
                        }
                    }
                }
            }
        }
        if (totalDepreciationAmortization.compareTo(BigDecimal.ZERO) != 0) {
            reportData.add(new CashFlowStatementRowBean("Operating Activities", "Depreciation & Amortization", totalDepreciationAmortization, false, sortOrder++));
            totalOperatingAdjustments = totalOperatingAdjustments.add(totalDepreciationAmortization);
        }

        // --- Changes in Working Capital (Operating Activities) ---
        Map<String, String> workingCapitalConfig = new HashMap<>(Map.of(
            "Accounts Receivable", "asset", "Inventory", "asset", "Prepaid Expenses", "asset",
            "Accounts Payable", "liability", "Accrued Expenses", "liability", "Deferred Revenue", "liability"
        ));
        LocalDate beginningDateForChanges = reportStartDate.minusDays(1);

        for (Account account : chartOfAccounts.getAccounts()) {
            if (account.getName() != null && workingCapitalConfig.containsKey(account.getName())) {
                if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts)) {
                    continue;
                }
                String category = workingCapitalConfig.get(account.getName());
                BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger, chartOfAccounts, selectedFundIds, applyFundFilter);
                BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDateForChanges, ledger, chartOfAccounts, selectedFundIds, applyFundFilter);
                BigDecimal change = endBalance.subtract(beginningBalance);

                if (change.compareTo(BigDecimal.ZERO) == 0) continue;

                String itemName = (change.compareTo(BigDecimal.ZERO) > 0 ? "Increase in " : "Decrease in ") + account.getName();
                BigDecimal adjustmentAmount = "asset".equals(category) ? change.negate() : change;

                reportData.add(new CashFlowStatementRowBean("Operating Activities", itemName, adjustmentAmount, false, sortOrder++));
                totalOperatingAdjustments = totalOperatingAdjustments.add(adjustmentAmount);
            }
        }
        BigDecimal cashFromOperations = netIncome.add(totalOperatingAdjustments);
        reportData.add(new CashFlowStatementRowBean("Operating Activities", "Net Cash from Operating Activities", cashFromOperations, true, sortOrder++));

        // --- Investing Activities ---
        sortOrder = 200;
        BigDecimal totalInvestingCashFlow = BigDecimal.ZERO;
        Set<String> cashEquivalentAccountNumbers = new HashSet<>();
        for (Account acc : chartOfAccounts.getAccounts()) {
            // Assuming getAccountTypeEnum() is the correct method returning AccountType enum
            if (acc.getAccountTypeEnum() == AccountType.BANK || acc.getAccountTypeEnum() == AccountType.CASH) {
                cashEquivalentAccountNumbers.add(acc.getAccountNumber());
            }
        }

        for (Account account : chartOfAccounts.getAccounts()) {
            if (account.getAccountTypeEnum() == AccountType.FIXED_ASSET && !cashEquivalentAccountNumbers.contains(account.getAccountNumber())) {
                if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts)) {
                    continue;
                }
                BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger, chartOfAccounts, selectedFundIds, applyFundFilter);
                BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDateForChanges, ledger, chartOfAccounts, selectedFundIds, applyFundFilter);
                BigDecimal change = endBalance.subtract(beginningBalance);

                if (change.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal cashFlowImpact = change.negate();
                    reportData.add(new CashFlowStatementRowBean("Investing Activities", "Change in " + account.getName(), cashFlowImpact, false, sortOrder++));
                    totalInvestingCashFlow = totalInvestingCashFlow.add(cashFlowImpact);
                }
            }
        }
        reportData.add(new CashFlowStatementRowBean("Investing Activities", "Net Cash from Investing Activities", totalInvestingCashFlow, true, sortOrder++));

        // --- Financing Activities ---
        sortOrder = 300;
        BigDecimal totalFinancingCashFlow = BigDecimal.ZERO;
        for (Account account : chartOfAccounts.getAccounts()) {
            // Assuming getAccountTypeEnum() is the correct method
            if (account.getAccountTypeEnum() == AccountType.LONG_TERM_LIABILITY || account.getAccountTypeEnum() == AccountType.EQUITY) {
                if ("Current Period Net Income".equalsIgnoreCase(account.getName()) || "Retained Earnings".equalsIgnoreCase(account.getName())) {
                    // continue; // Decided to include them if they change, representing new capital or distributions
                }
                if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts)) {
                    continue;
                }
                BigDecimal endBalance = getAccountBalanceAsOfDate(account, reportEndDate, ledger, chartOfAccounts, selectedFundIds, applyFundFilter);
                BigDecimal beginningBalance = getAccountBalanceAsOfDate(account, beginningDateForChanges, ledger, chartOfAccounts, selectedFundIds, applyFundFilter);
                BigDecimal change = endBalance.subtract(beginningBalance);

                if (change.compareTo(BigDecimal.ZERO) != 0) {
                    reportData.add(new CashFlowStatementRowBean("Financing Activities", "Change in " + account.getName(), change, false, sortOrder++));
                    totalFinancingCashFlow = totalFinancingCashFlow.add(change);
                }
            }
        }
        reportData.add(new CashFlowStatementRowBean("Financing Activities", "Net Cash from Financing Activities", totalFinancingCashFlow, true, sortOrder++));

        // --- Summary: Net Change in Cash ---
        sortOrder = 400;
        BigDecimal netChangeInCashCalculated = cashFromOperations.add(totalInvestingCashFlow).add(totalFinancingCashFlow);
        reportData.add(new CashFlowStatementRowBean("Summary", "Net Increase/Decrease in Cash", netChangeInCashCalculated, true, sortOrder++));

        BigDecimal cashAtBeginningOfPeriod = BigDecimal.ZERO;
        BigDecimal cashAtEndOfPeriod = BigDecimal.ZERO;

        for(Account cashAccount : chartOfAccounts.getAccounts()){
            // Assuming getAccountTypeEnum()
            if(cashAccount.getAccountTypeEnum() == AccountType.BANK || cashAccount.getAccountTypeEnum() == AccountType.CASH){
                if (applyFundFilter && !doesAccountMatchFunds(cashAccount, selectedFundIds, chartOfAccounts)) {
                    continue;
                }
                cashAtBeginningOfPeriod = cashAtBeginningOfPeriod.add(getAccountBalanceAsOfDate(cashAccount, beginningDateForChanges, ledger, chartOfAccounts, selectedFundIds, applyFundFilter));
                cashAtEndOfPeriod = cashAtEndOfPeriod.add(getAccountBalanceAsOfDate(cashAccount, reportEndDate, ledger, chartOfAccounts, selectedFundIds, applyFundFilter));
            }
        }
        reportData.add(new CashFlowStatementRowBean("Summary", "Cash at Beginning of Period", cashAtBeginningOfPeriod, false, sortOrder++));
        reportData.add(new CashFlowStatementRowBean("Summary", "Cash at End of Period", cashAtEndOfPeriod, true, sortOrder++));

        // Optional: Verification
        // BigDecimal actualNetChange = cashAtEndOfPeriod.subtract(cashAtBeginningOfPeriod);
        // if (netChangeInCashCalculated.abs().subtract(actualNetChange.abs()).abs().compareTo(new BigDecimal("0.01")) > 0) { // Compare absolute values for safety
        //    LOGGER.warning("Cash Flow Statement discrepancy: Calculated Net Change " + netChangeInCashCalculated +
        //                   " vs Actual Net Change from balances " + actualNetChange + ". Difference: " + netChangeInCashCalculated.subtract(actualNetChange) );
        // }

        return reportData;
    }

    public List<TrialBalanceRowBean> prepareTrialBalanceJasperData(
            ReportContext context, Ledger ledger, ChartOfAccounts chartOfAccounts) {

        List<TrialBalanceRowBean> reportData = new ArrayList<>();

        if (context.getEndDate() == null || ledger == null || chartOfAccounts == null) {
            LOGGER.warning("End date, ledger, or COA missing for Trial Balance data preparation.");
            return reportData;
        }

        LocalDate reportEndDate = context.getEndDate();
        long reportEndDateMillisInclusive = reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        long reportStartDateMillis = 0;
        if (context.getStartDate() != null) {
            reportStartDateMillis = context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        }

        List<String> selectedFundIds = context.getFundIds();
        boolean applyFundFilter = (selectedFundIds != null && !selectedFundIds.isEmpty());

        List<Account> accountsToList = chartOfAccounts.getAccounts();
        if (accountsToList == null) {
            accountsToList = new ArrayList<>();
        }

        accountsToList.sort(Comparator.comparing(Account::getAccountNumber));

        for (Account account : accountsToList) {
            if (account == null || account.getAccountNumber() == null || account.getName() == null || account.getAccountTypeEnum() == null) { // Use getAccountTypeEnum
                LOGGER.warning("TB Data: Skipping account with missing critical information: " + (account != null ? account.getAccountNumber() : "null account object"));
                continue;
            }

            if (applyFundFilter && !doesAccountMatchFunds(account, selectedFundIds, chartOfAccounts)) {
                continue;
            }

            BigDecimal accountBalance = account.getOpeningBalance() != null ? account.getOpeningBalance() : BigDecimal.ZERO;

            List<AccountingTransaction> transactions = ledger.getTransactions();
            if (transactions != null) {
                for (AccountingTransaction transaction : transactions) {
                    if (transaction == null || transaction.getBookingDateTimestamp() >= reportEndDateMillisInclusive) {
                        continue;
                    }
                    if (reportStartDateMillis > 0 && transaction.getBookingDateTimestamp() < reportStartDateMillis) {
                        continue;
                    }

                    if (transaction.getEntries() == null) continue;

                    for (AccountingEntry entry : transaction.getEntries()) {
                        if (entry == null || !account.getAccountNumber().equals(entry.getAccountNumber()) || entry.getAmount() == null) {
                            continue;
                        }

                        AccountType type = account.getAccountTypeEnum(); // Use getAccountTypeEnum
                        if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
                            if (entry.getAccountSide() == AccountSide.DEBIT) {
                                accountBalance = accountBalance.add(entry.getAmount());
                            } else {
                                accountBalance = accountBalance.subtract(entry.getAmount());
                            }
                        } else {
                            if (entry.getAccountSide() == AccountSide.CREDIT) {
                                accountBalance = accountBalance.add(entry.getAmount());
                            } else {
                                accountBalance = accountBalance.subtract(entry.getAmount());
                            }
                        }
                    }
                }
            }

            BigDecimal debitAmount = BigDecimal.ZERO;
            BigDecimal creditAmount = BigDecimal.ZERO;

            AccountType type = account.getAccountTypeEnum(); // Use getAccountTypeEnum
            if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
                if (accountBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    debitAmount = accountBalance;
                } else {
                    creditAmount = accountBalance.abs();
                }
            } else {
                if (accountBalance.compareTo(BigDecimal.ZERO) >= 0) {
                    creditAmount = accountBalance;
                } else {
                    debitAmount = accountBalance.abs();
                }
            }

            reportData.add(new TrialBalanceRowBean(account.getAccountNumber(), account.getName(), debitAmount, creditAmount));
        }
        return reportData;
    }

    public File generateJasperReport(ReportContext context, String outputFormat) throws Exception {
        Company currentCompany = CurrentCompany.getCompany();
        if (currentCompany == null) {
            System.err.println("No company is currently open. Cannot generate report.");
            throw new IllegalStateException("No company is currently open. Cannot generate report.");
        }

        AbstractReportGenerator reportGeneratorInstance = null;
        String reportType = context.getReportType();

        if (reportType == null || reportType.trim().isEmpty()) {
            throw new IllegalArgumentException("Report type must be specified in ReportContext.");
        }

        switch (reportType) {
            case "income_statement_jasper":
                reportGeneratorInstance = new IncomeStatementJasperGenerator(context, this);
                break;
            case "cash_flow_statement_jasper":
                reportGeneratorInstance = new CashFlowStatementJasperGenerator(context, this);
                break;
            // TODO: Add cases for other Jasper reports
            default:
                System.err.println("Unsupported or unknown Jasper report type: " + reportType);
                throw new IllegalArgumentException("Unsupported Jasper report type: " + reportType);
        }

        if (reportGeneratorInstance != null) {
            // --- NEW FILE HANDLING ---
            File generatedFile = reportGeneratorInstance.generateAndExportReport(outputFormat);

            if (generatedFile != null && generatedFile.exists()) {
                System.out.println("ReportService: Successfully received generated file: " + generatedFile.getAbsolutePath());
                return generatedFile;
            } else if (generatedFile != null && !generatedFile.exists()) {
                System.err.println("ReportService: Generator returned a File object, but the file does not exist at: " + generatedFile.getAbsolutePath());
                throw new java.io.FileNotFoundException("Generated report file reference returned by generator, but file not found: " + generatedFile.getAbsolutePath());
            } else {
                System.err.println("ReportService: Report generator failed to return a valid file object for report type: " + reportType + ".");
                throw new Exception("Report generation failed to produce a file for report type: " + reportType + ".");
            }
            // --- END NEW FILE HANDLING ---
        } else {
            // This case should ideally be caught by the default in the switch
            System.err.println("ReportService: No report generator instance for type: " + reportType);
            throw new IllegalArgumentException("No report generator configured for report type: " + reportType);
        }
    }
}
