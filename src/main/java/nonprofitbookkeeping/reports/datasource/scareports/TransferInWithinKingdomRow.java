package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a transfer received from another SCA account within
 * the same kingdom. Tracks the branch/account name, check details and amount.
 */
public class TransferInWithinKingdomRow
{
        private String branchOrAccount;
        private String checkNumber;
        private String checkDate;
        private BigDecimal amount;

        public TransferInWithinKingdomRow()
        {
        }

        public TransferInWithinKingdomRow(String branchOrAccount, String checkNumber, String checkDate, BigDecimal amount)
        {
                this.branchOrAccount = branchOrAccount;
                this.checkNumber = checkNumber;
                this.checkDate = checkDate;
                this.amount = amount;
        }

        public String getBranchOrAccount()
        {
                return branchOrAccount;
        }

        public void setBranchOrAccount(String branchOrAccount)
        {
                this.branchOrAccount = branchOrAccount;
        }

        public String getCheckNumber()
        {
                return checkNumber;
        }

        public void setCheckNumber(String checkNumber)
        {
                this.checkNumber = checkNumber;
        }

        public String getCheckDate()
        {
                return checkDate;
        }

        public void setCheckDate(String checkDate)
        {
                this.checkDate = checkDate;
        }

        public BigDecimal getAmount()
        {
                return amount;
        }

        public void setAmount(BigDecimal amount)
        {
                this.amount = amount;
        }
}

