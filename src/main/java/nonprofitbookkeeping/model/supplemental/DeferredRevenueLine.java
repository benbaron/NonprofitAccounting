package nonprofitbookkeeping.model.supplemental;

// TODO: Auto-generated Javadoc
/**
 * The Class DeferredRevenueLine.
 */
public final class DeferredRevenueLine extends TxnSupplementalLineBase
{
	
	/**
	 * Override @see nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase#getKind() 
	 */
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.DEFERRED_REVENUE;
	}
}
