package nonprofitbookkeeping.persistence.sclx;

import nonprofitbookkeeping.TestDatabase;
import nonprofitbookkeeping.model.sclx.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AccountRepositoryTest
{
    @TempDir
    Path tempDir;

    private AccountRepository repository;

    @BeforeEach
    void setUp() throws SQLException
    {
        TestDatabase.reset(tempDir);
        repository = new AccountRepository();
    }

    @Test
    void saveAndLoadDefaultRecord() throws SQLException
    {
        Account account = sampleAccount("1000", "Cash");

        repository.save(account);
        Account loaded = repository.load().orElseThrow();

        assertEquals("1000", loaded.getNumber());
        assertEquals("Cash", loaded.getName());
    }

    @Test
    void saveAndLoadKeyedRecordsAndListAll() throws SQLException
    {
        repository.save("a", sampleAccount("1100", "Checking"));
        repository.save("b", sampleAccount("1200", "Savings"));

        assertEquals("Checking", repository.load("a").orElseThrow().getName());
        assertEquals("Savings", repository.load("b").orElseThrow().getName());

        Map<String, Account> all = repository.loadAll();
        assertEquals(2, all.size());
        assertEquals("Checking", all.get("a").getName());
        assertEquals("Savings", all.get("b").getName());
    }

    @Test
    void deleteByIdRemovesOnlyThatRecord() throws SQLException
    {
        repository.save("a", sampleAccount("1100", "Checking"));
        repository.save("b", sampleAccount("1200", "Savings"));

        repository.delete("a");

        assertTrue(repository.load("a").isEmpty());
        assertTrue(repository.load("b").isPresent());
    }

    @Test
    void blankIdIsRejected()
    {
        assertThrows(IllegalArgumentException.class, () -> repository.save(" ", sampleAccount("1300", "Petty Cash")));
        assertThrows(IllegalArgumentException.class, () -> repository.load(""));
        assertThrows(IllegalArgumentException.class, () -> repository.delete(null));
    }

    private static Account sampleAccount(String number, String name)
    {
        Account account = new Account();
        account.setNumber(number);
        account.setName(name);
        account.setType(Account.AccountType.fromValue("ASSET"));
        account.setIncreaseSide(Account.IncreaseSide.fromValue("DEBIT"));
        account.setOpeningBalance("0.00");
        return account;
    }
}
