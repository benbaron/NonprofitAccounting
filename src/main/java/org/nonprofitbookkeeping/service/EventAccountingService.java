package org.nonprofitbookkeeping.service;

import org.nonprofitbookkeeping.model.AccountType;
import org.nonprofitbookkeeping.persistence.Jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Service-backed read model for event/activity accounting workspaces.
 *
 * <p>The application already models event-like accounting metadata as {@code Activity}
 * and links it to posted journal transactions through {@code TxnSplit.activity}.
 * This service intentionally performs read-only calculations from those posted
 * splits instead of creating or implying accounting postings.</p>
 */
public class EventAccountingService
{
    private final Jpa jpa;

    public EventAccountingService()
    {
        this(new Jpa());
    }

    public EventAccountingService(Jpa jpa)
    {
        this.jpa = Objects.requireNonNull(jpa, "jpa");
    }

    public List<EventAccountingWorkspace> listWorkspaces()
    {
        try (EntityManager em = this.jpa.em())
        {
            TypedQuery<Object[]> q = em.createQuery(
                "select a.id, a.code, a.name, a.active " +
                "from Activity a " +
                "order by a.active desc, a.code", Object[].class);
            List<EventAccountingWorkspace> out = new ArrayList<>();
            for (Object[] r : q.getResultList())
            {
                Long activityId = (Long) r[0];
                out.add(workspaceFor(activityId, (String) r[1], (String) r[2], (Boolean) r[3]));
            }
            return out;
        }
    }

    public EventAccountingWorkspace getWorkspace(long activityId)
    {
        try (EntityManager em = this.jpa.em())
        {
            Object[] row = em.createQuery(
                "select a.id, a.code, a.name, a.active from Activity a where a.id = :id", Object[].class)
                .setParameter("id", activityId)
                .getSingleResult();
            return workspaceFor((Long) row[0], (String) row[1], (String) row[2], (Boolean) row[3]);
        }
    }

    private EventAccountingWorkspace workspaceFor(Long activityId, String code, String name, boolean active)
    {
        List<EventTransactionRow> transactions = transactionsFor(activityId);
        List<EventTransactionRow> depositRefundRows = depositRefundRows(transactions);
        EventSummary summary = summarize(transactions, depositRefundRows);
        List<EventChecklistItem> checklist = checklist(summary, active);
        return new EventAccountingWorkspace(activityId, code, name, active, summary, transactions,
            depositRefundRows, checklist);
    }

    private List<EventTransactionRow> transactionsFor(Long activityId)
    {
        try (EntityManager em = this.jpa.em())
        {
            TypedQuery<Object[]> q = em.createQuery(
                "select t.id, t.txnDate, coalesce(p.displayName, ''), coalesce(t.memo, ''), " +
                "a.accountType, sum(s.amountSigned) " +
                "from TxnSplit s " +
                "join s.txn t " +
                "join s.account a " +
                "left join t.payee p " +
                "where s.activity.id = :activityId " +
                "group by t.id, t.txnDate, p.displayName, t.memo, a.accountType " +
                "order by t.txnDate, t.id", Object[].class);
            q.setParameter("activityId", activityId);
            List<EventTransactionRow> rows = new ArrayList<>();
            for (Object[] r : q.getResultList())
            {
                AccountType type = (AccountType) r[4];
                BigDecimal amount = normalized((BigDecimal) r[5]);
                rows.add(new EventTransactionRow((Long) r[0], (LocalDate) r[1], (String) r[2], (String) r[3],
                    type, amount, type == AccountType.ASSET || type == AccountType.BANK));
            }
            rows.sort(Comparator.comparing(EventTransactionRow::date).thenComparing(EventTransactionRow::transactionId));
            return rows;
        }
    }

    private static List<EventTransactionRow> depositRefundRows(List<EventTransactionRow> transactions)
    {
        java.util.Set<Long> expenseTxnIds = transactions.stream()
            .filter(row -> row.accountType() == AccountType.EXPENSE)
            .map(EventTransactionRow::transactionId)
            .collect(java.util.stream.Collectors.toSet());
        return transactions.stream()
            .filter(EventTransactionRow::depositOrRefund)
            .filter(row -> row.amount().compareTo(BigDecimal.ZERO) >= 0 || !expenseTxnIds.contains(row.transactionId()))
            .toList();
    }

    private static EventSummary summarize(List<EventTransactionRow> transactions, List<EventTransactionRow> depositRefundRows)
    {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal refunds = BigDecimal.ZERO;
        for (EventTransactionRow row : transactions)
        {
            if (row.accountType() == AccountType.INCOME)
            {
                income = income.add(row.amount().max(BigDecimal.ZERO));
            }
            else if (row.accountType() == AccountType.EXPENSE)
            {
                expenses = expenses.add(row.amount().max(BigDecimal.ZERO));
            }
        }
        for (EventTransactionRow row : depositRefundRows)
        {
            if (row.amount().compareTo(BigDecimal.ZERO) >= 0) deposits = deposits.add(row.amount());
            else refunds = refunds.add(row.amount().abs());
        }
        return new EventSummary(normalized(income), normalized(expenses), normalized(income.subtract(expenses)),
            normalized(deposits), normalized(refunds), transactions.size());
    }

    private static List<EventChecklistItem> checklist(EventSummary summary, boolean active)
    {
        return List.of(
            new EventChecklistItem("Journal transactions linked", summary.linkedTransactionCount() > 0,
                summary.linkedTransactionCount() > 0 ? "Linked transaction activity found." : "No posted journal transactions are linked to this activity."),
            new EventChecklistItem("Income and expenses reviewed", summary.income().compareTo(BigDecimal.ZERO) > 0 || summary.expenses().compareTo(BigDecimal.ZERO) > 0,
                "Review service-calculated income and expense totals before closing."),
            new EventChecklistItem("Deposits/refunds reviewed", summary.deposits().compareTo(BigDecimal.ZERO) > 0 || summary.refunds().compareTo(BigDecimal.ZERO) > 0,
                "Confirm bank, cash, or asset-side deposits/refunds are reconciled."),
            new EventChecklistItem("Activity retired when complete", !active,
                active ? "Activity is still active; retire it from reference data only after closeout is complete." : "Activity is inactive.")
        );
    }

    private static BigDecimal normalized(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value.stripTrailingZeros();
    }

    public record EventAccountingWorkspace(long activityId, String code, String name, boolean active,
                                           EventSummary summary,
                                           List<EventTransactionRow> linkedTransactions,
                                           List<EventTransactionRow> depositsAndRefunds,
                                           List<EventChecklistItem> closeChecklist) {}
    public record EventSummary(BigDecimal income, BigDecimal expenses, BigDecimal net,
                               BigDecimal deposits, BigDecimal refunds, int linkedTransactionCount) {}
    public record EventTransactionRow(long transactionId, LocalDate date, String payee, String memo,
                                      AccountType accountType, BigDecimal amount, boolean depositOrRefund) {}
    public record EventChecklistItem(String label, boolean complete, String detail) {}
}
