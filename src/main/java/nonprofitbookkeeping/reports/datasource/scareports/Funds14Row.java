package nonprofitbookkeeping.reports.datasource.scareports;

import java.math.BigDecimal;

/**
 * Row representing a single dedicated fund entry on the FUNDS_14 report.
 * Captures the fund's name, purpose and the financial activity for the period.
 */
public class Funds14Row {
    private String fundName;
    private String purpose;
    private BigDecimal beginBalance;
    private BigDecimal receipts;
    private BigDecimal disbursements;
    private BigDecimal transfersIn;
    private BigDecimal transfersOut;
    private BigDecimal endBalance;

    public Funds14Row() {
    }

    public Funds14Row(String fundName,
                      String purpose,
                      BigDecimal beginBalance,
                      BigDecimal receipts,
                      BigDecimal disbursements,
                      BigDecimal transfersIn,
                      BigDecimal transfersOut,
                      BigDecimal endBalance) {
        this.fundName = fundName;
        this.purpose = purpose;
        this.beginBalance = beginBalance;
        this.receipts = receipts;
        this.disbursements = disbursements;
        this.transfersIn = transfersIn;
        this.transfersOut = transfersOut;
        this.endBalance = endBalance;
    }

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BigDecimal getBeginBalance() {
        return beginBalance;
    }

    public void setBeginBalance(BigDecimal beginBalance) {
        this.beginBalance = beginBalance;
    }

    public BigDecimal getReceipts() {
        return receipts;
    }

    public void setReceipts(BigDecimal receipts) {
        this.receipts = receipts;
    }

    public BigDecimal getDisbursements() {
        return disbursements;
    }

    public void setDisbursements(BigDecimal disbursements) {
        this.disbursements = disbursements;
    }

    public BigDecimal getTransfersIn() {
        return transfersIn;
    }

    public void setTransfersIn(BigDecimal transfersIn) {
        this.transfersIn = transfersIn;
    }

    public BigDecimal getTransfersOut() {
        return transfersOut;
    }

    public void setTransfersOut(BigDecimal transfersOut) {
        this.transfersOut = transfersOut;
    }

    public BigDecimal getEndBalance() {
        return endBalance;
    }

    public void setEndBalance(BigDecimal endBalance) {
        this.endBalance = endBalance;
    }
}
