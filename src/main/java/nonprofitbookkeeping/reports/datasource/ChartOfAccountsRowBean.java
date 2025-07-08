package nonprofitbookkeeping.reports.datasource;

/**
 * Bean representing a row in the Chart of Accounts report.
 */
public class ChartOfAccountsRowBean {
    private String accountNum;
    private String accountName;
    private String accountType;

    public String getAccountNum() { return accountNum; }
    public String getAccountName() { return accountName; }
    public String getAccountType() { return accountType; }

    // JRXML field convenience getters
    public String getACCOUNTNUM() { return accountNum; }
    public String getACCOUNTNAME() { return accountName; }
    public String getACCOUNTTYPE() { return accountType; }
}
