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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private final AccountRepository accountRepository;

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
            this.journalRepository.upsertTransaction(transaction);
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
            LOGGER.log(Level.WARNING, "Failed to fetch next journal transaction id", ex);
        }
        return 1;
    }
}
