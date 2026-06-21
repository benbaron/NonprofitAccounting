package nonprofitbookkeeping.ui;

import java.util.Objects;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Carries a requested transaction identifier between a journal hyperlink and
 * the Ledger Register workspace.
 */
public final class LedgerNavigationContext
{
    private static final ObjectProperty<Integer> requestedTransactionId =
        new SimpleObjectProperty<>();

    private LedgerNavigationContext()
    {
    }

    /** Requests that the Ledger Register select the given transaction. */
    public static void requestTransaction(int transactionId)
    {
        requestedTransactionId.set(transactionId);
    }

    /** Returns the currently pending transaction selection. */
    public static Integer getRequestedTransactionId()
    {
        return requestedTransactionId.get();
    }

    /** Observable pending transaction selection. */
    public static ReadOnlyObjectProperty<Integer>
        requestedTransactionIdProperty()
    {
        return requestedTransactionId;
    }

    /** Clears the request only when it is still for the supplied ID. */
    public static void clearRequest(int transactionId)
    {
        if (Objects.equals(requestedTransactionId.get(), transactionId))
        {
            requestedTransactionId.set(null);
        }
    }
}
