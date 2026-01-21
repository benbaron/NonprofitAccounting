package nonprofitbookkeeping.model.supplemental;

public final class OtherLiabilitiesLine extends TxnSupplementalLineBase
{
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.OTHER_LIABILITY;
	}
}
