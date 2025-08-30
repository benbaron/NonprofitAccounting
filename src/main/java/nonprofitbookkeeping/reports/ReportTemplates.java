
package nonprofitbookkeeping.reports;

import java.util.LinkedHashMap;
import java.util.Map;

import nonprofitbookkeeping.reports.generator.*;

/**
 * Registry of available Jasper report templates.
 * Each entry maps a user facing display name to the JRXML template
 * and the generator class responsible for binding data.
 */
public final class ReportTemplates
{
	
	/** Immutable holder for template metadata. */
	public record TemplateInfo(String displayName,
		String jrxmlPath,
		Class<? extends AbstractReportGenerator> binderClass)
	{
		/**
		 * Derives the report type key used by {@link nonprofitbookkeeping.service.ReportService}
		 * from the binder class name. For example, a binder class named
		 * {@code TrialBalanceJasperGenerator} becomes {@code "trial_balance_jasper"}.
		 *
		 * @return report type identifier
		 */
		public String reportTypeKey()
		{
			String base = binderClass.getSimpleName()
				.replaceFirst("JasperGenerator$", "");
			String snake =
				base.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
			return snake + "_jasper";
			
		}
		
	}
	
	private static final Map<String, TemplateInfo> TEMPLATES =
		createTemplates();
	
	private ReportTemplates()
	{
	
	}
	
	private static Map<String, TemplateInfo> createTemplates()
	{
		Map<String, TemplateInfo> map = new LinkedHashMap<>();
		
		add(map, "jrxml/IncomeStatementAlt.jrxml",
			IncomeStatementJasperGenerator.class);
		add(map, "jrxml/TrialBalance.jrxml", TrialBalanceJasperGenerator.class);
		add(map, "jrxml/AccountLedger.jrxml",
			AccountLedgerJasperGenerator.class);
		add(map, "jrxml/BankReconciliation.jrxml",
			BankReconciliationJasperGenerator.class);
		add(map, "jrxml/BalanceSheet.jrxml", BalanceSheetJasperGenerator.class);
		add(map, "jrxml/ChartOfAccountsAlt.jrxml",
			ChartOfAccountsJasperGenerator.class);
		add(map, "jrxml/GeneralLedger.jrxml",
			GeneralLedgerJasperGenerator.class);
		
		add(map, "jrxml/sca-reports/ASSET_DTL_5a_ROWS.jrxml",
			AssetDtl5aJasperGenerator.class);
		add(map, "jrxml/sca-reports/BALANCE_3_FIXED_SEMANTIC_STRINGS_v2.jrxml",
			Balance3v2JasperGenerator.class);
		add(map, "jrxml/sca-reports/DEPR_DTL_8_ROWS_2SECTIONS.jrxml",
			DeprDtl8JasperGenerator.class);
		add(map, "jrxml/sca-reports/EXPENSE_DTL_12a_ROW_BASED.jrxml",
			ExpenseDtl12aJasperGenerator.class);
		add(map, "jrxml/sca-reports/EXPENSE_DTL_12b_ROW_BASED.jrxml",
			ExpenseDtl12bJasperGenerator.class);
		add(map, "jrxml/sca-reports/FundLedger.jrxml",
			FundLedgerJasperGenerator.class);
		add(map,
			"jrxml/sca-reports/FUNDS_14_AUTO_STYLED_labeled_rowbased.jrxml",
			Funds14JasperGenerator.class);
		add(map, "jrxml/sca-reports/INCOME_4_AUTO_STYLED_labeled.jrxml",
			Income4JasperGenerator.class);
		add(map,
			"jrxml/sca-reports/INCOME_DTL_11a_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml",
			IncomeDtl11aJasperGenerator.class);
		add(map,
			"jrxml/sca-reports/INCOME_DTL_11b_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml",
			IncomeDtl11bJasperGenerator.class);
		add(map,
			"jrxml/sca-reports/INCOME_DTL_11c_AUTO_STYLED_fixed_-_Copy_rowbased.jrxml",
			IncomeDtl11cJasperGenerator.class);
		add(map, "jrxml/sca-reports/INVENTORY_DTL_6_ROWS.jrxml",
			InventoryDtl6JasperGenerator.class);
		add(map, "jrxml/sca-reports/Ledger_Q1.jrxml",
			LedgerQ1JasperGenerator.class);
		add(map, "jrxml/sca-reports/LIABILITY_DETAIL_5b_ROW.jrxml",
			LiabilityDtl5bJasperGenerator.class);
		add(map,
			"jrxml/sca-reports/NEWSLETTER_15_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml",
			Newsletter15JasperGenerator.class);
		add(map, "jrxml/sca-reports/PRIMARY_ACCOUNT_2a_fixed_labeled.jrxml",
			PrimaryAccountJasperGenerator.class);
		add(map, "jrxml/sca-reports/REGALIA_SALES_DTL_7_ROWS_3SECTION.jrxml",
			RegaliaSalesDtl7JasperGenerator.class);
		add(map, "jrxml/sca-reports/SECONDARY_ACCOUNT_2B_fixed_labeled.jrxml",
			SecondaryAccountJasperGenerator.class);
		add(map,
			"jrxml/sca-reports/TRANSFER_IN_9_AUTO_STYLED_fixed_labeled_rowbased.jrxml",
			TransferIn9JasperGenerator.class);
		add(map,
			"jrxml/sca-reports/TRANSFER_OUT_10_AUTO_STYLED_fixed_labeled_-_Copy_rowbased.jrxml",
			TransferOut10JasperGenerator.class);
		add(map, "jrxml/sca-reports/CONTACT_INFO_1_fixed_labeled.jrxml",
			ContactInfoJasperGenerator.class);
		add(map, "jrxml/sca-reports/FINANCE_COMM_13_AUTO_STYLED_labeled.jrxml",
			FinanceComm13JasperGenerator.class);
		
		return Map.copyOf(map);
		
	}
	
	private static void add(Map<String, TemplateInfo> map, String jrxmlPath,
		Class<? extends AbstractReportGenerator> binder)
	{
		String base =
			binder.getSimpleName().replaceFirst("JasperGenerator$", "");
		String display = toDisplayName(base);
		map.put(display, new TemplateInfo(display, jrxmlPath, binder));
		
	}
	
	private static String toDisplayName(String base)
	{
		String withSpaces = base.replaceAll("([a-z])([A-Z])", "$1 $2");
		String[] parts = withSpaces.split("\\s+");
		StringBuilder sb = new StringBuilder();
		
		for (String part : parts)
		{
			
			if (part.isEmpty())
			{
				continue;
			}
			
			sb.append(Character.toUpperCase(part.charAt(0)))
				.append(part.substring(1).toLowerCase())
				.append(' ');
		}
		
		return sb.toString().trim();
		
	}
	
	/**
	 * Provides the map of available templates keyed by their display names.
	 *
	 * @return immutable mapping of display names to template info
	 */
	public static Map<String, TemplateInfo> templates()
	{
		return TEMPLATES;
		
	}
	
}

