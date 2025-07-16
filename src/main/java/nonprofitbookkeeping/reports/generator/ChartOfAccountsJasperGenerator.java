
package nonprofitbookkeeping.reports.generator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.datasource.ChartOfAccountsRowBean;
import nonprofitbookkeeping.service.ReportService;

/**
 * Generator for the Chart of Accounts report.
 */
public class ChartOfAccountsJasperGenerator extends AbstractReportGenerator {

    private final ReportService reportService;

    /**
     * Constructs the generator with the required {@link ReportService}.
     *
     * @param reportService service used to prepare report data
     */
    public ChartOfAccountsJasperGenerator(ReportService reportService)
    {
        this.reportService = reportService;
    }

    @Override
    protected List<ChartOfAccountsRowBean> getReportData() {
        Company company = CurrentCompany.getCompany();
        if (company == null || company.getChartOfAccounts() == null) {
            System.err.println("ChartOfAccountsJasperGenerator: Company or COA is null. Cannot generate data.");
            return Collections.emptyList();
        }

        ChartOfAccounts coa = company.getChartOfAccounts();
        return reportService.prepareChartOfAccountsJasperData(coa);
    }

    @Override
    protected Map<String, Object> getReportParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("P_REPORT_TITLE", "Chart of Accounts");
        Company company = CurrentCompany.getCompany();
        String companyName = "N/A";
        if (company != null && company.getCompanyProfile() != null &&
                company.getCompanyProfile().getCompanyName() != null) {
            companyName = company.getCompanyProfile().getCompanyName();
        }

        params.put("P_COMPANY_NAME", companyName);
        params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        return params;
    }

    @Override
    protected String getReportPath() {
        return "jrxml/ChartOfAccountsAlt.jrxml";
    }

    @Override
    public File generateAndExportReport(String format) throws Exception {
        String baseName = "Chart_of_Accounts_" + LocalDate.now();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(getReportPath())) {
            if (in == null) {
                throw new FileNotFoundException("JRXML not found: " + getReportPath());
            }
            JasperReport jasperReport = JasperCompileManager.compileReport(in);
            JRDataSource dataSource = new JRBeanCollectionDataSource(getReportData());
            JasperPrint print = JasperFillManager.fillReport(jasperReport, getReportParameters(), dataSource);
            File outDir = new File(getOutputDirectory());
            if (!outDir.exists()) { outDir.mkdirs(); }
            File outFile = new File(outDir, baseName + ("html".equalsIgnoreCase(format) ? ".html" : ".pdf"));
            if ("html".equalsIgnoreCase(format)) {
                return exportToHTML(print, outFile.getAbsolutePath());
            }
            return exportToPDF(print, outFile.getAbsolutePath());
        }
    }

}
