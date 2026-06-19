package org.nonprofitbookkeeping.ui;

import java.nio.file.Path;
import java.util.Objects;

import org.nonprofitbookkeeping.bridge.dashboard.DashboardDataBridge;
import org.nonprofitbookkeeping.persistence.Jpa;
import org.nonprofitbookkeeping.service.AccountLookupService;
import org.nonprofitbookkeeping.service.FundBalanceService;
import org.nonprofitbookkeeping.service.FundLookupService;
import org.nonprofitbookkeeping.service.ScheduleEligibilityService;

import nonprofitbookkeeping.importer.sclx.SclxImportService;
import nonprofitbookkeeping.service.LegacyNpbkImportService;

/**
 * Context-bound service factory for alternate JavaFX UI workflows.
 *
 * <p>Services that depend on the active H2/JPA database are created lazily after
 * {@link UiSessionContext} reports an open database. When the active database base path
 * changes, the provider closes the previous JPA bootstrap and rebuilds services on the
 * next access.</p>
 */
public class UiServiceProvider implements AutoCloseable
{
    private final UiSessionContext sessionContext;
    private ServiceBundle bundle;
    private Path bundleDatabaseBasePath;

    private final AlternateDataContextService companyAdministration;
    private final DatabaseAdministrationService databaseAdministration;
    private final ImportExportServices importExportServices;
    private final GlobalSearchService globalSearchService;

    public UiServiceProvider(UiSessionContext sessionContext)
    {
        this(sessionContext, null);
    }

    public UiServiceProvider(AlternateDataContextService companyAdministration)
    {
        this(Objects.requireNonNull(companyAdministration, "companyAdministration").sessionContext(), companyAdministration);
    }

    UiServiceProvider(UiSessionContext sessionContext, AlternateDataContextService companyAdministration)
    {
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext");
        this.companyAdministration = companyAdministration;
        this.databaseAdministration = new DatabaseAdministrationService(sessionContext, companyAdministration);
        this.importExportServices = new ImportExportServices();
        this.globalSearchService = new GlobalSearchService(this);
        this.sessionContext.activeDatabaseBasePathProperty().addListener((obs, oldPath, newPath) -> invalidateServices());
    }

    public UiSessionContext sessionContext()
    {
        return sessionContext;
    }

    public AccountLookupService accountLookup()
    {
        return services().accountLookup();
    }

    public FundLookupService fundLookup()
    {
        return services().fundLookup();
    }

    public FundBalanceService fundBalance()
    {
        return services().fundBalance();
    }

    public ScheduleEligibilityService scheduleEligibility()
    {
        return services().scheduleEligibility();
    }

    public DashboardDataBridge dashboardData()
    {
        return services().dashboardData();
    }

    public AlternateDataContextService companyAdministration()
    {
        if (companyAdministration != null)
        {
            return companyAdministration;
        }
        throw new IllegalStateException("No company administration service was supplied for this UI context.");
    }

    public DatabaseAdministrationService databaseAdministration()
    {
        return databaseAdministration;
    }

    public ImportExportServices importExport()
    {
        return importExportServices;
    }

    public GlobalSearchService globalSearch()
    {
        return globalSearchService;
    }

    boolean hasDatabaseServicesForCurrentContext()
    {
        return bundle != null && Objects.equals(bundleDatabaseBasePath, sessionContext.activeDatabaseBasePath());
    }

    void invalidateServices()
    {
        if (bundle != null)
        {
            bundle.jpa().close();
            bundle = null;
            bundleDatabaseBasePath = null;
        }
    }

    private ServiceBundle services()
    {
        Path activeDatabase = sessionContext.activeDatabaseBasePath();
        if (activeDatabase == null)
        {
            throw new IllegalStateException("No database is open for the alternate UI session.");
        }
        if (bundle == null || !Objects.equals(bundleDatabaseBasePath, activeDatabase))
        {
            invalidateServices();
            Jpa jpa = new Jpa();
            bundle = new ServiceBundle(
                jpa,
                new AccountLookupService(jpa),
                new FundLookupService(jpa),
                new FundBalanceService(jpa),
                new ScheduleEligibilityService(jpa),
                new DashboardDataBridge(this));
            bundleDatabaseBasePath = activeDatabase;
        }
        return bundle;
    }

    @Override
    public void close()
    {
        invalidateServices();
    }

    private record ServiceBundle(Jpa jpa, AccountLookupService accountLookup, FundLookupService fundLookup,
                                 FundBalanceService fundBalance, ScheduleEligibilityService scheduleEligibility,
                                 DashboardDataBridge dashboardData) {}

    /** Database-level operations available to alternate admin panels. */
    public static class DatabaseAdministrationService
    {
        private final UiSessionContext sessionContext;
        private final AlternateDataContextService contextService;

        DatabaseAdministrationService(UiSessionContext sessionContext, AlternateDataContextService contextService)
        {
            this.sessionContext = sessionContext;
            this.contextService = contextService;
        }

        public DatabaseOpenService.OpenResult openDatabase(Path basePath) throws Exception
        {
            if (contextService != null)
            {
                contextService.openDatabase(basePath);
                return new DatabaseOpenService.OpenResult(contextService.activeDatabaseBasePath(), null);
            }
            DatabaseOpenService.OpenResult result = DatabaseOpenService.openDatabase(basePath);
            sessionContext.openDatabase(result.basePath());
            return result;
        }

        public void closeDatabase()
        {
            if (contextService != null)
            {
                contextService.setActiveDatabaseBasePath(null);
            }
            else
            {
                sessionContext.clearDatabase();
            }
        }
    }

    /** Import/export services already available to the alternate UI layer. */
    public static class ImportExportServices
    {
        public LegacyNpbkImportService legacyNpbkImport()
        {
            return new LegacyNpbkImportService();
        }

        public SclxImportService sclxImport()
        {
            return new SclxImportService();
        }
    }
}
