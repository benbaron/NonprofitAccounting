package nonprofitbookkeeping.reports.jasper.beans;

import java.math.BigDecimal;

/**
 * Bean for the TransactionReport JRXML template.
 */
public class TransactionReportBean
{
    private String date;
    private String memo;
    private String accountNumber;
    private BigDecimal amount;

    public String getDate()
    {
        return this.date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getMemo()
    {
        return this.memo;
    }

    public void setMemo(String memo)
    {
        this.memo = memo;
    }

    public String getAccountNumber()
    {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber)
    {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount()
    {
        return this.amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }
}
