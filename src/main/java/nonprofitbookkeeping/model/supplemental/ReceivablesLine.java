package nonprofitbookkeeping.model.supplemental;

public final class ReceivablesLine extends TxnSupplementalLineBase
{
	
	/**
	 * Override @see nonprofitbookkeeping.model.supplemental.TxnSupplementalLineBase#getKind()
	 *
	 * @return the kind
	 */
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.RECEIVABLE;
	}
}
