package nonprofitbookkeeping.service;

/**
 * Feature-flagged finance write enforcement guard.
 */
public final class FinanceWriteEnforcement
{
    private static final String MODE_PROPERTY = "nonprofitbookkeeping.financeWriteEnforcement";
    private static final ThreadLocal<Boolean> FACADE_SCOPE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private FinanceWriteEnforcement()
    {
    }

    public static <T> T runWithinFacadeScope(CheckedSupplier<T> op) throws Exception
    {
        boolean prior = FACADE_SCOPE.get();
        FACADE_SCOPE.set(Boolean.TRUE);
        try
        {
            return op.get();
        }
        finally
        {
            FACADE_SCOPE.set(prior);
        }
    }

    public static void runWithinFacadeScope(CheckedRunnable op) throws Exception
    {
        runWithinFacadeScope(() -> {
            op.run();
            return null;
        });
    }

    public static void requireFacadeScope(String operation)
    {
        if (isEnforcementDisabled() || FACADE_SCOPE.get())
        {
            return;
        }
        String message = "Blocked finance-impacting write outside PostingFacade scope: " + operation;
        if (isReportOnly())
        {
            System.err.println("[FINANCE_WRITE_ENFORCEMENT][REPORT] " + message);
            return;
        }
        throw new IllegalStateException(message + ". Set -D" + MODE_PROPERTY + "=report for staged rollout.");
    }

    public static boolean isReportOnly()
    {
        return "report".equalsIgnoreCase(System.getProperty(MODE_PROPERTY, "enforce"));
    }

    public static boolean isEnforcementDisabled()
    {
        return "off".equalsIgnoreCase(System.getProperty(MODE_PROPERTY, "enforce"));
    }

    @FunctionalInterface
    public interface CheckedSupplier<T>
    {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedRunnable
    {
        void run() throws Exception;
    }
}
