package org.nonprofitbookkeeping.ui;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.records.BudgetRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Service/query calculations for the alternate Budget vs Actual workspace. */
class AlternateBudgetVsActualService
{
    private final Supplier<Company> companySupplier;
    private final BooleanSupplier companyOpenSupplier;
    private final Supplier<List<BudgetRecord>> budgetSupplier;

    AlternateBudgetVsActualService(Supplier<Company> companySupplier, BooleanSupplier companyOpenSupplier,
        Supplier<List<BudgetRecord>> budgetSupplier)
    {
        this.companySupplier = Objects.requireNonNull(companySupplier, "companySupplier");
        this.companyOpenSupplier = Objects.requireNonNull(companyOpenSupplier, "companyOpenSupplier");
        this.budgetSupplier = Objects.requireNonNull(budgetSupplier, "budgetSupplier");
    }

    List<BudgetVsActualRow> calculate(LocalDate from, LocalDate to, String fund)
    {
        Map<String, BigDecimal> budget = new LinkedHashMap<>();
        for (BudgetRecord record : this.budgetSupplier.get())
        {
            if (fund != null && !fund.isBlank() && !fund.equals(record.fundId())) continue;
            for (BudgetRecord.BudgetLineRecord line : record.lines())
            {
                String account = normalize(line.accountId());
                budget.merge(account, nullToZero(line.budgetedAmount()), BigDecimal::add);
            }
        }

        Map<String, BigDecimal> actual = new LinkedHashMap<>();
        Company company = this.companyOpenSupplier.getAsBoolean() ? this.companySupplier.get() : null;
        if (company != null && company.getLedger() != null && company.getLedger().getJournal() != null)
        {
            company.getLedger().getJournal().getJournalTransactions().stream()
                .filter(Objects::nonNull)
                .filter(tx -> within(tx.getDate(), from, to))
                .flatMap(tx -> tx.getEntries() == null ? java.util.stream.Stream.empty() : tx.getEntries().stream())
                .filter(entry -> fund == null || fund.isBlank() || fund.equals(entry.getFundNumber()))
                .forEach(entry -> actual.merge(normalize(entry.getAccountNumber()), signed(entry), BigDecimal::add));
        }

        java.util.LinkedHashSet<String> accounts = new java.util.LinkedHashSet<>();
        accounts.addAll(budget.keySet());
        if (accounts.isEmpty())
        {
            accounts.addAll(actual.keySet());
        }
        return accounts.stream()
            .map(account -> new BudgetVsActualRow(account, budget.getOrDefault(account, BigDecimal.ZERO),
                actual.getOrDefault(account, BigDecimal.ZERO)))
            .toList();
    }

    private boolean within(String date, LocalDate from, LocalDate to)
    {
        try
        {
            LocalDate parsed = date == null || date.isBlank() ? null : LocalDate.parse(date);
            return parsed != null && (from == null || !parsed.isBefore(from)) && (to == null || !parsed.isAfter(to));
        }
        catch (RuntimeException ex) { return false; }
    }

    private BigDecimal signed(AccountingEntry entry)
    {
        BigDecimal amount = nullToZero(entry.getAmount());
        return entry.getAccountSide() == AccountSide.DEBIT ? amount : amount.negate();
    }

    private BigDecimal nullToZero(BigDecimal amount) { return amount == null ? BigDecimal.ZERO : amount; }
    private String normalize(String value) { return value == null || value.isBlank() ? "Unassigned" : value.trim(); }

    record BudgetVsActualRow(String accountId, BigDecimal budget, BigDecimal actual)
    {
        BigDecimal variance() { return this.budget.subtract(this.actual); }
    }
}
