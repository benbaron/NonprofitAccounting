package nonprofitbookkeeping.model.supplemental;

// TODO: Auto-generated Javadoc
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
