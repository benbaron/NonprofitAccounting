
package nonprofitbookkeeping.ui.javafx.supplemental;

import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;

// TODO: Auto-generated Javadoc
/**
 * The Class SupplementalLineConfig.
 */
public class SupplementalLineConfig
{
	
	/** The kind. */
	public final SupplementalLineKind kind;
	
	/** The tab title. */
	public final String tabTitle;
	
	/** The show due date. */
	public final boolean showDueDate;
	
	/** The show start end. */
	public final boolean showStartEnd;
	
	/**
	 * Instantiates a new supplemental line config.
	 *
	 * @param kind the kind
	 * @param tabTitle the tab title
	 * @param showDueDate the show due date
	 * @param showStartEnd the show start end
	 */
	public SupplementalLineConfig(SupplementalLineKind kind, String tabTitle,
		boolean showDueDate, boolean showStartEnd)
	{
		this.kind = kind;
		this.tabTitle = tabTitle;
		this.showDueDate = showDueDate;
		this.showStartEnd = showStartEnd;
		
	}
	
	/**
	 * For kind.
	 *
	 * @param kind the kind
	 * @return the supplemental line config
	 */
	public static SupplementalLineConfig forKind(SupplementalLineKind kind)
	{
		return switch(kind)
		{
			case RECEIVABLE ->
				new SupplementalLineConfig(kind, "Receivables", true, false);
			case PAYABLE ->
				new SupplementalLineConfig(kind, "Payables", true, false);
			case PREPAID_EXPENSE -> new SupplementalLineConfig(kind,
				"Prepaid Expenses", false, true);
			case DEFERRED_REVENUE -> new SupplementalLineConfig(kind,
				"Deferred Revenue", false, true);
			case OTHER_ASSET ->
				new SupplementalLineConfig(kind, "Other Assets", false, false);
			case OTHER_LIABILITY -> new SupplementalLineConfig(kind,
				"Other Liabilities", true, false);
		};
		
	}
	
}
