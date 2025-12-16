
package nonprofitbookkeeping.reports.jasper.sca;

import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.reports.datasource.scareports.TRANSFER_IN_9Bean;
import nonprofitbookkeeping.reports.query.JournalQueryCriteria;
import nonprofitbookkeeping.reports.query.JournalQueryFacade;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

/**
 * Demonstrates how to use {@link JournalQueryFacade} to hydrate the
 * {@link TRANSFER_IN_9Bean} used by the SCA "Transfer In" report.
 */
public class ScaTransferInReportDataGenerator
{
	private static final DecimalFormat AMOUNT_FORMAT =
		new DecimalFormat("#,##0.00");
	
	private final JournalQueryFacade queryFacade;
	
	public ScaTransferInReportDataGenerator()
	{
		this(new JournalQueryFacade());
		
	}
	
	public ScaTransferInReportDataGenerator(JournalQueryFacade queryFacade)
	{
		this.queryFacade = Objects.requireNonNull(queryFacade);
		
	}
	
	/**
	 * Executes a journal query and maps each transaction into a
	 * {@link TRANSFER_IN_9Bean}. Only the primary fields are populated; the
	 * remaining bean slots can be filled later if the report template expects
	 * multiple rows per bean.
	 *
	 * @param criteria Filters describing which transactions should be
	 *                 included in the report.
	 * @return List of populated beans ready for use in a Jasper data source.
	 */
	public List<TRANSFER_IN_9Bean> generateBeans(JournalQueryCriteria criteria)
	{
		return this.queryFacade.fetchBeans(criteria,
			this::mapTransactionToBean);
		
	}
	
	private TRANSFER_IN_9Bean mapTransactionToBean(AccountingTransaction txn)
	{
		TRANSFER_IN_9Bean bean = new TRANSFER_IN_9Bean();
		bean.setSca_funds_transferred_detail_in(txn.getMemo());
		bean.setWithin_the_kingdom(txn.getToFrom());
		bean.setCheck(txn.getCheckNumber());
		bean.setCheck_date(txn.getDate());
		
		BigDecimal amount = this.queryFacade.sumAbsoluteEntryAmounts(txn);
		bean.setAmount(formatAmount(amount));
		bean.setTransfer_in_9_r2c3(amount.doubleValue());
		return bean;
		
	}
	
	private String formatAmount(BigDecimal amount)
	{
		
		if (amount == null)
		{
			return null;
		}
		
		return AMOUNT_FORMAT.format(amount);
		
	}
	
}
