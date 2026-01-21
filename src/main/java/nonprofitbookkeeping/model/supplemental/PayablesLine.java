package nonprofitbookkeeping.model.supplemental;

public final class PayablesLine extends TxnSupplementalLineBase
{
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.PAYABLE;
	}
}
