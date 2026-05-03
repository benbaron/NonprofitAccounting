package nonprofitbookkeeping.model.supplemental;

/**
 * The Class PayablesLine.
 */
public final class PayablesLine extends TxnSupplementalLineBase
{
	
	/**
	 * Override @see nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase#getKind() 
	 */
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.PAYABLE;
	}
}
