package nonprofitbookkeeping.model.supplemental;


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
