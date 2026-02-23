package nonprofitbookkeeping.model.supplemental;

// TODO: Auto-generated Javadoc
/**
 * The Class OtherAssetsLine.
 */
public final class OtherAssetsLine extends TxnSupplementalLineBase
{
	
	/**
	 * Override @see nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase#getKind() 
	 */
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.OTHER_ASSET;
	}
}
