package nonprofitbookkeeping.ui.panels;

/** Shell-specific navigation callbacks used by the shared dashboard. */
public interface DashboardNavigation
{
    default void openCashAndBank()
    {
    }

    default void openLedger()
    {
    }

    default void openFunds()
    {
    }

    default void openReports()
    {
    }

    default void openReconciliation()
    {
    }

    default void openUndepositedFunds()
    {
    }

    default void openTransactionInLedger(int transactionId)
    {
    }
}
