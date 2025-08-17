package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Data bean for BALANCE_3 (purpose-based names).
 * String fields are used for headers and signer names. Numerics are BigDecimal.
 */
public class Balance3Record_v2 implements SupplementalRecord
{
	// --- Header / context (Strings) ---
	public String header_version_info; // e.g., "Form v6.0"
	public String organization_name_corporate; // org/corporate name
	public String report_title_text; // e.g., "BALANCE SHEET"
	public String period_start_label; // "Start"
	public String period_end_label; // "End"
	public String contact_info; // contact block or code
	public String exchequer_name; // signer
	public String seneschal_name; // signer
	
	// --- Assets (Start / End / Diff) ---
	public BigDecimal undeposited_cash_start;
	public BigDecimal undeposited_cash_end;
	public BigDecimal undeposited_cash_diff;
	
	public BigDecimal cash_earning_interest_start;
	public BigDecimal cash_earning_interest_end;
	public BigDecimal cash_earning_interest_diff;
	
	public BigDecimal receivables_start;
	public BigDecimal receivables_end;
	public BigDecimal receivables_diff;
	
	public BigDecimal inventory_major_start;
	public BigDecimal inventory_major_end;
	public BigDecimal inventory_major_diff;
	
	public BigDecimal regalia_non_depreciated_start;
	public BigDecimal regalia_non_depreciated_end;
	public BigDecimal regalia_non_depreciated_diff;
	
	public BigDecimal depreciated_equipment_start;
	public BigDecimal depreciated_equipment_end;
	public BigDecimal depreciated_equipment_diff;
	
	public BigDecimal accumulated_depreciation_start;
	public BigDecimal accumulated_depreciation_end;
	public BigDecimal accumulated_depreciation_diff;
	
	public BigDecimal prepaid_expenses_start;
	public BigDecimal prepaid_expenses_end;
	public BigDecimal prepaid_expenses_diff;
	
	public BigDecimal other_assets_start;
	public BigDecimal other_assets_end;
	public BigDecimal other_assets_diff;
	
	public BigDecimal total_assets_start;
	public BigDecimal total_assets_end;
	public BigDecimal total_assets_diff;
	
	// --- Liabilities ---
	public BigDecimal newsletter_subscriptions_due_start;
	public BigDecimal newsletter_subscriptions_due_end;
	public BigDecimal newsletter_subscriptions_due_diff;
	
	public BigDecimal deferred_revenue_start;
	public BigDecimal deferred_revenue_end;
	public BigDecimal deferred_revenue_diff;
	
	public BigDecimal payables_start;
	public BigDecimal payables_end;
	public BigDecimal payables_diff;
	
	public BigDecimal other_liabilities_start;
	public BigDecimal other_liabilities_end;
	public BigDecimal other_liabilities_diff;
	
	public BigDecimal total_liabilities_start;
	public BigDecimal total_liabilities_end;
	public BigDecimal total_liabilities_diff;
	
	// --- Net Worth + Proof ---
	public BigDecimal net_worth_start;
	public BigDecimal net_worth_end;
	
	public BigDecimal change_in_net_worth; // (A) III(end) - III(start)
	public BigDecimal net_income_from_income_statement; // (B) Income Statement line 32
	
	// no-arg constructor
	public Balance3Record_v2()
	{
	
	}
	
}
