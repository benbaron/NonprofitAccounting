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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC-backed implementation that stores transactions using
 * {@link JournalRepository}.
 */
public class JournalLedgerPersistenceGateway implements LedgerPersistenceGateway
{
    private static final Logger LOGGER = Logger.getLogger(JournalLedgerPersistenceGateway.class.getName());

    private final JournalRepository journalRepository;

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
    }

    /**
     * Override @see nonprofitbookkeeping.ui.actions.scaledger.LedgerPersistenceGateway#saveTransactionWithEntries(nonprofitbookkeeping.model.AccountingTransaction) 
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

        try
        {
            ensureAccountsForEntries(transaction);
            if (LOGGER.isLoggable(Level.FINE))
            {
                int entryCount = transaction.getEntries() == null ? 0 : transaction.getEntries().size();
                LOGGER.fine(String.format(
                    "Persisting ledger transaction id=%d date=%s entries=%d",
                    transaction.getId(),
                    transaction.getDate(),
                    entryCount));
            }
            this.journalRepository.upsertTransaction(transaction);
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.fine(String.format("Ledger transaction id=%d persisted", transaction.getId()));
            }
            return transaction;
        }
        catch (SQLException ex)
        {
            LOGGER.log(Level.WARNING, "Failed to persist ledger transaction", ex);
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
            LOGGER.log(Level.WARNING, "Failed to fetch next journal transaction id", ex);
        }
        return 1;
    }

    private void ensureAccountsForEntries(AccountingTransaction transaction) throws SQLException
    {
        Set<AccountingEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty())
        {
            return;
        }

        AccountRepository accountRepository = new AccountRepository();
        List<Account> accounts = accountRepository.listAll();
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

            accountRepository.upsert(placeholder);
            byNumber.put(accountNumber, placeholder);
            if (LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.fine(String.format(
                    "Created placeholder account for ledger entry accountNumber=%s name=%s",
                    accountNumber,
                    entryName));
            }
        }
    }
}
