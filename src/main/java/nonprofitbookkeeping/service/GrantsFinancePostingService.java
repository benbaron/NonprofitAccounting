package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.model.Grant;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Grants finance-impacting posting service.
 */
public class GrantsFinancePostingService {
    public enum GrantEventType { AWARD, DRAWDOWN, RECOGNITION }

    private final PostingFacade postingFacade;
    private final JournalRepository journalRepository;

    public GrantsFinancePostingService() {
        this(new DefaultPostingFacade(), new JournalRepository());
    }

    GrantsFinancePostingService(PostingFacade postingFacade,
                                JournalRepository journalRepository) {
        this.postingFacade = postingFacade;
        this.journalRepository = journalRepository;
    }

    public PostingReference postFinancialEvent(String grantRecordId, Grant grant,
                                               GrantEventType eventType,
                                               LocalDate eventDate,
                                               BigDecimal amount,
                                               String debitAccount,
                                               String creditAccount,
                                               String fundNumber)
        throws SQLException {
        require(grantRecordId, grant, eventType, amount, debitAccount,
            creditAccount);
        int txnId = journalRepository.reserveNextTransactionId();
        PostingCommand cmd = new PostingCommand(
            toTxn(grantRecordId, grant, eventType, eventDate, amount,
                debitAccount, creditAccount, fundNumber, "ORIGINAL", txnId),
            "GRANTS", grantRecordId, "ORIGINAL",
            "GRANTS:" + grantRecordId + ":" + eventType);
        PostingReference ref = postingFacade.post(cmd);
        try
        {
            FinanceWriteEnforcement.runWithinFacadeScope(() -> {
                persistEffectivePosting(grantRecordId, ref);
                populateGrantPostingLink(grantRecordId, ref.journalTxnId(), amount,
                    eventDate, eventType);
            });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            if (e instanceof SQLException sql) throw sql;
            throw new IllegalStateException(e);
        }
        return ref;
    }

    public PostingReference editFinancialEvent(String grantRecordId,
                                               int existingTxnId, Grant grant,
                                               GrantEventType eventType,
                                               LocalDate eventDate,
                                               BigDecimal amount,
                                               String debitAccount,
                                               String creditAccount,
                                               String fundNumber)
        throws SQLException {
        require(grantRecordId, grant, eventType, amount, debitAccount,
            creditAccount);
        int txnId = journalRepository.reserveNextTransactionId();
        PostingCommand cmd = new PostingCommand(
            toTxn(grantRecordId, grant, eventType, eventDate, amount,
                debitAccount, creditAccount, fundNumber, "ADJUSTMENT", txnId),
            "GRANTS", grantRecordId, "ADJUSTMENT",
            "GRANTS:" + grantRecordId + ":A:" + eventType);
        PostingReference ref = postingFacade.amend(existingTxnId, cmd,
            "Grant financial edit");
        try
        {
            FinanceWriteEnforcement.runWithinFacadeScope(() -> {
                persistEffectivePosting(grantRecordId, ref);
                populateGrantPostingLink(grantRecordId, ref.journalTxnId(), amount,
                    eventDate, eventType);
            });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            if (e instanceof SQLException sql) throw sql;
            throw new IllegalStateException(e);
        }
        return ref;
    }

    public void editNonFinancial(Grant grant, String newPurpose,
                                 String newStatus) {
        if (grant == null) {
            return;
        }
        grant.setPurpose(newPurpose);
        grant.setStatus(newStatus);
    }

    public int orphanedGrantPostingLinks() throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM grant_posting_link gpl
            LEFT JOIN grant_record gr ON gr.grant_record_id = gpl.grant_record_id
            WHERE gr.grant_record_id IS NULL
        """;
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void persistEffectivePosting(String grantRecordId, PostingReference reference)
        throws SQLException {
        FinanceWriteEnforcement.requireFacadeScope("grant_record.canonical_txn_id");
        Long canonicalTxnId = reference.canonicalTxnId();
        long effectiveTxnId = canonicalTxnId == null ? reference.journalTxnId() : canonicalTxnId;
        String sql = "UPDATE grant_record SET canonical_txn_id=? WHERE grant_record_id=?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, effectiveTxnId);
            ps.setString(2, grantRecordId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Unknown grant_record_id: " + grantRecordId);
            }
        }
    }

    private void populateGrantPostingLink(String grantRecordId, int txnId,
                                          BigDecimal amount, LocalDate date,
                                          GrantEventType type)
        throws SQLException {
        FinanceWriteEnforcement.requireFacadeScope("grant_posting_link insert");
        String role = switch (type) {
            case AWARD -> "DEFERRAL";
            case DRAWDOWN -> "RELEASE";
            case RECOGNITION -> "REVENUE";
        };
        String sql = """
            INSERT INTO grant_posting_link(
              grant_record_id, posting_model, journal_entry_id,
              posting_role, recognized_amount, recognized_on
            ) VALUES(?, 'LEGACY', ?, ?, ?, ?)
        """;
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, grantRecordId);
            ps.setLong(2, latestJournalEntryId(txnId));
            ps.setString(3, role);
            ps.setBigDecimal(4, amount);
            ps.setDate(5, Date.valueOf(date == null ? LocalDate.now() : date));
            ps.executeUpdate();
        }
    }

    private long latestJournalEntryId(int txnId) throws SQLException {
        String sql = "SELECT MAX(id) FROM journal_entry WHERE txn_id=?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, txnId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long id = rs.getLong(1);
                if (rs.wasNull()) {
                    throw new IllegalStateException(
                        "No journal_entry rows found for txn_id=" + txnId);
                }
                return id;
            }
        }
    }

    private AccountingTransaction toTxn(String grantRecordId, Grant grant,
                                        GrantEventType type, LocalDate date,
                                        BigDecimal amount, String debitAccount,
                                        String creditAccount, String fundNumber,
                                        String linkRole, int txnId) {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(txnId);
        txn.setDate((date == null ? LocalDate.now() : date).toString());
        txn.setBookingDateTimestamp(System.currentTimeMillis());
        txn.setMemo(type + " grant " + grant.getGrantId());
        txn.setInfo(Map.of(
            "module", "GRANTS",
            "domain_record_id", grantRecordId,
            "link_role", linkRole,
            "idempotency_key", "GRANTS:" + grantRecordId + ":" + type));

        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(entry(amount, debitAccount, AccountSide.DEBIT, fundNumber));
        entries.add(entry(amount, creditAccount, AccountSide.CREDIT, fundNumber));
        txn.setEntries(entries);
        return txn;
    }

    private static AccountingEntry entry(BigDecimal amount, String account,
                                         AccountSide side, String fund) {
        AccountingEntry e = new AccountingEntry(amount, account, side);
        e.setFundNumber(fund);
        return e;
    }

    private static void require(String grantRecordId, Grant grant,
                                GrantEventType eventType, BigDecimal amount,
                                String debitAccount, String creditAccount) {
        Objects.requireNonNull(grant, "grant");
        Objects.requireNonNull(eventType, "eventType");
        if (grantRecordId == null || grantRecordId.isBlank()) {
            throw new IllegalArgumentException("grantRecordId required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (debitAccount == null || debitAccount.isBlank()
            || creditAccount == null || creditAccount.isBlank()) {
            throw new IllegalArgumentException("accounts required");
        }
    }
}
