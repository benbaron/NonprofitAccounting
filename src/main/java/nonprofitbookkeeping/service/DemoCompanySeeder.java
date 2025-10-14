package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CompanyProfileModel;
import nonprofitbookkeeping.model.Ledger;

/**
 * Populates a {@link Company} aggregate with a small but realistic chart of
 * accounts and a handful of ledger transactions so that freshly created
 * databases have something to explore. The intent is to support demos and
 * onboarding flows for the H2-backed persistence model.
 */
public final class DemoCompanySeeder
{
        private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;

        /**
         * Populates the supplied company with demo data. Existing ledgers and
         * charts of accounts are replaced.
         *
         * @param company aggregate to seed
         */
        public void seed(Company company)
        {
                Objects.requireNonNull(company, "company");

                seedProfile(company.getCompanyProfileModel());
                company.setChartOfAccounts(buildChartOfAccounts());
                company.setLedger(buildLedger());
        }

        private static void seedProfile(CompanyProfileModel profile)
        {
                if (profile == null)
                {
                        return;
                }

                if (profile.getCompanyName() == null || profile.getCompanyName().isBlank())
                {
                        profile.setCompanyName("Sample Nonprofit");
                }

                profile.setBaseCurrency(profile.getBaseCurrency() == null ? "USD"
                        : profile.getBaseCurrency());
                profile.setDefaultBankAccount(profile.getDefaultBankAccount() == null
                        ? "Operating Cash"
                        : profile.getDefaultBankAccount());
                profile.setFiscalYearStart(profile.getFiscalYearStart() == null
                        ? LocalDate.of(LocalDate.now().getYear(), 1, 1).toString()
                        : profile.getFiscalYearStart());
        }

        private static ChartOfAccounts buildChartOfAccounts()
        {
                ChartOfAccounts chart = new ChartOfAccounts();

                Account cash = new Account("1000", "Operating Cash", AccountSide.DEBIT);
                cash.setAccountType(AccountType.BANK);
                cash.setCurrency("USD");

                Account receivable = new Account("1200", "Grants Receivable", AccountSide.DEBIT);
                receivable.setAccountType(AccountType.ASSET);
                receivable.setCurrency("USD");

                Account equity = new Account("3000", "Unrestricted Net Assets", AccountSide.CREDIT);
                equity.setAccountType(AccountType.EQUITY);

                Account income = new Account("4000", "Individual Contributions", AccountSide.CREDIT);
                income.setAccountType(AccountType.INCOME);

                Account grantsIncome = new Account("4100", "Grant Income", AccountSide.CREDIT);
                grantsIncome.setAccountType(AccountType.INCOME);

                Account supplies = new Account("5100", "Program Supplies", AccountSide.DEBIT);
                supplies.setAccountType(AccountType.EXPENSE);

                chart.addAccount(cash);
                chart.addAccount(receivable);
                chart.addAccount(equity);
                chart.addAccount(income);
                chart.addAccount(grantsIncome);
                chart.addAccount(supplies);

                return chart;
        }

        private static Ledger buildLedger()
        {
                Ledger ledger = new Ledger();

                addTransaction(ledger, 1, LocalDate.now().minusDays(45), "Opening equity",
                        entry("1000", "Operating Cash", AccountSide.DEBIT, 5000),
                        entry("3000", "Unrestricted Net Assets", AccountSide.CREDIT, 5000));

                addTransaction(ledger, 2, LocalDate.now().minusDays(20), "Community donation",
                        entry("1000", "Operating Cash", AccountSide.DEBIT, 2500),
                        entry("4000", "Individual Contributions", AccountSide.CREDIT, 2500));

                addTransaction(ledger, 3, LocalDate.now().minusDays(10), "Grant award",
                        entry("1200", "Grants Receivable", AccountSide.DEBIT, 8000),
                        entry("4100", "Grant Income", AccountSide.CREDIT, 8000));

                addTransaction(ledger, 4, LocalDate.now().minusDays(5), "Supplies purchase",
                        entry("5100", "Program Supplies", AccountSide.DEBIT, 350),
                        entry("1000", "Operating Cash", AccountSide.CREDIT, 350));

                return ledger;
        }

        private static void addTransaction(Ledger ledger,
                int id,
                LocalDate date,
                String memo,
                AccountingEntry... entries)
        {
                AccountingTransaction txn = new AccountingTransaction();
                txn.setId(id);
                txn.setBookingDateTimestamp(Timestamp.valueOf(date.atStartOfDay()).getTime());
                txn.setDate(ISO_DATE.format(date));
                txn.setMemo(memo);

                Set<AccountingEntry> entrySet = new HashSet<>();

                for (AccountingEntry entry : entries)
                {
                        entrySet.add(entry);
                }

                txn.setEntries(entrySet);
                ledger.getJournal().addTransaction(txn);
        }

        private static AccountingEntry entry(String accountNumber,
                String accountName,
                AccountSide side,
                double amount)
        {
                AccountingEntry entry = new AccountingEntry(BigDecimal.valueOf(amount), accountNumber, side,
                        accountName);
                return entry;
        }
}
