package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.service.LedgerTransactionQueryService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service-backed operations for the native alternate Funds workspace.
 */
public class AlternateFundsService
{
    private final Supplier<Company> companySupplier;
    private final BooleanSupplier companyOpenSupplier;
    private final Persister persister;
    private final LedgerTransactionQueryService ledgerQueryService;

    public AlternateFundsService()
    {
        this(CurrentCompany::getCompany, CurrentCompany::isOpen, CurrentCompany::persist);
    }

    AlternateFundsService(Supplier<Company> companySupplier, BooleanSupplier companyOpenSupplier, Persister persister)
    {
        this.companySupplier = Objects.requireNonNull(companySupplier, "companySupplier");
        this.companyOpenSupplier = Objects.requireNonNull(companyOpenSupplier, "companyOpenSupplier");
        this.persister = Objects.requireNonNull(persister, "persister");
        this.ledgerQueryService = new LedgerTransactionQueryService(companySupplier, companyOpenSupplier);
    }

    public boolean hasOpenCompany()
    {
        return openCompany() != null;
    }

    public List<FundWorkspaceRow> fundRows()
    {
        Company company = openCompany();
        if (company == null)
        {
            return List.of();
        }
        Map<String, BigDecimal> balances = ledgerBalances(company);
        return knownFundNames(company).stream()
            .map(name -> new FundWorkspaceRow(name, balances.getOrDefault(name, BigDecimal.ZERO), !isDeactivated(name)))
            .sorted(Comparator.comparing(FundWorkspaceRow::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public List<Account> transferAccounts()
    {
        Company company = openCompany();
        if (company == null || company.getChartOfAccounts() == null)
        {
            return List.of();
        }
        return company.getChartOfAccounts().getAccounts().stream()
            .filter(account -> account != null && account.getAccountNumber() != null && !account.getAccountNumber().isBlank())
            .sorted(Comparator.comparing(Account::getAccountNumber))
            .toList();
    }

    public List<LedgerTransactionQueryService.LedgerTransactionRow> transactionsForFund(String fundName)
    {
        if (fundName == null || fundName.isBlank())
        {
            return List.of();
        }
        return this.ledgerQueryService.query(new LedgerTransactionQueryService.LedgerTransactionFilter(
            null, null, null, null, null, fundName, null, null));
    }

    public void addFund(String name) throws IOException
    {
        Company company = requireCompany();
        String normalized = requireName(name);
        if (knownFundNames(company).contains(normalized))
        {
            throw new IllegalArgumentException("Fund already exists.");
        }
        AccountingTransaction marker = fundAdminMarker("FUND_ADDED", normalized, "Fund added: " + normalized);
        company.getLedger().getJournal().addTransaction(marker);
        persist();
    }

    public void editFund(String originalName, String newName) throws IOException
    {
        Company company = requireCompany();
        String original = requireName(originalName);
        String replacement = requireName(newName);
        if (!original.equals(replacement) && knownFundNames(company).contains(replacement))
        {
            throw new IllegalArgumentException("Fund already exists.");
        }
        renameLedgerFundReferences(company, original, replacement);
        AccountingTransaction marker = fundAdminMarker("FUND_RENAMED", replacement, "Fund renamed from " + original + " to " + replacement);
        marker.setInfo(Map.of("module", "FUNDS", "action", "FUND_RENAMED", "old_fund", original, "new_fund", replacement));
        company.getLedger().getJournal().addTransaction(marker);
        persist();
    }

    public void deactivateFund(String name) throws IOException
    {
        Company company = requireCompany();
        String fund = requireName(name);
        AccountingTransaction marker = fundAdminMarker("FUND_DEACTIVATED", fund, "Fund deactivated: " + fund);
        company.getLedger().getJournal().addTransaction(marker);
        persist();
    }

    public AccountingTransaction recordRestrictionReclassification(LocalDate date, String memo, String fromFund,
        String toFund, BigDecimal amount, Account account) throws IOException
    {
        Company company = requireCompany();
        String from = requireName(fromFund);
        String to = requireName(toFund);
        if (from.equals(to))
        {
            throw new IllegalArgumentException("Source and destination funds must differ.");
        }
        if (amount == null || amount.signum() <= 0)
        {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (account == null || account.getAccountNumber() == null || account.getAccountNumber().isBlank())
        {
            throw new IllegalArgumentException("Select an account for the reclassification entry.");
        }

        AccountingTransaction tx = new AccountingTransaction();
        tx.setDate((date == null ? LocalDate.now() : date).toString());
        tx.setBookingDateTimestamp(System.currentTimeMillis());
        tx.setMemo(memo == null || memo.isBlank() ? "Fund restriction reclassification" : memo.trim());
        tx.setToFrom("Fund Reclassification");
        tx.setAssociatedFundName(to);
        tx.setInfo(Map.of("module", "FUNDS", "action", "RESTRICTION_RECLASSIFICATION", "from_fund", from, "to_fund", to,
            "movement_type", "NO_BANK_ACCOUNT_MOVEMENT"));

        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        AccountingEntry debit = new AccountingEntry(amount, account.getAccountNumber(), AccountSide.DEBIT, account.getName());
        debit.setFundNumber(to);
        entries.add(debit);
        AccountingEntry credit = new AccountingEntry(amount, account.getAccountNumber(), AccountSide.CREDIT, account.getName());
        credit.setFundNumber(from);
        entries.add(credit);
        tx.setEntries(entries);
        company.getLedger().getJournal().addTransaction(tx);
        persist();
        return tx;
    }

    private Map<String, BigDecimal> ledgerBalances(Company company)
    {
        if (company.getLedger() == null || company.getLedger().getJournal() == null)
        {
            return Map.of();
        }
        return company.getLedger().getJournal().getJournalTransactions().stream()
            .filter(Objects::nonNull)
            .flatMap(tx -> tx.getEntries() == null ? java.util.stream.Stream.empty() : tx.getEntries().stream())
            .filter(entry -> entry.getFundNumber() != null && !entry.getFundNumber().isBlank())
            .collect(Collectors.groupingBy(AccountingEntry::getFundNumber,
                Collectors.mapping(this::signedAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }

    private BigDecimal signedAmount(AccountingEntry entry)
    {
        BigDecimal amount = entry.getAmount() == null ? BigDecimal.ZERO : entry.getAmount();
        return entry.getAccountSide() == AccountSide.DEBIT ? amount : amount.negate();
    }

    private List<String> knownFundNames(Company company)
    {
        java.util.stream.Stream<String> entryFunds = company.getLedger().getJournal().getJournalTransactions().stream()
            .filter(Objects::nonNull)
            .flatMap(tx -> tx.getEntries() == null ? java.util.stream.Stream.empty() : tx.getEntries().stream())
            .map(AccountingEntry::getFundNumber);
        java.util.stream.Stream<String> markerFunds = company.getLedger().getJournal().getJournalTransactions().stream()
            .filter(Objects::nonNull)
            .filter(this::isFundAdminMarker)
            .map(AccountingTransaction::getAssociatedFundName);
        return java.util.stream.Stream.concat(entryFunds, markerFunds)
            .filter(name -> name != null && !name.isBlank())
            .distinct()
            .toList();
    }

    private void renameLedgerFundReferences(Company company, String original, String replacement)
    {
        company.getLedger().getJournal().getJournalTransactions().stream()
            .filter(Objects::nonNull)
            .forEach(tx -> {
                if (original.equals(tx.getAssociatedFundName()))
                {
                    tx.setAssociatedFundName(replacement);
                }
                if (tx.getEntries() != null)
                {
                    tx.getEntries().stream()
                        .filter(entry -> original.equals(entry.getFundNumber()))
                        .forEach(entry -> entry.setFundNumber(replacement));
                }
            });
    }

    private boolean isFundAdminMarker(AccountingTransaction tx)
    {
        return tx.getInfo() != null && "FUNDS".equals(tx.getInfo().get("module"));
    }

    private boolean isDeactivated(String name)
    {
        Company company = openCompany();
        return company != null && company.getLedger().getJournal().getJournalTransactions().stream()
            .anyMatch(tx -> tx != null && tx.getInfo() != null && "FUND_DEACTIVATED".equals(tx.getInfo().get("action"))
                && name.equals(tx.getAssociatedFundName()));
    }

    private AccountingTransaction fundAdminMarker(String action, String fundName, String memo)
    {
        AccountingTransaction marker = new AccountingTransaction();
        marker.setDate(LocalDate.now().toString());
        marker.setBookingDateTimestamp(System.currentTimeMillis());
        marker.setMemo(memo);
        marker.setToFrom("Fund Administration");
        marker.setAssociatedFundName(fundName);
        marker.setInfo(Map.of("module", "FUNDS", "action", action, "fund", fundName));
        return marker;
    }

    private Company openCompany()
    {
        return this.companyOpenSupplier.getAsBoolean() ? this.companySupplier.get() : null;
    }

    private Company requireCompany()
    {
        Company company = openCompany();
        if (company == null || company.getLedger() == null || company.getLedger().getJournal() == null)
        {
            throw new IllegalArgumentException("Open a company before managing funds.");
        }
        return company;
    }

    private String requireName(String name)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Fund name is required.");
        }
        return name.trim();
    }

    private void persist() throws IOException
    {
        this.persister.persist();
    }

    interface Persister
    {
        void persist() throws IOException;
    }

    public record FundWorkspaceRow(String name, BigDecimal ledgerBalance, boolean active) {}
}
