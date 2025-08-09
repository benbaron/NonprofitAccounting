/**
 * nonprofit-scaledger-ribbon.zip_expanded
 * CompanySummary.java
 * CompanySummary
 */
package nonprofitbookkeeping.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Provides static methods to retrieve summary financial data for the currently
 * loaded company.  Previous versions returned hard coded values, which caused
 * the dashboard totals to remain static.  The methods now inspect the
 * {@link CurrentCompany}'s {@link Ledger} and {@link ChartOfAccounts} to
 * calculate live balances.
 */
public class CompanySummary
{

    /**
     * Simple container for calculated totals.
     */
    private static class Totals
    {
        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal liabilities = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        BigDecimal ytdIncome()
        {
            return this.income.subtract(this.expenses);
        }
    }

    /**
     * Recomputes totals for the current company.
     *
     * @return a {@link Totals} instance containing live account balances
     */
    private static Totals computeTotals()
    {
        Totals totals = new Totals();

        if (!CurrentCompany.isOpen() || CurrentCompany.getCompany() == null)
        {
            return totals;
        }

        Company company = CurrentCompany.getCompany();
        Ledger ledger = company.getLedger();
        ChartOfAccounts coa = company.getChartOfAccounts();

        if (ledger == null || coa == null || coa.getAccounts() == null)
        {
            return totals;
        }

        Map<String, BigDecimal> balances = new HashMap<>();
        for (Account acc : coa.getAccounts())
        {
            BigDecimal opening = acc.getOpeningBalance() != null ? acc.getOpeningBalance() : BigDecimal.ZERO;
            balances.put(acc.getAccountNumber(), opening);
        }

        List<AccountingTransaction> txns = ledger.getTransactions();
        if (txns != null)
        {
            for (AccountingTransaction tx : txns)
            {
                if (tx.getEntries() == null)
                {
                    continue;
                }

                for (AccountingEntry entry : tx.getEntries())
                {
                    Account acc = coa.getAccount(entry.getAccountNumber());
                    if (acc == null || entry.getAmount() == null)
                    {
                        continue;
                    }

                    BigDecimal current = balances.getOrDefault(acc.getAccountNumber(), BigDecimal.ZERO);
                    BigDecimal amt = entry.getAmount();
                    AccountType type = acc.getAccountType();

                    if (entry.getAccountSide() == AccountSide.DEBIT)
                    {
                        if (type == AccountType.ASSET || type == AccountType.EXPENSE)
                        {
                            current = current.add(amt);
                        }
                        else
                        {
                            current = current.subtract(amt);
                        }
                    }
                    else
                    { // CREDIT
                        if (type == AccountType.ASSET || type == AccountType.EXPENSE)
                        {
                            current = current.subtract(amt);
                        }
                        else
                        {
                            current = current.add(amt);
                        }
                    }

                    balances.put(acc.getAccountNumber(), current);
                }
            }
        }

        for (Account acc : coa.getAccounts())
        {
            BigDecimal bal = balances.getOrDefault(acc.getAccountNumber(),
                    acc.getOpeningBalance() != null ? acc.getOpeningBalance() : BigDecimal.ZERO);

            AccountType type = acc.getAccountType();
            if (type == null)
            {
                continue;
            }

            switch (type)
            {
                case ASSET:
                case BANK:
                case CASH:
                case CHECKING:
                case INVEST:
                case SIMPLEINVEST:
                case MONEYMKRT:
                case MUTUAL:
                case FIXED_ASSET:
                    totals.assets = totals.assets.add(bal);
                    break;

                case LIABILITY:
                case CREDIT:
                case LONG_TERM_LIABILITY:
                case CREDITCARD:
                    totals.liabilities = totals.liabilities.add(bal);
                    break;

                case EQUITY:
                    totals.equity = totals.equity.add(bal);
                    break;

                case INCOME:
                    totals.income = totals.income.add(bal);
                    break;

                case EXPENSE:
                    totals.expenses = totals.expenses.add(bal);
                    break;

                default:
                    break;
            }
        }

        return totals;
    }

    /**
     * Gets the total assets of the current company as a string.
     *
     * @return the total assets formatted using {@link BigDecimal#toPlainString()}.
     */
    public static String getTotalAssets()
    {
        return computeTotals().assets.toPlainString();
    }

    /**
     * Gets the total liabilities of the current company as a string.
     *
     * @return the total liabilities formatted as plain string.
     */
    public static String getTotalLiabilities()
    {
        return computeTotals().liabilities.toPlainString();
    }

    /**
     * Gets the total equity of the current company as a string.
     *
     * @return the total equity formatted as plain string.
     */
    public static String getTotalEquity()
    {
        return computeTotals().equity.toPlainString();
    }

    /**
     * Gets the year-to-date (YTD) income value of the current company as a
     * string.
     *
     * @return YTD income formatted as plain string.
     */
    public static String getYtdIncomeValue()
    {
        return computeTotals().ytdIncome().toPlainString();
    }
	
}
