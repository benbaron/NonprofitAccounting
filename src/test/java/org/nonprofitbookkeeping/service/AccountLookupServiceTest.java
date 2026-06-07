package org.nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nonprofitbookkeeping.model.Account;
import org.nonprofitbookkeeping.persistence.Jpa;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountLookupServiceTest
{
    @TempDir
    Path tempDir;

    private Jpa jpa;

    @AfterEach
    void tearDown()
    {
        if (jpa != null)
        {
            jpa.close();
        }
    }

    @Test
    void listActivePostingAccounts_filtersAndSortsByCode() throws Exception
    {
        Database.init(tempDir.resolve("account-lookup"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();

        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            st.execute("INSERT INTO chart_of_accounts(name, version, status) VALUES ('TEST','v1','ACTIVE')");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('2000', 1, '2000', 'Inactive posting', 'LIABILITY', 'PAYABLE', 0, 'CREDIT', TRUE, FALSE)");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('1000', 1, '1000', 'Active posting A', 'ASSET', 'CASH', 0, 'DEBIT', TRUE, TRUE)");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('1500', 1, '1500', 'Active non-posting', 'ASSET', 'RECEIVABLE', 0, 'DEBIT', FALSE, TRUE)");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('1100', 1, '1100', 'Active posting B', 'ASSET', 'CASH', 0, 'DEBIT', TRUE, TRUE)");
        }

        jpa = new Jpa();
        AccountLookupService service = new AccountLookupService(jpa);

        List<Account> accounts = service.listActivePostingAccounts();
        assertEquals(List.of("1000", "1100"), accounts.stream().map(Account::getCode).toList());
    }
}
