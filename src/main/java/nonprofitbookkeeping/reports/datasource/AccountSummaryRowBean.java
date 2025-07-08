package nonprofitbookkeeping.reports.datasource;

/**
 * Simple bean representing a row in the Account Summary report.
 * Only minimal fields are provided to illustrate how data from
 * the application can be mapped to the fields defined in
 * {@code AccountSummary.jrxml}.
 */
public class AccountSummaryRowBean {
    private String actId;
    private String invDate;
    private String commentAll;
    private String comment;
    private String customer;
    private String debitFormat;
    private String creditFormat;
    private String accountNum;
    private String accountName;

    // Standard getters
    public String getActId() { return actId; }
    public String getInvDate() { return invDate; }
    public String getCommentAll() { return commentAll; }
    public String getComment() { return comment; }
    public String getCustomer() { return customer; }
    public String getDebitFormat() { return debitFormat; }
    public String getCreditFormat() { return creditFormat; }
    public String getAccountNum() { return accountNum; }
    public String getAccountName() { return accountName; }

    // Convenience getters matching JRXML field names
    public String getACT_ID() { return actId; }
    public String getINVDATE() { return invDate; }
    public String getCOMMENTALL() { return commentAll; }
    public String getCOMMENT() { return comment; }
    public String getCUSTOMER() { return customer; }
    public String getDEBITFORMAT() { return debitFormat; }
    public String getCREDITFORMAT() { return creditFormat; }
    public String getACCOUNTNUM() { return accountNum; }
    public String getACCOUNTNAME() { return accountName; }
}
