package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.core.ChartOfAccountsBuilder;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Service-backed company administration workflow for the alternate UI. */
public class AlternateCompanyAdminService
{
    public static final String DELETE_DEFINITION = "Delete removes one company row from the active H2 database's company_store table. "
        + "It does not delete the database file and does not attempt a cross-table purge beyond that serialized company row.";
    public static final String BACKUP_GUIDANCE = "Export or back up the database before deleting a company.";

    private final CompanyRepository repository;
    private final AlternateDataContextService contextService;
    private final UiSessionContext sessionContext;

    public AlternateCompanyAdminService(UiServiceProvider provider)
    {
        this(new CompanyRepository(), companyContext(provider), provider.sessionContext());
    }

    private static AlternateDataContextService companyContext(UiServiceProvider provider)
    {
        try
        {
            return provider.companyAdministration();
        }
        catch (IllegalStateException ex)
        {
            return new AlternateDataContextService();
        }
    }

    AlternateCompanyAdminService(CompanyRepository repository, AlternateDataContextService contextService, UiSessionContext sessionContext)
    {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.contextService = Objects.requireNonNull(contextService, "contextService");
        this.sessionContext = Objects.requireNonNull(sessionContext, "sessionContext");
    }

    public List<CompanyRecord> listCompanies() throws SQLException
    {
        requireDatabase();
        return this.repository.listCompanies();
    }

    public long createCompany(CreateCompanyRequest request) throws SQLException, IOException
    {
        requireDatabase();
        validateCreate(request);
        ensureUniqueName(request.organizationName(), null);
        Company company = new Company();
        company.setCompanyProfileModel(profileFor(request, false));
        long id = this.repository.save(null, company);
        openCompany(id);
        this.sessionContext.updateCompanyMetadata(new UiSessionContext.CompanyMetadata(false, false, true));
        return id;
    }

    public void openCompany(long companyId) throws IOException, SQLException
    {
        Company company = this.repository.load(companyId);
        String label = companyName(company, "Company " + companyId);
        this.contextService.openCompany(companyId, label);
    }

    public void closeActiveCompany()
    {
        CurrentCompany.close();
        this.contextService.clearActiveCompanyContext();
    }

    public void deleteCompany(long companyId, String typedCompanyName, boolean backupAcknowledged) throws SQLException, IOException
    {
        requireDatabase();
        if (!backupAcknowledged)
        {
            throw new IllegalArgumentException(BACKUP_GUIDANCE);
        }
        Company company = this.repository.load(companyId);
        String expectedName = companyName(company, "Company " + companyId);
        if (!expectedName.equals(typedCompanyName))
        {
            throw new IllegalArgumentException("Type the exact company name to confirm deletion: " + expectedName);
        }
        if (Objects.equals(this.sessionContext.activeCompanyId(), companyId))
        {
            throw new IllegalStateException("Close or switch away from the active company before deleting it.");
        }
        this.repository.delete(companyId);
    }

    public PopulateResult populateCompany(long companyId) throws SQLException, IOException
    {
        requireDatabase();
        Company company = this.repository.load(companyId);
        if (isPopulated(company))
        {
            return new PopulateResult(companyId, false, "Company already has a chart of accounts or transactions; no starter data was added.");
        }
        company.setChartOfAccounts(ChartOfAccountsBuilder.create()
            .addAccount("1000", "Cash", AccountSide.DEBIT)
            .addAccount("2000", "Liabilities", AccountSide.CREDIT)
            .addAccount("3000", "Net Assets", AccountSide.CREDIT)
            .addAccount("4000", "Contributions", AccountSide.CREDIT)
            .addAccount("5000", "Program Expense", AccountSide.DEBIT)
            .build());
        CompanyProfileModel profile = company.getCompanyProfileModel();
        if (profile != null && (profile.getChartOfAccountsType() == null || profile.getChartOfAccountsType().isBlank()))
        {
            profile.setChartOfAccountsType("Starter Nonprofit");
        }
        this.repository.save(companyId, company);
        if (Objects.equals(this.sessionContext.activeCompanyId(), companyId))
        {
            openCompany(companyId);
            this.sessionContext.updateCompanyMetadata(new UiSessionContext.CompanyMetadata(false, true, false));
        }
        return new PopulateResult(companyId, true, "Starter chart/settings added once; running populate again will not duplicate them.");
    }

    public long createSampleCompany()
        throws SQLException, IOException
    {
        CreateCompanyRequest request = new CreateCompanyRequest("Sample Nonprofit Company", "Non-Profit", "01-01", "USD", LocalDate.of(2026, 1, 1).toString(), true);
        ensureUniqueName(request.organizationName(), null);
        Company company = new Company();
        company.setCompanyProfileModel(profileFor(request, true));
        company.setChartOfAccounts(ChartOfAccountsBuilder.create()
            .addAccount("1000", "Sample Checking", AccountSide.DEBIT)
            .addAccount("4000", "Sample Contributions", AccountSide.CREDIT)
            .addAccount("5000", "Sample Outreach Expense", AccountSide.DEBIT)
            .build());
        long id = this.repository.save(null, company);
        openCompany(id);
        this.sessionContext.updateCompanyMetadata(new UiSessionContext.CompanyMetadata(true, true, true));
        return id;
    }

    private void validateCreate(CreateCompanyRequest request)
    {
        if (request == null || blank(request.organizationName()) || blank(request.legalStructure()) || blank(request.fiscalYearStart()) || blank(request.baseCurrency()) || blank(request.startingBalanceDate()))
        {
            throw new IllegalArgumentException("Organization name, legal structure, fiscal year start, base currency, and starting balance date are required.");
        }
        if (!request.fiscalYearStart().matches("\\d{2}-\\d{2}"))
        {
            throw new IllegalArgumentException("Fiscal year start must use MM-DD.");
        }
        LocalDate.parse(request.startingBalanceDate());
        if (request.baseCurrency().trim().length() != 3)
        {
            throw new IllegalArgumentException("Base currency must be a three-letter code.");
        }
    }

    private void ensureUniqueName(String name, Long ignoringId) throws SQLException
    {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (CompanyRecord record : this.repository.listCompanies())
        {
            if (!Objects.equals(record.id(), ignoringId) && record.name() != null && record.name().trim().toLowerCase(Locale.ROOT).equals(normalized))
            {
                throw new IllegalArgumentException("A company with this name already exists.");
            }
        }
    }

    private void requireDatabase()
    {
        if (!Database.isInitialized() || !this.sessionContext.isDatabaseOpen()) throw new IllegalStateException("Open a database before managing companies.");
    }

    private static CompanyProfileModel profileFor(CreateCompanyRequest request, boolean sample)
    {
        CompanyProfileModel profile = new CompanyProfileModel();
        profile.setCompanyName(request.organizationName().trim());
        profile.setLegalStructure(request.legalStructure().trim());
        profile.setFiscalYearStart(request.fiscalYearStart().trim());
        profile.setBaseCurrency(request.baseCurrency().trim().toUpperCase(Locale.ROOT));
        profile.setStartingBalanceDate(request.startingBalanceDate().trim());
        profile.setChartOfAccountsType(sample ? "Deterministic Sample" : "Unpopulated");
        profile.setEnableFundAccounting(request.enableFundAccounting());
        return profile;
    }

    private static boolean isPopulated(Company company)
    {
        return company.getChartOfAccounts() != null && !company.getChartOfAccounts().getAccounts().isEmpty()
            || company.getLedger() != null && company.getLedger().getJournal() != null && !company.getLedger().getJournal().getJournalTransactions().isEmpty();
    }

    private static String companyName(Company company, String fallback)
    {
        CompanyProfileModel profile = company == null ? null : company.getCompanyProfileModel();
        return profile == null || blank(profile.getCompanyName()) ? fallback : profile.getCompanyName();
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }

    public record CreateCompanyRequest(String organizationName, String legalStructure, String fiscalYearStart,
                                       String baseCurrency, String startingBalanceDate, boolean enableFundAccounting) {}
    public record PopulateResult(long companyId, boolean populated, String message) {}
}
