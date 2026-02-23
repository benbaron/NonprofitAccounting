package nonprofitbookkeeping.model.supplemental;

// TODO: Auto-generated Javadoc
/**
 * The Class OtherLiabilitiesLine.
 */
public final class OtherLiabilitiesLine extends TxnSupplementalLineBase
{
	
	/**
	 * Override @see nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase#getKind() 
	 */
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.OTHER_LIABILITY;
	}
}
