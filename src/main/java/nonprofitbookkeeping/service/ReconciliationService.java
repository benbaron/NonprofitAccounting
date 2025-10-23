
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.util.FormatUtils;

/**
 * Service class providing functionalities for account reconciliation.
 * This includes fetching unreconciled entries, marking entries as reconciled,
 * listing accounts eligible for reconciliation, and performing the reconciliation process.
 * Note: Several methods in this class are currently stub implementations.
 */
public class ReconciliationService
{
       private static final Logger LOGGER = Logger.getLogger(ReconciliationService.class.getName());

       private static final EnumSet<AccountType> RECONCILABLE_TYPES = EnumSet.of(AccountType.ASSET,
               AccountType.BANK,
               AccountType.CASH,
               AccountType.CHECKING,
               AccountType.CREDITCARD,
               AccountType.MONEYMKRT,
               AccountType.INVEST,
               AccountType.SIMPLEINVEST);

       private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

       /**
        * Guards access to the shared reconciliation state. All mutating
        * operations should synchronize on this lock to avoid race conditions
        * when UI code and background imports both add transactions.
        */
       private static final Object LOCK = new Object();

       /** Transactions queued for reconciliation. */
       private final List<AccountingTransaction> pendingTransactions = new ArrayList<>();

       /**
        * In-memory store of unreconciled transactions keyed by a human friendly
        * account identifier (preferably the account name).
        */
       private static final Map<String, List<AccountingTransaction>> UNRECONCILED = new LinkedHashMap<>();

       /**
        * Maps the display identifier used in {@link #UNRECONCILED} back to the
        * canonical account number. This allows callers to query using either
        * the name or the number and keeps lookups stable even if duplicate
        * names exist.
        */
       private static final Map<String, String> ACCOUNT_NUMBERS = new LinkedHashMap<>();

       /** Tracks the company instance last used to populate {@link #UNRECONCILED}. */
        private static Company loadedCompany;

       /** Hash of the ledger transactions when {@link #loadedCompany} was cached. */
        private static int loadedLedgerHash;
	
        /**
         * Clears all cached reconciliation state. Primarily intended for tests
         * and scenarios where the active company changes.
         */
        public static void reset()
        {
                synchronized (LOCK)
                {
                        resetLocked();
                }
        }

        /**
         * Fetches unreconciled entries for a specific account within a given date range.
         *
         * @param accountIdentifier The display name or account number to fetch entries for.
         * @param from              Inclusive lower bound in ISO-8601 format (yyyy-MM-dd). May be {@code null}.
         * @param to                Inclusive upper bound in ISO-8601 format (yyyy-MM-dd). May be {@code null}.
         * @return rows describing each unreconciled entry. Columns are transaction id,
         *         booking date, formatted amount, and description.
         */
        public static List<String[]> getUnreconciledEntries(String accountIdentifier, String from, String to)
        {
                ensureLoaded();

                LocalDate fromDate = parseDate(from);
                LocalDate toDate = parseDate(to);

                List<AccountingTransaction> transactions = getUnreconciled(accountIdentifier);
                if (transactions.isEmpty())
                {
                        return List.of();
                }

                List<String[]> rows = new ArrayList<>();
                for (AccountingTransaction tx : transactions)
                {
                        if (tx == null)
                        {
                                continue;
                        }

                        LocalDate txDate = transactionDate(tx);
                        if (!withinRange(txDate, fromDate, toDate))
                        {
                                continue;
                        }

                        BigDecimal amount = entryAmount(tx, accountIdentifier);
                        String formattedAmount = FormatUtils.formatCurrency(amount);
                        String dateText = txDate != null ? txDate.toString() : "";
                        String description = Optional.ofNullable(tx.getDescription()).orElse("");
                        String id = Optional.ofNullable(tx.getBookingDateTimestamp()).map(String::valueOf).orElse("-");

                        rows.add(new String[] { id, dateText, formattedAmount, description });
                }

                return rows;
        }

        /**
         * Marks a specific financial entry (transaction) as reconciled using its transaction ID.
         *
         * @param txnId The unique identifier (ID) of the transaction to be marked as reconciled.
         * @return {@code true} if a matching transaction was removed, {@code false} otherwise.
         */
        public static boolean reconcileEntry(Long txnId)
        {
                if (txnId == null)
                {
                        return false;
                }

                ensureLoaded();

                boolean removed = false;
                synchronized (LOCK)
                {
                        for (List<AccountingTransaction> list : UNRECONCILED.values())
                        {
                                if (list.removeIf(tx -> matchesId(tx, txnId)))
                                {
                                        removed = true;
                                }
                        }

                        UNRECONCILED.entrySet().removeIf(e -> e.getValue().isEmpty());
                        ACCOUNT_NUMBERS.entrySet().removeIf(e -> !UNRECONCILED.containsKey(e.getKey()));
                }

                if (removed)
                {
                        LOGGER.fine(() -> "Reconciled transaction ID: " + txnId);
                }

                return removed;
        }

        /**
         * Retrieves a list of unreconciled accounting transactions for the supplied account identifier.
         * The identifier can be either the display name or the canonical account number.
         */
        public static List<AccountingTransaction> getUnreconciled(String accountIdentifier)
        {
                if (accountIdentifier == null || accountIdentifier.isBlank())
                {
                        return List.of();
                }

                ensureLoaded();

                synchronized (LOCK)
                {
                        String key = resolveKey(accountIdentifier);
                        if (key == null)
                        {
                                return List.of();
                        }

                        List<AccountingTransaction> list = UNRECONCILED.get(key);
                        if (list == null || list.isEmpty())
                        {
                                return List.of();
                        }

                        return new ArrayList<>(list);
                }
        }

        /**
         * Lists accounts that are eligible for reconciliation.
         */
        public static List<String> listReconcilableAccounts()
        {
                ensureLoaded();

                synchronized (LOCK)
                {
                        if (UNRECONCILED.isEmpty())
                        {
                                return List.of();
                        }

                        return UNRECONCILED.keySet().stream()
                                .sorted(String::compareToIgnoreCase)
                                .collect(Collectors.toCollection(ArrayList::new));
                }
        }

        /**
         * Performs the reconciliation process for a given account.
         */
        public void reconcile(String accountIdentifier, String statementDate, BigDecimal endingBalance,
                List<Long> clearedIds)
        {
                if (accountIdentifier == null || accountIdentifier.isBlank() || clearedIds == null
                        || clearedIds.isEmpty())
                {
                        return;
                }

                ensureLoaded();

                synchronized (LOCK)
                {
                        String key = resolveKey(accountIdentifier);
                        if (key == null)
                        {
                                return;
                        }

                        List<AccountingTransaction> list = UNRECONCILED.get(key);
                        if (list == null)
                        {
                                return;
                        }

                        list.removeIf(tx -> tx != null
                                && tx.getBookingDateTimestamp() != null
                                && clearedIds.contains(tx.getBookingDateTimestamp()));

                        if (list.isEmpty())
                        {
                                UNRECONCILED.remove(key);
                                ACCOUNT_NUMBERS.remove(key);
                        }
                }

                this.pendingTransactions.clear();
        }

        /**
         * Adds a transaction to the reconciliation map. The transaction is bucketed
         * by each reconcilable account that participates in it.
         */
        public void addTransactionToReconcile(AccountingTransaction transaction)
        {
                if (transaction == null)
                {
                        return;
                }

                ensureLoaded();

                synchronized (LOCK)
                {
                        Company company = CurrentCompany.getCompany();
                        addTransactionInternal(company, transaction);
                }

                this.pendingTransactions.add(transaction);
        }

        /**
         * Ensures the unreconciled map is loaded from the current company's ledger.
         */
        private static void ensureLoaded()
        {
                Company company = CurrentCompany.getCompany();

                synchronized (LOCK)
                {
                        if (company == null || company.getLedger() == null)
                        {
                                resetLocked();
                                return;
                        }

                        List<AccountingTransaction> transactions = company.getLedger().getTransactions();
                        int ledgerHash = ledgerHash(transactions);

                        if (company == loadedCompany && ledgerHash == loadedLedgerHash && !UNRECONCILED.isEmpty())
                        {
                                return;
                        }

                        resetLocked();
                        loadedCompany = company;
                        loadedLedgerHash = ledgerHash;

                        if (transactions == null)
                        {
                                return;
                        }

                        for (AccountingTransaction tx : transactions)
                        {
                                addTransactionInternal(company, tx);
                        }
                }
        }

        private static void resetLocked()
        {
                UNRECONCILED.clear();
                ACCOUNT_NUMBERS.clear();
                loadedCompany = null;
                loadedLedgerHash = 0;
        }

        private static int ledgerHash(List<AccountingTransaction> transactions)
        {
                if (transactions == null || transactions.isEmpty())
                {
                        return 0;
                }

                int hash = 1;
                for (AccountingTransaction tx : transactions)
                {
                        long id = tx != null && tx.getBookingDateTimestamp() != null ? tx.getBookingDateTimestamp() : 0L;
                        hash = 31 * hash + Long.hashCode(id);
                }
                return hash;
        }

        private static void addTransactionInternal(Company company, AccountingTransaction transaction)
        {
                if (transaction == null || transaction.getEntries() == null || transaction.getEntries().isEmpty())
                {
                        return;
                }

                Map<String, String> keys = accountKeysForTransaction(company, transaction);
                if (keys.isEmpty())
                {
                        return;
                }

                for (Map.Entry<String, String> entry : keys.entrySet())
                {
                        String key = entry.getKey();
                        if (key == null || key.isBlank())
                        {
                                continue;
                        }

                        ACCOUNT_NUMBERS.putIfAbsent(key, entry.getValue());
                        List<AccountingTransaction> list = UNRECONCILED.computeIfAbsent(key, k -> new ArrayList<>());

                        if (list.stream().noneMatch(existing -> sameTransaction(existing, transaction)))
                        {
                                list.add(transaction);
                        }
                }
        }

        private static Map<String, String> accountKeysForTransaction(Company company, AccountingTransaction transaction)
        {
                Map<String, String> result = new LinkedHashMap<>();
                if (transaction == null || transaction.getEntries() == null)
                {
                        return result;
                }

                ChartOfAccounts chart = company != null ? company.getChartOfAccounts() : null;

                for (AccountingEntry entry : transaction.getEntries())
                {
                        if (entry == null)
                        {
                                continue;
                        }

                        String accountNumber = entry.getAccountNumber();
                        Account account = chart != null ? chart.getAccount(accountNumber) : null;
                        AccountType type = account != null ? account.getAccountType() : null;

                        boolean reconcilable = type != null && RECONCILABLE_TYPES.contains(type);

                        if (!reconcilable)
                        {
                                if (account == null && accountNumber != null && !accountNumber.isBlank())
                                {
                                        reconcilable = true;
                                }
                                else
                                {
                                        continue;
                                }
                        }

                        String key = accountKey(account, entry);
                        if (key == null || key.isBlank())
                        {
                                continue;
                        }

                        if (!result.containsKey(key))
                        {
                                result.put(key, accountNumber);
                        }
                }

                return result;
        }

        private static String accountKey(Account account, AccountingEntry entry)
        {
                if (account != null)
                {
                        String display = displayName(account);
                        if (display != null && !display.isBlank())
                        {
                                return display;
                        }
                }

                if (entry != null)
                {
                        if (entry.getAccountName() != null && !entry.getAccountName().isBlank())
                        {
                                return entry.getAccountName();
                        }

                        if (entry.getAccountNumber() != null && !entry.getAccountNumber().isBlank())
                        {
                                return entry.getAccountNumber();
                        }
                }

                return null;
        }

        private static String displayName(Account account)
        {
                if (account == null)
                {
                        return null;
                }

                if (account.getName() != null && !account.getName().isBlank())
                {
                        return account.getName();
                }

                return account.getAccountNumber();
        }

        private static boolean sameTransaction(AccountingTransaction left, AccountingTransaction right)
        {
                if (left == right)
                {
                        return true;
                }

                if (left == null || right == null)
                {
                        return false;
                }

                Long leftId = left.getBookingDateTimestamp();
                Long rightId = right.getBookingDateTimestamp();

                if (leftId != null && rightId != null)
                {
                        return leftId.equals(rightId);
                }

                return false;
        }

        private static boolean matchesId(AccountingTransaction tx, Long txnId)
        {
                if (tx == null || txnId == null)
                {
                        return false;
                }

                Long id = tx.getBookingDateTimestamp();
                return id != null && id.equals(txnId);
        }

        private static String resolveKey(String identifier)
        {
                if (identifier == null)
                {
                        return null;
                }

                if (UNRECONCILED.containsKey(identifier))
                {
                        return identifier;
                }

                for (Map.Entry<String, String> entry : ACCOUNT_NUMBERS.entrySet())
                {
                        if (identifier.equals(entry.getValue()))
                        {
                                return entry.getKey();
                        }
                }

                return null;
        }

        private static String resolveAccountNumber(String identifier)
        {
                if (identifier == null)
                {
                        return null;
                }

                String number = ACCOUNT_NUMBERS.get(identifier);
                if (number != null)
                {
                        return number;
                }

                for (String value : ACCOUNT_NUMBERS.values())
                {
                        if (identifier.equals(value))
                        {
                                return value;
                        }
                }

                return identifier;
        }

        private static BigDecimal entryAmount(AccountingTransaction tx, String accountIdentifier)
        {
                if (tx == null || tx.getEntries() == null)
                {
                        return BigDecimal.ZERO;
                }

                String accountNumber = resolveAccountNumber(accountIdentifier);
                BigDecimal total = BigDecimal.ZERO;

                for (AccountingEntry entry : tx.getEntries())
                {
                        if (entryMatchesKey(entry, accountIdentifier, accountNumber))
                        {
                                BigDecimal amount = entry.getAmount();
                                if (amount == null)
                                {
                                        continue;
                                }

                                switch (entry.getAccountSide())
                                {
                                case CREDIT:
                                        total = total.subtract(amount);
                                        break;
                                case DEBIT:
                                        total = total.add(amount);
                                        break;
                                default:
                                        break;
                                }
                        }
                }

                return total;
        }

        private static boolean entryMatchesKey(AccountingEntry entry, String identifier, String accountNumber)
        {
                if (entry == null)
                {
                        return false;
                }

                if (accountNumber != null && accountNumber.equals(entry.getAccountNumber()))
                {
                        return true;
                }

                if (identifier == null)
                {
                        return false;
                }

                if (identifier.equals(entry.getAccountNumber()))
                {
                        return true;
                }

                return identifier.equals(entry.getAccountName());
        }

        private static LocalDate transactionDate(AccountingTransaction tx)
        {
                if (tx == null)
                {
                        return null;
                }

                String date = tx.getDate();
                if (date != null && !date.isBlank())
                {
                        try
                        {
                                return LocalDate.parse(date, ISO_DATE);
                        }
                        catch (Exception ex)
                        {
                                // Fall back to timestamp
                        }
                }

                Long timestamp = tx.getBookingDateTimestamp();
                if (timestamp == null || timestamp == 0L)
                {
                        return null;
                }

                return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        }

        private static LocalDate parseDate(String value)
        {
                if (value == null || value.isBlank())
                {
                        return null;
                }

                try
                {
                        return LocalDate.parse(value, ISO_DATE);
                }
                catch (Exception ex)
                {
                        return null;
                }
        }

        private static boolean withinRange(LocalDate date, LocalDate from, LocalDate to)
        {
                if (date == null)
                {
                        return true;
                }

                if (from != null && date.isBefore(from))
                {
                        return false;
                }

                if (to != null && date.isAfter(to))
                {
                        return false;
                }

                return true;
        }
	
}
