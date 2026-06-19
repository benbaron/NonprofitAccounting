package org.nonprofitbookkeeping.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.nonprofitbookkeeping.bridge.dashboard.DashboardDataBridge;
import org.nonprofitbookkeeping.service.FundBalanceRow;

/** Presentation model for native alternate dashboard cards. */
class AlternateDashboardModel
{
    static final String EMPTY_STATE = "No data available for the active context.";
    static final String NOT_WIRED_STATE = "No service-backed data source is wired for this metric yet.";

    List<Card> cards(UiSessionContext context, DashboardDataBridge.DashboardSnapshot snapshot, String dataStatus,
        Integer unreconciledCount, Integer undepositedCount, List<String> recentTransactions)
    {
        List<FundBalanceRow> funds = snapshot == null ? List.of() : snapshot.rows();
        int accountCount = snapshot == null ? 0 : snapshot.accountCount();
        int fundCount = snapshot == null ? 0 : snapshot.fundCount();
        return List.of(
            new Card("Active database status", context.isDatabaseOpen() ? "Open" : "Not open",
                context.activeDatabaseBasePath() == null ? "No active database path." : context.activeDatabaseBasePath().toString()),
            new Card("Active company status", context.isCompanyOpen() ? "Open" : "Not open", context.sessionDisplayLabel()),
            new Card("Cash/bank balances", "Not wired", NOT_WIRED_STATE),
            new Card("Fund balances", funds.isEmpty() ? "No balances" : funds.size() + " funds",
                funds.isEmpty() ? (dataStatus == null ? EMPTY_STATE : dataStatus) : summarizeFunds(funds)),
            new Card("Restricted net assets", "Not wired", NOT_WIRED_STATE),
            new Card("Unrestricted net assets", "Not wired", NOT_WIRED_STATE),
            new Card("Unreconciled transactions", unreconciledCount == null ? "Not available" : unreconciledCount.toString(),
                unreconciledCount == null ? "Open a company to calculate unreconciled transactions." : "Loaded from reconciliation service for the active company."),
            new Card("Undeposited funds", undepositedCount == null ? "Not available" : undepositedCount.toString(),
                undepositedCount == null ? "Undeposited funds service could not load." : (undepositedCount == 0 ? EMPTY_STATE : "Loaded from undeposited funds service.")),
            new Card("Pending imports", "Not wired", NOT_WIRED_STATE),
            new Card("Recent transactions", recentTransactions.isEmpty() ? "No recent transactions" : recentTransactions.size() + " recent transactions",
                recentTransactions.isEmpty() ? (context.isCompanyOpen() ? EMPTY_STATE : "Open a company to load recent transactions.") : String.join("\n", recentTransactions)),
            new Card("Service-backed counts", accountCount + " posting accounts", fundCount + " active funds"));
    }

    private String summarizeFunds(List<FundBalanceRow> funds)
    {
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
        return funds.stream().limit(4)
            .map(row -> safe(row.getFundCode()) + " " + safe(row.getFundName()) + ": " + currency.format(defaultAmount(row.getBalance())))
            .reduce((a, b) -> a + "\n" + b).orElse(EMPTY_STATE);
    }

    private BigDecimal defaultAmount(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safe(String value)
    {
        return value == null || value.isBlank() ? "(blank)" : value;
    }

    record Card(String title, String value, String detail) {}
}
