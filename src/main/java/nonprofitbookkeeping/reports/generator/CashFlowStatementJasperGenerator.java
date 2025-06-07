package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.CashFlowStatementRowBean;
import nonprofitbookkeeping.service.ReportService;

import java.io.File;
import java.io.InputStream;
import java.io.FileNotFoundException; // Added for explicit exception
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
// Export related imports are not needed if using inherited methods from AbstractReportGenerator

public class CashFlowStatementJasperGenerator extends AbstractReportGenerator {

    private ReportContext reportContext;
    private ReportService reportService;

    public CashFlowStatementJasperGenerator(ReportContext reportContext, ReportService reportService) {
        this.reportContext = reportContext;
        this.reportService = reportService;
    }

    @Override
    protected String getReportPath() {
        return "jrxml/cash_flow_statement.jrxml";
    }

    @Override
    protected List<CashFlowStatementRowBean> getReportData() {
        Company company = CurrentCompany.getCompany();
        if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null) {
            System.err.println("CashFlowStatementJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
            return Collections.emptyList();
        }
        Ledger ledger = company.getLedger();
        ChartOfAccounts coa = company.getChartOfAccounts();

        return this.reportService.prepareCashFlowStatementJasperData(this.reportContext, ledger, coa);
    }

    @Override
    protected Map<String, Object> getReportParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("P_REPORT_TITLE", "Cash Flow Statement");

        Company company = CurrentCompany.getCompany();
        String companyName = "N/A";
        if (company != null && company.getCompanyProfile() != null && company.getCompanyProfile().getCompanyName() != null) {
            companyName = company.getCompanyProfile().getCompanyName();
        }
        params.put("P_COMPANY_NAME", companyName);

        String reportPeriod = "N/A";
        if (reportContext.getStartDate() != null && reportContext.getEndDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            reportPeriod = reportContext.getStartDate().format(formatter) + " - " + reportContext.getEndDate().format(formatter);
        }
        params.put("P_REPORT_PERIOD", reportPeriod);
        params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        return params;
    }

    @Override
    public File generateAndExportReport(String format) throws Exception {
        File generatedFile = null;
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // YYYY-MM-DD
        String reportBaseName = "Cash_Flow_Statement_Report_" + currentDateStr;


        try (InputStream reportStream = getClass().getClassLoader().getResourceAsStream(getReportPath())) {
            if (reportStream == null) {
                System.err.println("Cannot find report template: " + getReportPath());
                throw new FileNotFoundException("Report template not found: " + getReportPath());
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            List<?> reportDataList = getReportData();
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportDataList);

            Map<String, Object> parameters = getReportParameters();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            File outputDir = new File(getOutputDirectory());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            String outputFileName = reportBaseName + "." + format.toLowerCase();
            File outputFile = new File(outputDir, outputFileName);

            if ("pdf".equalsIgnoreCase(format)) {
                generatedFile = exportToPDF(jasperPrint, outputFile.getAbsolutePath());
            } else if ("html".equalsIgnoreCase(format)) {
                generatedFile = exportToHTML(jasperPrint, outputFile.getAbsolutePath());
            } else {
                System.out.println("Unsupported format for Cash Flow Statement: " + format + ". Defaulting to PDF.");
                File defaultOutputFile = new File(outputDir, reportBaseName + ".pdf");
                generatedFile = exportToPDF(jasperPrint, defaultOutputFile.getAbsolutePath());
            }

            if (generatedFile != null && generatedFile.exists()) {
                System.out.println(reportBaseName + " generated successfully at: " + generatedFile.getAbsolutePath());
            } else {
                String attemptedPath = (generatedFile != null) ? generatedFile.getAbsolutePath() : outputFile.getAbsolutePath();
                System.err.println("Report file " + attemptedPath + " was not created or found after export attempt.");
                throw new FileNotFoundException("Generated report file could not be confirmed after export: " + attemptedPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return generatedFile;
    }
    // Local exportToPDF/HTML methods are removed as they are now inherited from AbstractReportGenerator
}
