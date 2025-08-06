package reports.beans;

import java.util.List;

public class Newsletter15Report {
    private String orgName;
    private String reportTitle;
    private List<Newsletter15Row> rows;

    public Newsletter15Report() {} 

    public Newsletter15Report(String orgName, String reportTitle, List<Newsletter15Row> rows) {
        this.orgName = orgName;
        this.reportTitle = reportTitle;
        this.rows = rows;
    }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public List<Newsletter15Row> getRows() { return rows; }
    public void setRows(List<Newsletter15Row> rows) { this.rows = rows; }
}
