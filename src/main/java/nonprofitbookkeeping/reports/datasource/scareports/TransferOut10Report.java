package reports.beans;

import java.util.List;

public class TransferOut10Report {
    private String orgName;
    private String reportTitle;
    private List<TransferOut10Row> rows;

    public TransferOut10Report() {} 

    public TransferOut10Report(String orgName, String reportTitle, List<TransferOut10Row> rows) {
        this.orgName = orgName;
        this.reportTitle = reportTitle;
        this.rows = rows;
    }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public List<TransferOut10Row> getRows() { return rows; }
    public void setRows(List<TransferOut10Row> rows) { this.rows = rows; }
}
