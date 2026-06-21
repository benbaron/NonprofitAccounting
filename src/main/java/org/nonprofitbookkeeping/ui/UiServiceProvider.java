package org.nonprofitbookkeeping.ui;

import java.nio.file.Path;
import java.util.Objects;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.CurrentCompany;

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
        return this.sessionContext;
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
        if (this.companyAdministration != null)
        {
            return this.companyAdministration;
        }
        throw new IllegalStateException("No company administration service was supplied for this UI context.");
    }

    public DatabaseAdministrationService databaseAdministration()
    {
        return this.databaseAdministration;
    }

    public ImportExportServices importExport()
    {
        return this.importExportServices;
    }

    public GlobalSearchService globalSearch()
    {
        return this.globalSearchService;
    }

    boolean hasDatabaseServicesForCurrentContext()
    {
        return this.bundle != null && Objects.equals(this.bundleDatabaseBasePath, this.sessionContext.activeDatabaseBasePath());
    }

    void invalidateServices()
    {
        if (this.bundle != null)
        {
            this.bundle.jpa().close();
            this.bundle = null;
            this.bundleDatabaseBasePath = null;
        }
    }

    private ServiceBundle services()
    {
        Path activeDatabase = this.sessionContext.activeDatabaseBasePath();
        if (activeDatabase == null)
        {
            throw new IllegalStateException("No database is open for the alternate UI session.");
        }
        if (this.bundle == null || !Objects.equals(this.bundleDatabaseBasePath, activeDatabase))
        {
            invalidateServices();
            Jpa jpa = new Jpa();
            this.bundle = new ServiceBundle(
                jpa,
                new AccountLookupService(jpa),
                new FundLookupService(jpa),
                new FundBalanceService(jpa),
                new ScheduleEligibilityService(jpa),
                new DashboardDataBridge(this));
            this.bundleDatabaseBasePath = activeDatabase;
        }
        return this.bundle;
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
            if (this.contextService != null)
            {
                this.contextService.openDatabase(basePath);
                return new DatabaseOpenService.OpenResult(this.contextService.activeDatabaseBasePath(), null);
            }
            DatabaseOpenService.OpenResult result = DatabaseOpenService.openDatabase(basePath);
            this.sessionContext.openDatabase(result.basePath());
            return result;
        }

        public void closeDatabase()
        {
            CurrentCompany.close();
            Database.close();
            if (this.contextService != null)
            {
                this.contextService.setActiveDatabaseBasePath(null);
            }
            else
            {
                this.sessionContext.clearDatabase();
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
