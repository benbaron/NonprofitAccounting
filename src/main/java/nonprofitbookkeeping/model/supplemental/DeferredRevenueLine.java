package nonprofitbookkeeping.model.supplemental;

public final class DeferredRevenueLine extends TxnSupplementalLineBase
{
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.DEFERRED_REVENUE;
	}
}
