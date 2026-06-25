package nonprofitbookkeeping.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;

/** Shared company selection, configuration, lifecycle, and validation service. */
public class CompanyManagementService
{
    public enum ChartTemplate
    {
        SCA_BRANCH("SCA Branch"),
        STANDARD_NONPROFIT("Standard Nonprofit"),
        EMPTY("Empty Chart");

        private final String label;

        ChartTemplate(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return this.label;
        }
    }

    public record CompanyDefinition(
        String companyName,
        String legalStructure,
        String fiscalYearStart,
        String baseCurrency,
        LocalDate startingBalanceDate,
        ChartTemplate chartTemplate,
        boolean enableFundAccounting,
        boolean enableInventory,
        boolean enableMultiCurrency,
        String defaultBankAccount)
    {
    }

    public record CompanySummary(
        long id,
        String name,
        Instant updatedAt,
        Instant lastOpenedAt,
        String status,
        boolean archived,
        String legalStructure,
        String fiscalYearStart,
        String baseCurrency,
        String defaultBankAccount,
        boolean fundAccounting,
        int accountCount,
        int fundCount,
        int transactionCount,
        LocalDate earliestTransactionDate,
        LocalDate latestTransactionDate,
        String chartTemplate,
        List<String> warnings)
    {
        public CompanySummary
        {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }

    private final CompanyRepository repository;

    public CompanyManagementService()
    {
        this(new CompanyRepository());
    }

    public CompanyManagementService(CompanyRepository repository)
    {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public List<CompanySummary> listCompanies(boolean includeArchived)
        throws SQLException
    {
        requireDatabase();
        List<CompanySummary> result = new ArrayList<>();
        for (CompanyRecord record : this.repository.listCompanies())
        {
            boolean archived = CompanyUiMetadataStore.isArchived(record.id());
            if (!includeArchived && archived)
            {
                continue;
            }
            try
            {
                result.add(summarize(record, this.repository.load(record.id()),
                    archived));
            }
            catch (IOException ex)
            {
                result.add(new CompanySummary(record.id(), record.name(),
                    record.updatedAt(),
                    CompanyUiMetadataStore.getLastOpened(record.id()),
                    "Invalid", archived, "", "", "", "", false,
                    0, 0, 0, null, null, "",
                    List.of("Unable to load company: " + ex.getMessage())));
            }
        }
        return result;
    }

    public Company load(long companyId) throws SQLException, IOException
    {
        requireDatabase();
        return this.repository.load(companyId);
    }

    public CompanyDefinition definitionFor(long companyId)
        throws SQLException, IOException
    {
        Company company = load(companyId);
        CompanyProfileModel profile = company.getCompanyProfileModel();
        if (profile == null)
        {
            profile = new CompanyProfileModel();
        }
        ChartTemplate template = parseTemplate(profile.getChartOfAccountsType());
        return new CompanyDefinition(
            value(profile.getCompanyName()),
            value(profile.getLegalStructure()),
            defaultIfBlank(profile.getFiscalYearStart(), "01-01"),
            defaultIfBlank(profile.getBaseCurrency(), "USD"),
            parseDate(profile.getStartingBalanceDate(), LocalDate.now()),
            template,
            profile.isEnableFundAccounting(),
            profile.isEnableInventory(),
            profile.isEnableMultiCurrency(),
            value(profile.getDefaultBankAccount()));
    }

    public long create(CompanyDefinition definition)
        throws SQLException, IOException
    {
        requireDatabase();
        validateDefinition(definition, null);
        ensureUniqueName(definition.companyName(), null);
        Company company = new Company();
        applyDefinition(company, definition, true);
        return this.repository.save(null, company);
    }

    public void update(long companyId, CompanyDefinition definition)
        throws SQLException, IOException
    {
        requireDatabase();
        Company company = this.repository.load(companyId);
        validateDefinition(definition, company);
        ensureUniqueName(definition.companyName(), companyId);
        applyDefinition(company, definition, false);
        this.repository.save(companyId, company);
        if (Objects.equals(CurrentCompany.getCurrentCompanyId(), companyId))
        {
            CurrentCompany.forceCompanyLoad(companyId, company);
        }
    }

    public Company open(long companyId) throws SQLException, IOException
    {
        requireDatabase();
        if (CompanyUiMetadataStore.isArchived(companyId))
        {
            throw new IllegalStateException(
                "Restore the archived company before opening it.");
        }
        Company company = this.repository.load(companyId);
        validateNormativeConfiguration(company);
        CurrentCompany.forceCompanyLoad(companyId, company);
        CurrentCompany.markCompanyOpen();
        PreferencesService.setLastUsedCompanyId(companyId);
        CompanyUiMetadataStore.setLastOpened(companyId, Instant.now());
        return company;
    }

    public void setArchived(long companyId, boolean archived)
        throws SQLException
    {
        requireDatabase();
        if (archived && Objects.equals(CurrentCompany.getCurrentCompanyId(),
            companyId))
        {
            CurrentCompany.close();
        }
        CompanyUiMetadataStore.setArchived(companyId, archived);
    }

    public byte[] exportBackup(long companyId) throws SQLException
    {
        requireDatabase();
        return this.repository.exportCompany(companyId);
    }

    public void delete(long companyId, String typedName,
        boolean backupAcknowledged, boolean destructionAcknowledged)
        throws SQLException, IOException
    {
        requireDatabase();
        Company company = this.repository.load(companyId);
        String expected = companyName(company, "Company " + companyId);
        if (!expected.equals(typedName))
        {
            throw new IllegalArgumentException(
                "Type the exact company name: " + expected);
        }
        if (!backupAcknowledged)
        {
            throw new IllegalArgumentException(
                "Acknowledge that a backup has been exported.");
        }
        if (!destructionAcknowledged)
        {
            throw new IllegalArgumentException(
                "Acknowledge permanent deletion.");
        }
        if (Objects.equals(CurrentCompany.getCurrentCompanyId(), companyId))
        {
            CurrentCompany.close();
        }
        this.repository.delete(companyId);
        CompanyUiMetadataStore.remove(companyId);
        if (Objects.equals(PreferencesService.getLastUsedCompanyId(), companyId))
        {
            PreferencesService.setLastUsedCompanyId(null);
        }
    }

    public long createDeterministicSampleCompany()
        throws SQLException, IOException
    {
        CompanyDefinition definition = new CompanyDefinition(
            "Sample Nonprofit Company", "Non-Profit", "01-01", "USD",
            LocalDate.of(2026, 1, 1), ChartTemplate.STANDARD_NONPROFIT,
            true, false, false, "1000");
        ensureUniqueName(definition.companyName(), null);
        return create(definition);
    }

    public void validateNormativeConfiguration(Company company)
    {
        List<String> warnings = validationWarnings(company);
        if (!warnings.isEmpty())
        {
            throw new IllegalStateException(String.join(" ", warnings));
        }
    }

    private CompanySummary summarize(CompanyRecord record, Company company,
        boolean archived)
    {
        CompanyProfileModel profile = company.getCompanyProfileModel();
        List<String> warnings = validationWarnings(company);
        List<LocalDate> dates = transactionDates(company);
        String status = archived ? "Archived" :
            warnings.isEmpty() ? "Ready" : "Needs Setup";
        return new CompanySummary(
            record.id(),
            companyName(company, record.name()),
            record.updatedAt(),
            CompanyUiMetadataStore.getLastOpened(record.id()),
            status,
            archived,
            profile == null ? "" : value(profile.getLegalStructure()),
            profile == null ? "" : value(profile.getFiscalYearStart()),
            profile == null ? "" : value(profile.getBaseCurrency()),
            profile == null ? "" : value(profile.getDefaultBankAccount()),
            profile != null && profile.isEnableFundAccounting(),
            accountCount(company),
            company.getFunds() == null ? 0 : company.getFunds().size(),
            transactionCount(company),
            dates.isEmpty() ? null : dates.get(0),
            dates.isEmpty() ? null : dates.get(dates.size() - 1),
            profile == null ? "" : value(profile.getChartOfAccountsType()),
            warnings);
    }

    private void applyDefinition(Company company, CompanyDefinition definition,
        boolean applyTemplate)
    {
        CompanyProfileModel profile = company.getCompanyProfileModel();
        if (profile == null)
        {
            profile = new CompanyProfileModel();
            company.setCompanyProfileModel(profile);
        }
        profile.setCompanyName(definition.companyName().trim());
        profile.setLegalStructure(value(definition.legalStructure()).trim());
        profile.setFiscalYearStart(definition.fiscalYearStart().trim());
        profile.setBaseCurrency(definition.baseCurrency().trim()
            .toUpperCase(Locale.ROOT));
        profile.setStartingBalanceDate(definition.startingBalanceDate().toString());
        profile.setChartOfAccountsType(definition.chartTemplate().toString());
        profile.setEnableFundAccounting(definition.enableFundAccounting());
        profile.setEnableInventory(definition.enableInventory());
        profile.setEnableMultiCurrency(definition.enableMultiCurrency());

        if (applyTemplate && definition.chartTemplate() != ChartTemplate.EMPTY)
        {
            company.setChartOfAccounts(buildTemplate(definition.chartTemplate()));
        }
        profile.setDefaultBankAccount(value(definition.defaultBankAccount()).trim());
        validateDefaultBankAccount(company, profile.getDefaultBankAccount());
    }

    private ChartOfAccounts buildTemplate(ChartTemplate template)
    {
        ChartOfAccounts chart = new ChartOfAccounts();
        if (template == ChartTemplate.SCA_BRANCH)
        {
            addAccount(chart, "1000", "Checking Account", AccountType.BANK,
                AccountSide.DEBIT);
            addAccount(chart, "1100", "Undeposited Funds", AccountType.ASSET,
                AccountSide.DEBIT);
            addAccount(chart, "2000", "Liabilities", AccountType.LIABILITY,
                AccountSide.CREDIT);
            addAccount(chart, "3000", "Net Assets", AccountType.EQUITY,
                AccountSide.CREDIT);
            addAccount(chart, "4000", "Event and Donation Income",
                AccountType.INCOME, AccountSide.CREDIT);
            addAccount(chart, "5000", "Program and Event Expense",
                AccountType.EXPENSE, AccountSide.DEBIT);
        }
        else
        {
            addAccount(chart, "1000", "Operating Checking", AccountType.BANK,
                AccountSide.DEBIT);
            addAccount(chart, "1200", "Accounts Receivable",
                AccountType.ASSET, AccountSide.DEBIT);
            addAccount(chart, "2000", "Accounts Payable",
                AccountType.LIABILITY, AccountSide.CREDIT);
            addAccount(chart, "3000", "Net Assets", AccountType.EQUITY,
                AccountSide.CREDIT);
            addAccount(chart, "4000", "Contributions and Revenue",
                AccountType.INCOME, AccountSide.CREDIT);
            addAccount(chart, "5000", "Program Services Expense",
                AccountType.EXPENSE, AccountSide.DEBIT);
        }
        return chart;
    }

    private void addAccount(ChartOfAccounts chart, String number, String name,
        AccountType type, AccountSide side)
    {
        Account account = new Account();
        account.setAccountNumber(number);
        account.setName(name);
        account.setAccountType(type);
        account.setIncreaseSide(side);
        account.setOpeningBalance(BigDecimal.ZERO);
        chart.addAccount(account);
    }

    private void validateDefinition(CompanyDefinition definition,
        Company existing)
    {
        if (definition == null || blank(definition.companyName()))
        {
            throw new IllegalArgumentException("Company name is required.");
        }
        if (definition.startingBalanceDate() == null)
        {
            throw new IllegalArgumentException(
                "Starting balance date is required.");
        }
        try
        {
            MonthDay.parse("--" + definition.fiscalYearStart());
        }
        catch (RuntimeException ex)
        {
            throw new IllegalArgumentException(
                "Fiscal year start must be a valid MM-DD date.");
        }
        try
        {
            Currency.getInstance(definition.baseCurrency().trim()
                .toUpperCase(Locale.ROOT));
        }
        catch (RuntimeException ex)
        {
            throw new IllegalArgumentException(
                "Base currency must be a valid ISO-4217 code.");
        }
        if (definition.chartTemplate() == null)
        {
            throw new IllegalArgumentException("Choose a chart template.");
        }
        if (definition.chartTemplate() != ChartTemplate.EMPTY &&
            blank(definition.defaultBankAccount()))
        {
            throw new IllegalArgumentException(
                "A default bank account is required for this template.");
        }
        if (existing != null && definition.chartTemplate() !=
            parseTemplate(existing.getCompanyProfileModel() == null ? "" :
                existing.getCompanyProfileModel().getChartOfAccountsType()) &&
            transactionCount(existing) > 0)
        {
            throw new IllegalArgumentException(
                "The chart template cannot be changed after transactions exist.");
        }
    }

    private List<String> validationWarnings(Company company)
    {
        List<String> warnings = new ArrayList<>();
        CompanyProfileModel profile = company == null ? null :
            company.getCompanyProfileModel();
        if (profile == null)
        {
            return List.of("Company profile is missing.");
        }
        if (blank(profile.getCompanyName()))
        {
            warnings.add("Company name is missing.");
        }
        try
        {
            MonthDay.parse("--" + profile.getFiscalYearStart());
        }
        catch (RuntimeException ex)
        {
            warnings.add("Fiscal year start is invalid.");
        }
        try
        {
            Currency.getInstance(profile.getBaseCurrency());
        }
        catch (RuntimeException ex)
        {
            warnings.add("Base currency is invalid.");
        }
        try
        {
            validateDefaultBankAccount(company, profile.getDefaultBankAccount());
        }
        catch (RuntimeException ex)
        {
            warnings.add(ex.getMessage());
        }
        return warnings;
    }

    private void validateDefaultBankAccount(Company company,
        String accountNumber)
    {
        if (blank(accountNumber))
        {
            if (accountCount(company) > 0)
            {
                throw new IllegalArgumentException(
                    "Default bank account is not configured.");
            }
            return;
        }
        if (company.getChartOfAccounts() == null)
        {
            throw new IllegalArgumentException(
                "Default bank account is not present in the chart.");
        }
        Account account = company.getChartOfAccounts().getAccount(accountNumber);
        if (account == null)
        {
            throw new IllegalArgumentException(
                "Default bank account " + accountNumber +
                    " is not present in the chart.");
        }
        AccountType type = account.getAccountType();
        if (type != AccountType.BANK && type != AccountType.CASH &&
            type != AccountType.CHECKING && type != AccountType.MONEYMKRT)
        {
            throw new IllegalArgumentException(
                "Default bank account must be a bank or cash account.");
        }
    }

    private void ensureUniqueName(String name, Long ignoringId)
        throws SQLException
    {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (CompanyRecord record : this.repository.listCompanies())
        {
            if (!Objects.equals(record.id(), ignoringId) &&
                value(record.name()).trim().toLowerCase(Locale.ROOT)
                    .equals(normalized))
            {
                throw new IllegalArgumentException(
                    "A company with this name already exists.");
            }
        }
    }

    private ChartTemplate parseTemplate(String value)
    {
        for (ChartTemplate template : ChartTemplate.values())
        {
            if (template.toString().equalsIgnoreCase(value(value)))
            {
                return template;
            }
        }
        return ChartTemplate.EMPTY;
    }

    private List<LocalDate> transactionDates(Company company)
    {
        List<LocalDate> result = new ArrayList<>();
        if (company.getLedger() == null || company.getLedger().getTransactions()
            == null)
        {
            return result;
        }
        company.getLedger().getTransactions().forEach(transaction -> {
            try
            {
                result.add(LocalDate.parse(transaction.getDate()));
            }
            catch (DateTimeParseException | NullPointerException ex)
            {
                // Invalid legacy dates are reported elsewhere and omitted here.
            }
        });
        result.sort(LocalDate::compareTo);
        return result;
    }

    private int accountCount(Company company)
    {
        return company.getChartOfAccounts() == null ? 0 :
            company.getChartOfAccounts().getAccounts().size();
    }

    private int transactionCount(Company company)
    {
        return company.getLedger() == null ||
            company.getLedger().getTransactions() == null ? 0 :
                company.getLedger().getTransactions().size();
    }

    private String companyName(Company company, String fallback)
    {
        CompanyProfileModel profile = company == null ? null :
            company.getCompanyProfileModel();
        return profile == null || blank(profile.getCompanyName()) ? fallback :
            profile.getCompanyName();
    }

    private LocalDate parseDate(String value, LocalDate fallback)
    {
        try
        {
            return LocalDate.parse(value);
        }
        catch (RuntimeException ex)
        {
            return fallback;
        }
    }

    private String defaultIfBlank(String value, String fallback)
    {
        return blank(value) ? fallback : value;
    }

    private static String value(String value)
    {
        return value == null ? "" : value;
    }

    private static boolean blank(String value)
    {
        return value == null || value.isBlank();
    }

    private void requireDatabase()
    {
        if (!Database.isInitialized())
        {
            throw new IllegalStateException(
                "Open or initialize a database before managing companies.");
        }
    }
}
