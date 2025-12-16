
package nonprofitbookkeeping.reports.jasper;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.TransactionReportRowBean;
import nonprofitbookkeeping.reports.query.TransactionQueryFacade;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Generator responsible for producing the Jasper Transaction report. The
 * generator pulls transactions using {@link TransactionQueryFacade}, converts
 * them into {@link nonprofitbookkeeping.reports.datasource.TransactionReportRowBean}
 * instances, and delegates rendering to the shared {@link AbstractReportGenerator}
 * infrastructure.
 */
public class TransactionReportJasperGenerator extends AbstractReportGenerator
{
	private final TransactionQueryFacade queryFacade;
	private final TransactionQueryFacade.QueryConfig queryConfig;
	private final ReportContext context;
	
	/**
	 * 
	 * Constructor TransactionReportJasperGenerator
	 */
	public TransactionReportJasperGenerator()
	{
		this(new ReportContext());
		
	}
	
	/**
	 * 
	 * Constructor TransactionReportJasperGenerator
	 * @param context
	 */
	public TransactionReportJasperGenerator(ReportContext context)
	{
		this(context, new TransactionQueryFacade());
		
	}
	
	/**
	 * 
	 * Constructor TransactionReportJasperGenerator
	 * @param context
	 * @param queryFacade
	 */
	TransactionReportJasperGenerator(ReportContext context,
		TransactionQueryFacade queryFacade)
	{
		this.context = context == null ? new ReportContext() : context;
		this.queryFacade =
			queryFacade == null ? new TransactionQueryFacade() : queryFacade;
		this.queryConfig = buildQueryConfig(this.context);
		
	}
	
	
	/**
	 * 
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getReportData()
	 */
	@Override
	protected List<TransactionReportRowBean> getReportData()
	{
		return this.queryFacade.queryAndMap(
			this.queryConfig, // initialized by constructor
			this::toRowBean); //
		
	}
	
	/**
	 * To Row Bean
	 * @param record
	 * @return
	 */
	private TransactionReportRowBean toRowBean(
		TransactionQueryFacade.TransactionRecord record)
	{
		AccountingTransaction transaction = record.transaction();
		List<AccountingEntry> entries = record.entries();
		
		if (entries == null || entries.isEmpty())
		{
			return null;
		}
		
		AccountingEntry primaryEntry = entries.stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		
		if (primaryEntry == null)
		{
			return null;
		}
		
		Company company = CurrentCompany.getCompany();
		ChartOfAccounts chart =
			company == null ? null : company.getChartOfAccounts();
		Account account = chart == null ? null :
			chart.getAccount(primaryEntry.getAccountNumber());
		
		java.math.BigDecimal totalDebit = java.math.BigDecimal.ZERO;
		java.math.BigDecimal totalCredit = java.math.BigDecimal.ZERO;
		
		for (AccountingEntry entry : entries)
		{
			
			if (entry == null)
			{
				continue;
			}
			
			if (entry.getAccountSide() == AccountSide.DEBIT)
			{
				totalDebit = totalDebit.add(entry.getAmount());
			}
			else
			{
				totalCredit = totalCredit.add(entry.getAmount());
			}
			
		}
		
		String debit =
			totalDebit.compareTo(java.math.BigDecimal.ZERO) != 0 ?
				totalDebit.toPlainString() : "0";
		String credit =
			totalCredit.compareTo(java.math.BigDecimal.ZERO) != 0 ?
				totalCredit.toPlainString() : "0";
		
		String memo =
			transaction.getMemo() != null ?
				transaction.getMemo() : "";
		String accountNumber = account != null ?
			account.getAccountNumber() :
			primaryEntry.getAccountNumber();
		String accountName = account != null ?
			account.getName() :
			primaryEntry.getAccountNumber();
		
		return new TransactionReportRowBean(
			String.valueOf(transaction.getBookingDateTimestamp()),
			transaction.getDate(),
			memo,
			memo,
			"",
			transaction.getDate(),
			accountNumber,
			accountName,
			"",
			debit,
			credit);
		
	}
	
	@Override
	protected Map<String, Object> getReportParameters()
	{
		return Collections.emptyMap();
		
	}
	
	@Override
	protected String getReportPath()
	{
		return bundledReportPath();
		
	}
	
	/**
	 * Override @see nonprofitbookkeeping.reports.jasper.AbstractReportGenerator#getBaseName()
	 */
	@Override
	public String getBaseName()
	{
		return "Transaction_Report_" + LocalDate.now();
		
	}
	
	/**
	 * Builds query configuration
	 * @param context
	 * 
	 * @return query configuration
	 */
	private static TransactionQueryFacade.QueryConfig buildQueryConfig(
		ReportContext context)
	{
		TransactionQueryFacade.QueryConfig.Builder builder =
			TransactionQueryFacade.QueryConfig
				.builder()
				.withDateRange(
					context.getStartDate(),
					context.getEndDate())
				.withAccounts(
					context.getAccountIdsForDetailReport(),
					context.isRequireAllAccounts())
				.withMemoSubstring(
					context.getMemoFilter());
		
		AccountSide side = parseSide(context.getTransactionType());
		builder.withTransactionType(side);
		return builder.build();
		
	}
	
	/**
	 * Parses the Side debit/credit
	 * 
	 * @param sideText
	 * @return
	 */
	private static AccountSide parseSide(String sideText)
	{
		
		if (sideText == null || sideText.isBlank())
		{
			return null;
		}
		
		try
		{
			return AccountSide.valueOf(sideText.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
		
	}
	
}
