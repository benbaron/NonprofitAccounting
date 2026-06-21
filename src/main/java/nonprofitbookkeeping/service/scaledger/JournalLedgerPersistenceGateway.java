package nonprofitbookkeeping.service.scaledger;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.AccountRepository;
import nonprofitbookkeeping.persistence.JournalRepository;
import nonprofitbookkeeping.ui.actions.scaledger.LedgerPersistenceGateway;

import java.math.BigDecimal;
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

/**
 * JDBC-backed implementation that stores transactions using
 * {@link JournalRepository}.
 */
public class JournalLedgerPersistenceGateway implements LedgerPersistenceGateway
{
    private static final Logger LOGGER =
        LoggerFactory.getLogger(JournalLedgerPersistenceGateway.class);
    private static final String SCLX_TRANSACTION_ID_KEY = "sclx.transactionId";

    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;

    public JournalLedgerPersistenceGateway()
    {
        this(new JournalRepository());
    }

    public JournalLedgerPersistenceGateway(JournalRepository journalRepository)
    {
        this.journalRepository = journalRepository;
        this.accountRepository = new AccountRepository();
    }

    @Override
    public AccountingTransaction saveTransactionWithEntries(AccountingTransaction transaction)
    {
        if (transaction == null)
        {
            return null;
        }

        removeZeroValueEntries(transaction);
        if (transaction.getEntries() == null || transaction.getEntries().isEmpty())
        {
            LOGGER.info(
                "Skipping non-posting ledger transaction sclxId={} memo='{}': no nonzero entries",
                sclxTransactionId(transaction), transaction.getMemo());
            return transaction;
        }

        reuseStableSclxTransactionId(transaction);
        ensureIdentifiers(transaction);
        ensureEntryBackReferences(transaction);
        ensureAccountsExist(transaction);

        try
        {
            ensureAccountsForEntries(transaction);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                    "Persisting ledger transaction id={} sclxId={} date={} entries={}",
                    transaction.getId(),
                    sclxTransactionId(transaction),
                    transaction.getDate(),
                    transaction.getEntries().size());
            }
            this.journalRepository.upsertTransaction(transaction);
            return transaction;
        }
        catch (SQLException ex)
        {
            LOGGER.warn("Failed to persist ledger transaction", ex);
            throw new IllegalStateException("Failed to persist ledger transaction", ex);
        }
    }

    /**
     * Assigns a stable database identifier.
     *
     * <p>For SCLX imports, the external transaction identifier is already
     * persisted in {@code transaction_info}. Re-importing the same SCLX
     * transaction therefore reuses the original journal transaction id and
     * replaces that transaction's current entries instead of creating a
     * duplicate. Transactions without an SCLX identifier continue to receive
     * a newly reserved id.</p>
     */
    private void removeZeroValueEntries(AccountingTransaction transaction)
    {
        Set<AccountingEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty())
        {
            return;
        }

        LinkedHashSet<AccountingEntry> retained = new LinkedHashSet<>();
        for (AccountingEntry entry : entries)
        {
            if (entry == null || entry.getAmount() == null)
            {
                continue;
            }
            BigDecimal amount = entry.getAmount();
            if (amount.compareTo(BigDecimal.ZERO) == 0)
            {
                LOGGER.info(
                    "Skipping zero-value ledger entry sclxId={} account={} memo='{}'",
                    sclxTransactionId(transaction),
                    entry.getAccountNumber(),
                    transaction.getMemo());
                continue;
            }
            retained.add(entry);
        }
        transaction.setEntries(retained);
    }

    /**
     * Reuses the database transaction id already associated with an SCLX
     * transaction id. This makes an import retry update the same transaction
     * instead of allocating a duplicate id.
     */
    private void reuseStableSclxTransactionId(AccountingTransaction transaction)
    {
        if (transaction.getId() > 0)
        {
            return;
        }
        String sclxId = sclxTransactionId(transaction);
        if (sclxId == null || sclxId.isBlank())
        {
            return;
        }

        String sql = "SELECT txn_id FROM transaction_info "
            + "WHERE k = ? AND v = ? ORDER BY txn_id FETCH FIRST 1 ROWS ONLY";
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, SCLX_TRANSACTION_ID_KEY);
            ps.setString(2, sclxId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    transaction.setId(rs.getInt(1));
                    LOGGER.debug("Reusing journal transaction id={} for sclxId={}",
                        transaction.getId(), sclxId);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException(
                "Failed to resolve stable database id for SCLX transaction " + sclxId,
                ex);
        }
    }

    private String sclxTransactionId(AccountingTransaction transaction)
    {
        if (transaction == null || transaction.getInfo() == null)
        {
            return null;
        }
        return transaction.getInfo().get(SCLX_TRANSACTION_ID_KEY);
    }

    private void ensureIdentifiers(AccountingTransaction transaction)
    {
        if (transaction.getId() > 0)
        {
            return;
        }

        String sclxTransactionId = transaction.getInfo() == null
            ? null
            : transaction.getInfo().get(SCLX_TRANSACTION_ID_KEY);
        Integer existingId = findExistingSclxTransactionId(sclxTransactionId);
        if (existingId != null)
        {
            transaction.setId(existingId);
            LOGGER.debug(
                "Reusing journal transaction id={} for SCLX transactionId={}",
                existingId,
                sclxTransactionId);
            return;
        }

        transaction.setId(reserveNextTransactionId());
    }

    private Integer findExistingSclxTransactionId(String sclxTransactionId)
    {
        if (sclxTransactionId == null || sclxTransactionId.isBlank())
        {
            return null;
        }

        String sql = """
            SELECT txn_id
            FROM transaction_info
            WHERE k = ? AND v = ?
            ORDER BY txn_id
            FETCH FIRST 2 ROWS ONLY
            """;
        try (Connection connection = Database.get().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, SCLX_TRANSACTION_ID_KEY);
            ps.setString(2, sclxTransactionId.trim());
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return null;
                }
                int firstId = rs.getInt(1);
                if (rs.next())
                {
                    LOGGER.warn(
                        "Multiple journal transactions map to SCLX transactionId={}; reusing lowest id={}",
                        sclxTransactionId,
                        firstId);
                }
                return firstId;
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException(
                "Failed to resolve existing SCLX transaction id " + sclxTransactionId,
                ex);
        }
    }

    private int reserveNextTransactionId()
    {
        try
        {
            return this.journalRepository.reserveNextTransactionId();
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Failed to reserve journal transaction id", ex);
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
            if (entry != null)
            {
                entry.setTransaction(transaction);
            }
        }

        if (transaction.getInfo() == null)
        {
            transaction.setInfo(new LinkedHashMap<>());
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
            if (accountNumber != null && !accountNumber.isBlank())
            {
                byNumber.putIfAbsent(accountNumber, entry);
            }
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

        for (Map.Entry<String, AccountingEntry> item : byNumber.entrySet())
        {
            String accountNumber = item.getKey();
            if (existing.contains(accountNumber))
            {
                continue;
            }

            AccountingEntry source = item.getValue();
            Account placeholder = new Account();
            placeholder.setAccountNumber(accountNumber);
            String name = source == null ? null : source.getAccountName();
            placeholder.setName(name == null || name.isBlank() ? accountNumber : name);
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

        String sql = "SELECT account_number FROM account WHERE account_number IN ("
            + placeholders + ")";
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

    private void ensureAccountsForEntries(AccountingTransaction transaction) throws SQLException
    {
        Set<AccountingEntry> entries = transaction.getEntries();
        if (entries == null || entries.isEmpty())
        {
            return;
        }

        List<Account> accounts = this.accountRepository.listAll();
        Map<String, Account> byNumber = new HashMap<>();
        for (Account account : accounts)
        {
            if (account != null && account.getAccountNumber() != null)
            {
                byNumber.put(account.getAccountNumber(), account);
            }
        }

        for (AccountingEntry entry : entries)
        {
            if (entry == null)
            {
                continue;
            }
            String accountNumber = entry.getAccountNumber();
            if (accountNumber == null || accountNumber.isBlank()
                || byNumber.containsKey(accountNumber))
            {
                continue;
            }

            String entryName = entry.getAccountName();
            Account placeholder = new Account();
            placeholder.setAccountNumber(accountNumber);
            placeholder.setName(entryName == null || entryName.isBlank()
                ? accountNumber : entryName);
            if (entry.getAccountSide() != null)
            {
                placeholder.setIncreaseSide(entry.getAccountSide());
            }
            this.accountRepository.upsert(placeholder);
            byNumber.put(accountNumber, placeholder);
        }
    }
}
