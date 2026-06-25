package nonprofitbookkeeping.service;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import nonprofitbookkeeping.core.ChartOfAccountsBuilder;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.persistence.CompanyRepository;
import nonprofitbookkeeping.persistence.CompanyRepository.CompanyRecord;

/** Shared business logic for company selection and administration. */
public class CompanyManagementService
{
    public static final String TEMPLATE_SCA = "SCA Branch";
    public static final String TEMPLATE_NONPROFIT = "Standard Nonprofit";
    public static final String TEMPLATE_BASIC = "Basic";
    public static final String TEMPLATE_EMPTY = "Empty Chart";

    private final CompanyRepository repository;

    public CompanyManagementService()
    {
        this(new CompanyRepository());
    }

    public CompanyManagementService(CompanyRepository repository)
    {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    public List<CompanyRecord> listCompanies() throws SQLException
    {
        requireDatabase();
        return this.repository.listCompanies();
    }

    public Company load(long id) throws SQLException, IOException
    {
        requireDatabase();
        return this.repository.load(id);
    }

    public long save(Long id, CompanyProfileModel profile)
        throws SQLException, IOException
    {
        requireDatabase();
        validate(profile);
        ensureUniqueName(profile.getCompanyName(), id);

        Company company = id == null ? new Company() : this.repository.load(id);
        boolean chartIsEmpty = company.getChartOfAccounts() == null ||
            company.getChartOfAccounts().getAccounts().isEmpty();
        company.setCompanyProfileModel(profile);
        if (chartIsEmpty)
        {
            applyTemplate(company, profile.getChartOfAccountsType());
        }
        normalizeDefaultBank(company, profile);
        return this.repository.save(id, company);
    }

    public void markOpened(long id) throws SQLException
    {
        this.repository.markOpened(id);
    }

    public void setArchived(long id, boolean archived) throws SQLException
    {
        this.repository.setArchived(id, archived);
    }

    public byte[] exportCompany(long id) throws SQLException
    {
        return this.repository.exportCompany(id);
    }

    public void delete(long id) throws SQLException
    {
        this.repository.delete(id);
    }

    public CompanyPreview preview(CompanyRecord record)
        throws SQLException, IOException
    {
        Company company = load(record.id());
        CompanyProfileModel profile = company.getCompanyProfileModel();
        int accountCount = company.getChartOfAccounts() == null ? 0 :
            company.getChartOfAccounts().getAccounts().size();
        int fundCount = countFunds(company);
        List<AccountingTransaction> transactions = company.getLedger() == null ||
            company.getLedger().getJournal() == null ? List.of() :
            company.getLedger().getJournal().getJournalTransactions();
        LocalDate earliest = null;
        LocalDate latest = null;
        for (AccountingTransaction transaction : transactions)
        {
            LocalDate date = parseDate(transaction.getDate());
            if (date == null)
            {
                continue;
            }
            earliest = earliest == null || date.isBefore(earliest) ? date : earliest;
            latest = latest == null || date.isAfter(latest) ? date : latest;
        }

        List<String> warnings = validateOperationalSettings(company);
        return new CompanyPreview(record, profile, accountCount, fundCount,
            transactions.size(), earliest, latest, warnings);
    }

    public List<String> availableBankAccounts(Company company)
    {
        if (company == null || company.getChartOfAccounts() == null)
        {
            return List.of();
        }
        List<String> accounts = new ArrayList<>();
        for (Account account : company.getChartOfAccounts().getAccounts())
        {
            if (account == null)
            {
                continue;
            }
            AccountType type = account.getAccountType();
            if (type == AccountType.BANK || type == AccountType.CASH ||
                type == AccountType.CHECKING || type == AccountType.MONEYMKRT)
            {
                accounts.add(account.getAccountNumber());
            }
        }
        return List.copyOf(accounts);
    }

    public static List<String> chartTemplates()
    {
        return List.of(TEMPLATE_SCA, TEMPLATE_NONPROFIT, TEMPLATE_BASIC,
            TEMPLATE_EMPTY);
    }

    private int countFunds(Company company)
    {
        Set<String> funds = new HashSet<>();
        if (company.getChartOfAccounts() != null)
        {
            for (Account account : company.getChartOfAccounts().getAccounts())
            {
                if (account != null && account.getAssociatedFundIds() != null)
                {
                    funds.addAll(account.getAssociatedFundIds());
                }
            }
        }
        if (company.getLedger() != null &&
            company.getLedger().getJournal() != null)
        {
            for (AccountingTransaction transaction :
                company.getLedger().getJournal().getJournalTransactions())
            {
                if (transaction.getEntries() == null)
                {
                    continue;
                }
                for (AccountingEntry entry : transaction.getEntries())
                {
                    if (entry != null && entry.getFundNumber() != null &&
                        !entry.getFundNumber().isBlank())
                    {
                        funds.add(entry.getFundNumber());
                    }
                }
            }
        }
        return funds.size();
    }

    private void applyTemplate(Company company, String template)
    {
        String normalized = template == null ? TEMPLATE_EMPTY : template;
        ChartOfAccountsBuilder builder = ChartOfAccountsBuilder.create();
        if (TEMPLATE_EMPTY.equals(normalized))
        {
            company.setChartOfAccounts(builder.build());
            return;
        }

        builder.addAccount("1000", "Checking", AccountSide.DEBIT)
            .addAccount("1010", "Cash", AccountSide.DEBIT)
            .addAccount("2000", "Liabilities", AccountSide.CREDIT)
            .addAccount("3000", "Net Assets", AccountSide.CREDIT);

        if (TEMPLATE_SCA.equals(normalized))
        {
            builder.addAccount("4000", "Donations Received", AccountSide.CREDIT)
                .addAccount("4100", "Event Income", AccountSide.CREDIT)
                .addAccount("5000", "Site/Storage Rental", AccountSide.DEBIT)
                .addAccount("5100", "Fees & Honoraria", AccountSide.DEBIT)
                .addAccount("5200", "Supplies & Equipment", AccountSide.DEBIT)
                .addAccount("5300", "Food", AccountSide.DEBIT)
                .addAccount("5400", "Transfer Out", AccountSide.DEBIT);
        }
        else if (TEMPLATE_NONPROFIT.equals(normalized))
        {
            builder.addAccount("4000", "Contributions", AccountSide.CREDIT)
                .addAccount("4100", "Program Revenue", AccountSide.CREDIT)
                .addAccount("5000", "Program Expense", AccountSide.DEBIT)
                .addAccount("5100", "Administrative Expense", AccountSide.DEBIT)
                .addAccount("5200", "Fundraising Expense", AccountSide.DEBIT);
        }
        else
        {
            builder.addAccount("4000", "Income", AccountSide.CREDIT)
                .addAccount("5000", "Expense", AccountSide.DEBIT);
        }
        company.setChartOfAccounts(builder.build());
        assignTemplateTypes(company);
    }

    private void assignTemplateTypes(Company company)
    {
        for (Account account : company.getChartOfAccounts().getAccounts())
        {
            String number = account.getAccountNumber();
            if ("1000".equals(number))
            {
                account.setAccountType(AccountType.CHECKING);
            }
            else if ("1010".equals(number))
            {
                account.setAccountType(AccountType.CASH);
            }
            else if (number.startsWith("2"))
            {
                account.setAccountType(AccountType.LIABILITY);
            }
            else if (number.startsWith("3"))
            {
                account.setAccountType(AccountType.EQUITY);
            }
            else if (number.startsWith("4"))
            {
                account.setAccountType(AccountType.INCOME);
            }
            else if (number.startsWith("5"))
            {
                account.setAccountType(AccountType.EXPENSE);
            }
            else
            {
                account.setAccountType(AccountType.ASSET);
            }
        }
    }

    private void normalizeDefaultBank(Company company,
        CompanyProfileModel profile)
    {
        List<String> bankAccounts = availableBankAccounts(company);
        if (bankAccounts.isEmpty())
        {
            profile.setDefaultBankAccount(null);
            return;
        }
        if (profile.getDefaultBankAccount() == null ||
            !bankAccounts.contains(profile.getDefaultBankAccount()))
        {
            profile.setDefaultBankAccount(bankAccounts.get(0));
        }
    }

    private List<String> validateOperationalSettings(Company company)
    {
        List<String> warnings = new ArrayList<>();
        CompanyProfileModel profile = company.getCompanyProfileModel();
        if (profile == null)
        {
            return List.of("Company profile is missing.");
        }
        try
        {
            validateFiscalStart(profile.getFiscalYearStart());
        }
        catch (IllegalArgumentException ex)
        {
            warnings.add(ex.getMessage());
        }
        try
        {
            Currency.getInstance(profile.getBaseCurrency());
        }
        catch (RuntimeException ex)
        {
            warnings.add("Base currency is missing or invalid.");
        }
        List<String> banks = availableBankAccounts(company);
        if (!banks.isEmpty() &&
            !banks.contains(profile.getDefaultBankAccount()))
        {
            warnings.add("Default bank account is not a selectable bank account.");
        }
        return List.copyOf(warnings);
    }

    private void validate(CompanyProfileModel profile)
    {
        if (profile == null || blank(profile.getCompanyName()))
        {
            throw new IllegalArgumentException("Company name is required.");
        }
        validateFiscalStart(profile.getFiscalYearStart());
        try
        {
            Currency.getInstance(profile.getBaseCurrency());
        }
        catch (RuntimeException ex)
        {
            throw new IllegalArgumentException(
                "Base currency must be a valid three-letter ISO code.");
        }
        try
        {
            LocalDate.parse(profile.getStartingBalanceDate());
        }
        catch (RuntimeException ex)
        {
            throw new IllegalArgumentException(
                "Starting balance date must use YYYY-MM-DD.");
        }
        if (blank(profile.getChartOfAccountsType()))
        {
            throw new IllegalArgumentException(
                "Select a chart-of-accounts template.");
        }
    }

    private void validateFiscalStart(String value)
    {
        if (blank(value) || !value.matches("\\d{2}-\\d{2}"))
        {
            throw new IllegalArgumentException(
                "Fiscal year start must use MM-DD.");
        }
        try
        {
            MonthDay.parse("--" + value);
        }
        catch (DateTimeParseException ex)
        {
            throw new IllegalArgumentException(
                "Fiscal year start is not a valid month and day.");
        }
    }

    private void ensureUniqueName(String name, Long ignoringId)
        throws SQLException
    {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (CompanyRecord record : listCompanies())
        {
            if (!Objects.equals(record.id(), ignoringId) &&
                record.name() != null &&
                record.name().trim().toLowerCase(Locale.ROOT)
                    .equals(normalized))
            {
                throw new IllegalArgumentException(
                    "A company with this name already exists.");
            }
        }
    }

    private LocalDate parseDate(String value)
    {
        try
        {
            return value == null ? null : LocalDate.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            return null;
        }
    }

    private void requireDatabase()
    {
        if (!Database.isInitialized())
        {
            throw new IllegalStateException(
                "Open a database before managing companies.");
        }
    }

    private static boolean blank(String value)
    {
        return value == null || value.isBlank();
    }

    public record CompanyPreview(CompanyRecord record,
        CompanyProfileModel profile, int accountCount, int fundCount,
        int transactionCount, LocalDate earliestTransaction,
        LocalDate latestTransaction, List<String> warnings)
    {
        public CompanyPreview
        {
            warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
    }
}
