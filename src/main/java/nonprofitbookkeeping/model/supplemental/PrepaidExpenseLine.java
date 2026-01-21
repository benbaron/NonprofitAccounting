package nonprofitbookkeeping.model.supplemental;

public final class PrepaidExpenseLine extends TxnSupplementalLineBase
{
	@Override
	public SupplementalLineKind getKind()
	{
		return SupplementalLineKind.PREPAID_EXPENSE;
	}
}
