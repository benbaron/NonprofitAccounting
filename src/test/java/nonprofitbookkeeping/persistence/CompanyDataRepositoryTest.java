package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyDataRepositoryTest
{
        @TempDir Path tempDir;

        @BeforeEach
        void setUp() throws SQLException
        {
                Path dbFile = this.tempDir.resolve("company-data-test");
                Database.init(dbFile);
                FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
                Database.get().ensureSchema();
        }

        @Test
        void persist_allowsReplacingAccountsWhenJournalHasExistingRows() throws Exception
        {
                CompanyDataRepository repository = new CompanyDataRepository();
                AccountRepository accountRepository = new AccountRepository();

                repository.persist(createCompany("Cash", "Revenue"));

                assertDoesNotThrow(() -> repository.persist(createCompany("Cash - Checking", "Revenue - Donations")),
                        "Persisting a company with updated accounts should not violate referential integrity.");

                List<Account> stored = accountRepository.listAll();
                assertEquals(2, stored.size(), "Expected two accounts after replacement");
                assertTrue(stored.stream().anyMatch(a -> "Cash - Checking".equals(a.getName())));
                assertTrue(stored.stream().anyMatch(a -> "Revenue - Donations".equals(a.getName())));
        }

        private static Company createCompany(String cashName, String revenueName)
        {
                Company company = new Company();
                company.getCompanyProfileModel().setCompanyName("Test Co");

                Account cash = new Account();
                cash.setAccountNumber("1000");
                cash.setName(cashName);
                cash.setIncreaseSide(AccountSide.DEBIT);
                cash.setAccountType(AccountType.ASSET);

                Account revenue = new Account();
                revenue.setAccountNumber("4000");
                revenue.setName(revenueName);
                revenue.setIncreaseSide(AccountSide.CREDIT);
                revenue.setAccountType(AccountType.INCOME);

                company.getChartOfAccounts().addAccount(cash);
                company.getChartOfAccounts().addAccount(revenue);

                AccountingTransaction txn = new AccountingTransaction();
                txn.setId(1);
                txn.setBookingDateTimestamp(1L);
                txn.setDate("2024-01-01");
                txn.setMemo("Seed transaction");

                Set<AccountingEntry> entries = new LinkedHashSet<>();
                entries.add(new AccountingEntry(new BigDecimal("100.00"), cash.getAccountNumber(),
                        AccountSide.DEBIT, cashName));
                entries.add(new AccountingEntry(new BigDecimal("100.00"), revenue.getAccountNumber(),
                        AccountSide.CREDIT, revenueName));
                txn.setEntries(entries);

                company.getLedger().getJournal().replaceAllTransactions(List.of(txn));
                return company;
        }
}
