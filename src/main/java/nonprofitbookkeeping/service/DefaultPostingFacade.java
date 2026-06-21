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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class DefaultPostingFacade implements PostingFacade
{
    private final JournalRepository journalRepository;
    private final PostingDatePolicyValidator postingDatePolicyValidator;
    private final AccountFundRestrictionValidator accountFundRestrictionValidator;
    private final PostingLockValidator postingLockValidator;

    public DefaultPostingFacade()
    {
        this(new JournalRepository(), new NoOpPostingDatePolicyValidator(),
            new NoOpAccountFundRestrictionValidator(), new NoOpPostingLockValidator());
    }

    DefaultPostingFacade(JournalRepository journalRepository,
        PostingDatePolicyValidator postingDatePolicyValidator,
        AccountFundRestrictionValidator accountFundRestrictionValidator,
        PostingLockValidator postingLockValidator)
    {
        this.journalRepository = journalRepository;
        this.postingDatePolicyValidator = postingDatePolicyValidator;
        this.accountFundRestrictionValidator = accountFundRestrictionValidator;
        this.postingLockValidator = postingLockValidator;
    }

    public PostingReference post(PostingCommand command) throws SQLException
    {
        Objects.requireNonNull(command, "command");
        requireValid(command.transaction());
        this.postingDatePolicyValidator.validate(command);
        this.accountFundRestrictionValidator.validate(command);
        this.postingLockValidator.validate(command);
        enrichInfo(command);
        this.journalRepository.upsertTransaction(command.transaction());
        return referenceFor(command.transaction().getId());
    }

    public PostingReference reverse(int journalTxnId, String reason) throws SQLException
    {
        AccountingTransaction source = this.journalRepository.findTransactionById(journalTxnId)
            .orElseThrow(() -> new IllegalArgumentException("journalTxnId not found: " + journalTxnId));
        AccountingTransaction reversal = copyWithReversedEntries(source, this.journalRepository.reserveNextTransactionId(), reason);
        return post(new PostingCommand(reversal,
            source.getInfo().getOrDefault("module", "JOURNAL"),
            source.getInfo().get("domain_record_id"),
            "REVERSAL",
            "REVERSAL:" + journalTxnId));
    }

    public PostingReference amend(int oldJournalTxnId, PostingCommand newCommand,
        String reason) throws SQLException
    {
        PostingReference reversal = reverse(oldJournalTxnId, reason);
        try
        {
            return post(newCommand);
        }
        catch (SQLException | RuntimeException ex)
        {
            try
            {
                reverse(reversal.journalTxnId(),
                    "Compensating reversal for failed amend of txn " + oldJournalTxnId);
            }
            catch (Exception compensationEx)
            {
                ex.addSuppressed(compensationEx);
            }
            throw ex;
        }
    }

    private static void requireValid(AccountingTransaction txn)
    {
        if (txn == null || !txn.isBalanced())
        {
            throw new IllegalArgumentException("Posting requires a balanced transaction");
        }
    }

    private static void enrichInfo(PostingCommand command)
    {
        AccountingTransaction txn = command.transaction();
        Map<String, String> info = txn.getInfo();
        if (info == null)
        {
            info = new java.util.LinkedHashMap<>();
            txn.setInfo(info);
        }
        else if (!(info instanceof java.util.LinkedHashMap))
        {
            info = new java.util.LinkedHashMap<>(info);
            txn.setInfo(info);
        }
        info.putIfAbsent("module", command.module());
        info.putIfAbsent("domain_record_id", command.domainRecordId());
        info.putIfAbsent("link_role", command.linkRole());
        info.putIfAbsent("idempotency_key", command.idempotencyKey());
    }

    private PostingReference referenceFor(int txnId) throws SQLException
    {
        return new PostingReference(txnId, canonicalRefFor(txnId));
    }

    private String canonicalRefFor(int txnId) throws SQLException
    {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id FROM txn WHERE id = ?"))
        {
            ps.setInt(1, txnId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return "txn:" + rs.getLong(1);
                }
            }
        }
        return "journal_transaction:" + txnId;
    }

    private static AccountingTransaction copyWithReversedEntries(AccountingTransaction source,
        int newId, String reason)
    {
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(newId);
        txn.setDate(source.getDate());
        txn.setBookingDateTimestamp(System.currentTimeMillis());
        txn.setMemo("Reversal of txn " + source.getId() + (reason == null ? "" : " - " + reason));
        txn.setToFrom(source.getToFrom());
        txn.setInfo(source.getInfo() == null ? new java.util.LinkedHashMap<>() : new java.util.LinkedHashMap<>(source.getInfo()));
        LinkedHashSet<AccountingEntry> reversed = new LinkedHashSet<>();
        for (AccountingEntry e : source.getEntries())
        {
            AccountingEntry r = new AccountingEntry(e.getAmount() == null ? BigDecimal.ZERO : e.getAmount(),
                e.getAccountNumber(),
                e.getAccountSide() == AccountSide.DEBIT ? AccountSide.CREDIT : AccountSide.DEBIT,
                e.getAccountName());
            r.setFundNumber(e.getFundNumber());
            reversed.add(r);
        }
        txn.setEntries(reversed);
        return txn;
    }

}