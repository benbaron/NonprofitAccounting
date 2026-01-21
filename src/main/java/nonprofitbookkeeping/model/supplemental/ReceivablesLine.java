package nonprofitbookkeeping.model.supplemental;

public final class ReceivablesLine extends TxnSupplementalLineBase
{
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.RECEIVABLE;
	}
}
