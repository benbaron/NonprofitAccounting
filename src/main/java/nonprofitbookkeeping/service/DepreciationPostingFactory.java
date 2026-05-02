package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.supplemental.SupplementalLineKind;
import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;

final class DepreciationPostingFactory
{
    private DepreciationPostingFactory() {}

    static PostingCommand build(String recordId, BigDecimal amount, LocalDate postingDate) throws SQLException
    {
        JournalRepository journalRepository = new JournalRepository();
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(journalRepository.reserveNextTransactionId());
        txn.setDate((postingDate == null ? LocalDate.now() : postingDate).toString());
        txn.setMemo("Depreciation " + recordId);
        txn.setInfo(Map.of());
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(amount,
            resolveAccountNumberByCode(AccountType.EXPENSE.name(), DepreciationPostingRoles.expenseCode(),
                "Depreciation Expense"),
            AccountSide.DEBIT, "Depreciation Expense"));
        entries.add(new AccountingEntry(amount,
            resolveAccountNumberByCodeAndKind(AccountType.ASSET.name(), DepreciationPostingRoles.accumulatedDepreciationCode(),
                SupplementalLineKind.OTHER_ASSET,
                "Accumulated Depreciation"),
            AccountSide.CREDIT, "Accumulated Depreciation"));
        txn.setEntries(entries);
        return new PostingCommand(txn, "DEPRECIATION", recordId, "ORIGINAL", "depr:" + recordId);
    }

    private static String resolveAccountNumberByCode(String accountType,
        String accountCode,
        String label) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT account_number FROM account WHERE account_type = ? AND account_code = ? ORDER BY account_number LIMIT 1"))
        {
            ps.setString(1, accountType);
            ps.setString(2, accountCode);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    throw new IllegalStateException("Missing posting account for " + label +
                        " account_type=" + accountType + " account_code=" + accountCode);
                }
                return rs.getString(1);
            }
        }
    }

    private static String resolveAccountNumberByCodeAndKind(String accountType,
        String accountCode,
        SupplementalLineKind kind,
        String label) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("""
                 SELECT account_number
                 FROM account
                 WHERE account_type = ?
                   AND account_code = ?
                   AND (supplemental_kinds = ?
                        OR supplemental_kinds LIKE ?
                        OR supplemental_kinds LIKE ?
                        OR supplemental_kinds LIKE ?)
                 ORDER BY account_number
                 LIMIT 1
             """))
        {
            String encoded = kind.name();
            ps.setString(1, accountType);
            ps.setString(2, accountCode);
            ps.setString(3, encoded);
            ps.setString(4, encoded + ",%");
            ps.setString(5, "%," + encoded + ",%");
            ps.setString(6, "%," + encoded);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    throw new IllegalStateException("Missing posting account for " + label +
                        " account_type=" + accountType + " account_code=" + accountCode +
                        " supplemental_kind=" + encoded);
                }
                return rs.getString(1);
            }
        }
    }
}
