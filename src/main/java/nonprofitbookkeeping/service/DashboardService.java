package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import org.nonprofitbookkeeping.model.FundType;
import org.nonprofitbookkeeping.persistence.Jpa;
import org.nonprofitbookkeeping.service.FundBalanceRow;
import org.nonprofitbookkeeping.service.FundBalanceService;
import org.nonprofitbookkeeping.ui.UiServiceRegistry;

/** Builds the common dashboard snapshot used by both UI shells. */
public class DashboardService
{
    private static final Set<AccountType> ASSET_TYPES = EnumSet.of(
        AccountType.ASSET,
        AccountType.BANK,
        AccountType.CASH,
        AccountType.CHECKING,
        AccountType.INVEST,
        AccountType.SIMPLEINVEST,
        AccountType.MONEYMKRT,
        AccountType.MUTUAL,
        AccountType.FIXED_ASSET);

    private static final Set<AccountType> CASH_BANK_TYPES = EnumSet.of(
        AccountType.BANK,
        AccountType.CASH,
        AccountType.CHECKING,
        AccountType.MONEYMKRT);

    private static final Set<AccountType> LIABILITY_TYPES = EnumSet.of(
        AccountType.LIABILITY,
        AccountType.LONG_TERM_LIABILITY,
        AccountType.CREDIT,
        AccountType.CREDITCARD);

    /** Creates a dashboard snapshot for the current company. */
    public DashboardSnapshot load(LocalDate asOf, int recentLimit)
    {
        LocalDate effectiveAsOf = asOf == null ? LocalDate.now() : asOf;
        int effectiveLimit = Math.max(1, Math.min(100, recentLimit));

        if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
        {
            return emptySnapshot(effectiveAsOf,
                "Open a company to load dashboard data.");
        }

        Company company = CurrentCompany.getCompany();
        Ledger ledger = company.getLedger();
        LocalDate fiscalStart = fiscalPeriodStart(company, effectiveAsOf);
        Map<String, Account> accounts = accountsByNumber(company);
        Map<String, BigDecimal> balances = initialBalances(accounts);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        List<AccountingTransaction> transactions = ledger == null ? List.of() :
            ledger.getTransactions();
        if (transactions == null)
        {
            transactions = List.of();
        }

        for (AccountingTransaction transaction : transactions)
        {
            LocalDate transactionDate = parseDate(transaction.getDate());
            if (transactionDate == null || transactionDate.isAfter(effectiveAsOf))
            {
                continue;
            }

            for (AccountingEntry entry : safeEntries(transaction))
            {
                Account account = accounts.get(entry.getAccountNumber());
                if (account == null || entry.getAmount() == null)
                {
                    continue;
                }
                BigDecimal movement = signedNormalBalanceMovement(account, entry);
                balances.merge(account.getAccountNumber(), movement,
                    BigDecimal::add);

                if (!transactionDate.isBefore(fiscalStart))
                {
                    if (account.getAccountType() == AccountType.INCOME)
                    {
                        income = income.add(movement);
                    }
                    else if (account.getAccountType() == AccountType.EXPENSE)
                    {
                        expenses = expenses.add(movement);
                    }
                }
            }
        }

        BigDecimal cashAndBank = totalForTypes(accounts, balances,
            CASH_BANK_TYPES);
        BigDecimal assets = totalForTypes(accounts, balances, ASSET_TYPES);
        BigDecimal liabilities = totalForTypes(accounts, balances,
            LIABILITY_TYPES);

        List<FundBalanceRow> fundBalances = loadFundBalances(effectiveAsOf);
        BigDecimal restricted = fundBalances.stream()
            .filter(row -> row.getFundType() == FundType.TEMP_RESTRICTED ||
                row.getFundType() == FundType.PERM_RESTRICTED)
            .map(FundBalanceRow::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unrestricted = fundBalances.stream()
            .filter(row -> row.getFundType() != FundType.TEMP_RESTRICTED &&
                row.getFundType() != FundType.PERM_RESTRICTED)
            .map(FundBalanceRow::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        UnreconciledSummary unreconciled = unreconciledSummary(effectiveAsOf);
        int undeposited = undepositedCount();
        List<AccountingTransaction> recent = recentTransactions(
            transactions, effectiveAsOf, effectiveLimit);

        String companyName = company.getName();
        if (companyName == null || companyName.isBlank())
        {
            companyName = "Current Company";
        }

        return new DashboardSnapshot(
            companyName,
            effectiveAsOf,
            fiscalStart,
            cashAndBank,
            assets,
            liabilities,
            unrestricted,
            restricted,
            income,
            expenses,
            income.subtract(expenses),
            unreconciled.count(),
            unreconciled.amount(),
            undeposited,
            fundBalances,
            recent,
            "Dashboard updated through " + effectiveAsOf + ".");
    }

    /** Returns recent transactions by descending stable transaction ID. */
    List<AccountingTransaction> recentTransactions(
        List<AccountingTransaction> transactions, LocalDate asOf, int limit)
    {
        if (transactions == null || transactions.isEmpty())
        {
            return List.of();
        }
        int effectiveLimit = Math.max(1, Math.min(100, limit));
        return transactions.stream()
            .filter(transaction -> transaction != null)
            .filter(transaction -> {
                LocalDate date = parseDate(transaction.getDate());
                return date == null || !date.isAfter(asOf);
            })
            .sorted(Comparator.comparingInt(AccountingTransaction::getId)
                .reversed())
            .limit(effectiveLimit)
            .toList();
    }

    private DashboardSnapshot emptySnapshot(LocalDate asOf, String status)
    {
        return new DashboardSnapshot(
            "No company open",
            asOf,
            LocalDate.of(asOf.getYear(), 1, 1),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            0,
            BigDecimal.ZERO,
            0,
            List.of(),
            List.of(),
            status);
    }

    private Map<String, Account> accountsByNumber(Company company)
    {
        Map<String, Account> result = new LinkedHashMap<>();
        if (company.getChartOfAccounts() == null ||
            company.getChartOfAccounts().getAccounts() == null)
        {
            return result;
        }
        for (Account account : company.getChartOfAccounts().getAccounts())
        {
            if (account != null && account.getAccountNumber() != null)
            {
                result.put(account.getAccountNumber(), account);
            }
        }
        return result;
    }

    private Map<String, BigDecimal> initialBalances(
        Map<String, Account> accounts)
    {
        Map<String, BigDecimal> balances = new HashMap<>();
        for (Account account : accounts.values())
        {
            BigDecimal opening = account.getOpeningBalance();
            balances.put(account.getAccountNumber(),
                opening == null ? BigDecimal.ZERO : opening);
        }
        return balances;
    }

    private BigDecimal signedNormalBalanceMovement(Account account,
        AccountingEntry entry)
    {
        BigDecimal amount = entry.getAmount();
        AccountSide normal = account.getEffectiveIncreaseSide();
        return entry.getAccountSide() == normal ? amount : amount.negate();
    }

    private BigDecimal totalForTypes(Map<String, Account> accounts,
        Map<String, BigDecimal> balances, Set<AccountType> types)
    {
        BigDecimal total = BigDecimal.ZERO;
        for (Account account : accounts.values())
        {
            if (types.contains(account.getAccountType()))
            {
                total = total.add(balances.getOrDefault(
                    account.getAccountNumber(), BigDecimal.ZERO));
            }
        }
        return total;
    }

    private List<FundBalanceRow> loadFundBalances(LocalDate asOf)
    {
        try
        {
            return UiServiceRegistry.fundBalance().balancesAsOf(asOf);
        }
        catch (RuntimeException contextFailure)
        {
            if (!Database.isInitialized())
            {
                return List.of();
            }
            Jpa jpa = null;
            try
            {
                jpa = new Jpa();
                return new FundBalanceService(jpa).balancesAsOf(asOf);
            }
            catch (RuntimeException directFailure)
            {
                return List.of();
            }
            finally
            {
                if (jpa != null)
                {
                    jpa.close();
                }
            }
        }
    }

    private UnreconciledSummary unreconciledSummary(LocalDate asOf)
    {
        Map<Integer, AccountingTransaction> unique = new LinkedHashMap<>();
        try
        {
            for (String account : ReconciliationService.listReconcilableAccounts())
            {
                for (AccountingTransaction transaction :
                    ReconciliationService.getUnreconciled(account))
                {
                    LocalDate date = parseDate(transaction.getDate());
                    if (date == null || !date.isAfter(asOf))
                    {
                        unique.putIfAbsent(transaction.getId(), transaction);
                    }
                }
            }
        }
        catch (RuntimeException ex)
        {
            return new UnreconciledSummary(0, BigDecimal.ZERO);
        }

        BigDecimal amount = unique.values().stream()
            .map(AccountingTransaction::getTotalAmount)
            .filter(value -> value != null)
            .map(BigDecimal::abs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new UnreconciledSummary(unique.size(), amount);
    }

    private int undepositedCount()
    {
        try
        {
            return new UndepositedFundsService().listItems().size();
        }
        catch (RuntimeException ex)
        {
            return 0;
        }
    }

    private LocalDate fiscalPeriodStart(Company company, LocalDate asOf)
    {
        CompanyProfileModel profile = company.getCompanyProfileModel();
        MonthDay start = MonthDay.of(1, 1);
        if (profile != null && profile.getFiscalYearStart() != null &&
            !profile.getFiscalYearStart().isBlank())
        {
            try
            {
                start = MonthDay.parse("--" +
                    profile.getFiscalYearStart().trim());
            }
            catch (DateTimeParseException ex)
            {
                start = MonthDay.of(1, 1);
            }
        }
        LocalDate candidate = start.atYear(asOf.getYear());
        return candidate.isAfter(asOf) ? candidate.minusYears(1) : candidate;
    }

    private LocalDate parseDate(String value)
    {
        if (value == null || value.isBlank())
        {
            return null;
        }
        try
        {
            return LocalDate.parse(value);
        }
        catch (DateTimeParseException ex)
        {
            return null;
        }
    }

    private List<AccountingEntry> safeEntries(
        AccountingTransaction transaction)
    {
        if (transaction == null || transaction.getEntries() == null)
        {
            return List.of();
        }
        return new ArrayList<>(transaction.getEntries());
    }

    private record UnreconciledSummary(int count, BigDecimal amount)
    {
    }
}
