package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.IncomeStatementRowBean;
import nonprofitbookkeeping.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.io.File; // Added import
import java.io.InputStream;
import java.math.BigDecimal;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
// JasperExportManager and HtmlExporter related imports are not needed here
// if exportToPDF and exportToHTML are properly inherited from AbstractReportGenerator.


public class IncomeStatementJasperGenerator extends AbstractReportGenerator {

    private ReportContext reportContext;
    private ReportService reportService;

    public IncomeStatementJasperGenerator(ReportContext reportContext, ReportService reportService) {
        this.reportContext = reportContext;
        this.reportService = reportService;
    }

    @Override
    protected String getReportPath() {
        // Assuming JRXML files are in 'src/main/resources/jrxml/'
        // The path for ClassLoader.getResourceAsStream should be relative to resources root
        return "jrxml/income_statement.jrxml";
    }

    @Override
    protected List<IncomeStatementRowBean> getReportData() {
        Company company = CurrentCompany.getCompany();
        if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null) {
            System.err.println("IncomeStatementJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
            return Collections.emptyList();
        }
        Ledger ledger = company.getLedger();
        ChartOfAccounts coa = company.getChartOfAccounts();

        return this.reportService.prepareIncomeStatementJasperData(this.reportContext, ledger, coa);
    }

    @Override
    protected Map<String, Object> getReportParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("P_REPORT_TITLE", "Income Statement");

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

        // Net Income parameter calculation can be added here if needed by JRXML
        // List<IncomeStatementRowBean> data = getReportData(); // This might be inefficient if called again
        // Consider calculating sums from the data if not done by Jasper itself.
        // For now, assuming JRXML handles summary or it's part of the bean list.

        return params;
    }

    @Override
    public File generateAndExportReport(String format) throws Exception {
        File generatedFile = null;
        String reportBaseName = "Income_Statement_" +
                                (reportContext.getEndDate() != null ? reportContext.getEndDate().toString() : LocalDate.now().toString());


        try (InputStream reportStream = getClass().getClassLoader().getResourceAsStream(getReportPath())) {
            if (reportStream == null) {
                System.err.println("Cannot find report template: " + getReportPath());
                throw new java.io.FileNotFoundException("Report template not found: " + getReportPath());
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            List<?> reportDataList = getReportData();
            JRDataSource dataSource = new JRBeanCollectionDataSource(reportDataList);

            Map<String, Object> parameters = getReportParameters();

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            File outputDir = new File(getOutputDirectory()); // Method from AbstractReportGenerator
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
                System.out.println("Unsupported format for Income Statement: " + format + ". Defaulting to PDF.");
                File defaultOutputFile = new File(outputDir, reportBaseName + ".pdf");
                generatedFile = exportToPDF(jasperPrint, defaultOutputFile.getAbsolutePath());
            }

            if (generatedFile != null && generatedFile.exists()) { // Check existence after export
                System.out.println(reportBaseName + " generated successfully at: " + generatedFile.getAbsolutePath());
            } else {
                System.err.println("Report generation failed or file not found for: " + reportBaseName);
                // If generatedFile is null from export methods (if they can return null on failure)
                // or if the file doesn't exist after export call.
                throw new Exception("Report file was not created or found: " + outputFile.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return generatedFile;
    }
    // Local exportToPDF/HTML methods are removed as they are now inherited from AbstractReportGenerator
}
