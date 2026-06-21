package org.nonprofitbookkeeping.ui;

import org.nonprofitbookkeeping.service.FundBalanceRow;
import org.nonprofitbookkeeping.service.FundBalanceService;

import nonprofitbookkeeping.service.UndepositedFundsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Service-backed state calculator for the alternate monthly close / exchequer checklist. */
public class MonthlyCloseChecklistService
{
    private final CloseChecklistGateway gateway;

    public MonthlyCloseChecklistService(UiServiceProvider services)
    {
        this(new DefaultGateway(services));
    }

    MonthlyCloseChecklistService(CloseChecklistGateway gateway)
    {
        this.gateway = Objects.requireNonNull(gateway, "gateway");
    }

    public CloseChecklistState calculate(LocalDate periodEnd)
    {
        LocalDate effectiveEnd = periodEnd == null ? LocalDate.now() : periodEnd;
        List<CloseChecklistItem> items = new ArrayList<>();
        if (!this.gateway.isDatabaseOpen())
        {
            return new CloseChecklistState(effectiveEnd, List.of(
                CloseChecklistItem.blocked("Open database", "Open a database before calculating monthly close readiness.")));
        }
        if (!this.gateway.isCompanyOpen())
        {
            return new CloseChecklistState(effectiveEnd, List.of(
                CloseChecklistItem.blocked("Open company", "Open the branch company before calculating monthly close readiness.")));
        }

        items.add(reconciliationItem());
        items.add(undepositedFundsItem());
        items.add(CloseChecklistItem.notWired("Resolve pending imports",
            "No service-backed pending import queue summary is wired to the alternate checklist yet. Use Import/Export previews and blocking-error summaries."));
        items.add(fundBalanceItem(effectiveEnd));
        items.add(requiredReportsItem());
        items.add(CloseChecklistItem.actionRequired("Export/backup database",
            "Use Database Administration to export or back up the active database; this checklist does not infer completion from files."));
        items.add(CloseChecklistItem.notWired("Lock/close period",
            "A general accounting period close/lock service is not available to the alternate checklist. Statement/depreciation-specific locks remain in their own workflows."));
        return new CloseChecklistState(effectiveEnd, List.copyOf(items));
    }

    private CloseChecklistItem reconciliationItem()
    {
        try
        {
            List<String> accounts = this.gateway.reconcilableAccounts();
            if (accounts.isEmpty())
            {
                return CloseChecklistItem.notWired("Reconcile bank accounts",
                    "No reconcilable bank/cash accounts were returned by the reconciliation service.");
            }
            return CloseChecklistItem.actionRequired("Reconcile bank accounts",
                "Review unreconciled activity for " + accounts.size() + " reconcilable account(s) in Reconcile Accounts.");
        }
        catch (RuntimeException ex)
        {
            return CloseChecklistItem.blocked("Reconcile bank accounts", safe(ex));
        }
    }

    private CloseChecklistItem undepositedFundsItem()
    {
        try
        {
            int count = this.gateway.undepositedFundsCount();
            if (count == 0)
            {
                return CloseChecklistItem.complete("Review undeposited funds", "No undeposited funds items are currently listed.");
            }
            return CloseChecklistItem.actionRequired("Review undeposited funds", count + " undeposited funds item(s) need review before close.");
        }
        catch (RuntimeException ex)
        {
            return CloseChecklistItem.blocked("Review undeposited funds", safe(ex));
        }
    }

    private CloseChecklistItem fundBalanceItem(LocalDate periodEnd)
    {
        try
        {
            List<FundBalanceRow> rows = this.gateway.fundBalancesAsOf(periodEnd);
            if (rows.isEmpty())
            {
                return CloseChecklistItem.actionRequired("Verify fund balances", "No fund balances were returned as of " + periodEnd + ". Verify funds and postings.");
            }
            return CloseChecklistItem.actionRequired("Verify fund balances",
                "Review " + rows.size() + " fund balance row(s) as of " + periodEnd + "; accounting approval is still manual.");
        }
        catch (RuntimeException ex)
        {
            return CloseChecklistItem.blocked("Verify fund balances", safe(ex));
        }
    }

    private CloseChecklistItem requiredReportsItem()
    {
        try
        {
            int count = this.gateway.reportCatalogCount();
            if (count == 0)
            {
                return CloseChecklistItem.notWired("Generate required reports", "The reports workspace returned no report definitions.");
            }
            return CloseChecklistItem.actionRequired("Generate required reports",
                "Generate and save the required branch reports from " + count + " available report definition(s).");
        }
        catch (RuntimeException ex)
        {
            return CloseChecklistItem.blocked("Generate required reports", safe(ex));
        }
    }

    private static String safe(RuntimeException ex)
    {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    interface CloseChecklistGateway
    {
        boolean isDatabaseOpen();
        boolean isCompanyOpen();
        List<String> reconcilableAccounts();
        int undepositedFundsCount();
        List<FundBalanceRow> fundBalancesAsOf(LocalDate periodEnd);
        int reportCatalogCount();
    }

    private static class DefaultGateway implements CloseChecklistGateway
    {
        private final UiServiceProvider services;
        private final AlternateReconciliationService reconciliationService = new AlternateReconciliationService();
        private final UndepositedFundsService undepositedFundsService = new UndepositedFundsService();
        private final AlternateReportsWorkspaceService reportsService = new AlternateReportsWorkspaceService();

        DefaultGateway(UiServiceProvider services) { this.services = Objects.requireNonNull(services, "services"); }
        public boolean isDatabaseOpen() { return this.services.sessionContext().isDatabaseOpen(); }
        public boolean isCompanyOpen() { return this.services.sessionContext().isCompanyOpen(); }
        public List<String> reconcilableAccounts() { return this.reconciliationService.listAccounts(); }
        public int undepositedFundsCount() { return this.undepositedFundsService.listItems().size(); }
        public List<FundBalanceRow> fundBalancesAsOf(LocalDate periodEnd)
        {
            FundBalanceService fundBalanceService = this.services.fundBalance();
            return fundBalanceService.balancesAsOf(periodEnd);
        }
        public int reportCatalogCount() { return this.reportsService.catalog().size(); }
    }

    public record CloseChecklistState(LocalDate periodEnd, List<CloseChecklistItem> items) {}
    public record CloseChecklistItem(String label, ChecklistStatus status, String detail)
    {
        static CloseChecklistItem complete(String label, String detail) { return new CloseChecklistItem(label, ChecklistStatus.COMPLETE, detail); }
        static CloseChecklistItem actionRequired(String label, String detail) { return new CloseChecklistItem(label, ChecklistStatus.ACTION_REQUIRED, detail); }
        static CloseChecklistItem blocked(String label, String detail) { return new CloseChecklistItem(label, ChecklistStatus.BLOCKED, detail); }
        static CloseChecklistItem notWired(String label, String detail) { return new CloseChecklistItem(label, ChecklistStatus.NOT_WIRED, detail); }
    }
    public enum ChecklistStatus { COMPLETE, ACTION_REQUIRED, BLOCKED, NOT_WIRED }
}
