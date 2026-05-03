package nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.persistence.JournalRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Posting flow for FundTransfer domain operations.
 */
public class FundTransferPostingService {
    private final PostingFacade postingFacade;
    private final FundTransferLifecycleService lifecycleService;
    private final JournalRepository journalRepository;

    public FundTransferPostingService() {
        this(new DefaultPostingFacade(), new FundTransferLifecycleService(),
            new JournalRepository());
    }

    FundTransferPostingService(PostingFacade postingFacade,
                               FundTransferLifecycleService lifecycleService,
                               JournalRepository journalRepository) {
        this.postingFacade = postingFacade;
        this.lifecycleService = lifecycleService;
        this.journalRepository = journalRepository;
    }

    public PostingReference postTransfer(long transferId, LocalDate date,
                                         String fromFund, String toFund,
                                         BigDecimal amount, String memo,
                                         String clearingAccount,
                                         String transferAccount)
        throws SQLException {
        validateInputs(transferId, fromFund, toFund, amount,
            clearingAccount, transferAccount);
        lifecycleService.transitionStatus(transferId, "POSTING", null,
            "posting started");
        try {
            int txnId = journalRepository.reserveNextTransactionId();
            PostingCommand command = new PostingCommand(
                toTransaction(transferId, date, fromFund, toFund, amount, memo,
                    clearingAccount, transferAccount, "ORIGINAL", txnId),
                "FUNDS", String.valueOf(transferId), "ORIGINAL",
                "FUNDS:" + transferId);
            PostingReference posted = postingFacade.post(command);
            try
            {
                FinanceWriteEnforcement.runWithinFacadeScope(() ->
                    lifecycleService.transitionStatus(transferId, "POSTED",
                        (long) posted.journalTxnId(), "posted via facade"));
            }
            catch (Exception e)
            {
                if (e instanceof SQLException sql) throw sql;
                if (e instanceof RuntimeException re) throw re;
                throw new IllegalStateException(e);
            }
            return posted;
        } catch (RuntimeException | SQLException ex) {
            lifecycleService.transitionStatus(transferId, "FAILED", null,
                ex.getMessage());
            throw ex;
        }
    }

    public PostingReference amendTransfer(long transferId, int oldJournalTxnId,
                                          LocalDate date, String fromFund,
                                          String toFund, BigDecimal amount,
                                          String memo, String clearingAccount,
                                          String transferAccount)
        throws SQLException {
        validateInputs(transferId, fromFund, toFund, amount,
            clearingAccount, transferAccount);
        int txnId = journalRepository.reserveNextTransactionId();
        PostingCommand amended = new PostingCommand(
            toTransaction(transferId, date, fromFund, toFund, amount, memo,
                clearingAccount, transferAccount, "ADJUSTMENT", txnId),
            "FUNDS", String.valueOf(transferId), "ADJUSTMENT",
            "FUNDS:" + transferId + ":A");
        return postingFacade.amend(oldJournalTxnId, amended,
            "Fund transfer financial edit");
    }

    private static void validateInputs(long transferId, String fromFund,
                                       String toFund, BigDecimal amount,
                                       String clearingAccount,
                                       String transferAccount) {
        if (transferId <= 0) {
            throw new IllegalArgumentException("transferId is required");
        }
        if (fromFund == null || toFund == null || fromFund.isBlank()
            || toFund.isBlank() || fromFund.equals(toFund)) {
            throw new IllegalArgumentException("from/to funds must be distinct");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (clearingAccount == null || clearingAccount.isBlank()
            || transferAccount == null || transferAccount.isBlank()) {
            throw new IllegalArgumentException("accounts are required");
        }
    }

    private AccountingTransaction toTransaction(long transferId, LocalDate date,
                                                String fromFund, String toFund,
                                                BigDecimal amount, String memo,
                                                String clearingAccount,
                                                String transferAccount,
                                                String role, int txnId) {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(txnId);
        txn.setDate((date == null ? LocalDate.now() : date).toString());
        txn.setBookingDateTimestamp(System.currentTimeMillis());
        txn.setMemo(memo == null || memo.isBlank()
            ? "Fund transfer " + transferId : memo);
        txn.setInfo(Map.of(
            "module", "FUNDS",
            "domain_record_id", String.valueOf(transferId),
            "link_role", role,
            "idempotency_key", "FUNDS:" + transferId));

        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(entry(amount, clearingAccount, AccountSide.CREDIT, fromFund));
        entries.add(entry(amount, transferAccount, AccountSide.DEBIT, fromFund));
        entries.add(entry(amount, transferAccount, AccountSide.CREDIT, toFund));
        entries.add(entry(amount, clearingAccount, AccountSide.DEBIT, toFund));
        txn.setEntries(entries);
        return txn;
    }

    private static AccountingEntry entry(BigDecimal amount, String account,
                                         AccountSide side, String fund) {
        AccountingEntry e = new AccountingEntry(amount, account, side);
        e.setFundNumber(fund);
        return e;
    }

    public int countFundLines(int txnId, String fundNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM journal_entry WHERE txn_id=? AND fund_number=?";
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, txnId);
            ps.setString(2, fundNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
