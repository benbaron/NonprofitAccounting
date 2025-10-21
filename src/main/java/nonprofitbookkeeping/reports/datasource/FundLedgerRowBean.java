package nonprofitbookkeeping.reports.datasource;

import java.math.BigDecimal;

/**
 * Simple bean representing a single row in the Fund Ledger report.
 */
public class FundLedgerRowBean
{
        private String date;
        private String description;
        private BigDecimal debit;
        private BigDecimal credit;
        private BigDecimal runningBalance;

        public FundLedgerRowBean(String date, String description, BigDecimal debit, BigDecimal credit,
                BigDecimal runningBalance)
        {
                this.date = date;
                this.description = description;
                this.debit = debit;
                this.credit = credit;
                this.runningBalance = runningBalance;
        }

        public String getDate()
        {
                return this.date;
        }

        public void setDate(String date)
        {
                this.date = date;
        }

        public String getDescription()
        {
                return this.description;
        }

        public void setDescription(String description)
        {
                this.description = description;
        }

        public BigDecimal getDebit()
        {
                return this.debit;
        }

        public void setDebit(BigDecimal debit)
        {
                this.debit = debit;
        }

        public BigDecimal getCredit()
        {
                return this.credit;
        }

        public void setCredit(BigDecimal credit)
        {
                this.credit = credit;
        }

        public BigDecimal getRunningBalance()
        {
                return this.runningBalance;
        }

        public void setRunningBalance(BigDecimal runningBalance)
        {
                this.runningBalance = runningBalance;
        }
}
