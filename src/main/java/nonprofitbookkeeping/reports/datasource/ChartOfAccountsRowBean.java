package nonprofitbookkeeping.reports.datasource;

/**
 * Bean used for the Chart of Accounts report.
 */
public class ChartOfAccountsRowBean {
    private String accountNumber;
    private String accountName;
    private String type;

    public ChartOfAccountsRowBean(String accountNumber, String accountName, String type) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.type = type;
    }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
