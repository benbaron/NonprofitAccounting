package org.nonprofitbookkeeping.ui;

import java.nio.file.Path;

import javafx.beans.value.ChangeListener;

import org.nonprofitbookkeeping.service.AccountLookupService;
import org.nonprofitbookkeeping.service.FundBalanceService;
import org.nonprofitbookkeeping.service.FundLookupService;
import org.nonprofitbookkeeping.service.ScheduleEligibilityService;

/**
 * @deprecated Use a {@link UiServiceProvider} bound to the active {@link UiSessionContext} instead.
 * This compatibility facade delegates to a context-bound provider and will be removed once native
 * alternate panels receive providers directly.
 */
@Deprecated(forRemoval = false)
public final class UiServiceRegistry
{
    private static final UiSessionContext COMPATIBILITY_CONTEXT = new UiSessionContext();
    private static final UiServiceProvider COMPATIBILITY_PROVIDER = new UiServiceProvider(COMPATIBILITY_CONTEXT);
    private static UiSessionContext boundSessionContext;
    private static ChangeListener<Path> databasePathListener;
    private static ChangeListener<Long> companyIdListener;
    private static ChangeListener<String> companyLabelListener;

    private UiServiceRegistry() {}

    public static UiServiceProvider provider()
    {
        return COMPATIBILITY_PROVIDER;
    }

    public static synchronized void bindSessionContext(UiSessionContext sessionContext)
    {
        if (sessionContext == null)
        {
            unbindSessionContext();
            copyStateFrom(null);
            return;
        }
        if (boundSessionContext == sessionContext)
        {
            copyStateFrom(sessionContext);
            return;
        }

        unbindSessionContext();
        boundSessionContext = sessionContext;
        databasePathListener = (obs, oldPath, newPath) -> syncDatabase(newPath);
        companyIdListener = (obs, oldId, newId) -> syncCompany(boundSessionContext);
        companyLabelListener = (obs, oldLabel, newLabel) -> syncCompany(boundSessionContext);
        sessionContext.activeDatabaseBasePathProperty().addListener(databasePathListener);
        sessionContext.activeCompanyIdProperty().addListener(companyIdListener);
        sessionContext.activeCompanyDisplayLabelProperty().addListener(companyLabelListener);
        copyStateFrom(sessionContext);
    }

    static synchronized UiSessionContext compatibilitySessionContext()
    {
        return COMPATIBILITY_CONTEXT;
    }

    private static void unbindSessionContext()
    {
        if (boundSessionContext == null)
        {
            return;
        }
        boundSessionContext.activeDatabaseBasePathProperty().removeListener(databasePathListener);
        boundSessionContext.activeCompanyIdProperty().removeListener(companyIdListener);
        boundSessionContext.activeCompanyDisplayLabelProperty().removeListener(companyLabelListener);
        boundSessionContext = null;
        databasePathListener = null;
        companyIdListener = null;
        companyLabelListener = null;
    }

    private static void copyStateFrom(UiSessionContext sessionContext)
    {
        if (sessionContext == null)
        {
            COMPATIBILITY_CONTEXT.clearDatabase();
            return;
        }
        syncDatabase(sessionContext.activeDatabaseBasePath());
        syncCompany(sessionContext);
    }

    private static void syncDatabase(Path databasePath)
    {
        if (databasePath == null)
        {
            COMPATIBILITY_CONTEXT.clearDatabase();
        }
        else
        {
            COMPATIBILITY_CONTEXT.openDatabase(databasePath);
        }
    }

    private static void syncCompany(UiSessionContext sessionContext)
    {
        if (sessionContext == null || sessionContext.activeCompanyId() == null)
        {
            COMPATIBILITY_CONTEXT.clearCompany();
            return;
        }
        COMPATIBILITY_CONTEXT.openCompany(sessionContext.activeCompanyId(), sessionContext.activeCompanyDisplayLabel());
    }

    public static AccountLookupService accountLookup() { return COMPATIBILITY_PROVIDER.accountLookup(); }
    public static FundLookupService fundLookup() { return COMPATIBILITY_PROVIDER.fundLookup(); }
    public static FundBalanceService fundBalance() { return COMPATIBILITY_PROVIDER.fundBalance(); }
    public static ScheduleEligibilityService schedules() { return COMPATIBILITY_PROVIDER.scheduleEligibility(); }
}
