package nonprofitbookkeeping.reports.generator;

import nonprofitbookkeeping.model.Company;
import nonprofitbookkeeping.model.CurrentCompany;
import nonprofitbookkeeping.model.Ledger;
import nonprofitbookkeeping.model.ChartOfAccounts;
import nonprofitbookkeeping.reports.ReportContext;
import nonprofitbookkeeping.reports.datasource.TrialBalanceRowBean;
import nonprofitbookkeeping.service.ReportService;
import nonprofitbookkeeping.exception.ActionCancelledException;
import nonprofitbookkeeping.exception.NoFileCreatedException;


import java.io.File;
import java.io.FileNotFoundException; // For specific exception
import java.io.InputStream;
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

public class TrialBalanceJasperGenerator extends AbstractReportGenerator {

    private ReportContext reportContext;
    private ReportService reportService;

    public TrialBalanceJasperGenerator(ReportContext reportContext, ReportService reportService) {
        this.reportContext = reportContext;
        this.reportService = reportService;
    }

    @Override
    protected String getReportPath() throws ActionCancelledException, NoFileCreatedException {
        // This JRXML already exists in src/main/resources/reports/
        return "reports/TrialBalanceReport.jrxml";
    }

    @Override
    protected List<TrialBalanceRowBean> getReportData() {
        Company company = CurrentCompany.getCompany();
        if (company == null || company.getLedger() == null || company.getChartOfAccounts() == null) {
            System.err.println("TrialBalanceJasperGenerator: Company, Ledger, or COA is null. Cannot generate data.");
            return Collections.emptyList();
        }
        Ledger ledger = company.getLedger();
        ChartOfAccounts coa = company.getChartOfAccounts();

        return this.reportService.prepareTrialBalanceJasperData(this.reportContext, ledger, coa);
    }

    @Override
    protected Map<String, Object> getReportParameters() {
        Map<String, Object> params = new HashMap<>();

        // Standardized Parameters (using P_ convention for clarity)
        params.put("P_REPORT_TITLE", "Trial Balance");

        Company company = CurrentCompany.getCompany();
        String companyName = "N/A";
        if (company != null && company.getCompanyProfile() != null && company.getCompanyProfile().getCompanyName() != null) {
            companyName = company.getCompanyProfile().getCompanyName();
        }
        params.put("P_COMPANY_NAME", companyName);

        String reportAsOfDate = "N/A";
        if (reportContext.getEndDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            reportAsOfDate = "As of " + reportContext.getEndDate().format(formatter);
        }
        params.put("P_AS_OF_DATE", reportAsOfDate);
        params.put("P_GENERATION_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        // Parameters matching the existing TrialBalanceReport.jrxml:
        // <parameter name="reporttitle" class="java.lang.String"/>
        // <parameter name="dateToday" class="java.lang.String"/>
        // <parameter name="companyname" class="java.lang.String"/>
        // It seems the JRXML uses lowercase for these specific ones. Let's ensure they are provided.
        params.put("reporttitle", "Trial Balance");
        params.put("dateToday", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)); // YYYY-MM-DD format
        params.put("companyname", companyName); // JRXML might expect "companyname"

        return params;
    }

    @Override
    public File generateAndExportReport(String format) throws Exception {
        File generatedFile = null;
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String reportBaseName = "Trial_Balance_Report_" +
                                (reportContext.getEndDate() != null ? reportContext.getEndDate().toString() : currentDateStr);

        String jrxmlPath = getReportPath();

        try (InputStream reportStream = getClass().getClassLoader().getResourceAsStream(jrxmlPath)) {
            if (reportStream == null) {
                System.err.println("Cannot find report template: " + jrxmlPath);
                throw new FileNotFoundException("Report template not found: " + jrxmlPath);
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
                System.out.println("Unsupported format for Trial Balance: " + format + ". Defaulting to PDF.");
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
        } catch (ActionCancelledException | NoFileCreatedException e) {
             System.err.println("Report generation pre-check failed: " + e.getMessage());
             throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return generatedFile;
    }
}
