package nonprofitbookkeeping.ui.javafx.supplemental;

import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

public class SupplementalLineConfig
{
	public final SupplementalLineKind kind;
	public final String tabTitle;
	public final boolean showDueDate;
	public final boolean showStartEnd;

	public SupplementalLineConfig(SupplementalLineKind kind, String tabTitle,
		boolean showDueDate, boolean showStartEnd)
	{
		this.kind = kind;
		this.tabTitle = tabTitle;
		this.showDueDate = showDueDate;
		this.showStartEnd = showStartEnd;
	}

	public static SupplementalLineConfig forKind(SupplementalLineKind kind)
	{
		return switch (kind)
		{
			case RECEIVABLE -> new SupplementalLineConfig(kind, "Receivables", true, false);
			case PAYABLE -> new SupplementalLineConfig(kind, "Payables", true, false);
			case PREPAID_EXPENSE -> new SupplementalLineConfig(kind, "Prepaid Expenses", false, true);
			case DEFERRED_REVENUE -> new SupplementalLineConfig(kind, "Deferred Revenue", false, true);
			case OTHER_ASSET -> new SupplementalLineConfig(kind, "Other Assets", false, false);
			case OTHER_LIABILITY -> new SupplementalLineConfig(kind, "Other Liabilities", true, false);
		};
	}
}
