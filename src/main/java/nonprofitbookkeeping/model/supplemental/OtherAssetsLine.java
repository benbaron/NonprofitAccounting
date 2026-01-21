package nonprofitbookkeeping.model.supplemental;

public final class OtherAssetsLine extends TxnSupplementalLineBase
{
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.OTHER_ASSET;
	}
}
