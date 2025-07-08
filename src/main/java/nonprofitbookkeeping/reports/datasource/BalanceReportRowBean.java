package nonprofitbookkeeping.reports.datasource;

/**
 * Bean representing a row in the Balance Report.
 */
public class BalanceReportRowBean {
    private String accountNum;
    private String accountDesc;
    private String formattedIncomingAmount;
    private String formattedAmount;
    private String formattedOutgoingAmount;

    public String getAccountNum() { return accountNum; }
    public String getAccountDesc() { return accountDesc; }
    public String getFormattedIncomingAmount() { return formattedIncomingAmount; }
    public String getFormattedAmount() { return formattedAmount; }
    public String getFormattedOutgoingAmount() { return formattedOutgoingAmount; }

    // JRXML field convenience getters
    public String getACCOUNTNUM() { return accountNum; }
    public String getACCOUNTDESC() { return accountDesc; }
    public String getFORMATED_INCOMMINGAMOUNT() { return formattedIncomingAmount; }
    public String getFORMATED_AMOUNT() { return formattedAmount; }
    public String getFORMATED_OUTGOINGAMOUNT() { return formattedOutgoingAmount; }
}
