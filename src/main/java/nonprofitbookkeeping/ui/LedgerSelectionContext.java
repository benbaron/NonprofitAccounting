package nonprofitbookkeeping.ui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import nonprofitbookkeeping.model.AccountingTransaction;

/**
 * Shared selection state for the ledger workspace subpanels.
 */
public final class LedgerSelectionContext
{
	public enum LedgerSubpanel
	{
		REGISTER,
		EDITOR
	}

	private static final ObjectProperty<AccountingTransaction> selectedTransaction =
		new SimpleObjectProperty<>(null);
	private static final ObjectProperty<LedgerSubpanel> selectedSubpanel =
		new SimpleObjectProperty<>(LedgerSubpanel.REGISTER);

	private LedgerSelectionContext()
	{
	}

	public static ObjectProperty<AccountingTransaction> selectedTransactionProperty()
	{
		return selectedTransaction;
	}

	public static AccountingTransaction getSelectedTransaction()
	{
		return selectedTransaction.get();
	}

	public static void setSelectedTransaction(AccountingTransaction transaction)
	{
		selectedTransaction.set(transaction);
	}

	public static ObjectProperty<LedgerSubpanel> selectedSubpanelProperty()
	{
		return selectedSubpanel;
	}

	public static LedgerSubpanel getSelectedSubpanel()
	{
		return selectedSubpanel.get();
	}

	public static void setSelectedSubpanel(LedgerSubpanel subpanel)
	{
		selectedSubpanel.set(subpanel);
	}
}
