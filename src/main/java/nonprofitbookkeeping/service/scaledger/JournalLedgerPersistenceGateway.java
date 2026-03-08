package nonprofitbookkeeping.service.scaledger;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.JournalRepository;
import nonprofitbookkeeping.ui.actions.scaledger.LedgerPersistenceGateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import java.util.HashSet;
import java.util.LinkedHashMap;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * JDBC-backed implementation that stores transactions using
 * {@link JournalRepository}.
 */
public class JournalLedgerPersistenceGateway implements LedgerPersistenceGateway
{
    
    /** The Constant LOGGER. */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(JournalLedgerPersistenceGateway.class);

    /** The journal repository. */
    private final JournalRepository journalRepository;
    
    /** The account repository. */
    private final AccountRepository accountRepository;

    /**
     * Instantiates a new journal ledger persistence gateway.
     */
    public JournalLedgerPersistenceGateway()
    {
        this(new JournalRepository());
    }

    /**
     * Instantiates a new journal ledger persistence gateway.
     *
     * @param journalRepository the journal repository
     */
    public JournalLedgerPersistenceGateway(JournalRepository journalRepository)
    {
        this.journalRepository = journalRepository;
        this.accountRepository = new AccountRepository();
    }

    /**
     * Override @see nonprofitbookkeeping.ui.actions.scaledger.LedgerPersistenceGateway#saveTransactionWithEntries(nonprofitbookkeeping.model.AccountingTransaction)
     *
     * @param transaction the transaction
     * @return the accounting transaction
     */
    @Override
    public AccountingTransaction saveTransactionWithEntries(AccountingTransaction transaction)
    {
        if (transaction == null)
        {
            return null;
        }

        ensureIdentifiers(transaction);
        ensureEntryBackReferences(transaction);
        ensureAccountsExist(transaction);

        try
        {
            ensureAccountsForEntries(transaction);
            if (LOGGER.isDebugEnabled())
            {
                int entryCount = transaction.getEntries() == null ? 0 : transaction.getEntries().size();
                LOGGER.debug(
                    "Persisting ledger transaction id={} date={} entries={}",
                    transaction.getId(),
                    transaction.getDate(),
                    entryCount);
            }
            this.journalRepository.upsertTransaction(transaction);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Ledger transaction id={} persisted",
                    transaction.getId());
            }
            return transaction;
        }
        catch (SQLException ex)
        {
            LOGGER.warn("Failed to persist ledger transaction", ex);
            throw new IllegalStateException("Failed to persist ledger transaction", ex);
        }
    }

    /**
     * Ensure identifiers.
     *
     * @param transaction the transaction
     */
    private void ensureIdentifiers(AccountingTransaction transaction)
    {
        if (transaction.getId() <= 0)
        {
            transaction.setId(fetchNextTransactionId());
        }
    }

    /**
     * Ensure entry back references.
     *
     * @param transaction the transaction
     */
    private void ensureEntryBackReferences(AccountingTransaction transaction)
    {
        Set<AccountingEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty())
        {
            return;
        }

        if (!(entries instanceof LinkedHashSet))
        {
            transaction.setEntries(new LinkedHashSet<>(entries));
            entries = transaction.getEntries();
        }

        for (AccountingEntry entry : entries)
        {
            if (entry == null)
            {
                continue;
            }
            entry.setTransaction(transaction);
        }

        Map<String, String> info = transaction.getInfo();
        if (info == null)
        {
            transaction.setInfo(new java.util.LinkedHashMap<>());
        }
    }

    /**
     * Ensure accounts exist.
     *
     * @param transaction the transaction
     */
    private void ensureAccountsExist(AccountingTransaction transaction)
    {
        Set<AccountingEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty())
        {
            return;
        }

        Map<String, AccountingEntry> byNumber = new LinkedHashMap<>();
        for (AccountingEntry entry : entries)
        {
            if (entry == null)
            {
                continue;
            }

            String accountNumber = entry.getAccountNumber();
            if (accountNumber == null || accountNumber.isBlank())
            {
                continue;
            }

            byNumber.putIfAbsent(accountNumber, entry);
        }

        if (byNumber.isEmpty())
        {
            return;
        }

        Set<String> existing;
        try
        {
            existing = fetchExistingAccountNumbers(byNumber.keySet());
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to query existing accounts", ex);
        }

        for (Map.Entry<String, AccountingEntry> entry : byNumber.entrySet())
        {
            String accountNumber = entry.getKey();
            if (existing.contains(accountNumber))
            {
                continue;
            }

            AccountingEntry source = entry.getValue();
            Account placeholder = new Account();
            placeholder.setAccountNumber(accountNumber);

            String name = source == null ? null : source.getAccountName();
            if (name == null || name.isBlank())
            {
                name = accountNumber;
            }
            placeholder.setName(name);

            if (source != null && source.getAccountSide() != null)
            {
                placeholder.setIncreaseSide(source.getAccountSide());
            }

            try
            {
                this.accountRepository.upsert(placeholder);
            }
            catch (SQLException ex)
            {
                throw new IllegalStateException(
                    "Failed to create placeholder account for " + accountNumber, ex);
            }
        }
    }

    /**
     * Fetch existing account numbers.
     *
     * @param accountNumbers the account numbers
     * @return the sets the
     * @throws SQLException the SQL exception
     */
    private Set<String> fetchExistingAccountNumbers(Set<String> accountNumbers) throws SQLException
    {
        if (accountNumbers == null || accountNumbers.isEmpty())
        {
            return java.util.Collections.emptySet();
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < accountNumbers.size(); i++)
        {
            if (i > 0)
            {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        String sql = "SELECT account_number FROM account WHERE account_number IN (" + placeholders + ")";

        Set<String> existing = new HashSet<>();
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            int index = 0;
            for (String number : accountNumbers)
            {
                ps.setString(++index, number);
            }

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    existing.add(rs.getString(1));
                }
            }
        }

        return existing;
    }

    /**
     * Fetch next transaction id.
     *
     * @return the int
     */
    private int fetchNextTransactionId()
    {
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COALESCE(MAX(id), 0) + 1 FROM journal_transaction");
             ResultSet rs = ps.executeQuery())
        {
            if (rs.next())
            {
                return rs.getInt(1);
            }
        }
        catch (SQLException ex)
        {
            LOGGER.warn("Failed to fetch next journal transaction id", ex);
        }
        return 1;
    }

    /**
     * Ensure accounts for entries.
     *
     * @param transaction the transaction
     * @throws SQLException the SQL exception
     */
    private void ensureAccountsForEntries(AccountingTransaction transaction) throws SQLException
    {
        Set<AccountingEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty())
        {
            return;
        }

        AccountRepository accountRepository1 = new AccountRepository();
        List<Account> accounts = accountRepository1.listAll();
        Map<String, Account> byNumber = new HashMap<>();

        for (Account account : accounts)
        {
            if (account == null || account.getAccountNumber() == null)
            {
                continue;
            }
            byNumber.put(account.getAccountNumber(), account);
        }

        for (AccountingEntry entry : entries)
        {
            if (entry == null)
            {
                continue;
            }

            String accountNumber = entry.getAccountNumber();
            if (accountNumber == null || accountNumber.isBlank())
            {
                continue;
            }

            if (byNumber.containsKey(accountNumber))
            {
                continue;
            }

            String entryName = entry.getAccountName();
            Account placeholder = new Account();
            placeholder.setAccountNumber(accountNumber);
            if (entryName == null || entryName.isBlank())
            {
                entryName = accountNumber;
            }
            placeholder.setName(entryName);
            if (entry.getAccountSide() != null)
            {
                placeholder.setIncreaseSide(entry.getAccountSide());
            }

            accountRepository1.upsert(placeholder);
            byNumber.put(accountNumber, placeholder);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                    "Created placeholder account for ledger entry accountNumber={} name={}",
                    accountNumber,
                    entryName);
            }
        }
    }
}
