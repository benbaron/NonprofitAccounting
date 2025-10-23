
package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
 * The service maintains a cache of unreconciled transactions for reconcilable accounts and exposes
 * helpers used by both Swing and JavaFX reconciliation panels.
 */
public class ReconciliationService
{
       private static final Logger LOGGER = Logger.getLogger(ReconciliationService.class.getName());

       /** Default flag written to {@link AccountingTransaction#setClearBank(String)} when no statement date is provided. */
       private static final String DEFAULT_CLEARED_MARKER = "Cleared";

       /** Date formatter used when emitting reconciliation table rows. */
       private static final DateTimeFormatter OUTPUT_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

       /**
        * In-memory store of unreconciled transactions keyed by account number.
        * Populated from the active company's ledger on demand.
        */
       private static final Map<String, List<AccountingTransaction>> UNRECONCILED = new LinkedHashMap<>();

       /** Snapshot of reconcilable accounts keyed by account number. */
       private static final Map<String, Account> ACCOUNT_INDEX = new HashMap<>();
	
        /**
         * Builds a lightweight table of unreconciled entries for the supplied account. Optional
         * {@code from} and {@code to} parameters constrain the results to a specific date range.
         * Each row contains the booking timestamp, posting date, memo, and formatted amount for
         * the account's portion of the transaction.
         *
         * @param account account number to inspect
         * @param from inclusive start date filter (may be {@code null})
         * @param to inclusive end date filter (may be {@code null})
         * @return list of display rows suitable for tabular presentation
         */
    public static List<String[]> getUnreconciledEntries(String account, String from, String to)
    {
            if (account == null || account.isBlank())
            {
                    return List.of();
            }

            rebuildIndex();

            List<AccountingTransaction> transactions = UNRECONCILED.get(account);

            if (transactions == null || transactions.isEmpty())
            {
                    return List.of();
            }

            LocalDate fromDate = parseDate(from);
            LocalDate toDate = parseDate(to);
            Account acct = ACCOUNT_INDEX.get(account);

            List<String[]> rows = new ArrayList<>();

            for (AccountingTransaction tx : transactions)
            {
                    LocalDate txDate = transactionDate(tx);

                    if (fromDate != null && txDate != null && txDate.isBefore(fromDate))
                    {
                            continue;
                    }

                    if (toDate != null && txDate != null && txDate.isAfter(toDate))
                    {
                            continue;
                    }

                    BigDecimal amount = amountForAccount(tx, account, acct);
                    String[] row = new String[]
                    {
                            String.valueOf(tx.getBookingDateTimestamp()),
                            txDate != null ? OUTPUT_DATE.format(txDate) : "",
                            tx.getMemo() != null ? tx.getMemo() : "",
                            FormatUtils.formatCurrency(amount)
                    };
                    rows.add(row);
            }

            return rows;
    }
	
        /**
         * Marks a specific financial entry (transaction) as reconciled using its booking timestamp.
         * The transaction is removed from the unreconciled cache and its {@code clearBank} field is
         * updated with a default marker so subsequent lookups treat it as cleared.
         *
         * @param txnId The unique booking timestamp of the transaction to be marked as reconciled.
         * @return {@code true} when a matching transaction was found and updated, {@code false} otherwise.
         */
    public static boolean reconcileEntry(Long txnId)
    {
            if (txnId == null)
            {
                    return false;
            }

            rebuildIndex();

            Iterator<Map.Entry<String, List<AccountingTransaction>>> accountIt =
                    UNRECONCILED.entrySet().iterator();

            while (accountIt.hasNext())
            {
                    Map.Entry<String, List<AccountingTransaction>> entry = accountIt.next();
                    List<AccountingTransaction> list = entry.getValue();

                    Iterator<AccountingTransaction> txIt = list.iterator();
                    while (txIt.hasNext())
                    {
                            AccountingTransaction tx = txIt.next();

                            if (tx == null)
                            {
                                    continue;
                            }

                            if (Objects.equals(txnId, tx.getBookingDateTimestamp()))
                            {
                                    markCleared(tx, null);
                                    txIt.remove();

                                    if (list.isEmpty())
                                    {
                                            accountIt.remove();
                                            ACCOUNT_INDEX.remove(entry.getKey());
                                    }

                                    return true;
                            }
                    }
            }

            return false;
    }


        /**
         * Retrieves unreconciled transactions for the supplied account number.
         *
         * @param value account number whose unreconciled transactions should be returned
         * @return matching transactions ordered by booking timestamp; {@link List#of()} when none exist
         */
      public static List<AccountingTransaction> getUnreconciled(String value)
      {
              if (value == null || value.isBlank())
                      return List.of();

              rebuildIndex();

              List<AccountingTransaction> list = UNRECONCILED.get(value);
              if (list == null || list.isEmpty())
                      return List.of();

              return new ArrayList<>(list);
      }

        /**
         * Lists account numbers that currently have unreconciled activity and are eligible for reconciliation.
         * The identifiers correspond to accounts in the chart of accounts whose type is typically reconciled
         * (bank, cash, checking, credit card, money market).
         *
         * @return A list of account numbers sorted by their display name.
         */
      public static List<String> listReconcilableAccounts()
      {
              rebuildIndex();

              return UNRECONCILED.keySet().stream()
                      .sorted(Comparator.comparing(ReconciliationService::accountDisplayName,
                              String.CASE_INSENSITIVE_ORDER)
                              .thenComparing(String::valueOf))
                      .collect(Collectors.toCollection(ArrayList::new));
      }

        /**
         * Marks the supplied transactions as cleared for the specified account and removes them from the
         * unreconciled cache. Each transaction's {@code clearBank} field is updated with the provided
         * statement date so subsequent lookups treat it as reconciled.
         *
         * @param accountIdentifier account number whose transactions should be reconciled
         * @param statementDate statement date associated with the reconciliation (may be {@code null})
         * @param endingBalance statement ending balance (currently informational only)
         * @param clearedIds booking timestamps of the transactions that should be marked cleared
         */
       public void reconcile(String accountIdentifier, String statementDate, BigDecimal endingBalance, List<Long> clearedIds)
       {
               if (accountIdentifier == null || accountIdentifier.isBlank())
               {
                       return;
               }

               if (clearedIds == null || clearedIds.isEmpty())
               {
                       return;
               }

               rebuildIndex();

               List<AccountingTransaction> list = UNRECONCILED.get(accountIdentifier);
               if (list == null || list.isEmpty())
               {
                       return;
               }

               Set<Long> ids = clearedIds.stream()
                       .filter(Objects::nonNull)
                       .collect(Collectors.toCollection(HashSet::new));

               if (ids.isEmpty())
               {
                       return;
               }

               list.removeIf(tx ->
               {
                       if (tx == null)
                       {
                               return false;
                       }

                       Long timestamp = tx.getBookingDateTimestamp();
                       if (timestamp == null || !ids.contains(timestamp))
                       {
                               return false;
                       }

                       markCleared(tx, statementDate);
                       ids.remove(timestamp);
                       return true;
               });

               if (list.isEmpty())
               {
                       UNRECONCILED.remove(accountIdentifier);
                       ACCOUNT_INDEX.remove(accountIdentifier);
               }
       }

        /**
         * Ensures the provided transaction is tracked by the reconciliation cache. When the transaction is
         * not already part of the current company's ledger it is attached before the cache is rebuilt.
         *
         * @param transaction the transaction to make available to reconciliation workflows
         */
       public void addTransactionToReconcile(AccountingTransaction transaction)
       {
               if (transaction == null)
               {
                       return;
               }

               Company company = CurrentCompany.getCompany();

               if (company == null)
               {
                       return;
               }

               if (company.getLedger() != null)
               {
                       List<AccountingTransaction> ledgerTxns = company.getLedger().getTransactions();

                       boolean present = ledgerTxns != null && ledgerTxns.stream()
                               .anyMatch(existing -> sameTransaction(existing, transaction));

                       if (!present)
                       {
                               try
                               {
                                       company.getLedger().getJournal().addTransaction(transaction);
                               }
                               catch (RuntimeException ex)
                               {
                                       LOGGER.fine(() -> "Unable to attach transaction to ledger: " + ex.getMessage());
                               }
                       }
               }

               rebuildIndex();
       }

       private static synchronized void rebuildIndex()
       {
               UNRECONCILED.clear();
               ACCOUNT_INDEX.clear();

               Company company = CurrentCompany.getCompany();

               if (company == null)
               {
                       return;
               }

               Map<String, Account> reconcilable = collectReconcilableAccounts(company.getChartOfAccounts());

               if (reconcilable.isEmpty())
               {
                       return;
               }

               ACCOUNT_INDEX.putAll(reconcilable);

               if (company.getLedger() == null)
               {
                       return;
               }

               List<AccountingTransaction> transactions = company.getLedger().getTransactions();

               if (transactions == null || transactions.isEmpty())
               {
                       return;
               }

               for (AccountingTransaction tx : transactions)
               {
                       includeTransaction(tx, reconcilable);
               }

               UNRECONCILED.values().forEach(list ->
                       list.sort(Comparator.comparingLong(ReconciliationService::transactionTimestamp)));
       }

       private static Map<String, Account> collectReconcilableAccounts(ChartOfAccounts chart)
       {
               Map<String, Account> accounts = new HashMap<>();

               if (chart == null)
               {
                       return accounts;
               }

               for (Account account : chart.getAccounts())
               {
                       if (isReconcilableAccount(account))
                       {
                               accounts.put(account.getAccountNumber(), account);
                       }
               }

               return accounts;
       }

       private static void includeTransaction(AccountingTransaction tx, Map<String, Account> accounts)
       {
               if (tx == null || isCleared(tx))
               {
                       return;
               }

               if (tx.getEntries() == null)
               {
                       return;
               }

               for (AccountingEntry entry : tx.getEntries())
               {
                       if (entry == null)
                       {
                               continue;
                       }

                       String accountNumber = entry.getAccountNumber();

                       if (accountNumber == null || !accounts.containsKey(accountNumber))
                       {
                               continue;
                       }

                       List<AccountingTransaction> list = UNRECONCILED
                               .computeIfAbsent(accountNumber, k -> new ArrayList<>());

                       boolean exists = list.stream()
                               .anyMatch(existing -> sameTransaction(existing, tx));

                       if (!exists)
                       {
                               list.add(tx);
                       }
               }
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

               Long lhs = left.getBookingDateTimestamp();
               Long rhs = right.getBookingDateTimestamp();

               return lhs != null && Objects.equals(lhs, rhs);
       }

       private static boolean isReconcilableAccount(Account account)
       {
               if (account == null)
               {
                       return false;
               }

               String number = account.getAccountNumber();

               if (number == null || number.isBlank())
               {
                       return false;
               }

               AccountType type = account.getAccountType();

               return type == AccountType.BANK
                       || type == AccountType.CASH
                       || type == AccountType.CHECKING
                       || type == AccountType.CREDITCARD
                       || type == AccountType.MONEYMKRT;
       }

       private static boolean isCleared(AccountingTransaction tx)
       {
               return tx.getClearBank() != null && !tx.getClearBank().isBlank();
       }

       private static long transactionTimestamp(AccountingTransaction tx)
       {
               if (tx == null)
               {
                       return Long.MAX_VALUE;
               }

               Long timestamp = tx.getBookingDateTimestamp();

               if (timestamp != null && timestamp.longValue() != 0L)
               {
                       return timestamp.longValue();
               }

               LocalDate date = transactionDate(tx);

               if (date != null)
               {
                       return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
               }

               return Long.MAX_VALUE;
       }

       private static LocalDate transactionDate(AccountingTransaction tx)
       {
               if (tx == null)
               {
                       return null;
               }

               String dateText = tx.getDate();

               if (dateText != null && !dateText.isBlank())
               {
                       try
                       {
                               return LocalDate.parse(dateText);
                       }
                       catch (DateTimeParseException ex)
                       {
                               // fall through to timestamp parsing
                       }
               }

               Long timestamp = tx.getBookingDateTimestamp();

               if (timestamp == null || timestamp.longValue() == 0L)
               {
                       return null;
               }

               return Instant.ofEpochMilli(timestamp)
                       .atZone(ZoneId.systemDefault())
                       .toLocalDate();
       }

       private static LocalDate parseDate(String text)
       {
               if (text == null || text.isBlank())
               {
                       return null;
               }

               try
               {
                       return LocalDate.parse(text.trim());
               }
               catch (DateTimeParseException ex)
               {
                       return null;
               }
       }

       private static BigDecimal amountForAccount(AccountingTransaction tx, String accountNumber, Account account)
       {
               if (tx == null || accountNumber == null || tx.getEntries() == null)
               {
                       return BigDecimal.ZERO;
               }

               BigDecimal total = BigDecimal.ZERO;
               AccountSide natural = account != null ? account.getIncreaseSide() : null;

               for (AccountingEntry entry : tx.getEntries())
               {
                       if (entry == null || !accountNumber.equals(entry.getAccountNumber()))
                       {
                               continue;
                       }

                       BigDecimal amount = entry.getAmount();
                       if (amount == null)
                       {
                               continue;
                       }

                       AccountSide side = entry.getAccountSide();

                       if (side == null || natural == null)
                       {
                               total = total.add(amount);
                               continue;
                       }

                       if (side == natural)
                       {
                               total = total.add(amount);
                       }
                       else
                       {
                               total = total.subtract(amount);
                       }
               }

               return total;
       }

       private static void markCleared(AccountingTransaction tx, String statementDate)
       {
               String marker = (statementDate != null && !statementDate.isBlank())
                       ? statementDate
                       : DEFAULT_CLEARED_MARKER;

               tx.setClearBank(marker);

               Map<String, String> info = ensureInfoMap(tx);
               info.put("reconciledOn", marker);
       }

       private static Map<String, String> ensureInfoMap(AccountingTransaction tx)
       {
               Map<String, String> info = tx.getInfo();

               if (info instanceof HashMap || info instanceof LinkedHashMap)
               {
                       return info;
               }

               Map<String, String> copy = new LinkedHashMap<>();

               if (info != null)
               {
                       copy.putAll(info);
               }

               tx.setInfo(copy);
               return copy;
       }

       private static String accountDisplayName(String accountNumber)
       {
               Account account = ACCOUNT_INDEX.get(accountNumber);

               if (account != null && account.getName() != null)
               {
                       return account.getName();
               }

               return accountNumber;
       }
	
}
