package reports.beans;

import java.util.List;

public class Funds14Report {
    private String orgName;
    private String reportTitle;
    private List<Funds14Row> rows;

    public Funds14Report() {} 

    public Funds14Report(String orgName, String reportTitle, List<Funds14Row> rows) {
        this.orgName = orgName;
        this.reportTitle = reportTitle;
        this.rows = rows;
    }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getReportTitle() { return reportTitle; }
    public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }

    public List<Funds14Row> getRows() { return rows; }
    public void setRows(List<Funds14Row> rows) { this.rows = rows; }
}
