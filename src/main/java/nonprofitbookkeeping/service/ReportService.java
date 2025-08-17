
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.model.budget.BudgetLine;
import nonprofitbookkeeping.model.budget.Periodicity;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.ReportMetadata;
import nonprofitbookkeeping.reports.generator.AbstractReportGenerator;
import nonprofitbookkeeping.reports.generator.AccountLedgerJasperGenerator;
import nonprofitbookkeeping.reports.generator.AccountSummaryJasperGenerator;
import nonprofitbookkeeping.reports.generator.BalanceResultReportGenerator;
import nonprofitbookkeeping.reports.generator.BankReconciliationJasperGenerator;
import nonprofitbookkeeping.reports.generator.CashFlowStatementJasperGenerator;
import nonprofitbookkeeping.reports.generator.ChartOfAccountsJasperGenerator;
import nonprofitbookkeeping.reports.generator.FundLedgerJasperGenerator;
import nonprofitbookkeeping.reports.generator.GeneralJournalJasperGenerator;
import nonprofitbookkeeping.reports.generator.GeneralLedgerJasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeStatementAltJasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeStatementJasperGenerator;
import nonprofitbookkeeping.reports.generator.TrialBalanceJasperGenerator;
import nonprofitbookkeeping.reports.generator.TransactionReportJasperGenerator;
import nonprofitbookkeeping.reports.generator.AssetDtl5aJasperGenerator;
import nonprofitbookkeeping.reports.generator.Balance3v2JasperGenerator;
import nonprofitbookkeeping.reports.generator.ContactInfoJasperGenerator;
import nonprofitbookkeeping.reports.generator.DeprDtl8JasperGenerator;
import nonprofitbookkeeping.reports.generator.ExpenseDtl12aJasperGenerator;
import nonprofitbookkeeping.reports.generator.ExpenseDtl12bJasperGenerator;
import nonprofitbookkeeping.reports.generator.FinanceComm13JasperGenerator;
import nonprofitbookkeeping.reports.generator.Funds14JasperGenerator;
import nonprofitbookkeeping.reports.generator.Income4JasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeDtl11aJasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeDtl11bJasperGenerator;
import nonprofitbookkeeping.reports.generator.IncomeDtl11cJasperGenerator;
import nonprofitbookkeeping.reports.generator.InventoryDtl6JasperGenerator;
import nonprofitbookkeeping.reports.generator.LedgerQ1JasperGenerator;
import nonprofitbookkeeping.reports.generator.LiabilityDtl5bJasperGenerator;
import nonprofitbookkeeping.reports.generator.Newsletter15JasperGenerator;
import nonprofitbookkeeping.reports.generator.PrimaryAccountJasperGenerator;
import nonprofitbookkeeping.reports.generator.PrimaryAccountReconciliationJasperGenerator;
import nonprofitbookkeeping.reports.generator.RegaliaSalesDtl7JasperGenerator;
import nonprofitbookkeeping.reports.generator.SecondaryAccountJasperGenerator;
import nonprofitbookkeeping.reports.generator.TransferIn9JasperGenerator;
import nonprofitbookkeeping.reports.generator.TransferOut10JasperGenerator;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.File;
import java.io.IOException;
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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.service.SupplementalRecordService;

/**
 * Service class responsible for preparing data contexts for various financial reports
 * and orchestrating the generation of reports using templating engines like JasperReports.
 * It interacts with other services and models (e.g., {@link Ledger}, {@link ChartOfAccounts}, {@link Budget})
 * to gather and process data according to the criteria specified in a {@link ReportContext}.
 */
public class ReportService
{
	/** Logger for this class. */
	public static final Logger LOGGER =
		Logger.getLogger(ReportService.class.getName());
	
	/** Standard date formatter (ISO Local Date, e.g., "YYYY-MM-DD") used in some report outputs. */
	private static final DateTimeFormatter DATE_FORMATTER =
		DateTimeFormatter.ISO_LOCAL_DATE;
	
	/** Service for managing supplemental record links. */
	private final SupplementalRecordService supplementalRecordService;
	
	/** Default constructor uses the built-in registry. */
	public ReportService()
	{
		this(createDefaultRegistry());
		
	}
	
	/** 
	 * DI-friendly constructor that accepts a registry. 
	 * The registry remains mutable for runtime changes. 
	 */
	public ReportService(Map<ReportType,
		BiFunction<ReportContext, ReportService,
			AbstractReportGenerator>> registry)
	{
		this.generatorRegistry = new ConcurrentHashMap<>(registry);
		this.supplementalRecordService = new SupplementalRecordService();
		
	}
	
	
	/**
	 * Generates a very basic plain text report summarizing the provided
	 * {@link ReportContext}. This is used as a fallback when the user
	 * requests a "text" output format.
	 *
	 * @param ctx The context describing the report that was requested.
	 * @return The created text {@link File}.
	 * @throws IOException If the file cannot be written.
	 */
	public static File generatePlainTextReport(ReportContext ctx)
		throws IOException
	{
		File outputDirectory =
			new File(System.getProperty("user.home"),
				"NonprofitBookkeepingReports");
		
		if (!outputDirectory.exists())
		{
			outputDirectory.mkdirs();
		}
		
		String baseName =
			(ctx.getReportType() != null ? ctx.getReportType() : "report") +
				"_" +
				System.currentTimeMillis() + ".txt";
		
		File outFile = new File(outputDirectory, baseName);
		
		try (java.io.PrintWriter pw = new java.io.PrintWriter(outFile))
		{
			pw.println("Report Type: " + ctx.getReportType());
			
			if (ctx.getStartDate() != null)
			{
				pw.println("Start Date: " + ctx.getStartDate());
			}
			
			if (ctx.getEndDate() != null)
			{
				pw.println("End Date: " + ctx.getEndDate());
			}
			
			pw.println("Generated on: " + java.time.LocalDate.now());
			pw.println(
				"(Text output not formatted. Use PDF or HTML for full detail.)");
		}
		
		return outFile;
		
	}
	
	/**
	 * Generate a Jasper report using the generator registry and export it to disk.
	 *
	 * @param ctx          ReportContext describing what to build (includes reportType, dates, etc.)
	 * @param outputFormat Preferred output format: "pdf" (default), "html", or "xlsx".
	 * @return File pointing to the generated artifact on disk
	 * @throws JRException  if Jasper fails to fill/export
	 * @throws IOException  if file operations fail
	 * @throws IllegalArgumentException if the report type is unknown or no generator is registered
	 */
	public File generateJasperReport(ReportContext ctx, String outputFormat)
		throws JRException, IOException
	{
		
		if (ctx == null || ctx.getReportType() == null)
		{
			throw new IllegalArgumentException(
				"ReportContext and reportType are required.");
		}
		
		// Resolve report type and generator
		ReportType type = ReportType.fromId(ctx.getReportType());
		
		if (type == null)
		{
			throw new IllegalArgumentException(
				"Unknown reportType: " + ctx.getReportType());
		}
		
		BiFunction<ReportContext, ReportService,
			AbstractReportGenerator> factory = this.generatorRegistry.get(type);
		
		if (factory == null)
		{
			throw new IllegalArgumentException(
				"No generator registered for reportType: " + type.id());
		}
		
		AbstractReportGenerator generator = factory.apply(ctx, this);
		
		if (generator == null)
		{
			throw new IllegalStateException(
				"Generator factory returned null for " + type.id());
		}
		
		if (ctx.getBeans() != null)
		{
			generator.setReportData(ctx.getBeans());
		}
		
		// Ask the generator to build the JasperPrint
		JasperPrint print = generator.generatePrint();
		
		// Normalize format; default to PDF
		String fmt =
			(outputFormat == null ? "pdf" : outputFormat).trim().toLowerCase();
		
		File out =
			generator.writeJasperOutput(fmt, print, generator.getBaseName());
		LOGGER.info("Report generated: " + out.getAbsolutePath());
		return out;
		
	}
	
	/** 
	 * Mapping of Jasper report types to their generator constructors. 
	 */
	private final Map<ReportType,
		BiFunction<ReportContext, ReportService,
			AbstractReportGenerator>> generatorRegistry;
	
	/** Enum-safe keys for the Jasper report generator registry. */
	public enum ReportType
	{
		INCOME_STATEMENT_JASPER("income_statement_jasper"),
		CASH_FLOW_STATEMENT_JASPER("cash_flow_statement_jasper"),
		TRIAL_BALANCE_JASPER("trial_balance_jasper"),
		BALANCE_SHEET_JASPER("balance_sheet_jasper"),
		ACCOUNT_LEDGER_JASPER("account_ledger_jasper"),
		ACCOUNT_SUMMARY_JASPER("account_summary_jasper"),
		BANK_RECONCILIATION_JASPER("bank_reconciliation_jasper"),
		CHART_OF_ACCOUNTS_JASPER("chart_of_accounts_jasper"),
		FUND_LEDGER_JASPER("fund_ledger_jasper"),
		GENERAL_JOURNAL_JASPER("general_journal_jasper"),
		GENERAL_LEDGER_JASPER("general_ledger_jasper"),
		INCOME_STATEMENT_ALT_JASPER("income_statement_alt_jasper"),
		TRANSACTION_REPORT_JASPER("transaction_report_jasper"),
		SCA_ASSET_DTL_5A_JASPER("sca_asset_dtl_5a_jasper"),
		SCA_BALANCE_3_JASPER("sca_balance_3_jasper"),
		SCA_BALANCE_3_V2_JASPER("sca_balance_3_v2_jasper"),
		SCA_CONTACT_INFO_JASPER("sca_contact_info_jasper"),
		SCA_DEPR_DTL_8_JASPER("sca_depr_dtl_8_jasper"),
		SCA_EXPENSE_DTL_12A_JASPER("sca_expense_dtl_12a_jasper"),
		SCA_EXPENSE_DTL_12B_JASPER("sca_expense_dtl_12b_jasper"),
		SCA_FINANCE_COMM_13_JASPER("sca_finance_comm_13_jasper"),
		SCA_FUNDS_14_JASPER("sca_funds_14_jasper"),
		SCA_INCOME_4_JASPER("sca_income_4_jasper"),
		SCA_INCOME_DTL_11A_JASPER("sca_income_dtl_11a_jasper"),
		SCA_INCOME_DTL_11B_JASPER("sca_income_dtl_11b_jasper"),
		SCA_INCOME_DTL_11C_JASPER("sca_income_dtl_11c_jasper"),
		SCA_INVENTORY_DTL_6_JASPER("sca_inventory_dtl_6_jasper"),
		SCA_LEDGER_Q1_JASPER("sca_ledger_q1_jasper"),
		SCA_LIABILITY_DTL_5B_JASPER("sca_liability_dtl_5b_jasper"),
		SCA_NEWSLETTER_15_JASPER("sca_newsletter_15_jasper"),
		SCA_PRIMARY_ACCOUNT_JASPER("sca_primary_account_jasper"),
		SCA_PRIMARY_ACCOUNT_RECONCILIATION_JASPER(
			"sca_primary_account_reconciliation_jasper"),
		SCA_REGALIA_SALES_DTL_7_JASPER("sca_regalia_sales_dtl_7_jasper"),
		SCA_SECONDARY_ACCOUNT_JASPER("sca_secondary_account_jasper"),
		SCA_TRANSFER_IN_9_JASPER("sca_transfer_in_9_jasper"),
		SCA_TRANSFER_OUT_10_JASPER("sca_transfer_out_10_jasper");
		
		private final String id;
		
		ReportType(String id)
		{
			this.id = id;
			
		}
		
		public String id()
		{
			return this.id;
			
		}
		
		public static ReportType fromId(String id)
		{
			if (id == null)
				return null;
			String norm = id.trim().toLowerCase();
			
			for (ReportType t : values())
			{
				if (t.id.equals(norm))
					return t;
			}
			
			return null;
			
		}
		
	}
	
	/** Factory for the built-in generator registry. */
	private static
		Map<ReportType,
			BiFunction<ReportContext, ReportService, AbstractReportGenerator>>
		createDefaultRegistry()
	{
		Map<ReportType,
			BiFunction<ReportContext, ReportService,
				AbstractReportGenerator>> map =
					new EnumMap<>(ReportType.class);
		
		map.put(ReportType.INCOME_STATEMENT_JASPER,
			(ctx, svc) -> new IncomeStatementJasperGenerator(ctx, svc));
		map.put(ReportType.CASH_FLOW_STATEMENT_JASPER,
			(ctx, svc) -> new CashFlowStatementJasperGenerator(ctx, svc));
		map.put(ReportType.TRIAL_BALANCE_JASPER,
			(ctx, svc) -> new TrialBalanceJasperGenerator(ctx, svc));
		map.put(ReportType.BALANCE_SHEET_JASPER,
			(ctx, svc) -> new BalanceResultReportGenerator(null));
		map.put(ReportType.ACCOUNT_LEDGER_JASPER,
			(ctx, svc) -> new AccountLedgerJasperGenerator());
		map.put(ReportType.ACCOUNT_SUMMARY_JASPER,
			(ctx, svc) -> new AccountSummaryJasperGenerator());
		map.put(ReportType.BANK_RECONCILIATION_JASPER,
			(ctx, svc) -> new BankReconciliationJasperGenerator());
		map.put(ReportType.CHART_OF_ACCOUNTS_JASPER,
			(ctx, svc) -> new ChartOfAccountsJasperGenerator(svc));
		map.put(ReportType.FUND_LEDGER_JASPER,
			(ctx, svc) -> new FundLedgerJasperGenerator());
		map.put(ReportType.GENERAL_JOURNAL_JASPER,
			(ctx, svc) -> new GeneralJournalJasperGenerator());
		map.put(ReportType.GENERAL_LEDGER_JASPER,
			(ctx, svc) -> new GeneralLedgerJasperGenerator());
		map.put(ReportType.INCOME_STATEMENT_ALT_JASPER,
			(ctx, svc) -> new IncomeStatementAltJasperGenerator());
		map.put(ReportType.TRANSACTION_REPORT_JASPER,
			(ctx, svc) -> new TransactionReportJasperGenerator());
		map.put(ReportType.SCA_ASSET_DTL_5A_JASPER,
			(ctx, svc) -> new AssetDtl5aJasperGenerator());
		map.put(ReportType.SCA_BALANCE_3_V2_JASPER,
			(ctx, svc) -> new Balance3v2JasperGenerator());
		map.put(ReportType.SCA_CONTACT_INFO_JASPER,
			(ctx, svc) -> new ContactInfoJasperGenerator());
		map.put(ReportType.SCA_DEPR_DTL_8_JASPER,
			(ctx, svc) -> new DeprDtl8JasperGenerator());
		map.put(ReportType.SCA_EXPENSE_DTL_12A_JASPER,
			(ctx, svc) -> new ExpenseDtl12aJasperGenerator());
		map.put(ReportType.SCA_EXPENSE_DTL_12B_JASPER,
			(ctx, svc) -> new ExpenseDtl12bJasperGenerator());
		map.put(ReportType.SCA_FINANCE_COMM_13_JASPER,
			(ctx, svc) -> new FinanceComm13JasperGenerator());
		map.put(ReportType.SCA_FUNDS_14_JASPER,
			(ctx, svc) -> new Funds14JasperGenerator());
		map.put(ReportType.SCA_INCOME_4_JASPER,
			(ctx, svc) -> new Income4JasperGenerator());
		map.put(ReportType.SCA_INCOME_DTL_11A_JASPER,
			(ctx, svc) -> new IncomeDtl11aJasperGenerator());
		map.put(ReportType.SCA_INCOME_DTL_11B_JASPER,
			(ctx, svc) -> new IncomeDtl11bJasperGenerator());
		map.put(ReportType.SCA_INCOME_DTL_11C_JASPER,
			(ctx, svc) -> new IncomeDtl11cJasperGenerator());
		map.put(ReportType.SCA_INVENTORY_DTL_6_JASPER,
			(ctx, svc) -> new InventoryDtl6JasperGenerator());
		map.put(ReportType.SCA_LEDGER_Q1_JASPER,
			(ctx, svc) -> new LedgerQ1JasperGenerator());
		map.put(ReportType.SCA_LIABILITY_DTL_5B_JASPER,
			(ctx, svc) -> new LiabilityDtl5bJasperGenerator());
		map.put(ReportType.SCA_NEWSLETTER_15_JASPER,
			(ctx, svc) -> new Newsletter15JasperGenerator());
		map.put(ReportType.SCA_PRIMARY_ACCOUNT_JASPER,
			(ctx, svc) -> new PrimaryAccountJasperGenerator());
		map.put(ReportType.SCA_PRIMARY_ACCOUNT_RECONCILIATION_JASPER,
			(ctx, svc) -> new PrimaryAccountReconciliationJasperGenerator());
		map.put(ReportType.SCA_REGALIA_SALES_DTL_7_JASPER,
			(ctx, svc) -> new RegaliaSalesDtl7JasperGenerator());
		map.put(ReportType.SCA_SECONDARY_ACCOUNT_JASPER,
			(ctx, svc) -> new SecondaryAccountJasperGenerator());
		map.put(ReportType.SCA_TRANSFER_IN_9_JASPER,
			(ctx, svc) -> new TransferIn9JasperGenerator());
		map.put(ReportType.SCA_TRANSFER_OUT_10_JASPER,
			(ctx, svc) -> new TransferOut10JasperGenerator());
		return map;
		
	}
	
	
	/** Allow runtime registration / replacement of a generator (mutable registry). */
	public void registerGenerator(ReportType type,
		BiFunction<ReportContext,
			ReportService,
			AbstractReportGenerator> factory)
	{
		
		if (type == null || factory == null)
		{
			throw new IllegalArgumentException("type and factory are required");
		}
		
		this.generatorRegistry.put(type, factory);
		
	}
	
	/** Remove an existing generator mapping, returning the previous factory if any. */
	public
		BiFunction<ReportContext, ReportService, AbstractReportGenerator>
		unregisterGenerator(ReportType type)
	{
		return this.generatorRegistry.remove(type);
		
	}
	
	
	/**
	 * Checks if a given account is associated with any of the selected funds.
	 * This is a helper method used for filtering report data based on fund selections.
	 *
	 * @param account The {@link Account} to check.
	 * @param selectedFundNames A list of names of the funds selected for filtering.
	 * @param chartOfAccounts The {@link ChartOfAccounts} (currently unused in this specific method logic but could be for future enhancements).
	 *
	 * @return {@code true} if {@code selectedFundNames} is null or empty (implying no filter),
	 *         or if the {@code account} is not null, has associated funds, and at least one of its
	 *         associated funds is in the {@code selectedFundNames} list. Returns {@code false} otherwise.
	 */
	public static boolean doesAccountMatchFunds(Account account,
		List<String> selectedFundNames,
		ChartOfAccounts chartOfAccounts)
	{
		
		if (selectedFundNames == null || selectedFundNames.isEmpty())
		{
			return true;
			// No fund filter applied, so account matches by default.
		}
		
		if (account == null)
		{
			return false; // Null account cannot match.
		}
		
		List<String> associatedFunds = account.getAssociatedFundIds();
		
		if (associatedFunds == null || associatedFunds.isEmpty())
		{
			return false;
			// Account has no associated funds, so cannot match specific fund
			// selection.
		}
		
		for (String fundId : associatedFunds)
		{
			
			if (fundId != null && selectedFundNames.contains(fundId))
			{
				return true; // Account is associated with at least one of the
								// selected funds.
			}
			
		}
		
		return false; // Account is not associated with any of the selected
						// funds.
		
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
			throw new NullPointerException(
				"Account cannot be null for balance calculation.");
		}
		
		BigDecimal balance =
			account.getOpeningBalance() == null ? BigDecimal.ZERO :
				account.getOpeningBalance();
		
		if (entries == null)
		{
			return balance;
		}
		
		AccountSide increaseSide = account.getIncreaseSide();
		
		if (increaseSide == null)
		{
			LOGGER.warning(
				"Account " + account.getAccountNumber() +
					" has no defined increase side.");
			return balance;
		}
		
		for (AccountingEntry entry : entries)
		{
			
			if (entry == null || entry.getAmount() == null)
			{
				continue;
			}
			
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
				{
					balance = balance.add(entry.getAmount());
				}
				else
				{
					balance = balance.subtract(entry.getAmount());
				}
				
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
	public static BigDecimal getAccountBalanceAsOfDate(Account account,
		LocalDate date,
		Ledger ledger,
		ChartOfAccounts chartOfAccounts,
		List<String> selectedFundNames,
		boolean applyFundFilter)
	{
		
		if (account == null)
		{
			throw new NullPointerException(
				"Account cannot be null for balance calculation.");
		}
		
		if (date == null)
		{
			throw new NullPointerException(
				"Date cannot be null for balance calculation.");
		}
		
		if (ledger == null)
		{
			throw new NullPointerException(
				"Ledger cannot be null for balance calculation.");
		}
		
		if (applyFundFilter &&
			!doesAccountMatchFunds(account, selectedFundNames, chartOfAccounts))
		{
			return BigDecimal.ZERO;
		}
		
		long endDateMillisInclusive = // Inclusive of the 'date'
			date.atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC)
				.toInstant()
				.toEpochMilli();
		
		List<AccountingEntry> relevantEntries = new ArrayList<>();
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions != null)
		{
			
			for (AccountingTransaction transaction : transactions)
			{
				
				if (transaction == null ||
					transaction.getBookingDateTimestamp() >
						endDateMillisInclusive)
				{
					continue;
				}
				
				Set<AccountingEntry> entries = transaction.getEntries();
				
				if (entries == null)
				{
					continue;
				}
				
				for (AccountingEntry entry : entries)
				{
					if (entry == null || entry.getAmount() == null)
						continue;
					
					if (!account.getAccountNumber()
						.equals(entry.getAccountNumber()))
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
	static Map<String, Object> prepareTrialBalanceContext(ReportContext context,
		Ledger ledger,
		ChartOfAccounts chartOfAccounts)
	{
		List<Map<String, Object>> trialBalanceItems = new ArrayList<>();
		BigDecimal totalDebits = BigDecimal.ZERO;
		BigDecimal totalCredits = BigDecimal.ZERO;
		
		if (context.getEndDate() == null)
		{
			throw new IllegalArgumentException(
				"End date must be provided for Trial Balance.");
		}
		
		LocalDate reportEndDate = context.getEndDate();
		long reportEndDateMillisExclusive = // Transactions strictly before the
											// start of the
											// next day
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter =
			(selectedFundNames != null && !selectedFundNames.isEmpty());
		
		long reportStartDateMillis = 0; // Default to beginning of time if no
										// start date
		
		if (context.getStartDate() != null)
		{
			reportStartDateMillis =
				context.getStartDate().atStartOfDay(ZoneOffset.UTC).toInstant()
					.toEpochMilli();
		}
		
		List<AccountingTransaction> transactions = ledger.getTransactions();
		
		if (transactions == null)
		{
			transactions = new ArrayList<AccountingTransaction>(); // Ensure
																	// non-null
			LOGGER
				.info("No transactions found in the ledger for Trial Balance.");
		}
		
		List<Account> accounts =
			(chartOfAccounts != null && chartOfAccounts.getAccounts() != null) ?
				chartOfAccounts.getAccounts() : new ArrayList<Account>(); // Ensure
																			// non-null
		
		for (Account account : accounts)
		{
			
			if (account == null || account.getAccountNumber() == null ||
				account.getName() == null ||
				account.getIncreaseSide() == null ||
				account.getAccountType() == null)
			{
				LOGGER.warning(
					"TB: Skipping account with missing critical information: " +
						(account != null ? account.getAccountNumber() :
							"null account object"));
				continue;
			}
			
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundNames,
					chartOfAccounts))
			{
				continue; // Skip account if it doesn't match fund filter
			}
			
			// Calculate balance for this account considering the period
			BigDecimal accountBalance =
				account.getOpeningBalance() == null ? BigDecimal.ZERO :
					account.getOpeningBalance();
			
			for (AccountingTransaction transaction : transactions)
			{
				
				if (transaction == null ||
					transaction.getBookingDateTimestamp() >=
						reportEndDateMillisExclusive ||
					transaction.getBookingDateTimestamp() <
						reportStartDateMillis)
				{
					continue;
				}
				
				Set<AccountingEntry> entries = transaction.getEntries();
				if (entries == null)
					continue;
				
				for (AccountingEntry entry : entries)
				{
					
					if (entry == null ||
						!account.getAccountNumber()
							.equals(entry.getAccountNumber()) ||
						entry.getAmount() == null)
					{
						continue;
					}
					
					AccountSide increaseSide = account.getIncreaseSide(); // Use
																			// direct
																			// enum
					if (increaseSide == null)
						continue; // Should not happen
						
					if (increaseSide == AccountSide.DEBIT)
					{
						
						if (entry.getAccountSide() == AccountSide.DEBIT)
						{
							accountBalance =
								accountBalance.add(entry.getAmount());
						}
						else // CREDIT
						{
							accountBalance =
								accountBalance.subtract(entry.getAmount());
						}
						
					}
					else // increaseSide is CREDIT
					{
						
						if (entry.getAccountSide() == AccountSide.CREDIT)
						{
							accountBalance =
								accountBalance.add(entry.getAmount());
						}
						else // DEBIT
						{
							accountBalance =
								accountBalance.subtract(entry.getAmount());
						}
						
					}
					
				}
				
			}
			
			BigDecimal finalDebitAmount = BigDecimal.ZERO;
			BigDecimal finalCreditAmount = BigDecimal.ZERO;
			
			AccountSide increaseSide = account.getIncreaseSide(); // Use direct
																	// enum
			
			if (increaseSide == null)
			{
				continue;
			}
			
			if (increaseSide == AccountSide.DEBIT)
			{
				
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
				{
					finalDebitAmount = accountBalance;
				}
				else
				{
					finalCreditAmount = accountBalance.abs(); // Negative
																// balance for
				}
				
				// debit-normal account
				// shown as credit
			}
			else // increaseSide is CREDIT
			{
				
				if (accountBalance.compareTo(BigDecimal.ZERO) >= 0)
				{
					finalCreditAmount = accountBalance;
				}
				else
				{
					finalDebitAmount = accountBalance.abs(); // Negative balance
																// for
				}
				
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
		
		// Round totals to 2 decimal places for comparison, common in financial
		// reports
		totalDebits = totalDebits.setScale(2, RoundingMode.HALF_UP);
		totalCredits = totalCredits.setScale(2, RoundingMode.HALF_UP);
		
		if (totalDebits.compareTo(totalCredits) != 0)
		{
			LOGGER.warning("Trial Balance (fund-filtered: " + applyFundFilter +
				") totals do not match! Debits: " + totalDebits +
				", Credits: " + totalCredits +
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
		jxlsContext.put("totalsMatch",
			totalDebits.compareTo(totalCredits) == 0);
		
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
	static Map<String, Object> prepareCashFlowStatementContext(
		ReportContext context,
		Ledger ledger,
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
		boolean applyFundFilter =
			(selectedFundNames != null && !selectedFundNames.isEmpty());
		
		jxlsContext.put("reportStartDate", reportStartDate.toString());
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		
		ReportContext incomeStatementPeriodContext = new ReportContext();
		incomeStatementPeriodContext.setStartDate(reportStartDate);
		incomeStatementPeriodContext.setEndDate(reportEndDate);
		incomeStatementPeriodContext.setFundIds(selectedFundNames);
		Map<String,
			Object> incomeStatementContext = IncomeStatementJasperGenerator
				.prepareIncomeStatementContext(incomeStatementPeriodContext,
					ledger,
					chartOfAccounts);
		BigDecimal netIncome =
			(BigDecimal) incomeStatementContext.getOrDefault("netIncome",
				BigDecimal.ZERO);
		jxlsContext.put("netIncome", netIncome);
		
		List<Account> cashEquivalentAccounts = new ArrayList<>();
		Set<String> cashEquivalentAccountNames = new HashSet<>(); // Using names
																	// for
																	// lookup
																	// in
																	// working
																	// capital
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			AccountType accType = account.getAccountType(); // Use direct enum
			
			if (accType != null)
			{
				
				if (accType == AccountType.BANK ||
					accType == AccountType.CASH ||
					accType == AccountType.CHECKING) // Assuming CHECKING is
														// cash equivalent
				{
					
					if (applyFundFilter &&
						!doesAccountMatchFunds(account, selectedFundNames,
							chartOfAccounts))
					{
						continue; // Skip if this cash account doesn't match
									// fund filter
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
			cashAtEndOfPeriod = cashAtEndOfPeriod
				.add(getAccountBalanceAsOfDate(acc, reportEndDate,
					ledger, chartOfAccounts, selectedFundNames, true));
		}
		
		jxlsContext.put("cashAtEndOfPeriod", cashAtEndOfPeriod);
		
		BigDecimal cashAtBeginningOfPeriod = BigDecimal.ZERO;
		LocalDate beginningDate = reportStartDate.minusDays(1);
		
		for (Account acc : cashEquivalentAccounts)
		{
			cashAtBeginningOfPeriod =
				cashAtBeginningOfPeriod.add(getAccountBalanceAsOfDate(acc,
					beginningDate, ledger, chartOfAccounts, selectedFundNames,
					true));
		}
		
		jxlsContext.put("cashAtBeginningOfPeriod", cashAtBeginningOfPeriod);
		
		BigDecimal netChangeInCashActual =
			cashAtEndOfPeriod.subtract(cashAtBeginningOfPeriod);
		jxlsContext.put("netChangeInCashActual", netChangeInCashActual);
		
		BigDecimal totalOperatingAdjustments = BigDecimal.ZERO;
		BigDecimal totalDepreciationAmortization = BigDecimal.ZERO;
		Set<String> deprAmortAccountNames =
			Set.of("Depreciation Expense", "Amortization Expense",
				"Depreciation",
				"Amortization");
		long periodStartDateMillis =
			reportStartDate.atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		long periodEndDateMillisExclusive =
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			
			if (account.getName() != null &&
				deprAmortAccountNames.contains(account.getName()))
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames,
						chartOfAccounts))
				{
					continue;
				}
				
				// Sum debit entries to depreciation/amortization accounts
				// during the period
				for (AccountingTransaction transaction : ledger
					.getTransactions())
				{
					
					if (transaction.getBookingDateTimestamp() >=
						periodStartDateMillis &&
						transaction.getBookingDateTimestamp() <
							periodEndDateMillisExclusive)
					{
						
						for (AccountingEntry entry : transaction.getEntries())
						{
							
							if (entry.getAccountNumber()
								.equals(account.getAccountNumber()) &&
								entry.getAccountSide() == AccountSide.DEBIT) // Depreciation/Amortization
																				// is
																				// an
																				// expense
							{
								totalDepreciationAmortization =
									totalDepreciationAmortization
										.add(entry.getAmount());
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		if (totalDepreciationAmortization.compareTo(BigDecimal.ZERO) != 0)
		{
			operatingActivitiesItems
				.add(Map.of("name", "Depreciation & Amortization", "amount",
					totalDepreciationAmortization));
			totalOperatingAdjustments =
				totalOperatingAdjustments.add(totalDepreciationAmortization);
		}
		
		// Define standard working capital accounts
		Map<String,
			String> workingCapitalConfig =
				new HashMap<>(Map.of("Accounts Receivable", "asset",
					"Inventory", "asset", "Prepaid Expenses", "asset",
					"Accounts Payable",
					"liability",
					"Accrued Expenses", "liability", "Deferred Revenue",
					"liability"));
		
		for (Account account : chartOfAccounts.getAccounts())
		{
			
			if (account.getName() != null &&
				workingCapitalConfig.containsKey(account.getName()))
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames,
						chartOfAccounts))
				{
					continue;
				}
				
				String category = workingCapitalConfig.get(account.getName());
				BigDecimal endBalance =
					getAccountBalanceAsOfDate(account, reportEndDate, ledger,
						chartOfAccounts, selectedFundNames, true); // applyFundFilter
																	// true
				BigDecimal beginningBalance =
					getAccountBalanceAsOfDate(account, beginningDate,
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
				BigDecimal adjustmentAmount =
					"asset".equals(category) ? change.negate() : change;
				String itemName = (change.compareTo(BigDecimal.ZERO) > 0 ?
					("asset".equals(category) ? "Increase in " :
						"Increase in ") :
					("asset".equals(category) ? "Decrease in " :
						"Decrease in ")) +
					account.getName();
				
				operatingActivitiesItems
					.add(Map.of("name", itemName, "amount", adjustmentAmount));
				totalOperatingAdjustments =
					totalOperatingAdjustments.add(adjustmentAmount);
			}
			
		}
		
		jxlsContext.put("operatingActivitiesItems", operatingActivitiesItems);
		BigDecimal cashFromOperations =
			netIncome.add(totalOperatingAdjustments);
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
					!doesAccountMatchFunds(account, selectedFundNames,
						chartOfAccounts))
				{
					continue;
				}
				
				BigDecimal endBalance =
					getAccountBalanceAsOfDate(account, reportEndDate, ledger,
						chartOfAccounts, selectedFundNames, true);
				BigDecimal beginningBalance =
					getAccountBalanceAsOfDate(account, beginningDate,
						ledger, chartOfAccounts, selectedFundNames, true);
				BigDecimal change = endBalance.subtract(beginningBalance); // Increase
																			// in
																			// fixed
																			// asset
				
				if (change.compareTo(BigDecimal.ZERO) != 0)
				{
					BigDecimal cashFlowImpact = change.negate(); // Increase in
																	// asset =
																	// cash
																	// outflow
					investingActivitiesItems.add(
						Map.of("name", "Change in " + account.getName(),
							"amount",
							cashFlowImpact));
					totalInvestingAdjustments =
						totalInvestingAdjustments.add(cashFlowImpact);
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
				(accType == AccountType.LONG_TERM_LIABILITY ||
					accType == AccountType.EQUITY))
			{
				
				
				if ("Current Period Net Income"
					.equalsIgnoreCase(account.getName()))
				{
					continue;
				}
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames,
						chartOfAccounts))
				{
					continue;
				}
				
				BigDecimal endBalance =
					getAccountBalanceAsOfDate(account, reportEndDate, ledger,
						chartOfAccounts, selectedFundNames, true);
				BigDecimal beginningBalance =
					getAccountBalanceAsOfDate(account, beginningDate,
						ledger, chartOfAccounts, selectedFundNames, true);
				BigDecimal change = endBalance.subtract(beginningBalance); // Increase
																			// in
																			// L/E
																			// =
																			// cash
																			// inflow
				
				if (change.compareTo(BigDecimal.ZERO) != 0)
				{
					financingActivitiesItems
						.add(Map.of("name", "Change in " + account.getName(),
							"amount",
							change));
					totalFinancingAdjustments =
						totalFinancingAdjustments.add(change);
				}
				
			}
			
		}
		
		jxlsContext.put("financingActivitiesItems", financingActivitiesItems);
		jxlsContext.put("cashFromFinancing", totalFinancingAdjustments);
		
		BigDecimal netChangeInCashCalculated =
			cashFromOperations.add(totalInvestingAdjustments)
				.add(totalFinancingAdjustments);
		jxlsContext.put("netChangeInCash", netChangeInCashCalculated);
		jxlsContext.put("netChangeInCashCalculated", netChangeInCashCalculated);
		
		// Add discrepancy check line
		BigDecimal discrepancy =
			netChangeInCashCalculated.subtract(netChangeInCashActual);
		jxlsContext.put("discrepancy", discrepancy);
		
		if (discrepancy.abs().compareTo(new BigDecimal("0.01")) > 0)
		{
			LOGGER.warning(
				"Cash Flow Statement (fund-filtered: " + applyFundFilter +
					") discrepancy: " +
					discrepancy + ". Calculated Net Change: " +
					netChangeInCashCalculated +
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
	static Map<String, Object> prepareBudgetVsActualsContext(
		ReportContext context,
		Ledger ledger,
		ChartOfAccounts chartOfAccounts,
		Budget budget)
	{
		
		if (context.getStartDate() == null || context.getEndDate() == null ||
			budget == null ||
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
		boolean applyFundFilter =
			(selectedFundNames != null && !selectedFundNames.isEmpty());
		
		jxlsContext.put("budgetName", budget.getBudgetName());
		jxlsContext.put("fiscalYear", budget.getFiscalYear());
		jxlsContext.put("reportStartDate", reportStartDate.toString());
		jxlsContext.put("reportEndDate", reportEndDate.toString());
		jxlsContext.put("reportDate", LocalDate.now().toString());
		
		Map<String, BigDecimal> actualAmounts = new HashMap<>();
		BigDecimal totalActualIncome = BigDecimal.ZERO;
		BigDecimal totalActualExpenses = BigDecimal.ZERO;
		
		long periodStartDateMillis =
			reportStartDate.atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		long periodEndDateMillisExclusive =
			reportEndDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
				.toEpochMilli();
		
		if (ledger != null && ledger.getTransactions() != null)
		{
			
			for (AccountingTransaction transaction : ledger.getTransactions())
			{
				
				if (transaction.getBookingDateTimestamp() >=
					periodStartDateMillis &&
					transaction.getBookingDateTimestamp() <
						periodEndDateMillisExclusive)
				{
					
					for (AccountingEntry entry : transaction.getEntries())
					{
						Account account = chartOfAccounts
							.getAccount(entry.getAccountNumber());
						
						if (account == null || account.getAccountType() == null)
						{
							continue;
						}
						
						if (applyFundFilter &&
							!doesAccountMatchFunds(account, selectedFundNames,
								chartOfAccounts))
						{
							continue;
						}
						
						AccountType accountType = account.getAccountType();
						BigDecimal currentActual =
							actualAmounts.getOrDefault(
								account.getAccountNumber(),
								BigDecimal.ZERO);
						BigDecimal amount = entry.getAmount();
						
						if (accountType == AccountType.INCOME)
						{
							
							if (entry.getAccountSide() == AccountSide.CREDIT)
							{
								currentActual = currentActual.add(amount);
							}
							else // DEBIT
							{
								currentActual = currentActual.subtract(amount);
							}
							
						}
						else if (accountType == AccountType.EXPENSE)
						{
							
							if (entry.getAccountSide() == AccountSide.DEBIT) // Expense
																				// increases
																				// on
																				// debit
							{
								currentActual = currentActual.add(amount);
							}
							else // CREDIT
							{
								currentActual = currentActual.subtract(amount);
							}
							
						}
						
						actualAmounts.put(account.getAccountNumber(),
							currentActual);
					}
					
				}
				
			}
			
		}
		
		// Calculate total actual income and expenses from the aggregated
		// actualAmounts
		// map
		for (Map.Entry<String, BigDecimal> entry : actualAmounts.entrySet())
		{
			Account account = chartOfAccounts.getAccount(entry.getKey());
			
			if (account != null && account.getAccountType() != null) // Use
																		// direct
																		// enum
			{
				
				if (applyFundFilter &&
					!doesAccountMatchFunds(account, selectedFundNames,
						chartOfAccounts))
				{
					continue;
				}
				
				AccountType type = account.getAccountType(); // Use direct enum
				if (type == AccountType.INCOME)
					totalActualIncome = totalActualIncome.add(entry.getValue());
				else if (type == AccountType.EXPENSE)
					totalActualExpenses =
						totalActualExpenses.add(entry.getValue());
			}
			
		}
		
		BigDecimal totalBudgetedIncome = BigDecimal.ZERO;
		BigDecimal totalBudgetedExpenses = BigDecimal.ZERO;
		int budgetFiscalYear = budget.getFiscalYear();
		long daysInFiscalYear =
			LocalDate.of(budgetFiscalYear, 1, 1).isLeapYear() ? 366 : 365;
		long daysInReportPeriod =
			ChronoUnit.DAYS.between(reportStartDate, reportEndDate) + 1;
		
		for (BudgetLine line : budget.getBudgetLines())
		{
			String accountId = line.getAccountId();
			Account account = chartOfAccounts.getAccount(accountId);
			String accountName =
				(account != null && account.getName() != null) ?
					account.getName() : line.getAccountName(); // Prefer COA
																// name
			
			if (account == null || account.getAccountType() == null) // Use
																		// direct
																		// enum
			{
				LOGGER.warning(
					"BvA: Skipping budget line for account ID " + accountId +
						" as account or type is not found/valid in COA.");
				continue;
			}
			
			// Filter budget lines based on fund selection
			boolean lineIsRelevantForFundFilter = !applyFundFilter; // If no
																	// filter,
																	// all
																	// lines are
																	// relevant
			
			if (applyFundFilter)
			{
				
				if (line.getFundId() != null &&
					!line.getFundId().trim().isEmpty())
				{
					
					// Line has a specific fund, check if it's in selected funds
					if (selectedFundNames.contains(line.getFundId()))
					{
						lineIsRelevantForFundFilter = true;
					}
					
				}
				else
				{
					
					// Line has no specific fund, check if its associated
					// account matches
					// the general fund filter
					if (doesAccountMatchFunds(account, selectedFundNames,
						chartOfAccounts))
					{
						lineIsRelevantForFundFilter = true;
					}
					
				}
				
			}
			
			if (!lineIsRelevantForFundFilter)
			{
				continue; // Skip this budget line as it doesn't match fund
							// criteria
			}
			
			AccountType accountType = account.getAccountType(); // Use direct
																// enum
			BigDecimal budgetedAmountForPeriod = BigDecimal.ZERO;
			boolean useProRatedAnnual = true; // Default to pro-rating annual
												// total
			
			// Attempt to use periodic amounts if available and periodicity
			// matches common
			// scenarios
			if (line.getPeriodicAmounts() != null &&
				!line.getPeriodicAmounts().isEmpty())
			{
				
				if (line.getPeriodicity() == Periodicity.MONTHLY &&
					line.getPeriodicAmounts().size() == 12)
				{
					useProRatedAnnual = false; // We have monthly data, use it
					budgetedAmountForPeriod = BigDecimal.ZERO; // Reset for
																// summing
																// monthly
					
					for (int i = 0; i < 12; i++)
					{
						// Construct date for the i-th month of the budget's
						// fiscal year
						// This assumes budget.fiscalYear is correctly aligned
						// with the
						// budget lines' year context.
						LocalDate monthInFiscalYear =
							LocalDate.of(budget.getFiscalYear(), i + 1, 1);
						
						// Check if this month falls within the report's date
						// range
						if (!monthInFiscalYear
							.isBefore(reportStartDate.withDayOfMonth(1)) &&
							!monthInFiscalYear
								.isAfter(reportEndDate.withDayOfMonth(1)))
						{
							budgetedAmountForPeriod =
								budgetedAmountForPeriod
									.add(line.getPeriodicAmounts().get(i));
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
							LocalDate.of(budget.getFiscalYear(), (i * 3) + 1,
								1);
						LocalDate quarterEndDate =
							quarterStartDate.plusMonths(3).minusDays(1);
						
						// Check if the quarter overlaps with the report period
						if (!(quarterEndDate.isBefore(reportStartDate) ||
							quarterStartDate.isAfter(reportEndDate)))
						{
							budgetedAmountForPeriod =
								budgetedAmountForPeriod
									.add(line.getPeriodicAmounts().get(i));
						}
						
					}
					
				} // Add other periodicities like ANNUAL if periodicAmounts
					// might contain a single annual value
				else if (line.getPeriodicity() == Periodicity.ANNUAL &&
					line.getPeriodicAmounts().size() == 1)
				{
					// If ANNUAL and one periodic amount, assume it's the annual
					// total, then pro-rate it.
					// This is similar to using getTotalBudgetedAmount(), so
					// pro-rating logic below will handle it.
					// No need to set useProRatedAnnual = false here if we
					// intend to pro-rate this annual figure.
					// If it's meant to be used as-is ONLY if report period is
					// full year, logic would be different.
				}
				
			}
			
			// If periodic amounts weren't suitable or available, pro-rate the
			// total annual budgeted amount
			if (useProRatedAnnual && line.getTotalBudgetedAmount() != null)
			{
				
				if (daysInFiscalYear > 0)
				{ // Avoid division by zero
					budgetedAmountForPeriod =
						line.getTotalBudgetedAmount()
							.multiply(new BigDecimal(daysInReportPeriod))
							.divide(new BigDecimal(daysInFiscalYear), 2,
								RoundingMode.HALF_UP);
				}
				else
				{
					budgetedAmountForPeriod = BigDecimal.ZERO; // Or handle
																// error
				}
				
			}
			
			if (accountType == AccountType.INCOME)
				totalBudgetedIncome =
					totalBudgetedIncome.add(budgetedAmountForPeriod);
			else if (accountType == AccountType.EXPENSE)
				totalBudgetedExpenses =
					totalBudgetedExpenses.add(budgetedAmountForPeriod);
			
			BigDecimal actualAmountForPeriod =
				actualAmounts.getOrDefault(accountId, BigDecimal.ZERO);
			BigDecimal variance =
				actualAmountForPeriod.subtract(budgetedAmountForPeriod); // Actual
																			// -
																			// Budgeted
			BigDecimal variancePercent = BigDecimal.ZERO;
			
			if (budgetedAmountForPeriod.compareTo(BigDecimal.ZERO) != 0)
			// Avoid division by zero
			{
				variancePercent =
					variance
						.divide(budgetedAmountForPeriod.abs(), 4,
							RoundingMode.HALF_UP)
						.multiply(new BigDecimal("100"));
			}
			else if (actualAmountForPeriod.compareTo(BigDecimal.ZERO) != 0)
			{
				variancePercent = new BigDecimal("100.00"); // Or -100.00 if
															// actual is
															// negative, or
															// handle as "N/A"
			}
			
			Map<String, Object> item = new HashMap<>();
			item.put("accountCategory", accountType.toString());
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
		jxlsContext.put("totalIncomeVariance",
			totalActualIncome.subtract(totalBudgetedIncome));
		jxlsContext.put("totalBudgetedExpenses", totalBudgetedExpenses);
		jxlsContext.put("totalActualExpenses", totalActualExpenses);
		jxlsContext.put("totalExpenseVariance",
			totalActualExpenses.subtract(totalBudgetedExpenses));
		BigDecimal totalBudgetedNet =
			totalBudgetedIncome.subtract(totalBudgetedExpenses);
		BigDecimal totalActualNet =
			totalActualIncome.subtract(totalActualExpenses);
		jxlsContext.put("totalBudgetedNet", totalBudgetedNet);
		jxlsContext.put("totalActualNet", totalActualNet);
		jxlsContext.put("totalNetVariance",
			totalActualNet.subtract(totalBudgetedNet));
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
	static Map<String, Object> prepareAccountActivityContext(
		ReportContext context,
		Ledger ledger,
		ChartOfAccounts chartOfAccounts)
	{
		
		if (context.getAccountIdsForDetailReport() == null ||
			context.getAccountIdsForDetailReport().isEmpty() ||
			context.getStartDate() == null ||
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
		
		List<String> selectedAccountIds =
			context.getAccountIdsForDetailReport();
		List<String> selectedFundNames = context.getFundIds();
		boolean applyFundFilter =
			(selectedFundNames != null && !selectedFundNames.isEmpty());
		
		for (String accountId : selectedAccountIds)
		{
			Account account = chartOfAccounts.getAccount(accountId);
			
			if (account == null)
			{
				LOGGER.warning(
					"Account Activity Detail: Account not found for ID: " +
						accountId);
				continue;
			}
			
			if (applyFundFilter &&
				!doesAccountMatchFunds(account, selectedFundNames,
					chartOfAccounts))
			{
				LOGGER.info(
					"Account Activity Detail: Skipping account " + accountId +
						" as it does not match selected fund criteria.");
				continue;
			}
			
			BigDecimal openingBalance =
				getAccountBalanceAsOfDate(account,
					context.getStartDate().minusDays(1), ledger,
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
						context.getStartDate().atStartOfDay(ZoneOffset.UTC)
							.toInstant()
							.toEpochMilli() &&
						tx.getBookingDateTimestamp() <
							context.getEndDate().plusDays(1) // Exclusive end
								.atStartOfDay(ZoneOffset.UTC).toInstant()
								.toEpochMilli())
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
			
			// Sort transactions by date, then perhaps by an internal order if
			// available
			accountTransactions
				.sort(Comparator.comparingLong(
					AccountingTransaction::getBookingDateTimestamp));
			
			for (AccountingTransaction transaction : accountTransactions)
			{
				LocalDate transactionDate =
					Instant.ofEpochMilli(transaction.getBookingDateTimestamp())
						.atZone(ZoneId.systemDefault()).toLocalDate();
				
				// Each transaction might have multiple entries; we are
				// interested in the one for `accountId`
				for (AccountingEntry entry : transaction.getEntries())
				{
					
					if (entry.getAccountNumber().equals(accountId))
					{
						Map<String, Object> entryData = new HashMap<>();
						entryData.put("date",
							transactionDate.format(DATE_FORMATTER));
						entryData.put("transactionId",
							transaction.getBookingDateTimestamp());
						
						// Attempt to find the "other side" of the transaction
						// for a more meaningful description
						String description =
							transaction.getMemo() != null ?
								transaction.getMemo() : "";
						
						if (transaction.getEntries().size() > 1)
						{
							
							for (AccountingEntry otherEntry : transaction
								.getEntries())
							{
								
								if (!otherEntry.getAccountNumber()
									.equals(accountId))
								{
									Account otherAccount =
										chartOfAccounts
											.getAccount(
												otherEntry.getAccountNumber());
									
									if (otherAccount != null &&
										otherAccount.getName() != null)
									{
										description =
											description.isEmpty() ?
												otherAccount.getName() :
												description + " / " +
													otherAccount.getName();
										// Take the first "other" account name
										// for simplicity
										break;
									}
									
								}
								
							}
							
						}
						
						if (description.isEmpty())
						{
							description = "Journal Entry";
						}
						
						entryData.put("description", description);
						
						BigDecimal debitAmount =
							(entry.getAccountSide() == AccountSide.DEBIT) ?
								entry.getAmount() : BigDecimal.ZERO;
						BigDecimal creditAmount =
							(entry.getAccountSide() == AccountSide.CREDIT) ?
								entry.getAmount() : BigDecimal.ZERO;
						entryData.put("debit", debitAmount);
						entryData.put("credit", creditAmount);
						
						AccountSide increaseSide = account.getIncreaseSide();
						if (increaseSide == null)
							continue;
						
						if (increaseSide == AccountSide.DEBIT)
						{
							runningBalance = runningBalance.add(debitAmount)
								.subtract(creditAmount);
						}
						else // CREDIT
						{
							runningBalance = runningBalance
								.subtract(debitAmount).add(creditAmount);
						}
						
						entryData.put("runningBalance", runningBalance);
						entryItems.add(entryData);
						break;
					}
					
				}
				
			}
			
			Map<String, Object> singleAccountReportData = new HashMap<>();
			singleAccountReportData.put("accountName", account.getName());
			singleAccountReportData.put("accountNumber",
				account.getAccountNumber());
			singleAccountReportData.put("openingBalance", openingBalance);
			singleAccountReportData.put("entries", entryItems);
			singleAccountReportData.put("closingBalance", runningBalance);
			accountsReportDataList.add(singleAccountReportData);
		}
		
		jxlsContext.put("accountsDetail", accountsReportDataList);
		return jxlsContext;
		
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
		
		File dir = new File(System.getProperty("user.home"),
			"NonprofitBookkeepingReports");
		
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
					
					String created = Instant
						.ofEpochMilli(f.lastModified()).toString();
					results.add(new ReportMetadata(f.getName(), created,
						f.getAbsolutePath()));
				}
				
				results
					.sort((a, b) -> b.getCreated().compareTo(a.getCreated()));
			}
			
		}
		
		return results;
		
	}
		
	
	/** Accessor for the supplemental record service. */
	public SupplementalRecordService getSupplementalRecordService()
	{
		return this.supplementalRecordService;
		
	}	
	
	/**
     * Merge supplemental record data into the provided transactions.
     */
    public void mergeSupplementalData(java.util.Collection<AccountingTransaction> txs)
    {
        if (txs == null)
            return;
        for (AccountingTransaction tx : txs)
        {
            if (tx.getEntries() == null)
                continue;
            for (AccountingEntry e : tx.getEntries())
            {
                String id = e.getSupplementalRecordId();
                if (id != null && !id.isBlank())
                {
                    Map<String,String> data = this.supplementalRecordService.getSupplementalData(id);
                    if (data != null && !data.isEmpty())
                    {
                        Map<String,String> info = tx.getInfo() == null ? new HashMap<>() : new HashMap<>(tx.getInfo());
                        info.putAll(data);
                        tx.setInfo(info);
                    }
                }
            }
        }
    }
}
