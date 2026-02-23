package nonprofitbookkeeping.model.supplemental;

// TODO: Auto-generated Javadoc
/**
 * The Class PrepaidExpenseLine.
 */
public final class PrepaidExpenseLine extends TxnSupplementalLineBase
{
	
	/**
	 * serialVersionUID : long
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Override @see nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase#getKind() 
	 */
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.PREPAID_EXPENSE;
	}
}
