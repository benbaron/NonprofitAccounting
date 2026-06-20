package org.nonprofitbookkeeping.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import nonprofitbookkeeping.model.AccountingTransaction;
import nonprofitbookkeeping.service.ReconciliationService;
import nonprofitbookkeeping.util.FormatUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Service-facing model logic for the native alternate reconciliation workspace. */
public class AlternateReconciliationService
{
    private final ReconciliationGateway gateway;

    public AlternateReconciliationService()
    {
        this(new DefaultReconciliationGateway(new ReconciliationService()));
    }

    AlternateReconciliationService(ReconciliationGateway gateway)
    {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
    }

    public List<String> listAccounts()
    {
        return gateway.listAccounts();
    }

    public List<ReconciliationRow> loadRows(String account)
    {
        return gateway.loadTransactions(account).stream().map(ReconciliationRow::new).toList();
    }

    public ReconciliationSummary summarize(BigDecimal beginningBalance, BigDecimal endingBalance, List<ReconciliationRow> rows)
    {
        BigDecimal safeBeginning = beginningBalance == null ? BigDecimal.ZERO : beginningBalance;
        BigDecimal safeEnding = endingBalance == null ? BigDecimal.ZERO : endingBalance;
        BigDecimal cleared = clearedTotal(rows);
        return new ReconciliationSummary(safeBeginning, safeEnding, cleared, safeEnding.subtract(safeBeginning).subtract(cleared));
    }

    public BigDecimal clearedTotal(List<ReconciliationRow> rows)
    {
        if (rows == null) return BigDecimal.ZERO;
        return rows.stream().filter(ReconciliationRow::isCleared).map(ReconciliationRow::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BalanceParseResult parseEndingBalance(String value)
    {
        if (value == null || value.isBlank())
        {
            return BalanceParseResult.invalid("Enter the statement ending balance.");
        }
        try
        {
            return BalanceParseResult.valid(new BigDecimal(value.trim()));
        }
        catch (NumberFormatException ex)
        {
            return BalanceParseResult.invalid("Statement ending balance must be a valid number.");
        }
    }

    public SaveResult save(String account, LocalDate statementDate, BigDecimal beginningBalance, String endingBalanceText, List<ReconciliationRow> rows)
    {
        if (account == null || account.isBlank()) return SaveResult.failed("Select an account before saving reconciliation.", null);
        if (statementDate == null) return SaveResult.failed("Select a statement date before saving reconciliation.", null);
        BalanceParseResult parsed = parseEndingBalance(endingBalanceText);
        if (!parsed.valid()) return SaveResult.failed(parsed.message(), null);
        ReconciliationSummary summary = summarize(beginningBalance, parsed.balance(), rows);
        if (summary.difference().compareTo(BigDecimal.ZERO) != 0)
        {
            return SaveResult.failed("Cannot save reconciliation while difference is " + FormatUtils.formatCurrency(summary.difference()) + ". Adjustment workflow is not implemented.", null);
        }
        gateway.reconcile(account, statementDate.toString(), parsed.balance(), selectedIds(rows));
        return SaveResult.saved("Reconciliation saved.");
    }

    private static List<Long> selectedIds(List<ReconciliationRow> rows)
    {
        if (rows == null) return List.of();
        return rows.stream().filter(ReconciliationRow::isCleared).map(ReconciliationRow::id).toList();
    }

    interface ReconciliationGateway
    {
        List<String> listAccounts();
        List<AccountingTransaction> loadTransactions(String account);
        void reconcile(String account, String statementDate, BigDecimal endingBalance, List<Long> clearedIds);
    }

    private record DefaultReconciliationGateway(ReconciliationService service) implements ReconciliationGateway
    {
        public List<String> listAccounts() { return ReconciliationService.listReconcilableAccounts(); }
        public List<AccountingTransaction> loadTransactions(String account) { return ReconciliationService.getUnreconciled(account); }
        public void reconcile(String account, String statementDate, BigDecimal endingBalance, List<Long> clearedIds)
        {
            service.reconcile(account, statementDate, endingBalance, clearedIds);
        }
    }

    public record ReconciliationSummary(BigDecimal beginningBalance, BigDecimal statementEndingBalance, BigDecimal clearedTotal, BigDecimal difference) {}

    public record BalanceParseResult(boolean valid, BigDecimal balance, String message)
    {
        static BalanceParseResult valid(BigDecimal balance) { return new BalanceParseResult(true, balance, ""); }
        static BalanceParseResult invalid(String message) { return new BalanceParseResult(false, null, message); }
    }

    public static class ReconciliationRow
    {
        private final long id;
        private final String date;
        private final String memo;
        private final BigDecimal amount;
        private final BooleanProperty cleared = new SimpleBooleanProperty(false);

        public ReconciliationRow(AccountingTransaction transaction)
        {
            this(transaction.getBookingDateTimestamp() == null ? 0L : transaction.getBookingDateTimestamp(), transaction.getDate(), transaction.getMemo(), transaction.getTotalAmount());
        }

        public ReconciliationRow(long id, String date, String memo, BigDecimal amount)
        {
            this.id = id;
            this.date = date == null ? "" : date;
            this.memo = memo == null ? "" : memo;
            this.amount = amount == null ? BigDecimal.ZERO : amount;
        }

        public long id() { return id; }
        public String date() { return date; }
        public String memo() { return memo; }
        public BigDecimal amount() { return amount; }
        public boolean isCleared() { return cleared.get(); }
        public void setCleared(boolean value) { cleared.set(value); }
        public BooleanProperty clearedProperty() { return cleared; }
    }
}
