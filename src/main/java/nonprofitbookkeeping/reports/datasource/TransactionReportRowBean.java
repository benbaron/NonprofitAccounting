package nonprofitbookkeeping.reports.datasource;

/**
 * Bean representing a row in the Transaction Report.
 */
public class TransactionReportRowBean {
    private String actId;
    private String invDate;
    private String commentAll;
    private String comment;
    private String fileInfo;
    private String regDate;
    private String accountNum;
    private String accountName;
    private String customer;
    private String debitFormat;
    private String creditFormat;

    public String getActId() { return actId; }
    public String getInvDate() { return invDate; }
    public String getCommentAll() { return commentAll; }
    public String getComment() { return comment; }
    public String getFileInfo() { return fileInfo; }
    public String getRegDate() { return regDate; }
    public String getAccountNum() { return accountNum; }
    public String getAccountName() { return accountName; }
    public String getCustomer() { return customer; }
    public String getDebitFormat() { return debitFormat; }
    public String getCreditFormat() { return creditFormat; }

    // Convenience getters matching JRXML field names
    public String getact_id() { return actId; }
    public String getACT_ID() { return actId; }
    public String getinvdate() { return invDate; }
    public String getINVDATE() { return invDate; }
    public String getCOMMENTALL() { return commentAll; }
    public String getCOMMENT() { return comment; }
    public String getfileinfo() { return fileInfo; }
    public String getFILEINFO() { return fileInfo; }
    public String getregdate() { return regDate; }
    public String getREGDATE() { return regDate; }
    public String getACCOUNTNUM() { return accountNum; }
    public String getACCOUNTNAME() { return accountName; }
    public String getCUSTOMER() { return customer; }
    public String getDEBITFORMAT() { return debitFormat; }
    public String getCREDITFORMAT() { return creditFormat; }
}
