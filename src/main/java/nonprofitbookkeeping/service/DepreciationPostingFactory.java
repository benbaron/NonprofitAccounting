package nonprofitbookkeeping.service;

import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountType;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
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
            resolveAccountNumber(AccountType.EXPENSE.name(), null, "Depreciation Expense"),
            AccountSide.DEBIT, "Depreciation Expense"));
        entries.add(new AccountingEntry(amount,
            resolveAccountNumber(AccountType.ASSET.name(), "ACCUMULATED_DEPRECIATION", "Accumulated Depreciation"),
            AccountSide.CREDIT, "Accumulated Depreciation"));
        txn.setEntries(entries);
        return new PostingCommand(txn, "DEPRECIATION", recordId, "ORIGINAL", "depr:" + recordId);
    }

    private static String resolveAccountNumber(String accountType, String subtype, String label) throws SQLException
    {
        String sql = (subtype == null)
            ? "SELECT account_number FROM account WHERE account_type = ? ORDER BY account_number LIMIT 1"
            : "SELECT account_number FROM account WHERE account_type = ? AND subtype = ? ORDER BY account_number LIMIT 1";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, accountType);
            if (subtype != null)
            {
                ps.setString(2, subtype);
            }
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    throw new IllegalStateException("Missing posting account for " + label +
                        " account_type=" + accountType + (subtype == null ? "" : " subtype=" + subtype));
                }
                return rs.getString(1);
            }
        }
    }
}
