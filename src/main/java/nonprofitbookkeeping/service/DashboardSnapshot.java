package nonprofitbookkeeping.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import nonprofitbookkeeping.model.AccountingTransaction;
import org.nonprofitbookkeeping.service.FundBalanceRow;

/** Immutable data consumed by both dashboard shells. */
public record DashboardSnapshot(
    String companyName,
    LocalDate asOfDate,
    LocalDate fiscalPeriodStart,
    BigDecimal cashAndBank,
    BigDecimal totalAssets,
    BigDecimal totalLiabilities,
    BigDecimal unrestrictedNetAssets,
    BigDecimal restrictedNetAssets,
    BigDecimal periodIncome,
    BigDecimal periodExpenses,
    BigDecimal periodSurplus,
    int unreconciledCount,
    BigDecimal unreconciledAmount,
    int undepositedCount,
    List<FundBalanceRow> fundBalances,
    List<AccountingTransaction> recentTransactions,
    String status)
{
    public DashboardSnapshot
    {
        companyName = companyName == null ? "No company open" : companyName;
        cashAndBank = defaultAmount(cashAndBank);
        totalAssets = defaultAmount(totalAssets);
        totalLiabilities = defaultAmount(totalLiabilities);
        unrestrictedNetAssets = defaultAmount(unrestrictedNetAssets);
        restrictedNetAssets = defaultAmount(restrictedNetAssets);
        periodIncome = defaultAmount(periodIncome);
        periodExpenses = defaultAmount(periodExpenses);
        periodSurplus = defaultAmount(periodSurplus);
        unreconciledAmount = defaultAmount(unreconciledAmount);
        fundBalances = fundBalances == null ? List.of() : List.copyOf(fundBalances);
        recentTransactions = recentTransactions == null ? List.of() :
            List.copyOf(recentTransactions);
        status = status == null ? "" : status;
    }

    private static BigDecimal defaultAmount(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
