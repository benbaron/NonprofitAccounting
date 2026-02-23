
package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.*;
import nonprofitbookkeeping.model.budget.Budget;
import nonprofitbookkeeping.reports.jasper.runtime.ReportBundles;
import nonprofitbookkeeping.reports.jasper.runtime.ReportContext;
import nonprofitbookkeeping.reports.jasper.runtime.ReportMetadata;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
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
		LoggerFactory.getLogger(ReportService.class);
	
	
	/** 
	 * Mapping of Jasper report types to their generator constructors. 
	 */
	private final Map<ReportType, String> generatorRegistry;
	
	/** Default constructor uses the built-in registry. */
	public ReportService()
	{
		this(createDefaultRegistry());
		
	}
	
	/**
	 * DI-friendly constructor that accepts a registry. 
	 * The registry remains mutable for runtime changes.
	 *
	 * @param registry the registry
	 */
	public ReportService(Map<ReportType, String> registry)
	{
		this.generatorRegistry = new ConcurrentHashMap<>(registry);
		
	}
	
	/** Enum-safe keys for the Jasper report generator registry. */
	public enum ReportType
	{
		
		/** The income statement jasper. */
		INCOME_STATEMENT_JASPER("income_statement_jasper"),
		
		/** The cash flow statement jasper. */
		CASH_FLOW_STATEMENT_JASPER("cash_flow_statement_jasper"),
		
		/** The trial balance jasper. */
		TRIAL_BALANCE_JASPER("trial_balance_jasper"),
		
		/** The balance sheet jasper. */
		BALANCE_SHEET_JASPER("balance_sheet_jasper"),
		
		/** The account ledger jasper. */
		ACCOUNT_LEDGER_JASPER("account_ledger_jasper"),
		
		/** The account summary jasper. */
		ACCOUNT_SUMMARY_JASPER("account_summary_jasper"),
		
		/** The bank reconciliation jasper. */
		BANK_RECONCILIATION_JASPER("bank_reconciliation_jasper"),
		
		/** The chart of accounts jasper. */
		CHART_OF_ACCOUNTS_JASPER("chart_of_accounts_jasper"),
		
		/** The fund ledger jasper. */
		FUND_LEDGER_JASPER("fund_ledger_jasper"),
		
		/** The general journal jasper. */
		GENERAL_JOURNAL_JASPER("general_journal_jasper"),
		
		/** The general ledger jasper. */
		GENERAL_LEDGER_JASPER("general_ledger_jasper"),
		
		/** The income statement alt jasper. */
		INCOME_STATEMENT_ALT_JASPER("income_statement_alt_jasper"),
		
		/** The transaction report jasper. */
		TRANSACTION_REPORT_JASPER("transaction_report_jasper"),
		
		/** The asset dtl 5a jasper. */
		ASSET_DTL_5A_JASPER("asset_dtl_5a_jasper"),
		
		/** The asset dtl 5c jasper. */
		ASSET_DTL_5C_JASPER("asset_dtl_5c_jasper"),
		
		/** The balance 3 jasper. */
		BALANCE_3_JASPER("balance_3_jasper"),
		
		/** The comments jasper. */
		COMMENTS_JASPER("comments_jasper"),
		
		/** The contact info 1 jasper. */
		CONTACT_INFO_1_JASPER("contact_info_1_jasper"),
		
		/** The contents jasper. */
		CONTENTS_JASPER("contents_jasper"),
		
		/** The depr dtl 8 jasper. */
		DEPR_DTL_8_JASPER("depr_dtl_8_jasper"),
		
		/** The depr dtl 8b jasper. */
		DEPR_DTL_8B_JASPER("depr_dtl_8b_jasper"),
		
		/** The depr dtl 8c jasper. */
		DEPR_DTL_8C_JASPER("depr_dtl_8c_jasper"),
		
		/** The expense dtl 12a jasper. */
		EXPENSE_DTL_12A_JASPER("expense_dtl_12a_jasper"),
		
		/** The expense dtl 12b jasper. */
		EXPENSE_DTL_12B_JASPER("expense_dtl_12b_jasper"),
		
		/** The finance comm 13 jasper. */
		FINANCE_COMM_13_JASPER("finance_comm_13_jasper"),
		
		/** The free form jasper. */
		FREE_FORM_JASPER("free_form_jasper"),
		
		/** The funds 14 jasper. */
		FUNDS_14_JASPER("funds_14_jasper"),
		
		/** The income 4 jasper. */
		INCOME_4_JASPER("income_4_jasper"),
		
		/** The income dtl 11a jasper. */
		INCOME_DTL_11A_JASPER("income_dtl_11a_jasper"),
		
		/** The income dtl 11b jasper. */
		INCOME_DTL_11B_JASPER("income_dtl_11b_jasper"),
		
		/** The income dtl 11c jasper. */
		INCOME_DTL_11C_JASPER("income_dtl_11c_jasper"),
		
		/** The inventory dtl 6 jasper. */
		INVENTORY_DTL_6_JASPER("inventory_dtl_6_jasper"),
		
		/** The inventory dtl 6b jasper. */
		INVENTORY_DTL_6B_JASPER("inventory_dtl_6b_jasper"),
		
		/** The liability dtl 5b jasper. */
		LIABILITY_DTL_5B_JASPER("liability_dtl_5b_jasper"),
		
		/** The liability dtl 5d jasper. */
		LIABILITY_DTL_5D_JASPER("liability_dtl_5d_jasper"),
		
		/** The newsletter 15 jasper. */
		NEWSLETTER_15_JASPER("newsletter_15_jasper"),
		
		/** The primary account 2a jasper. */
		PRIMARY_ACCOUNT_2A_JASPER("primary_account_2a_jasper"),
		
		/** The regalia sales dtl 7 jasper. */
		REGALIA_SALES_DTL_7_JASPER("regalia_sales_dtl_7_jasper"),
		
		/** The regalia sales dtl 7b jasper. */
		REGALIA_SALES_DTL_7B_JASPER("regalia_sales_dtl_7b_jasper"),
		
		/** The secondary accounts 2b jasper. */
		SECONDARY_ACCOUNTS_2B_JASPER("secondary_accounts_2b_jasper"),
		
		/** The secondary accounts 2c jasper. */
		SECONDARY_ACCOUNTS_2C_JASPER("secondary_accounts_2c_jasper"),
		
		/** The secondary accounts 2d jasper. */
		SECONDARY_ACCOUNTS_2D_JASPER("secondary_accounts_2d_jasper"),
		
		/** The transfer in 9 jasper. */
		TRANSFER_IN_9_JASPER("transfer_in_9_jasper"),
		
		/** The transfer in 9b jasper. */
		TRANSFER_IN_9B_JASPER("transfer_in_9b_jasper"),
		
		/** The transfer in 9c jasper. */
		TRANSFER_IN_9C_JASPER("transfer_in_9c_jasper"),
		
		/** The transfer in 9d jasper. */
		TRANSFER_IN_9D_JASPER("transfer_in_9d_jasper"),
		
		/** The transfer out 10 jasper. */
		TRANSFER_OUT_10_JASPER("transfer_out_10_jasper"),
		
		/** The transfer out 10b jasper. */
		TRANSFER_OUT_10B_JASPER("transfer_out_10b_jasper"),
		
		/** The transfer out 10c jasper. */
		TRANSFER_OUT_10C_JASPER("transfer_out_10c_jasper"),
		
		/** The transfer out 10d jasper. */
		TRANSFER_OUT_10D_JASPER("transfer_out_10d_jasper"),
<<<<<<< HEAD
		
		/** The sca asset dtl 5a jasper. */
=======
		UNDEPOSITED_FUNDS_JASPER("undeposited_funds_jasper"),
>>>>>>> branch 'main' of git@github.com:benbaron/NonprofitAccounting.git
		SCA_ASSET_DTL_5A_JASPER("sca_asset_dtl_5a_jasper"),
		
		/** The sca balance 3 jasper. */
		SCA_BALANCE_3_JASPER("sca_balance_3_jasper"),
		
		/** The sca balance 3 v2 jasper. */
		SCA_BALANCE_3_V2_JASPER("sca_balance_3_v2_jasper"),
		
		/** The sca contact info jasper. */
		SCA_CONTACT_INFO_JASPER("sca_contact_info_jasper"),
		
		/** The sca depr dtl 8 jasper. */
		SCA_DEPR_DTL_8_JASPER("sca_depr_dtl_8_jasper"),
		
		/** The sca expense dtl 12a jasper. */
		SCA_EXPENSE_DTL_12A_JASPER("sca_expense_dtl_12a_jasper"),
		
		/** The sca expense dtl 12b jasper. */
		SCA_EXPENSE_DTL_12B_JASPER("sca_expense_dtl_12b_jasper"),
		
		/** The sca finance comm 13 jasper. */
		SCA_FINANCE_COMM_13_JASPER("sca_finance_comm_13_jasper"),
		
		/** The sca funds 14 jasper. */
		SCA_FUNDS_14_JASPER("sca_funds_14_jasper"),
		
		/** The sca income 4 jasper. */
		SCA_INCOME_4_JASPER("sca_income_4_jasper"),
		
		/** The sca income dtl 11a jasper. */
		SCA_INCOME_DTL_11A_JASPER("sca_income_dtl_11a_jasper"),
		
		/** The sca income dtl 11b jasper. */
		SCA_INCOME_DTL_11B_JASPER("sca_income_dtl_11b_jasper"),
		
		/** The sca income dtl 11c jasper. */
		SCA_INCOME_DTL_11C_JASPER("sca_income_dtl_11c_jasper"),
		
		/** The sca inventory dtl 6 jasper. */
		SCA_INVENTORY_DTL_6_JASPER("sca_inventory_dtl_6_jasper"),
		
		/** The sca ledger q1 jasper. */
		SCA_LEDGER_Q1_JASPER("sca_ledger_q1_jasper"),
		
		/** The sca liability dtl 5b jasper. */
		SCA_LIABILITY_DTL_5B_JASPER("sca_liability_dtl_5b_jasper"),
		
		/** The sca newsletter 15 jasper. */
		SCA_NEWSLETTER_15_JASPER("sca_newsletter_15_jasper"),
		
		/** The sca primary account jasper. */
		SCA_PRIMARY_ACCOUNT_JASPER("sca_primary_account_jasper"),
		
		/** The sca primary account reconciliation jasper. */
		SCA_PRIMARY_ACCOUNT_RECONCILIATION_JASPER(
			"sca_primary_account_reconciliation_jasper"),
		
		/** The sca regalia sales dtl 7 jasper. */
		SCA_REGALIA_SALES_DTL_7_JASPER("sca_regalia_sales_dtl_7_jasper"),
		
		/** The sca secondary account jasper. */
		SCA_SECONDARY_ACCOUNT_JASPER("sca_secondary_account_jasper"),
		
		/** The sca transfer in 9 jasper. */
		SCA_TRANSFER_IN_9_JASPER("sca_transfer_in_9_jasper"),
		
		/** The sca transfer out 10 jasper. */
		SCA_TRANSFER_OUT_10_JASPER("sca_transfer_out_10_jasper");
		
		/** The id. */
		private final String id;
		
		/**
		 * Instantiates a new report type.
		 *
		 * @param id the id
		 */
		ReportType(String id)
		{
			this.id = id;
			
		}
		
		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id()
		{
			return this.id;
			
		}
		
		/**
		 * fromId.
		 *
		 * @param id the id
		 * @return ReportType enum
		 */
		public static ReportType fromId(String id)
		{
			
			if (id == null)
			{
				return null;
			}
			
			// trim and downcase
			String norm = id.trim().toLowerCase();
			
			// Search all the enumerated values for the normalized string
			for (ReportType t : values())
			{
				
				if (t.id.equals(norm))
				{
					return t;
				}
				
			}
			
			return null;
			
		}
		
	}
	
	/**
	 * Factory for the built-in generator registry.
	 *
	 * @return the map
	 */
	private static
		Map<ReportType, String> createDefaultRegistry()
	{
		Map<ReportType, String> map = new EnumMap<>(ReportType.class);
		
		map.put(ReportType.INCOME_STATEMENT_JASPER,
			generatorClass("IncomeStatementJasperGenerator"));
		map.put(ReportType.CASH_FLOW_STATEMENT_JASPER,
			generatorClass("CashFlowStatementJasperGenerator"));
		map.put(ReportType.TRIAL_BALANCE_JASPER,
			generatorClass("TrialBalanceJasperGenerator"));
		map.put(ReportType.BALANCE_SHEET_JASPER,
			generatorClass("BalanceResultReportGenerator"));
		map.put(ReportType.ACCOUNT_LEDGER_JASPER,
			generatorClass("AccountLedgerJasperGenerator"));
		map.put(ReportType.ACCOUNT_SUMMARY_JASPER,
			generatorClass("AccountSummaryJasperGenerator"));
		map.put(ReportType.BANK_RECONCILIATION_JASPER,
			generatorClass("BankReconciliationJasperGenerator"));
		map.put(ReportType.CHART_OF_ACCOUNTS_JASPER,
			generatorClass("ChartOfAccountsJasperGenerator"));
		map.put(ReportType.FUND_LEDGER_JASPER,
			generatorClass("FundLedgerJasperGenerator"));
		map.put(ReportType.GENERAL_JOURNAL_JASPER,
			generatorClass("GeneralJournalJasperGenerator"));
		map.put(ReportType.GENERAL_LEDGER_JASPER,
			generatorClass("GeneralLedgerJasperGenerator"));
		map.put(ReportType.INCOME_STATEMENT_ALT_JASPER,
			generatorClass("IncomeStatementAltJasperGenerator"));
		map.put(ReportType.TRANSACTION_REPORT_JASPER,
			generatorClass("TransactionReportJasperGenerator"));
		map.put(ReportType.SCA_ASSET_DTL_5A_JASPER,
			generatorClass("AssetDtl5aJasperGenerator"));
		map.put(ReportType.SCA_BALANCE_3_V2_JASPER,
			generatorClass("Balance3v2JasperGenerator"));
		map.put(ReportType.SCA_CONTACT_INFO_JASPER,
			generatorClass("ContactInfoJasperGenerator"));
		map.put(ReportType.SCA_DEPR_DTL_8_JASPER,
			generatorClass("DeprDtl8JasperGenerator"));
		map.put(ReportType.SCA_EXPENSE_DTL_12A_JASPER,
			generatorClass("ExpenseDtl12aJasperGenerator"));
		map.put(ReportType.SCA_EXPENSE_DTL_12B_JASPER,
			generatorClass("ExpenseDtl12bJasperGenerator"));
		map.put(ReportType.SCA_FINANCE_COMM_13_JASPER,
			generatorClass("FinanceComm13JasperGenerator"));
		map.put(ReportType.SCA_FUNDS_14_JASPER,
			generatorClass("Funds14JasperGenerator"));
		map.put(ReportType.SCA_INCOME_4_JASPER,
			generatorClass("Income4JasperGenerator"));
		map.put(ReportType.SCA_INCOME_DTL_11A_JASPER,
			generatorClass("IncomeDtl11aJasperGenerator"));
		map.put(ReportType.SCA_INCOME_DTL_11B_JASPER,
			generatorClass("IncomeDtl11bJasperGenerator"));
		map.put(ReportType.SCA_INCOME_DTL_11C_JASPER,
			generatorClass("IncomeDtl11cJasperGenerator"));
		map.put(ReportType.SCA_INVENTORY_DTL_6_JASPER,
			generatorClass("InventoryDtl6JasperGenerator"));
		map.put(ReportType.SCA_LEDGER_Q1_JASPER,
			generatorClass("LedgerQ1JasperGenerator"));
		map.put(ReportType.SCA_LIABILITY_DTL_5B_JASPER,
			generatorClass("LiabilityDtl5bJasperGenerator"));
		map.put(ReportType.SCA_NEWSLETTER_15_JASPER,
			generatorClass("Newsletter15JasperGenerator"));
		map.put(ReportType.SCA_PRIMARY_ACCOUNT_JASPER,
			generatorClass("PrimaryAccountJasperGenerator"));
		map.put(ReportType.SCA_PRIMARY_ACCOUNT_RECONCILIATION_JASPER,
			generatorClass("PrimaryAccountReconciliationJasperGenerator"));
		map.put(ReportType.SCA_REGALIA_SALES_DTL_7_JASPER,
			generatorClass("RegaliaSalesDtl7JasperGenerator"));
		map.put(ReportType.SCA_SECONDARY_ACCOUNT_JASPER,
			generatorClass("SecondaryAccountJasperGenerator"));
		map.put(ReportType.SCA_TRANSFER_IN_9_JASPER,
			generatorClass("TransferIn9JasperGenerator"));
		map.put(ReportType.SCA_TRANSFER_OUT_10_JASPER,
			generatorClass("TransferOut10JasperGenerator"));
		
		registerBundledGenerators(map);
		return map;
		
	}
	
	/**
	 * Register bundled generators.
	 *
	 * @param registry the registry
	 */
	private static
		void registerBundledGenerators(Map<ReportType, String> registry)
	{
		
		for (ReportBundles.Bundle bundle : ReportBundles.bundles())
		{
			registry.putIfAbsent(bundle.reportType(),
				bundle.generatorClassName());
		}
		
	}
	
	
	/**
	 * Generator class.
	 *
	 * @param simpleName the simple name
	 * @return the string
	 */
	private static String generatorClass(String simpleName)
	{
		return "nonprofitbookkeeping.reports.jasper." + simpleName;
		
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
		
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("Starting Jasper report generation for reportType={}",
				ctx.getReportType());
		}
		
		// Resolve report type and generator
		ReportType type = ReportType.fromId(ctx.getReportType());
		LOGGER.info("Resolving report generator for reportType '{}'.",
			ctx.getReportType());
		
		if (type == null)
		{
			throw new IllegalArgumentException(
				"Unknown reportType: " + ctx.getReportType());
		}
		
		String generatorClassName = this.generatorRegistry.get(type);
		
		if (generatorClassName == null || generatorClassName.isBlank())
		{
			throw new IllegalArgumentException(
				"No generator registered for reportType: " + type.id());
		}
		
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("Selected generator {} for reportType={}",
				generatorClassName,
				type.id());
		}
		
		Object generator = ReportGeneratorLoader
			.instantiate(generatorClassName, ctx, this);
		
		if (ctx.getBeans() != null)
		{
			ReportGeneratorLoader.setReportData(generator, ctx.getBeans());
		}
		
		// Ask the generator to build the JasperPrint
		JasperPrint print = ReportGeneratorLoader.generatePrint(generator);
		
		// Normalize format; default to PDF
		String fmt =
			(outputFormat == null ? "pdf" : outputFormat).trim().toLowerCase();
		
		if (LOGGER.isInfoEnabled())
		{
			LOGGER.info("Resolved output format: {}", fmt);
		}
		
		String baseName = ReportGeneratorLoader.getBaseName(generator);
		File out = ReportGeneratorLoader
			.writeOutput(generator, fmt, print, baseName);
		LOGGER.info("Report generated: {}", out.getAbsolutePath());
		return out;
		
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
			LOGGER.warn("Account {} has no defined increase side.",
				account.getAccountNumber());
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
	
	
}
