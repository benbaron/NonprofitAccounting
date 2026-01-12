package nonprofitbookkeeping.ui.actions.scaledger;

import nonprofitbookkeeping.model.Account;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.model.CurrentCompany;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Converts parsed {@link LedgerRow} objects into {@link AccountingTransaction}
 * aggregates that the rest of the application can persist.
 */
public class LedgerToDomainMapper
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Map a single ledger row into a fully populated AccountingTransaction.
     *
     * @param row       ledger row to map
     * @param sheetName name of the sheet that produced the row
     * @return populated AccountingTransaction
     */
    public AccountingTransaction mapRowToTransaction(LedgerRow row, String sheetName)
    {
        Objects.requireNonNull(row, "row");

        AccountingTransaction transaction = new AccountingTransaction();
        if (row.getDate() != null)
        {
            transaction.setDate(DATE_FORMATTER.format(row.getDate()));
            transaction.setBookingDateTimestamp(row.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        else
        {
            transaction.setBookingDateTimestamp(Instant.now().toEpochMilli());
        }

        transaction.setCheckNumber(row.getCheckNumber());
        transaction.setClearBank(row.getClearedBankTag());
        transaction.setToFrom(row.getToFrom());
        transaction.setMemo(row.getMemo());
        transaction.setBudgetTracking(row.getBudgetNotes());

        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        String primaryFund = null;

        for (LedgerSplit split : row.getSplits())
        {
            AccountingEntry entry = mapSplit(split, transaction);
            if (entry != null)
            {
                entries.add(entry);
                if (primaryFund == null && entry.getFundNumber() != null && !entry.getFundNumber().isBlank())
                {
                    primaryFund = entry.getFundNumber();
                }
            }
        }

        transaction.setEntries(entries);

        if (primaryFund != null)
        {
            transaction.setAssociatedFundName(primaryFund);
        }

        Map<String, String> info = new LinkedHashMap<>();
        if (sheetName != null && !sheetName.isBlank())
        {
            info.put("ledgerSheetName", sheetName);
        }
        if (row.getSheetRowNumber() != null)
        {
            info.put("ledgerSheetRow", Integer.toString(row.getSheetRowNumber()));
        }
        transaction.setInfo(info);

        return transaction;
    }

    private AccountingEntry mapSplit(LedgerSplit split, AccountingTransaction parent)
    {
        if (split == null)
        {
            return null;
        }

        BigDecimal amount = split.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0)
        {
            return null;
        }

        String accountNumber = determineAccountIdentifier(split);
        if (accountNumber == null || accountNumber.isBlank())
        {
            return null;
        }

        Account account = resolveAccount(accountNumber);
        if (account != null && account.getAccountNumber() != null && !account.getAccountNumber().isBlank())
        {
            accountNumber = account.getAccountNumber();
        }

        int sign = amount.signum();
        BigDecimal magnitude = amount.abs();
        AccountSide side = determineSide(split, sign);

        String accountName = account != null && account.getName() != null && !account.getName().isBlank()
            ? account.getName()
            : accountNumber;

        AccountingEntry entry = new AccountingEntry(magnitude, accountNumber, side, accountName);
        entry.setFundNumber(split.getFund());
        entry.setAccountName(accountName);
        entry.setTransaction(parent);
        return entry;
    }

    private Account resolveAccount(String identifier)
    {
        if (identifier == null || identifier.isBlank())
        {
            return null;
        }

        if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
        {
            return null;
        }

        ChartOfAccounts chart = CurrentCompany.getCompany().getChartOfAccounts();
        if (chart == null)
        {
            return null;
        }

        Account account = chart.getAccount(identifier);
        if (account != null)
        {
            return account;
        }

        return chart.getAccountByName(identifier);
    }

    private String determineAccountIdentifier(LedgerSplit split)
    {
        if (split.getCanonicalCategory() != null && !split.getCanonicalCategory().isBlank())
        {
            return split.getCanonicalCategory();
        }
        return split.getPrimaryRawCategory();
    }

    private AccountSide determineSide(LedgerSplit split, int sign)
    {
        if (sign == 0)
        {
            return AccountSide.DEBIT;
        }

        boolean amountIsPositive = sign > 0;

        if (split.getAssetLiabilityAccount() != null && !split.getAssetLiabilityAccount().isBlank())
        {
            return amountIsPositive ? AccountSide.DEBIT : AccountSide.CREDIT;
        }
        if (split.getIncomeCategory() != null && !split.getIncomeCategory().isBlank())
        {
            return amountIsPositive ? AccountSide.CREDIT : AccountSide.DEBIT;
        }
        if (split.getExpenseCategory() != null && !split.getExpenseCategory().isBlank())
        {
            return amountIsPositive ? AccountSide.DEBIT : AccountSide.CREDIT;
        }

        // Default to debit/credit based on sign if no category hints are available
        return amountIsPositive ? AccountSide.DEBIT : AccountSide.CREDIT;
    }
}
