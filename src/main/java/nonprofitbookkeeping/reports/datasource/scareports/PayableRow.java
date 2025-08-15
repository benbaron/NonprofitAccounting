package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a single payable. Payables capture amounts owed to a party
 * along with a reason for the obligation.
 */
public class PayableRow
{
        private String owedTo;
        private String reason;
        private BigDecimal priorAmount;
        private BigDecimal currentAmount;

        public PayableRow()
        {
        }

        public PayableRow(String owedTo, String reason, BigDecimal priorAmount, BigDecimal currentAmount)
        {
                this.owedTo = owedTo;
                this.reason = reason;
                this.priorAmount = priorAmount;
                this.currentAmount = currentAmount;
        }

        public String getOwedTo()
        {
                return owedTo;
        }

        public void setOwedTo(String owedTo)
        {
                this.owedTo = owedTo;
        }

        public String getReason()
        {
                return reason;
        }

        public void setReason(String reason)
        {
                this.reason = reason;
        }

        public BigDecimal getPriorAmount()
        {
                return priorAmount;
        }

        public void setPriorAmount(BigDecimal priorAmount)
        {
                this.priorAmount = priorAmount;
        }

        public BigDecimal getCurrentAmount()
        {
                return currentAmount;
        }

        public void setCurrentAmount(BigDecimal currentAmount)
        {
                this.currentAmount = currentAmount;
        }
}

