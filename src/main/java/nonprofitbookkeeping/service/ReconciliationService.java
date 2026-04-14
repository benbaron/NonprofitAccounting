package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Collectors;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.util.FormatUtils;

/**
 * Service class providing functionalities for account reconciliation.
 * This includes fetching unreconciled entries, marking entries as reconciled,
 * listing accounts eligible for reconciliation, and performing the reconciliation process.
 */
public class ReconciliationService
{
        private static final Logger LOGGER =
                LoggerFactory.getLogger(ReconciliationService.class);

        /** Marker stored in {@link AccountingTransaction#setClearBank(String)} when a transaction is cleared. */
        private static final String CLEARED_FLAG = "CLEARED";

        /** Account types that can be reconciled against external statements. */
        private static final Set<AccountType> RECONCILABLE_TYPES = EnumSet.of(
                AccountType.BANK,
                AccountType.CASH,
                AccountType.CHECKING,
                AccountType.CREDIT,
                AccountType.CREDITCARD,
                AccountType.MONEYMKRT,
                AccountType.INVEST,
                AccountType.SIMPLEINVEST,
                AccountType.MUTUAL);

        /** Guard object for all access to {@link #UNRECONCILED}. */
        private static final Object LOCK = new Object();

        /** Transactions queued for reconciliation during the current session. */
        private final List<AccountingTransaction> pendingTransactions = new ArrayList<>();

        /** Last company whose ledger was loaded into {@link #UNRECONCILED}. */
        private static Company lastLoadedCompany;

        /**
         * In-memory store of unreconciled transactions keyed by account number.
         * This is populated lazily from the current company's ledger or via
         * {@link #addTransactionToReconcile(AccountingTransaction)}.
         */
        private static final Map<String, List<AccountingTransaction>> UNRECONCILED = new HashMap<>();

        /**
         * Fetches unreconciled entries for a specific account within a given date range.
         *
         * @param account the account identifier to fetch unreconciled entries for
         * @param from start date (inclusive) in ISO-8601 format or {@code null}
         * @param to end date (inclusive) in ISO-8601 format or {@code null}
         * @return A {@link List} of {@code String[]} where each array represents an unreconciled entry's details
         */
        public static List<String[]> getUnreconciledEntries(String account, String from, String to)
        {
                if (account == null || account.isBlank())
                {
                        return List.of();
                }

                LocalDate fromDate = parseDate(from);
                LocalDate toDate = parseDate(to);

                return getUnreconciled(account).stream()
                        .filter(tx -> withinRange(tx, fromDate, toDate))
                        .map(tx -> new String[]
                        {
                                safe(tx.getDate()),
                                safe(tx.getMemo()),
                                FormatUtils.formatCurrency(defaultAmount(tx)),
                                clearedLabel(tx)
                        })
                        .collect(Collectors.toList());
        }

        /**
         * Marks a specific financial entry (transaction) as reconciled using its transaction ID.
         *
         * @param txnId The unique identifier (booking timestamp) of the transaction to be marked as reconciled
         * @return {@code true} if a matching transaction was found and marked cleared, {@code false} otherwise
         */
        public static boolean reconcileEntry(Long txnId)
        {
                if (txnId == null)
                {
                        return false;
                }

                synchronized (LOCK)
                {
                        ensureLoaded();

                        for (List<AccountingTransaction> list : UNRECONCILED.values())
                        {
                                for (int i = 0; i < list.size(); i++)
                                {
                                        AccountingTransaction tx = list.get(i);

                                        if (Objects.equals(tx.getBookingDateTimestamp(), txnId))
                                        {
                                                markCleared(tx, null);
                                                list.remove(i);
                                                return true;
                                        }
                                }
                        }
                }

                return false;
        }

        /**
         * Retrieves a list of unreconciled accounting transactions for the specified account.
         *
         * @param value account identifier
         * @return immutable list of unreconciled transactions for the account
         */
        public static List<AccountingTransaction> getUnreconciled(String value)
        {
                if (value == null || value.isBlank())
                {
                        return List.of();
                }

                synchronized (LOCK)
                {
                        ensureLoaded();

                        List<AccountingTransaction> list = UNRECONCILED.get(value);

                        if (list == null || list.isEmpty())
                        {
                                return List.of();
                        }

                        return list.stream()
                                .filter(ReconciliationService::isUncleared)
                                .sorted(ReconciliationService::compareTransactions)
                                .collect(Collectors.toUnmodifiableList());
                }
        }

        /**
         * Lists accounts that are eligible for reconciliation.
         *
         * @return sorted list of reconcilable account numbers
         */
        public static List<String> listReconcilableAccounts()
        {
                synchronized (LOCK)
                {
                        ensureLoaded();
                        return UNRECONCILED.keySet().stream()
                                .sorted()
                                .collect(Collectors.toCollection(ArrayList::new));
                }
        }

        /**
         * Performs the reconciliation process for a given account.
         *
         * @param accountIdentifier account to reconcile
         * @param statementDate statement ending date (used to mark transactions)
         * @param endingBalance ending balance supplied by the statement (logged for diagnostics)
         * @param clearedIds collection of booking timestamps that should be marked cleared
         */
        public void reconcile(String accountIdentifier, String statementDate, BigDecimal endingBalance, List<Long> clearedIds)
        {
                if (accountIdentifier == null || accountIdentifier.isBlank() || clearedIds == null)
                {
                        return;
                }

                synchronized (LOCK)
                {
                        ensureLoaded();

                        List<AccountingTransaction> list = UNRECONCILED.get(accountIdentifier);

                        if (list == null || list.isEmpty())
                        {
                                return;
                        }

                        list.removeIf(tx -> shouldClear(tx, clearedIds, statementDate));

                        if (list.isEmpty())
                        {
                                UNRECONCILED.remove(accountIdentifier);
                        }
                }

                this.pendingTransactions.removeIf(tx -> clearedIds.contains(tx.getBookingDateTimestamp()));

                if (endingBalance != null)
                {
                        LOGGER.debug("Reconciled account {} with ending balance {}",
                                accountIdentifier,
                                endingBalance);
                }

                try
                {
                        new OperationalReconciliationService()
                                .reconcileFromBookingTimestamps(accountIdentifier,
                                        statementDate, endingBalance, clearedIds);
                }
                catch (Exception ex)
                {
                        LOGGER.warn(
                                "Operational reconciliation persistence skipped for account {}: {}",
                                accountIdentifier,
                                ex.getMessage());
                }
        }

        /**
         * Adds a transaction to a list or batch of transactions that are pending reconciliation.
         *
         * @param transaction The {@link AccountingTransaction} to add to the reconciliation batch
         */
        public void addTransactionToReconcile(AccountingTransaction transaction)
        {
                if (transaction == null)
                {
                        return;
                }

                synchronized (LOCK)
                {
                        ensureLoaded();

                        Set<String> accountNumbers = extractReconcilableAccounts(transaction);

                        if (accountNumbers.isEmpty())
                        {
                                return;
                        }

                        for (String accountNumber : accountNumbers)
                        {
                                List<AccountingTransaction> list =
                                        UNRECONCILED.computeIfAbsent(accountNumber, k -> new ArrayList<>());

                                if (!containsTransaction(list, transaction))
                                {
                                        list.add(transaction);
                                }
                        }
                }

                if (!containsTransaction(this.pendingTransactions, transaction))
                {
                        this.pendingTransactions.add(transaction);
                }
        }

        /**
         * Ensures the unreconciled map is loaded from the current company's ledger.
         */
        private static void ensureLoaded()
        {
                Company company = CurrentCompany.getCompany();

                if (company == null)
                {
                        UNRECONCILED.clear();
                        lastLoadedCompany = null;
                        return;
                }

                if (company != lastLoadedCompany || UNRECONCILED.isEmpty())
                {
                                UNRECONCILED.clear();
                                lastLoadedCompany = company;
                                loadLedgerTransactions(company);
                }
        }

        private static void loadLedgerTransactions(Company company)
        {
                Ledger ledger = company.getLedger();

                if (ledger == null)
                {
                        return;
                }

                List<AccountingTransaction> ledgerTxns = ledger.getTransactions();

                if (ledgerTxns == null)
                {
                        return;
                }

                for (AccountingTransaction tx : ledgerTxns)
                {
                        if (!isUncleared(tx))
                        {
                                continue;
                        }

                        for (String accountNumber : extractReconcilableAccounts(tx))
                        {
                                UNRECONCILED.computeIfAbsent(accountNumber, k -> new ArrayList<>()).add(tx);
                        }
                }

                UNRECONCILED.values().forEach(list -> list.sort(ReconciliationService::compareTransactions));
        }

        private static boolean withinRange(AccountingTransaction tx, LocalDate from, LocalDate to)
        {
                if (tx == null)
                {
                        return false;
                }

                LocalDate txDate = parseDate(tx.getDate());

                if (txDate == null)
                {
                        return from == null && to == null;
                }

                boolean afterStart = from == null || !txDate.isBefore(from);
                boolean beforeEnd = to == null || !txDate.isAfter(to);
                return afterStart && beforeEnd;
        }

        private static LocalDate parseDate(String value)
        {
                if (value == null || value.isBlank())
                {
                        return null;
                }

                try
                {
                        return LocalDate.parse(value.trim());
                }
                catch (DateTimeParseException ex)
                {
                        LOGGER.debug("Unable to parse date '{}': {}",
                                value,
                                ex.getMessage());
                        return null;
                }
        }

        private static BigDecimal defaultAmount(AccountingTransaction tx)
        {
                return tx != null && tx.getTotalAmount() != null ? tx.getTotalAmount() : BigDecimal.ZERO;
        }

        private static String clearedLabel(AccountingTransaction tx)
        {
                if (tx == null)
                {
                        return "";
                }

                return isUncleared(tx) ? "UNRECONCILED" : safe(tx.getClearBank());
        }

        private static String safe(String value)
        {
                return value == null ? "" : value;
        }

        private static boolean isUncleared(AccountingTransaction tx)
        {
                return tx != null && (tx.getClearBank() == null || tx.getClearBank().isBlank());
        }

        private static int compareTransactions(AccountingTransaction a, AccountingTransaction b)
        {
                if (a == b)
                {
                        return 0;
                }

                if (a == null)
                {
                        return 1;
                }

                if (b == null)
                {
                        return -1;
                }

                Long aTs = a.getBookingDateTimestamp();
                Long bTs = b.getBookingDateTimestamp();

                if (aTs != null && bTs != null)
                {
                        int cmp = aTs.compareTo(bTs);

                        if (cmp != 0)
                        {
                                return cmp;
                        }
                }

                return safe(a.getDate()).compareTo(safe(b.getDate()));
        }

        private static boolean shouldClear(AccountingTransaction tx, List<Long> clearedIds, String statementDate)
        {
                if (tx == null)
                {
                        return false;
                }

                Long id = tx.getBookingDateTimestamp();

                if (id == null || !clearedIds.contains(id))
                {
                        return false;
                }

                markCleared(tx, statementDate);
                return true;
        }

        private static void markCleared(AccountingTransaction tx, String statementDate)
        {
                if (tx == null)
                {
                        return;
                }

                if (statementDate != null && !statementDate.isBlank())
                {
                        tx.setClearBank(statementDate);
                }
                else
                {
                        tx.setClearBank(CLEARED_FLAG);
                }
        }

        private static Set<String> extractReconcilableAccounts(AccountingTransaction transaction)
        {
                if (transaction == null || transaction.getEntries() == null)
                {
                        return Set.of();
                }

                Company company = CurrentCompany.getCompany();
                Set<String> results = new HashSet<>();

                for (AccountingEntry entry : transaction.getEntries())
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

                        if (company == null || company.getChartOfAccounts() == null)
                        {
                                results.add(accountNumber);
                                continue;
                        }

                        Account account = company.getChartOfAccounts().getAccount(accountNumber);

                        if (account == null || account.getAccountType() == null
                                || RECONCILABLE_TYPES.contains(account.getAccountType()))
                        {
                                results.add(accountNumber);
                        }
                }

                return results;
        }

        private static boolean containsTransaction(List<AccountingTransaction> list, AccountingTransaction candidate)
        {
                if (list == null || candidate == null)
                {
                        return false;
                }

                Long id = candidate.getBookingDateTimestamp();

                for (AccountingTransaction existing : list)
                {
                        if (existing == candidate)
                        {
                                return true;
                        }

                        if (existing != null && id != null && Objects.equals(existing.getBookingDateTimestamp(), id))
                        {
                                return true;
                        }
                }

                return false;
        }
}
