package org.nonprofitbookkeeping.bridge.dashboard;

import java.time.LocalDate;
import java.util.List;

import org.nonprofitbookkeeping.service.FundBalanceRow;
import org.nonprofitbookkeeping.ui.UiServiceProvider;
import org.nonprofitbookkeeping.ui.UiServiceRegistry;

/** Loads dashboard aggregates through the active alternate UI service provider. */
public class DashboardDataBridge
{
    private final UiServiceProvider services;

    @Deprecated(forRemoval = false)
    public DashboardDataBridge()
    {
        this(UiServiceRegistry.provider());
    }

    public DashboardDataBridge(UiServiceProvider services)
    {
        this.services = services;
    }

    public DashboardSnapshot load()
    {
        return load(LocalDate.now());
    }

    public DashboardSnapshot load(LocalDate asOf)
    {
        LocalDate effectiveAsOf = asOf == null ? LocalDate.now() : asOf;
        List<FundBalanceRow> rows = this.services.fundBalance().balancesAsOf(effectiveAsOf);
        int accountCount = this.services.accountLookup().listActivePostingAccounts().size();
        int fundCount = this.services.fundLookup().listActiveFunds().size();
        return new DashboardSnapshot(rows, accountCount, fundCount);
    }

    public record DashboardSnapshot(List<FundBalanceRow> rows, int accountCount, int fundCount) {}
}
